/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.component.hud;

import com.almuradev.almura.client.gui.GuiConfig;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.container.UIPanel;
import net.malisis.core.renderer.icon.provider.GuiIconProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class UIHUDPanel extends UIPanel {

    public UIHUDPanel(MalisisGui gui, int width, int height) {
        super(gui, width, height);
        this.iconProvider = new GuiIconProvider(GuiConfig.Icon.VANILLA_CONTAINER_INVENTORY_ADVANCEMENT);
    }
}
