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
    int BLACK = Color.ofRgb(0, 0, 0).getRgb();
    FontOptions BLACK_FO = FontOptions.builder().color(BLACK).build();

    int DARK_BLUE = Color.ofRgb(0, 0, 170).getRgb();
    FontOptions DARK_BLUE_FO = FontOptions.builder().color(DARK_BLUE).build();

    int DARK_AQUA = Color.ofRgb(0, 170, 170).getRgb();
    FontOptions DARK_AQUA_FO = FontOptions.builder().color(DARK_AQUA).build();

    int DARK_GREEN = Color.ofRgb(0, 170, 0).getRgb();
    FontOptions DARK_GREEN_FO = FontOptions.builder().color(DARK_GREEN).build();

    int DARK_RED = org.spongepowered.api.util.Color.ofRgb(170, 0, 0).getRgb();
    FontOptions DARK_RED_FO = FontOptions.builder().color(DARK_RED).build();

    int DARK_PURPLE = org.spongepowered.api.util.Color.ofRgb(170, 0, 170).getRgb();
    FontOptions DARK_PURPLE_FO = FontOptions.builder().color(DARK_PURPLE).build();

    int GOLD = Color.ofRgb(255, 170, 0).getRgb();
    FontOptions GOLD_FO = FontOptions.builder().color(GOLD).build();

    int GRAY = Color.ofRgb(170, 170, 170).getRgb();
    FontOptions GRAY_FO = FontOptions.builder().color(GRAY).build();

    int DARK_GRAY = org.spongepowered.api.util.Color.ofRgb(85, 85, 85).getRgb();
    FontOptions DARK_GRAY_FO = FontOptions.builder().color(DARK_GRAY).build();

    int BLUE = Color.ofRgb(85, 85, 255).getRgb();
    FontOptions BLUE_FO = FontOptions.builder().color(BLUE).build();

    int GREEN = Color.ofRgb(85, 255, 85).getRgb();
    FontOptions GREEN_FO = FontOptions.builder().color(GREEN).build();

    int AQUA = Color.ofRgb(85, 255, 255).getRgb();
    FontOptions AQUA_FO = FontOptions.builder().color(AQUA).build();

    int RED = org.spongepowered.api.util.Color.ofRgb(255, 85, 85).getRgb();
    FontOptions RED_FO = FontOptions.builder().color(RED).build();

    int LIGHT_PURPLE = org.spongepowered.api.util.Color.ofRgb(255, 85, 255).getRgb();
    FontOptions LIGHT_PURPLE_FO = FontOptions.builder().color(LIGHT_PURPLE).build();

    int YELLOW = org.spongepowered.api.util.Color.ofRgb(255, 255, 85).getRgb();
    FontOptions YELLOW_FO = FontOptions.builder().color(YELLOW).build();

    int WHITE = org.spongepowered.api.util.Color.ofRgb(255, 255, 255).getRgb();
    FontOptions WHITE_FO = FontOptions.builder().color(WHITE).build();
}

