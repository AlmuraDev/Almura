/*
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

    public static final FontOptions FRO_COLOR_ALMURA_BLUE = FontOptions.builder().color(Color.ofRgb(19, 111, 181).getRgb()).build();
    public static final FontOptions FRO_COLOR_GRAY = FontOptions.builder().color(Color.ofRgb(128, 128, 128).getRgb()).build();
    public static final FontOptions FRO_COLOR_ORANGE = FontOptions.builder().color(Color.ofRgb(255, 165, 0).getRgb()).build();
    public static final FontOptions FRO_COLOR_RED = FontOptions.builder().color(Color.ofRgb(255, 0, 0).getRgb()).build();
    public static final FontOptions FRO_COLOR_LIGHT_RED = FontOptions.builder().color(Color.ofRgb(255, 51, 51).getRgb()).build();
    public static final FontOptions FRO_COLOR_WHITE = FontOptions.builder().color(Color.WHITE.getRgb()).build();

    private FontOptionsConstants() {
    }
}
