/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.components;

import com.almuradev.almura.client.gui.GuiConstants;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.element.GuiShape;
import net.malisis.core.client.gui.element.SimpleGuiShape;
import net.malisis.core.renderer.icon.GuiIcon;

public class UIPropertyBar extends UIComponent<UIPropertyBar> {

    public static final org.spongepowered.api.util.Color LIGHT_GREEN = org.spongepowered.api.util.Color.ofRgb(0, 255, 0);
    public static final org.spongepowered.api.util.Color LIGHT_ORANGE = org.spongepowered.api.util.Color.ofRgb(204, 172, 0);
    public static final org.spongepowered.api.util.Color DARK_RED = org.spongepowered.api.util.Color.ofRgb(175, 22, 30);
    private final GuiShape iconShape;
    private final GuiShape barShape;
    private final GuiIcon barIcon;
    private final GuiIcon symbolIcon;
    private final int iconSize = 7;
    private final int iconGap = 3;
    private GuiTexture texture = GuiConstants.TEXTURE_SPRITESHEET;
    private boolean relativeColor = true;
    private int color = 0;
    private float amount;

    public UIPropertyBar(MalisisGui gui, GuiIcon symbolIcon) {
        super(gui);

        setSize(105, 7);

        this.symbolIcon = symbolIcon;
        this.barIcon = GuiConstants.LEGACY_ICON_BAR;

        iconShape = new SimpleGuiShape();
        iconShape.setSize(iconSize, iconSize);
        iconShape.storeState();

        barShape = new SimpleGuiShape();
        barShape.setSize(width - iconSize - iconGap, 7);
        barShape.translate(iconSize + iconGap, 0, 1);
        barShape.storeState();
    }

    public UIPropertyBar setRelativeColor(boolean relativeColor) {
        this.relativeColor = relativeColor;
        return this;
    }

    public int getColor() {

        if (!relativeColor) {
            return color;
        }

        if (amount >= 0.6F) {
            return LIGHT_GREEN.getRgb();
        }
        if (amount >= 0.3F) {
            return LIGHT_ORANGE.getRgb();
        }
        return DARK_RED.getRgb();
    }

    public UIPropertyBar setColor(int color) {
        this.color = color;
        return this;
    }

    public float getAmount() {
        return amount;
    }

    public UIPropertyBar setAmount(float amount) {
        this.amount = amount;
        return this;
    }

    private int getBarWidth() {
        // Amount can be greater than one due to custom armor.
        if (amount > 1) {
            amount = 1;
        }
        return (int) ((width - iconSize - iconGap - 2) * amount);
    }

    @Override
    public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
        renderer.bindTexture(texture);

        // draw the icon
        iconShape.resetState();
        rp.icon.set(symbolIcon);
        renderer.drawShape(iconShape, rp);

        // draw the background
        barShape.resetState();
        rp.icon.set(barIcon);
        renderer.drawShape(barShape, rp);
    }

    @Override
    public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
        // draw the colored rectangle
        if (amount > 0) {
            renderer.drawRectangle(iconSize + iconGap + 1, 1, 0, getBarWidth(), 5, getColor(), 255);
        }
    }

}
