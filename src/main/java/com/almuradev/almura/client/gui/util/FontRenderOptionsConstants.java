/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.util;

import net.malisis.core.renderer.font.FontRenderOptions;
import org.spongepowered.api.util.Color;

public final class FontRenderOptionsConstants {

    public static final FontRenderOptions FRO_COLOR_WHITE = FontRenderOptionsBuilder.builder().color(Color.WHITE.getRgb()).build();
    public static final FontRenderOptions FRO_COLOR_WHITE_SCALE_080 = FontRenderOptionsBuilder.builder().from(FRO_COLOR_WHITE)
            .fontScale(0.8F).build();
    public static final FontRenderOptions FRO_COLOR_ORANGE = FontRenderOptionsBuilder.builder().color(Color.ofRgb(255, 165, 0).getRgb()).build();
    public static final FontRenderOptions FRO_COLOR_ORANGE_SCALE_080 = FontRenderOptionsBuilder.builder().from(FRO_COLOR_ORANGE)
            .fontScale(0.8F).build();
    public static final FontRenderOptions FRO_SCALE_050 = FontRenderOptionsBuilder.builder().fontScale(0.50f).build();
    public static final FontRenderOptions FRO_SCALE_065 = FontRenderOptionsBuilder.builder().fontScale(0.65f).build();
    public static final FontRenderOptions FRO_SCALE_070 = FontRenderOptionsBuilder.builder().fontScale(0.70f).build();
    public static final FontRenderOptions FRO_SCALE_110 = FontRenderOptionsBuilder.builder().fontScale(1.10f).build();

    private FontRenderOptionsConstants() {}
}
