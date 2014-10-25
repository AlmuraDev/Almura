/**
 * This file is part of AlmuraMod, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almuramod.gui;

import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.container.UIPanel;
import net.malisis.core.client.gui.component.container.UIWindow;
import net.malisis.core.client.gui.component.control.UICloseHandle;
import net.malisis.core.client.gui.component.control.UIMoveHandle;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.event.MouseEvent;

public class MessageBox extends MalisisGui {
    public MessageBox(String title, String message) {
        //Title bar
        UIWindow window = new UIWindow(title, 200, 75).setPosition(0, -10, Anchor.CENTER | Anchor.MIDDLE);
        window.clipContent = false;
        window.setName(title);
        this.guiscreenBackground = false;

        //Message contents
        UIPanel panel = new UIPanel(window.getWidth() - 11, window.getHeight() - 43);
        panel.setPosition(0, 10, Anchor.LEFT | Anchor.TOP);
        panel.setBackgroundColor(0x7767AE);

        panel.add(new UILabel(message).setAnchor(Anchor.LEFT | Anchor.TOP));
        window.add(panel);
        window.add(new UIButton("Close") {
            @Override
            public void onClick(MouseEvent.Release event) {
                close();
            }
        }.setSize(5, 5).setAnchor(Anchor.RIGHT | Anchor.BOTTOM));

        new UIMoveHandle(window);

        new UICloseHandle(window) {
            @Override
            public void onClose() {
                close();
            }
        };

        addToScreen(window);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return true;
    }
}
