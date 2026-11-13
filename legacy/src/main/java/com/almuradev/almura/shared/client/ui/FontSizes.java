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

    FontOptions TINY = FontOptions.builder()
            .scale(0.6f)
            .build();
    FontOptions VERY_SMALL = FontOptions.builder()
            .scale(0.7f)
            .build();
    FontOptions SMALLER = FontOptions.builder()
            .scale(0.8f)
            .build();
    FontOptions SMALL = FontOptions.builder()
            .scale(0.9f)
            .build();
    FontOptions NORMAL = FontOptions.builder()
            .scale(1.0f)
            .build();
}
