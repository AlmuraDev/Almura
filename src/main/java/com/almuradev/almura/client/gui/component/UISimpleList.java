/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.component;

import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.container.UIListContainer;
import net.malisis.core.client.gui.component.control.UIScrollBar;
import net.malisis.core.client.gui.component.control.UISlimScrollbar;
import net.minecraft.client.gui.GuiScreen;

public class UISimpleList<S extends UIContainer<S>> extends UIListContainer<UISimpleList<S>, S> {

    public UISimpleList(MalisisGui gui, int width, int height) {
        super(gui, width, height);

        unregister(scrollbar);
        this.removeAllControlComponents();

        this.scrollbar = new UISlimScrollbar(gui, self(), UIScrollBar.Type.VERTICAL);
        this.scrollbar.setAutoHide(true);
    }

    public UIScrollBar getScrollBar() {
        return this.scrollbar;
    }

    @Override
    public int getContentWidth() {
        return super.getWidth() - (this.scrollbar.isVisible() ? this.scrollbar.getWidth() + 2 : 0);
    }

    @Override
    public int getElementHeight(UIContainer element) {
        return element.getHeight();
    }

    @Override
    public void drawElementBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick, UIContainer element, boolean isHovered) {
    }

    @Override
    public void drawElementForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick, UIContainer element, boolean isHovered) {
        element.draw(renderer, mouseX, mouseY, partialTick);
    }

    @Override
    public float getScrollStep() {
        return (GuiScreen.isCtrlKeyDown() ? 0.125F : 0.075F);
    }
}
