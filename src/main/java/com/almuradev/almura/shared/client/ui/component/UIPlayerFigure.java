/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.client.ui.component;

import net.malisis.core.client.gui.render.GuiRenderer;
import net.malisis.core.client.gui.render.GuiTexture;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.BasicContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResource;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import javax.annotation.Nullable;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A player drawn in 3D from a skin in the jar, holding a diamond sword, playing once: standing as they are, raising the sword, a star burst
 * from its tip at the top, slowly lowering it, then turning to face the viewer and holding there. Only for show, the mouse passes through it.
 */
@SideOnly(Side.CLIENT)
public final class UIPlayerFigure extends BasicContainer<UIPlayerFigure> {

    private static final Logger LOGGER = LogManager.getLogger("Almura");
    // Skins read from the jar, converted to the 64 x 64 layout the model uses (empty if one couldn't be read)
    private static final Map<ResourceLocation, Optional<ResourceLocation>> SKINS = new HashMap<>();

    // The sequence, in seconds from when the figure first shows: standing, raising, held up (the burst), lowering, turning to the front
    private static final float STAND_END = 1.5F, RAISE_END = 2.3F, HOLD_END = 3.3F, LOWER_END = 5.3F, TURN_END = 6.3F;
    // The burst: how long it lasts from the top of the raise, how fast it flares, its size (in blocks) and rays
    private static final float BURST_TIME = 1.0F, BURST_FLARE = 0.15F, BURST_SIZE = 0.55F;
    private static final int BURST_RAYS = 8;
    // Turned three quarters before facing the viewer
    private static final float BASE_YAW = 30.0F;
    // The figure's height in blocks: the player and the raised sword above their head
    private static final float FIGURE_BLOCKS = 3.0F;
    // Width for its height (room for the arms and the raised sword out to the side)
    public static final float WIDTH_RATIO = 0.62F;
    // The diamond sword's tip in its item model (0 to 1 each way, from the bottom left of its sprite)
    private static final float TIP_X = 0.94F, TIP_Y = 0.94F, TIP_Z = 0.5F;
    private static final int[] CAPABILITIES = {GL11.GL_ALPHA_TEST, GL11.GL_DEPTH_TEST, GL11.GL_BLEND, GL11.GL_LIGHTING, GL11.GL_CULL_FACE,
        GL11.GL_COLOR_MATERIAL, GL12.GL_RESCALE_NORMAL, GL11.GL_TEXTURE_2D};

    private final ResourceLocation skin;
    private final ModelPlayer model;
    private final ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
    private final FloatBuffer matrix = BufferUtils.createFloatBuffer(16);
    // When the sequence started (the first frame with the skin), -1 until then; and the last frame drawn
    private long startMillis = -1, lastDrawnMillis;
    // Not drawn for this long: it was off screen, play the sequence again
    private static final long RESTART_AFTER_MILLIS = 500;

    /**
     * @param skin The player's skin in the jar (either the old 64 x 32 or the 64 x 64 layout)
     * @param slim Whether the skin has slim arms
     */
    public UIPlayerFigure(final MalisisGui gui, final ResourceLocation skin, final boolean slim) {
        super();
        this.skin = skin;
        this.model = new ModelPlayer(0.0F, slim);
        this.setBackgroundAlpha(0);
        this.setPadding(0, 0);
        // The raised sword and its burst may reach past the figure's area
        this.setClipContent(false);
        this.setForeground(this::drawFigure);
    }

    private void drawFigure(final GuiRenderer renderer) {
        final ResourceLocation texture = loadSkin(this.skin).orElse(null);
        if (texture == null) {
            return;
        }
        // Starts over whenever the figure shows again after being away (e.g. back to the main menu from the multiplayer screen)
        final long now = Minecraft.getSystemTime();
        if (this.startMillis < 0 || now - this.lastDrawnMillis > RESTART_AFTER_MILLIS) {
            this.startMillis = now;
        }
        this.lastDrawnMillis = now;
        final float seconds = (now - this.startMillis) / 1000.0F;

        // Drawn outside of the renderer's batch, leaving every setting it touches as it found it (the panorama draws with what is left)
        Tessellator.getInstance().draw();
        final boolean[] enabled = new boolean[CAPABILITIES.length];
        for (int i = 0; i < CAPABILITIES.length; i++) {
            enabled[i] = GL11.glIsEnabled(CAPABILITIES[i]);
        }
        final boolean depthMask = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
        final int shadeModel = GL11.glGetInteger(GL11.GL_SHADE_MODEL);

        renderer.bindTexture(new GuiTexture(texture, 64, 64));
        final float[] tip = this.drawModel(this.model, seconds);
        if (tip != null) {
            this.drawBurst(tip[0], tip[1], burstAmount(seconds), seconds);
        }
        // What the model wrote must not hide what is drawn after it
        GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT);

