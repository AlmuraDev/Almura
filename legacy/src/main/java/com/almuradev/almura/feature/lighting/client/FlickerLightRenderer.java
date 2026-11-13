/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.lighting.client;

import com.almuradev.content.type.block.flicker.FlickerLightTileEntity;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

/**
 * Draws a light's fast flicker by laying a darkened copy of its model over it. Nothing is drawn while the light is steady,
 * and the chunk is never rebuilt for this.
 */
@SideOnly(Side.CLIENT)
final class FlickerLightRenderer extends TileEntitySpecialRenderer<FlickerLightTileEntity> {

    /** Below this much darkening the overlay isn't worth drawing. */
    private static final float MIN_DARKNESS = 0.02f;
    /** Grows the overlay a hair so it sits on top of the model instead of fighting it. */
    private static final float OVERLAY_SCALE = 1.004f;
    // OpenGL 1.4 blend factors (LWJGL only names them in ARBImaging)
    private static final int GL_CONSTANT_ALPHA = 0x8003;
    private static final int GL_ONE_MINUS_CONSTANT_ALPHA = 0x8004;

    private final ClientFlickerManager manager;

    FlickerLightRenderer(final ClientFlickerManager manager) {
        this.manager = manager;
    }

    @Override
    public void render(final FlickerLightTileEntity te, final double x, final double y, final double z, final float partialTicks,
            final int destroyStage, final float alpha) {
        final float darkness = 1f - this.manager.getBrightness(te.getPos(), partialTicks);
        if (darkness < MIN_DARKNESS) {
            return;
        }

        final IBlockState state = te.getWorld().getBlockState(te.getPos());
        final BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
        final IBakedModel model = dispatcher.getModelForState(state);
        final boolean lighting = GL11.glIsEnabled(GL11.GL_LIGHTING);

        this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);
        GlStateManager.scale(OVERLAY_SCALE, OVERLAY_SCALE, OVERLAY_SCALE);
        GlStateManager.translate(-0.5, -0.5, -0.5);

        GlStateManager.disableLighting();
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1f);
        GlStateManager.enableBlend();
        GlStateManager.depthMask(false);
        // Black, blended by how dark the light should be right now
        GL14.glBlendColor(0f, 0f, 0f, darkness);
        GlStateManager.blendFunc(GL_CONSTANT_ALPHA, GL_ONE_MINUS_CONSTANT_ALPHA);

        dispatcher.getBlockModelRenderer().renderModelBrightnessColor(state, model, 1f, 0f, 0f, 0f);

        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL14.glBlendColor(0f, 0f, 0f, 0f);
        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
        if (lighting) {
            GlStateManager.enableLighting();
        }
        GlStateManager.popMatrix();
    }
}
