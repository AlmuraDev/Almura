/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.util;

import com.almuradev.almura.client.gui.util.builders.FontRenderOptionsBuilder;
import net.malisis.core.renderer.font.FontRenderOptions;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.util.Color;

@SideOnly(Side.CLIENT)
public final class FontRenderOptionsConstants {

    public static final FontRenderOptions FRO_COLOR_ALMURA_BLUE = FontRenderOptionsBuilder.builder().color(Color.ofRgb(19, 111, 181).getRgb())
            .build();
    public static final FontRenderOptions FRO_COLOR_GRAY = FontRenderOptionsBuilder.builder().color(Color.ofRgb(128, 128, 128).getRgb()).build();
    public static final FontRenderOptions FRO_COLOR_GRAY_SCALE_110 = FontRenderOptionsBuilder.builder().from(FRO_COLOR_GRAY).fontScale(1.10F).build();
    public static final FontRenderOptions FRO_COLOR_ORANGE = FontRenderOptionsBuilder.builder().color(Color.ofRgb(255, 165, 0).getRgb()).build();
    public static final FontRenderOptions FRO_COLOR_ORANGE_SCALE_080 = FontRenderOptionsBuilder.builder().from(FRO_COLOR_ORANGE)
            .fontScale(0.8F).build();
    public static final FontRenderOptions FRO_COLOR_ORANGE_SCALE_110 = FontRenderOptionsBuilder.builder().from(FRO_COLOR_ORANGE)
            .fontScale(1.10F).build();
    public static final FontRenderOptions FRO_COLOR_RED = FontRenderOptionsBuilder.builder().color(Color.ofRgb(255, 0, 0).getRgb()).build();
    public static final FontRenderOptions FRO_COLOR_WHITE = FontRenderOptionsBuilder.builder().color(Color.WHITE.getRgb()).build();
    public static final FontRenderOptions FRO_COLOR_WHITE_SCALE_080 = FontRenderOptionsBuilder.builder().from(FRO_COLOR_WHITE)
            .fontScale(0.8F).build();
    public static final FontRenderOptions FRO_SCALE_050 = FontRenderOptionsBuilder.builder().fontScale(0.50f).build();
    public static final FontRenderOptions FRO_SCALE_065 = FontRenderOptionsBuilder.builder().fontScale(0.65f).build();
    public static final FontRenderOptions FRO_SCALE_070 = FontRenderOptionsBuilder.builder().fontScale(0.70f).build();
    public static final FontRenderOptions FRO_SCALE_105 = FontRenderOptionsBuilder.builder().fontScale(1.05f).build();
    public static final FontRenderOptions FRO_SCALE_110 = FontRenderOptionsBuilder.builder().fontScale(1.10f).build();
    public static final FontRenderOptions FRO_SCALE_150 = FontRenderOptionsBuilder.builder().fontScale(1.50f).build();

    private FontRenderOptionsConstants() {}
}