        for (int i = 0; i < CAPABILITIES.length; i++) {
            setEnabled(CAPABILITIES[i], enabled[i]);
        }
        GlStateManager.depthMask(depthMask);
        GlStateManager.shadeModel(shadeModel);
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        renderer.bindDefaultTexture();
        renderer.startDrawing();
    }

    /**
     * Draws the model posed for this moment of the sequence.
     *
     * @return Where the sword's tip is (in the screen's coordinates) while the burst shows, otherwise null
     */
    @Nullable
    private float[] drawModel(final ModelPlayer model, final float seconds) {
        final float raise = raiseAmount(seconds);
        final float turn = smooth(clamp((seconds - LOWER_END) / (TURN_END - LOWER_END)));
        this.pose(model, seconds, raise, turn);

        // The screen's own matrix, to find the sword's tip on it later
        this.matrix.clear();
        GlStateManager.getFloat(GL11.GL_MODELVIEW_MATRIX, this.matrix);
        final float baseScaleX = this.matrix.get(0), baseScaleY = this.matrix.get(5);
        final float baseX = this.matrix.get(12), baseY = this.matrix.get(13);

        // Feet at the bottom of the figure's area, sized to leave room above the head for the raised sword
        final float pixelsPerBlock = this.getHeight() / FIGURE_BLOCKS;
        final float feetX = this.screenX() + this.getWidth() / 2.0F;
        final float feetY = this.screenY() + this.getHeight();

        // The same steps as the inventory's player preview (GuiInventory.drawEntityOnScreen) and RenderPlayer, so the model is lit and
        // turned the way the game draws players
        GlStateManager.pushMatrix();
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.enableColorMaterial();
        GlStateManager.enableAlpha();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.translate(feetX, feetY, 100.0F);
        GlStateManager.scale(-pixelsPerBlock, pixelsPerBlock, pixelsPerBlock);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);

        // Three quarters with a gentle sway, turning to face the viewer at the end
        final float sway = (float) Math.sin(seconds * 0.5F) * 12.0F;
        GlStateManager.rotate(lerp(BASE_YAW + sway, 0.0F, turn), 0.0F, 1.0F, 0.0F);

        // RenderLivingBase: facing out, flipped into model space with the feet at 0, at the size RenderPlayer draws players
        GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.enableRescaleNormal();
        GlStateManager.disableCull();
        GlStateManager.scale(-1.0F, -1.0F, 1.0F);
        GlStateManager.scale(0.9375F, 0.9375F, 0.9375F);
        GlStateManager.translate(0.0F, -1.501F, 0.0F);

        final float scale = 0.0625F;
        model.bipedHead.render(scale);
        model.bipedHeadwear.render(scale);
        model.bipedBody.render(scale);
        model.bipedBodyWear.render(scale);
        model.bipedRightArm.render(scale);
        model.bipedRightArmwear.render(scale);
        model.bipedLeftArm.render(scale);
        model.bipedLeftArmwear.render(scale);
        model.bipedRightLeg.render(scale);
        model.bipedRightLegwear.render(scale);
        model.bipedLeftLeg.render(scale);
        model.bipedLeftLegwear.render(scale);

        // The sword in the right hand, as LayerHeldItem places held items
        float[] tip = null;
        GlStateManager.pushMatrix();
        model.postRenderArm(scale, EnumHandSide.RIGHT);
        GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(1.0F / 16.0F, 0.125F, -0.625F);
        if (burstAmount(seconds) > 0.0F) {
            // Where the tip ends up: the held sword's third person placement (item/handheld) and RenderItem's centring
            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0F, 4.0F / 16.0F, 0.5F / 16.0F);
            GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(55.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.scale(0.85F, 0.85F, 0.85F);
            GlStateManager.translate(TIP_X - 0.5F, TIP_Y - 0.5F, TIP_Z - 0.5F);
            this.matrix.clear();
            GlStateManager.getFloat(GL11.GL_MODELVIEW_MATRIX, this.matrix);
            tip = new float[] {(this.matrix.get(12) - baseX) / baseScaleX, (this.matrix.get(13) - baseY) / baseScaleY};
            GlStateManager.popMatrix();
        }
        Minecraft.getMinecraft().getRenderItem().renderItem(this.sword, ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND);
        GlStateManager.popMatrix();

        RenderHelper.disableStandardItemLighting();
        GlStateManager.popMatrix();
        return tip;
    }

    /**
     * Standing, the sword arm raised overhead by {@code raise} (0 lowered, 1 raised), the head turned to the front by {@code turn}.
     */
    private void pose(final ModelPlayer model, final float seconds, final float raise, final float turn) {
        final float breath = seconds * 1.8F;
        final float idle = 1.0F - turn;

        model.bipedHead.rotateAngleY = (float) Math.sin(seconds * 0.7F) * 0.25F * idle;
        model.bipedHead.rotateAngleX = -0.35F * raise + (float) Math.sin(seconds * 0.9F) * 0.04F * idle;

        // Holding the sword: forward a little; raised: up overhead and out to the side
        model.bipedRightArm.rotateAngleX = lerp(-0.35F + (float) Math.sin(breath) * 0.03F, -2.85F, raise);
        model.bipedRightArm.rotateAngleY = lerp(0.0F, -0.3F, raise);
        model.bipedRightArm.rotateAngleZ = lerp((float) Math.cos(breath) * 0.05F + 0.05F, 0.35F, raise);

        model.bipedLeftArm.rotateAngleX = (float) Math.sin(breath + 1.0F) * 0.04F;
        model.bipedLeftArm.rotateAngleY = 0.0F;
        model.bipedLeftArm.rotateAngleZ = -(float) Math.cos(breath) * 0.05F - 0.05F - 0.1F * raise;

        model.bipedRightLeg.rotateAngleX = 0.0F;
        model.bipedLeftLeg.rotateAngleX = 0.0F;
        model.bipedBody.rotateAngleY = 0.0F;

        ModelBase.copyModelAngles(model.bipedHead, model.bipedHeadwear);
        ModelBase.copyModelAngles(model.bipedBody, model.bipedBodyWear);
        ModelBase.copyModelAngles(model.bipedRightArm, model.bipedRightArmwear);
        ModelBase.copyModelAngles(model.bipedLeftArm, model.bipedLeftArmwear);
        ModelBase.copyModelAngles(model.bipedRightLeg, model.bipedRightLegwear);
        ModelBase.copyModelAngles(model.bipedLeftLeg, model.bipedLeftLegwear);
    }

    /**
     * A star burst: a soft glow with long and short rays, turning slowly, drawn over everything at {@code (x, y)}.
     */
    private void drawBurst(final float x, final float y, final float amount, final float seconds) {
        if (amount <= 0.0F) {
            return;
        }
        final float size = this.getHeight() / FIGURE_BLOCKS * BURST_SIZE * (0.5F + 0.5F * amount);

        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.disableAlpha();
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE); // Adding light
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 300.0F);
        GlStateManager.rotate(seconds * 40.0F, 0.0F, 0.0F, 1.0F);

        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);

        // Glow
        final int segments = 16;
        final float glow = size * 0.35F;
        for (int i = 0; i < segments; i++) {
            final double a0 = Math.PI * 2 * i / segments, a1 = Math.PI * 2 * (i + 1) / segments;
            buffer.pos(0, 0, 0).color(1.0F, 1.0F, 0.9F, 0.9F * amount).endVertex();
            buffer.pos(Math.cos(a0) * glow, Math.sin(a0) * glow, 0).color(1.0F, 0.95F, 0.6F, 0.0F).endVertex();
            buffer.pos(Math.cos(a1) * glow, Math.sin(a1) * glow, 0).color(1.0F, 0.95F, 0.6F, 0.0F).endVertex();
        }

        // Rays, long and short in turn, fading to their tips
        for (int i = 0; i < BURST_RAYS; i++) {
            final double angle = Math.PI * 2 * i / BURST_RAYS;
            final float length = size * (i % 2 == 0 ? 1.0F : 0.55F);
            final float width = size * 0.07F;
            final double cos = Math.cos(angle), sin = Math.sin(angle);
            final double sideX = -sin * width, sideY = cos * width;
            final double tipX = cos * length, tipY = sin * length;
            buffer.pos(sideX, sideY, 0).color(1.0F, 1.0F, 0.85F, amount).endVertex();
            buffer.pos(-sideX, -sideY, 0).color(1.0F, 1.0F, 0.85F, amount).endVertex();
            buffer.pos(tipX, tipY, 0).color(1.0F, 0.9F, 0.5F, 0.0F).endVertex();
        }
        tessellator.draw();

        GlStateManager.popMatrix();
        GlStateManager.enableTexture2D();
    }

    /**
     * @return How far the sword is raised: up from the end of standing, held, then lowered slowly
     */
    private static float raiseAmount(final float seconds) {
        if (seconds < STAND_END || seconds >= LOWER_END) {
            return 0.0F;
        }
        if (seconds < RAISE_END) {
            return smooth((seconds - STAND_END) / (RAISE_END - STAND_END));
        }
        if (seconds < HOLD_END) {
            return 1.0F;
        }
        return 1.0F - smooth((seconds - HOLD_END) / (LOWER_END - HOLD_END));
    }

    /**
     * @return How bright the burst is: flaring at the top of the raise, then fading
     */
    private static float burstAmount(final float seconds) {
        final float t = seconds - RAISE_END;
        if (t < 0.0F || t >= BURST_TIME) {
            return 0.0F;
        }
        return t < BURST_FLARE ? t / BURST_FLARE : 1.0F - (t - BURST_FLARE) / (BURST_TIME - BURST_FLARE);
    }

    private static float smooth(final float t) {
        return t * t * (3.0F - 2.0F * t);
    }

    private static float clamp(final float t) {
        return Math.max(0.0F, Math.min(1.0F, t));
    }

    private static float lerp(final float from, final float to, final float amount) {
        return from + (to - from) * amount;
    }

    /**
     * Turns a setting on or off through GlStateManager, so its record of the setting stays right.
     */
    private static void setEnabled(final int capability, final boolean enabled) {
        final Runnable enable, disable;
        switch (capability) {
            case GL11.GL_ALPHA_TEST:
                enable = GlStateManager::enableAlpha;
                disable = GlStateManager::disableAlpha;
                break;
            case GL11.GL_DEPTH_TEST:
                enable = GlStateManager::enableDepth;
                disable = GlStateManager::disableDepth;
                break;
            case GL11.GL_BLEND:
                enable = GlStateManager::enableBlend;
                disable = GlStateManager::disableBlend;
                break;
            case GL11.GL_LIGHTING:
                enable = GlStateManager::enableLighting;
                disable = GlStateManager::disableLighting;
                break;
            case GL11.GL_CULL_FACE:
                enable = GlStateManager::enableCull;
                disable = GlStateManager::disableCull;
                break;
            case GL11.GL_COLOR_MATERIAL:
                enable = GlStateManager::enableColorMaterial;
                disable = GlStateManager::disableColorMaterial;
                break;
            case GL12.GL_RESCALE_NORMAL:
                enable = GlStateManager::enableRescaleNormal;
                disable = GlStateManager::disableRescaleNormal;
                break;
            default:
                enable = GlStateManager::enableTexture2D;
                disable = GlStateManager::disableTexture2D;
                break;
        }
        (enabled ? enable : disable).run();
    }

    /**
     * Only for show: the mouse goes through it to whatever is behind.
     */
    @Nullable
    @Override
    public UIComponent getComponentAt(final int x, final int y) {
        return null;
    }


    /**
     * Reads a skin from the jar once, converted the way the game converts player skins (old 64 x 32 skins to the 64 x 64 layout).
     */
    private static Optional<ResourceLocation> loadSkin(final ResourceLocation skin) {
        return SKINS.computeIfAbsent(skin, key -> {
            final Minecraft minecraft = Minecraft.getMinecraft();
            try (final IResource resource = minecraft.getResourceManager().getResource(key)) {
                final BufferedImage image = new ImageBufferDownload().parseUserSkin(TextureUtil.readBufferedImage(resource.getInputStream()));
                return Optional.of(minecraft.getTextureManager().getDynamicTextureLocation("almura_figure", new DynamicTexture(image)));
            } catch (final IOException e) {
                LOGGER.error("Could not read the skin '{}'.", key, e);
                return Optional.empty();
            }
        });
    }
}
