package com.almuradev.almura.shared.client.ui.component;

import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.decoration.UITooltip;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

public class UISaneTooltip extends UITooltip {

    public UISaneTooltip(MalisisGui gui) {
        super(gui);
    }

    public UISaneTooltip(MalisisGui gui, String text) {
        super(gui, text);
    }

    @SuppressWarnings("deprecation")
    public UISaneTooltip(MalisisGui gui, Text text) {
        this(gui, TextSerializers.LEGACY_FORMATTING_CODE.serialize(text));
    }

    @Override
    public void draw(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {

        final int realX = mouseX + this.getOffsetX();
        final int realY = mouseY + this.getOffsetY();

        // Handle drawing off the right side of the screen
        if (realX + this.getWidth() > getGui().width) {
            mouseX = getGui().width - this.getWidth();
        } else if (realX < 0) { // Handle drawing off the left side of the screen
            mouseX = 0;
        }

        // Handle drawing off the top of the screen
        if (realY + this.getHeight() > getGui().height) {
            mouseY = getGui().width - this.getHeight();
        } else if (realY < 0) { // Handle drawing off the bottom of the screen
            mouseY = 0;
        }

        super.draw(renderer, mouseX, mouseY, partialTick);
    }
}
