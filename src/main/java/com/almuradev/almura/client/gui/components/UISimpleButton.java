package com.almuradev.almura.client.gui.components;

import com.almuradev.almura.client.gui.SimpleGui;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.renderer.icon.provider.GuiIconProvider;

public class UISimpleButton extends UIButton {

    public UISimpleButton(MalisisGui gui) {
        super(gui);
        this.construct();
    }

    public UISimpleButton(MalisisGui gui, String text) {
        super(gui, text);
        this.construct();
    }

    public UISimpleButton(MalisisGui gui, UIImage image) {
        super(gui, image);
        this.construct();
    }

    private void construct() {
        this.iconProvider = new GuiIconProvider(SimpleGui.ICON_EMPTY);
        this.iconPressedProvider = new GuiIconProvider(SimpleGui.ICON_EMPTY);
    }

    @Override
    public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
        super.drawBackground(renderer, mouseX, mouseY, partialTick);
    }

    @Override
    public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTicks) {
        super.drawForeground(renderer, mouseX, mouseY, partialTicks);
    }
}