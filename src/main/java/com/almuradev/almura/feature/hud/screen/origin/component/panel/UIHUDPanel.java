/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.hud.screen.origin.component.panel;

import com.almuradev.almura.shared.client.GuiConfig;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.container.UIPanel;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.renderer.icon.provider.GuiIconProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.util.Color;

@SideOnly(Side.CLIENT)
public class UIHUDPanel extends UIPanel {

    int gray = org.spongepowered.api.util.Color.ofRgb(128, 128, 128).getRgb();
    int orange = org.spongepowered.api.util.Color.ofRgb(255, 165, 0).getRgb();
    int red = org.spongepowered.api.util.Color.ofRgb(255, 0, 0).getRgb();
    int light_red = org.spongepowered.api.util.Color.ofRgb(255, 51, 51).getRgb();
    int white = org.spongepowered.api.util.Color.WHITE.getRgb();

    public UIHUDPanel(MalisisGui gui, int width, int height) {
        super(gui, width, height);
        this.iconProvider = new GuiIconProvider(GuiConfig.Icon.VANILLA_CONTAINER_INVENTORY_ADVANCEMENT);
    }

    public FontOptions setFontColorandSize(int color, float size) {
        return FontOptions.builder()
                .color(color)
                .scale(size)
                .build();
    }
}
