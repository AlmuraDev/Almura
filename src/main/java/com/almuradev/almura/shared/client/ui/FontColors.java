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

    int GRAY = Color.ofRgb(128, 128, 128).getRgb();
    FontOptions GRAY_FO = FontOptions.builder()
            .color(GRAY)
            .build();

    int ORANGE = org.spongepowered.api.util.Color.ofRgb(255, 165, 0).getRgb();
    FontOptions ORANGE_FO = FontOptions.builder()
            .color(ORANGE)
            .build();

    int RED = org.spongepowered.api.util.Color.ofRgb(255, 0, 0).getRgb();
    FontOptions RED_FO = FontOptions.builder()
            .color(RED)
            .build();

    int LIGHT_RED = org.spongepowered.api.util.Color.ofRgb(255, 51, 51).getRgb();
    FontOptions LIGHT_RED_FO = FontOptions.builder()
            .color(LIGHT_RED)
            .build();

    int WHITE = org.spongepowered.api.util.Color.WHITE.getRgb();
    FontOptions WHITE_FO = FontOptions.builder()
            .color(WHITE)
            .build();
}
