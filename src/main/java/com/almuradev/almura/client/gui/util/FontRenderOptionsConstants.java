/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.util;

import net.malisis.core.renderer.font.FontRenderOptions;
import org.spongepowered.api.util.Color;

public class FontRenderOptionsConstants {

    public static final FontRenderOptions FRO_COLOR_WHITE = new FontRenderOptions();
    public static final FontRenderOptions FRO_COLOR_ORANGE = new FontRenderOptions();
    public static final FontRenderOptions FRO_SCALE_050 = new FontRenderOptions();
    public static final FontRenderOptions FRO_SCALE_065 = new FontRenderOptions();
    public static final FontRenderOptions FRO_SCALE_070 = new FontRenderOptions();
    public static final FontRenderOptions FRO_SCALE_110 = new FontRenderOptions();

    private static final Color ORANGE = Color.ofRgb(255, 165, 0);

    static {
        FRO_COLOR_WHITE.color = Color.WHITE.getRgb();
        FRO_COLOR_ORANGE.color = ORANGE.getRgb();
        FRO_SCALE_050.fontScale = 0.50f;
        FRO_SCALE_065.fontScale = 0.65f;
        FRO_SCALE_070.fontScale = 0.7f;
        FRO_SCALE_110.fontScale = 1.1f;
    }
}
