/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.client.ui.component;

import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.container.UIListContainer;
import net.malisis.core.client.gui.component.control.UIScrollBar;
import net.malisis.core.client.gui.component.control.UISlimScrollbar;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class UISimpleList<S> extends UIListContainer<S> {

    public UISimpleList(MalisisGui gui, int width, int height) {
        super(gui, width, height);

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
    public float getScrollStep() {
        return (GuiScreen.isCtrlKeyDown() ? 0.125F : 0.075F);
    }

    @Override
    public boolean onScrollWheel(int x, int y, int delta) {
        return this.scrollbar.onScrollWheel(x, y, delta);
    }
}
