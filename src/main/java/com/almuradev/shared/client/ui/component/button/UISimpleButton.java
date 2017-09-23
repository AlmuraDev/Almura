/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.shared.client.ui.component.button;

import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
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

        if (this.image != null) {
            w = this.image.getWidth();
            h = this.image.getHeight();
        } else {
            w = (int) this.font.getStringWidth(this.text, this.fontOptions);
            h = (int) this.font.getStringHeight(this.fontOptions);
        }

        int x = (this.width - w) / 2;
        int y = (this.height - h) / 2;

        x += this.offsetX;
        y += this.offsetY;

        if (this.image != null) {
            this.image.setPosition(x, y);
            this.image.setZIndex(this.zIndex);
            this.image.draw(renderer, mouseX, mouseY, partialTicks);
        } else {
            renderer.drawText(this.font, this.text, x, y, this.zIndex, isHovered() ? this.hoveredFontOptions : this.fontOptions);
        }
    }
}
