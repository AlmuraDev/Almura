/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.almanac.client;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * One crop stage drawn as a 3D model in the Almanac's growth stages window, through {@link CropStageRenderer}.
 *
 * @param state the planted block at this stage
 * @param view where to look, shared by every stage of the crop so they line up
 * @param scale GUI pixels per block
 */
public record CropStageRenderState(
    BlockState state,
    CropStageView view,
    int x0,
    int y0,
    int x1,
    int y1,
    float scale,
    @Nullable ScreenRectangle scissorArea,
    @Nullable ScreenRectangle bounds
) implements PictureInPictureRenderState {

    CropStageRenderState(final BlockState state, final CropStageView view, final int x0, final int y0, final int x1, final int y1, final float scale) {
        this(state, view, x0, y0, x1, y1, scale, null, PictureInPictureRenderState.getBounds(x0, y0, x1, y1, null));
    }
}
