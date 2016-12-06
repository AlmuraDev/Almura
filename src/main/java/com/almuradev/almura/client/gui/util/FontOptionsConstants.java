/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.util;

import net.malisis.core.renderer.font.FontOptions;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.util.Color;

@SideOnly(Side.CLIENT)
public final class FontOptionsConstants {

    public static final FontOptions FRO_COLOR_ALMURA_BLUE = FontOptions.builder().color(Color.ofRgb(19, 111, 181).getRgb())
            .build();
    public static final FontOptions FRO_COLOR_GRAY = FontOptions.builder().color(Color.ofRgb(128, 128, 128).getRgb()).build();
    public static final FontOptions FRO_COLOR_GRAY_SCALE_110 = FontOptions.builder().from(FRO_COLOR_GRAY).scale(1.10F).build();
    public static final FontOptions FRO_COLOR_ORANGE = FontOptions.builder().color(Color.ofRgb(255, 165, 0).getRgb()).build();
    public static final FontOptions FRO_COLOR_ORANGE_SCALE_080 = FontOptions.builder().from(FRO_COLOR_ORANGE)
            .scale(0.8F).build();
    public static final FontOptions FRO_COLOR_ORANGE_SCALE_110 = FontOptions.builder().from(FRO_COLOR_ORANGE)
            .scale(1.10F).build();
    public static final FontOptions FRO_COLOR_RED = FontOptions.builder().color(Color.ofRgb(255, 0, 0).getRgb()).build();
    public static final FontOptions FRO_COLOR_WHITE = FontOptions.builder().color(Color.WHITE.getRgb()).build();
    public static final FontOptions FRO_COLOR_WHITE_SCALE_080 = FontOptions.builder().from(FRO_COLOR_WHITE)
            .scale(0.8F).build();
    public static final FontOptions FRO_COLOR_WHITE_SCALE_050 = FontOptions.builder().from(FRO_COLOR_WHITE)
            .scale(0.5f).build();
    public static final FontOptions FRO_SCALE_050 = FontOptions.builder().scale(0.50f).build();
    public static final FontOptions FRO_SCALE_065 = FontOptions.builder().scale(0.65f).build();
    public static final FontOptions FRO_SCALE_070 = FontOptions.builder().scale(0.70f).build();
    public static final FontOptions FRO_SCALE_105 = FontOptions.builder().scale(1.05f).build();
    public static final FontOptions FRO_SCALE_110 = FontOptions.builder().scale(1.10f).build();
    public static final FontOptions FRO_SCALE_150 = FontOptions.builder().scale(1.50f).build();

    private FontOptionsConstants() {
    }
}
