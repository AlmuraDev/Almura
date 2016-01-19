/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client;

import com.almuradev.almura.util.Color;
import com.almuradev.almura.util.Colors;
import net.malisis.core.renderer.font.FontRenderOptions;

public class FontRenderOptionsConstants {

    public static final FontRenderOptions FRO_COLOR_WHITE = new FontRenderOptions();
    public static final FontRenderOptions FRO_COLOR_ORANGE = new FontRenderOptions();
    public static final FontRenderOptions FRO_SCALE_050 = new FontRenderOptions();
    public static final FontRenderOptions FRO_SCALE_065 = new FontRenderOptions();
    public static final FontRenderOptions FRO_SCALE_070 = new FontRenderOptions();
    public static final FontRenderOptions FRO_SCALE_110 = new FontRenderOptions();

    private static final Color ORANGE = new Color("orange", 16753920);

    static {
        FRO_COLOR_WHITE.color = Colors.WHITE.getGuiColorCode();
        FRO_COLOR_ORANGE.color = ORANGE.getGuiColorCode();
        FRO_SCALE_050.fontScale = 0.50f;
        FRO_SCALE_065.fontScale = 0.65f;
        FRO_SCALE_070.fontScale = 0.7f;
        FRO_SCALE_110.fontScale = 1.1f;
    }
}
