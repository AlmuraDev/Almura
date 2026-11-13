/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.menu.figure;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import org.jspecify.annotations.Nullable;

/**
 * The figure for one frame: how far into its sequence, and where on screen.
 *
 * @param seconds how far into the sequence
 * @param scale the figure's scale, in GUI pixels per block
 */
public record AlmuraFigureRenderState(
    float seconds,
    int x0,
    int y0,
    int x1,
    int y1,
    float scale,
    @Nullable ScreenRectangle scissorArea,
    @Nullable ScreenRectangle bounds
) implements PictureInPictureRenderState {

    public AlmuraFigureRenderState(final float seconds, final int x0, final int y0, final int x1, final int y1, final float scale) {
        this(seconds, x0, y0, x1, y1, scale, null, PictureInPictureRenderState.getBounds(x0, y0, x1, y1, null));
    }
}
