/**
 * This file is part of AlmuraMod, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almuramod.gui;

import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.container.UIPanel;
import net.malisis.core.client.gui.component.container.UITabGroup;
import net.malisis.core.client.gui.component.container.UIWindow;
import net.malisis.core.client.gui.component.control.UICloseHandle;
import net.malisis.core.client.gui.component.control.UIMoveHandle;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UICheckBox;
import net.malisis.core.client.gui.component.interaction.UITab;
import net.malisis.core.client.gui.event.MouseEvent;

public class TabTester extends MalisisGui {
    public TabTester(String title, String... tabNames) {
        //Title bar
        UIWindow window = new UIWindow(title, 200, 75).setPosition(0, -10, Anchor.CENTER | Anchor.MIDDLE);
        window.clipContent = false;
        window.setName(title);
        this.guiscreenBackground = false;

        UIPanel panel = new UIPanel(window.getWidth() - 11, window.getHeight() - 43);
        panel.setPosition(0, 10, Anchor.LEFT | Anchor.TOP);

        window.add(panel);

        UITabGroup tabGroup = new UITabGroup(UITabGroup.Position.TOP);

        for (String string : tabNames) {
            UITab tab = new UITab(string);
            UIContainer container = (UIContainer) new UIContainer().setSize(window.getWidth(), window.getHeight());
            container.setBackgroundColor(0x255555);
            container.add(new UICheckBox("Checkster").setAnchor(Anchor.LEFT | Anchor.TOP));
            tabGroup.addTab(tab, container);
        }

        panel.add(tabGroup);

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
