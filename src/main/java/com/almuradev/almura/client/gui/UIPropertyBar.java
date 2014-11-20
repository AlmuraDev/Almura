/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui;

import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.UIBackgroundContainer;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.icon.GuiIcon;
import net.malisis.core.renderer.animation.transformation.ITransformable;

public class UIPropertyBar extends UIContainer<UIPropertyBar> implements ITransformable.Color {

    private final UIImage barImage;
    private final UIBackgroundContainer background;
    private final int gapBetweenSymbolAndBar = 10;
    private final int symbolWidth = 7;
    private final int symbolHeight = 7;

    public UIPropertyBar(MalisisGui gui, GuiTexture spriteSheet, GuiIcon symbolIcon, GuiIcon barIcon) {
        this(gui, spriteSheet, symbolIcon, barIcon, UIComponent.INHERITED, UIComponent.INHERITED);
    }

    public UIPropertyBar(MalisisGui gui, GuiTexture spriteSheet, GuiIcon symbolIcon, GuiIcon barIcon, int width, int height) {
        super(gui, width, height);

        final UIImage symbolImage = new UIImage(gui, spriteSheet, symbolIcon);
        symbolImage.setSize(symbolWidth, symbolHeight);
        symbolImage.setPosition(UIComponent.INHERITED, UIComponent.INHERITED);

        barImage = new UIImage(gui, spriteSheet, barIcon);
        barImage.setSize(UIComponent.INHERITED - gapBetweenSymbolAndBar, UIComponent.INHERITED);
        barImage.setPosition(gapBetweenSymbolAndBar, UIComponent.INHERITED);

        background = new UIBackgroundContainer(gui, UIComponent.INHERITED - gapBetweenSymbolAndBar, UIComponent.INHERITED - 3);
        background.setPosition(gapBetweenSymbolAndBar + 1, UIComponent.INHERITED + 1);
        background.setClipContent(false);

        add(symbolImage, background, barImage);
    }

    @Override
    public void setColor(int color) {
        background.setColor(color);
    }

    public int getAmount() {
        return background.getWidth();
    }

    public UIPropertyBar setAmount(float percentage) {
        background.setSize((int) (percentage * (getWidth() - gapBetweenSymbolAndBar)), background.getHeight());
        return this;
    }

    @Override
    public UIPropertyBar setVisible(boolean visible) {
        background.setVisible(visible);
        return this;
    }
}
