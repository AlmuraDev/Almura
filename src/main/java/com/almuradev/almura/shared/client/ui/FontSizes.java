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

@SideOnly(Side.CLIENT)
public interface FontSizes {

    // Be advised using this interface will remove all other options created prior.

    FontOptions NORMAL = FontOptions.builder()
            .scale(1.0F)
            .build();
    FontOptions SMALL = FontOptions.builder()
            .scale(0.9F)
            .build();
    FontOptions SMALLER = FontOptions.builder()
            .scale(0.8F)
            .build();
    FontOptions VERYSMALL = FontOptions.builder()
            .scale(0.7F)
            .build();
    FontOptions TINY = FontOptions.builder()
            .scale(0.6F)
            .build();
}
