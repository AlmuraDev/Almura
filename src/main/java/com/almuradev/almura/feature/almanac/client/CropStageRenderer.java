/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.almanac.client;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.texture.OverlayTexture;

/**
 * Draws a crop stage's block model, turned as a block in the inventory, as 3.5's Almanac did. Registered with NeoForge by the client's setup.
 */
public final class CropStageRenderer extends PictureInPictureRenderer<CropStageRenderState> {

    // Full brightness, as vanilla's items in the GUI
    private static final int LIGHT = 15728880;
    private static final BlockDisplayContext DISPLAY = BlockDisplayContext.create();

    private final BlockModelRenderState model = new BlockModelRenderState();

    @Override
    public Class<CropStageRenderState> getRenderStateClass() {
        return CropStageRenderState.class;
    }

    @Override
    protected void renderToTexture(final CropStageRenderState state, final PoseStack poseStack, final SubmitNodeCollector collector) {
        final Minecraft minecraft = Minecraft.getInstance();
        minecraft.gameRenderer.lighting().setupFor(Lighting.Entry.ITEMS_FLAT);
        final CropStageView view = state.view();
        // Up the right way, as vanilla draws items here; then turned as an inventory block, its view's centre in the middle
        poseStack.scale(1f, -1f, -1f);
        poseStack.rotateDegrees(Axis.XP, CropStageView.TILT);
        poseStack.rotateDegrees(Axis.YP, CropStageView.TURN);
        poseStack.translate(-view.centerX(), -view.centerY(), -view.centerZ());
        try {
            minecraft.getBlockModelResolver().update(this.model, state.state(), DISPLAY);
            this.model.submitMultiLayer(poseStack, collector, LIGHT, OverlayTexture.NO_OVERLAY, 0);
        } catch (final RuntimeException e) {
            // A model that can't draw outside the world shows nothing rather than break the screen
        }
    }

    @Override
    protected float getTranslateY(final int height, final int guiScale) {
        return height / 2f;
    }

    @Override
    protected String getTextureLabel() {
        return "almura crop stage";
    }
}
