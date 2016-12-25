/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.component;

import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.interaction.UIButton;

public class UISimpleButton extends UIButton {

    public UISimpleButton(MalisisGui gui) {
        super(gui);
    }

    public UISimpleButton(MalisisGui gui, String text) {
        super(gui, text);
    }

    public UISimpleButton(MalisisGui gui, UIImage image) {
        super(gui, image);
    }

    // Draw nothing to avoid drawing the background images
    @Override
    public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTicks) {
        int w;
        int h;

        if (image != null) {
            w = image.getWidth();
            h = image.getHeight();
        } else {
            w = (int) font.getStringWidth(text, fontOptions);
            h = (int) font.getStringHeight(fontOptions);
        }

        int x = (width - w) / 2;
        int y = (height - h) / 2;

        x += offsetX;
        y += offsetY;

        if (image != null) {
            image.setPosition(x, y);
            image.setZIndex(zIndex);
            image.draw(renderer, mouseX, mouseY, partialTicks);
        } else {
            renderer.drawText(font, text, x, y, zIndex, isHovered() ? hoveredFontOptions : fontOptions);
        }
    }
}