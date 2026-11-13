/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.menu.figure;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Unit;

/**
 * Draws the figure into its area of the screen, the way vanilla's skin preview draws a player: the posed model, and the diamond sword in its
 * right hand. Registered with NeoForge in {@link com.almuradev.almura.AlmuraClient}.
 */
public final class AlmuraFigureRenderer extends PictureInPictureRenderer<AlmuraFigureRenderState> {

    // Full brightness, as vanilla's skin preview
    private static final int LIGHT = 15728880;

    @Override
    public Class<AlmuraFigureRenderState> getRenderStateClass() {
        return AlmuraFigureRenderState.class;
    }

    @Override
    protected void renderToTexture(final AlmuraFigureRenderState state, final PoseStack poseStack, final SubmitNodeCollector collector) {
        Minecraft.getInstance().gameRenderer.lighting().setupFor(Lighting.Entry.PLAYER_SKIN);

        AlmuraFigure.pose(state.seconds());
        AlmuraFigure.toModel(poseStack, state.seconds());
        collector.submitModel(AlmuraFigure.drawn(), Unit.INSTANCE, poseStack, AlmuraFigure.SKIN, LIGHT, OverlayTexture.NO_OVERLAY, 0);

        poseStack.pushPose();
        AlmuraFigure.toHand(poseStack);
        AlmuraFigure.toSword(poseStack);
        FigureSword.submit(poseStack, collector, LIGHT);
        poseStack.popPose();
    }

    @Override
    protected String getTextureLabel() {
        return "almura figure";
    }
}
