/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.client.ui;

import net.malisis.core.renderer.font.FontOptions;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.util.Color;

@SideOnly(Side.CLIENT)
public interface FontColors {

    // Be advised using this interface will remove all other options created prior.

    FontOptions FRO_GRAY = FontOptions.builder()
            .color(Color.ofRgb(128, 128, 128).getRgb())
            .build();
    FontOptions FRO_ORANGE = FontOptions.builder()
            .color(Color.ofRgb(255, 165, 0).getRgb())
            .build();
    FontOptions FRO_RED = FontOptions.builder()
            .color(Color.ofRgb(255, 0, 0).getRgb())
            .build();
    FontOptions FRO_LIGHT_RED = FontOptions.builder()
            .color(Color.ofRgb(255, 51, 51).getRgb())
            .build();
    FontOptions FRO_WHITE = FontOptions.builder()
            .color(Color.WHITE.getRgb())
            .build();
}
