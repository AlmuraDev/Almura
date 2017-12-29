/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.client.ui;

import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.renderer.font.MalisisFont;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface Fonts {

    MalisisFont MINECRAFT = MalisisFont.minecraftFont;

    // TODO(kashike): rf when MaCo PR is merged
    static FontOptions colorAndScale(final int color, final float scale) {
        return FontOptions.builder()
                .color(color)
                .scale(scale)
                .build();
    }
}
