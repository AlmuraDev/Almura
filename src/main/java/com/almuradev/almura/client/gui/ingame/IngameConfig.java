/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.ingame;

import com.almuradev.almura.client.ChatColor;
import com.almuradev.almura.client.ClientProxy;
import com.almuradev.almura.client.gui.AlmuraGui;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.component.container.UIBackgroundContainer;
import net.malisis.core.client.gui.component.decoration.UILabel;

public class IngameConfig extends AlmuraGui {

    public IngameConfig() {
        guiscreenBackground = false; // prevent full screen black background.

        // Construct Hud with all elements
        final UIBackgroundContainer configPanel = new UIBackgroundContainer(this);
        configPanel.setSize(285, 175);
        configPanel.setPosition(5, 0, Anchor.CENTER | Anchor.MIDDLE);
        configPanel.setColor(Integer.MIN_VALUE);
        configPanel.setTopAlpha(180);
        configPanel.setBottomAlpha(180);
        configPanel.setClipContent(false);

        // Title
        UILabel configTitle = new UILabel(this, ChatColor.AQUA + "Almura Config");
        configTitle.setPosition(0, 3, Anchor.CENTER | Anchor.TOP);
        configTitle.setFontScale(1.1F);

        configPanel.add(configTitle);

        addToScreen(configPanel);
    }

    @Override
    protected void keyTyped(char keyChar, int keyCode) {
        if (ClientProxy.BINDING_CONFIG_GUI.getKeyCode() == keyCode) {
            close();
        }
    }
}
