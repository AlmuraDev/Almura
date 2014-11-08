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
import net.malisis.core.renderer.animation.transformation.ITransformable;

public class UIPropertyBar extends UIContainer implements ITransformable.Color {

    private final UIBackgroundContainer background;
    private final int gapBetweenSymbolAndBar = 10;
    private final int symbolWidth = 7;
    private final int symbolHeight = 7;

    public UIPropertyBar(MalisisGui gui, GuiTexture symbolTexture, GuiTexture barTexture) {
        this(gui, symbolTexture, barTexture, UIComponent.INHERITED, UIComponent.INHERITED);
    }

    public UIPropertyBar(MalisisGui gui, GuiTexture symbolTexture, GuiTexture barTexture, int width, int height) {
        super(gui, width, height);

        final UIImage symbolImage = new UIImage(gui, symbolTexture, null);
        symbolImage.setSize(symbolWidth, symbolHeight);
        symbolImage.setPosition(UIComponent.INHERITED, UIComponent.INHERITED);

        final UIImage barImage = new UIImage(gui, barTexture, null);
        barImage.setSize(UIComponent.INHERITED - gapBetweenSymbolAndBar, UIComponent.INHERITED);
        barImage.setPosition(gapBetweenSymbolAndBar, UIComponent.INHERITED);

        background = new UIBackgroundContainer(gui, UIComponent.INHERITED - gapBetweenSymbolAndBar, UIComponent.INHERITED - 3);
        background.setPosition(gapBetweenSymbolAndBar, UIComponent.INHERITED + 1);

        add(symbolImage, background, barImage);
    }

    @Override
    public void setColor(int color) {
        background.setColor(color);
    }

    public UIPropertyBar setAmount(int amount) {
        background.setSize(amount > getWidth() ? getWidth() : amount, background.getHeight());
        return this;
    }

    public int getAmount() {
        return background.getWidth();
    }
}
