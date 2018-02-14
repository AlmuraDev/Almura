package com.almuradev.almura.feature.healthbar.asm.mixin.client.renderer.entity;

import com.almuradev.almura.asm.StaticAccess;
import com.almuradev.almura.feature.title.ClientTitleManager;
import com.google.inject.Inject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.entity.living.animal.Animal;
import org.spongepowered.api.entity.living.monster.Monster;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;


@SideOnly(Side.CLIENT)
@Mixin(value = RenderLivingBase.class, priority = 999)
public abstract class MixinRenderLivingBase extends Render<EntityLivingBase> {

    @Inject private static ClientTitleManager manager;
    @Shadow protected ModelBase mainModel;

    public MixinRenderLivingBase(RenderManager renderManagerIn, ModelBase modelBaseIn, float shadowSizeIn) {
        super(renderManagerIn);
        this.mainModel = modelBaseIn;
        this.shadowSize = shadowSizeIn;
    }

    /**
     * @author Dockter
     * @reason To have control over rendering names
     */

    @Overwrite // Was override but synthetic bridge conflict occurred.
    public void renderName(EntityLivingBase entity, double x, double y, double z) {
        if ((entity instanceof Player || entity instanceof EntityPlayerSP) && StaticAccess.config.get().client.playerNameRenderDistance > 0) {
            this.renderLabel(entity, entity.getDisplayName().getFormattedText(), x, y, z, StaticAccess.config.get().client.playerNameRenderDistance, StaticAccess.config.get().client.displayNames, StaticAccess.config.get().client.displayHealthbars);
        }

        if (entity instanceof Monster && StaticAccess.config.get().client.enemyNameRenderDistance > 0) {
            this.renderLabel(entity, entity.getDisplayName().getFormattedText(), x, y, z, StaticAccess.config.get().client.enemyNameRenderDistance, StaticAccess.config.get().client.displayNames, StaticAccess.config.get().client.displayHealthbars);
        }

        if (entity instanceof Animal && StaticAccess.config.get().client.animalNameRenderDistance > 0) {
            this.renderLabel(entity, entity.getDisplayName().getFormattedText(), x, y, z, StaticAccess.config.get().client.animalNameRenderDistance, StaticAccess.config.get().client.displayNames, StaticAccess.config.get().client.displayHealthbars);
        }
    }

    private void renderLabel(EntityLivingBase entityIn, String name, double x, double y, double z, int maxDistance, boolean showName, boolean showHealthbar) {
        double distance = entityIn.getDistanceSq(this.renderManager.renderViewEntity);
        double distanceLimit = (maxDistance*maxDistance); // Default is 4096

        if (distance <= distanceLimit) {  //default 4096
            boolean isSneaking = entityIn.isSneaking();
            float viewerYaw = this.renderManager.playerViewY;
            float viewerPitch = this.renderManager.playerViewX;
            boolean isThirdPersonFrontal = this.renderManager.options.thirdPersonView == 2;
            float sneakAdjustment = entityIn.height + 0.5F - (isSneaking ? 0.25F : 0.0F);
            int verticalShift = 0;

            drawCombo(entityIn, this.getFontRendererFromRenderManager(), name, (float) x, (float) y + sneakAdjustment, (float) z, verticalShift, viewerYaw, viewerPitch, isThirdPersonFrontal, isSneaking, showName, showHealthbar);
        }
    }


    private void drawCombo(EntityLivingBase entityIn, FontRenderer fontRendererIn, String str, float x, float y, float z, int verticalShift, float viewerYaw, float viewerPitch, boolean isThirdPersonFrontal, boolean isSneaking, boolean showName,
            boolean showHealthbar) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-viewerYaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate((float) (isThirdPersonFrontal ? -1 : 1) * viewerPitch, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(-0.025F, -0.025F, 0.025F);
        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);

        final String title = manager.getTitle(entityIn.getUniqueID());

        if (!isSneaking) {
            GlStateManager.disableDepth();
        }

        int i = fontRendererIn.getStringWidth(str) / 2;

        double a = entityIn.getMaxHealth() / entityIn.getHealth();
        double b = 100 / a;
        double c = (double) (i * 2) / 100;
        double size = c * b;

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

        GlStateManager.disableTexture2D();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();

        if (showName) {
            if (title != null) {
                int t = fontRendererIn.getStringWidth(title) / 2;
                int m = t;

                if (t > i) {
                    m = t;
                } else {
                    m = i;
                }

                // Draw dark box for Title & Nameplate.
                bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
                bufferbuilder.pos((double) (-m - 1), (double) (-1 + verticalShift - 10), 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
                bufferbuilder.pos((double) (-m - 1), (double) (18 + verticalShift - 10), 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
                bufferbuilder.pos((double) (m + 1), (double) (18 + verticalShift - 10), 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
                bufferbuilder.pos((double) (m + 1), (double) (-1 + verticalShift - 10), 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
                tessellator.draw();
            } else {
                // Draw dark box for Nameplate.
                bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
                bufferbuilder.pos((double) (-i - 1), (double) (-1 + verticalShift), 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
                bufferbuilder.pos((double) (-i - 1), (double) (8 + verticalShift), 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
                bufferbuilder.pos((double) (i + 1), (double) (8 + verticalShift), 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
                bufferbuilder.pos((double) (i + 1), (double) (-1 + verticalShift), 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
                tessellator.draw();
            }
        }

        // Re-position for healthbar.
        int healthbarOffset = verticalShift + 10;

        if (showHealthbar && entityIn.getHealth() > 0 && !isSneaking) {
            // Draw healthbar
            bufferbuilder = tessellator.getBuffer();
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
            bufferbuilder.pos((double) (-i - 1), (double) (-1 + healthbarOffset), 0.0D).color(1.0F, 0.0F, 0.0F, 0.45F).endVertex();
            bufferbuilder.pos((double) (-i - 1), (double) (3 + healthbarOffset), 0.0D).color(1.0F, 0.0F, 0.0F, 0.45F).endVertex();
            bufferbuilder.pos((double) ((-i + 1) + size), (double) (3 + healthbarOffset), 0.0D).color(1.0F, 0.0F, 0.0F, 0.45F).endVertex();
            bufferbuilder.pos((double) ((-i + 1) + size), (double) (-1 + healthbarOffset), 0.0D).color(1.0F, 0.0F, 0.0F, 0.45F).endVertex();
            tessellator.draw();
        }

        GlStateManager.enableTexture2D();

        if (title != null) {
            verticalShift = verticalShift - 10;
        }

        if (!isSneaking) {
            if (showName) {
                if (title != null) {
                    fontRendererIn.drawString(title, -fontRendererIn.getStringWidth(title) / 2, verticalShift + 10, 553648127);
                }
                fontRendererIn.drawString(str, -fontRendererIn.getStringWidth(str) / 2, verticalShift, 553648127);
            }
            GlStateManager.enableDepth();
        }

        GlStateManager.depthMask(true);
        if (showName) {
            if (title != null) {
                fontRendererIn.drawString(title, -fontRendererIn.getStringWidth(title) / 2, verticalShift + 10, isSneaking ? 553648127 : -1);
            }
            fontRendererIn.drawString(str, -fontRendererIn.getStringWidth(str) / 2, verticalShift, isSneaking ? 553648127 : -1);
        }
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }
}
