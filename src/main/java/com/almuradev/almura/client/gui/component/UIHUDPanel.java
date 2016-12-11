package com.almuradev.almura.client.gui.component;

import com.almuradev.almura.client.gui.GuiConstants;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.container.UIPanel;
import net.malisis.core.renderer.icon.provider.GuiIconProvider;

public class UIHUDPanel extends UIPanel {

    public UIHUDPanel(MalisisGui gui) {
        super(gui);
        iconProvider = new GuiIconProvider(GuiConstants.VANILLA_ACHIEVEMENT_BACKGROUND_SPRITESHEET.getXYResizableIcon(96, 202, 160,
                32, 5));
    }

    public UIHUDPanel(MalisisGui gui, int width, int height) {
        super(gui, width, height);
        iconProvider = new GuiIconProvider(GuiConstants.VANILLA_ACHIEVEMENT_BACKGROUND_SPRITESHEET.getXYResizableIcon(96, 202, 160,
                32, 5));
    }
}
