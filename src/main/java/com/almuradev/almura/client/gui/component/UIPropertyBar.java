/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.component;

import com.almuradev.almura.client.gui.GuiConstants;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.element.GuiShape;
import net.malisis.core.client.gui.element.SimpleGuiShape;
import net.malisis.core.renderer.icon.GuiIcon;

import javax.annotation.Nullable;

public class UIPropertyBar extends UIComponent {
    private static final int ICON_SIZE = 9;

    private final GuiShape iconShape;
    private int color;
    private float amount = 1.0f;
    @Nullable private GuiIcon backgroundIcon, foregroundIcon;

    public UIPropertyBar(MalisisGui gui, int width, int height) {
        super(gui);
        this.width = width;
        this.height = height;
        this.iconShape = new SimpleGuiShape();
    }

    @Override
    public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
        int shapeWidth = 0;
        int shapeX = 0;

        if (this.backgroundIcon != null && this.foregroundIcon != null) {
            renderer.bindTexture(GuiConstants.VANILLA_ICON_SPRITESHEET);

            this.iconShape.resetState();
            this.iconShape.setSize(ICON_SIZE, ICON_SIZE);
            this.rp.icon.set(backgroundIcon);
            renderer.drawShape(this.iconShape, this.rp);

            this.iconShape.resetState();
            this.iconShape.setSize(ICON_SIZE, ICON_SIZE);
            this.rp.icon.set(foregroundIcon);
            renderer.drawShape(this.iconShape, this.rp);

            renderer.next();

            shapeWidth = this.width - ICON_SIZE - 2;
            shapeX = ICON_SIZE + 2;
        }


        renderer.enableBlending();
        renderer.disableTextures();
        this.rp.usePerVertexAlpha.set(true);
        this.rp.usePerVertexColor.set(true);

        this.shape.resetState();
        this.shape.setSize(shapeWidth, ICON_SIZE - 2);
        this.shape.setPosition(shapeX, 1);
        this.shape.getVertexes("TopLeft").get(0).setColor(0).setAlpha(180);
        this.shape.getVertexes("TopRight").get(0).setColor(0).setAlpha(180);
        this.shape.getVertexes("BottomLeft").get(0).setColor(0).setAlpha(180);
        this.shape.getVertexes("BottomRight").get(0).setColor(0).setAlpha(180);
        renderer.drawShape(this.shape, this.rp);

        this.shape.resetState();
        this.shape.setSize(this.getConvertedFill(), ICON_SIZE - 4);
        this.shape.setPosition(shapeX + 1, 2);
        this.shape.getVertexes("TopLeft").get(0).setColor(color).setAlpha(alpha);
        this.shape.getVertexes("TopRight").get(0).setColor(color).setAlpha(alpha);
        this.shape.getVertexes("BottomLeft").get(0).setColor(color).setAlpha(alpha);
        this.shape.getVertexes("BottomRight").get(0).setColor(color).setAlpha(alpha);
        renderer.drawShape(this.shape, this.rp);

        renderer.next();
        renderer.enableTextures();
    }

    @Override
    public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
    }

    public int getColor() {
        return this.color;
    }

    public UIPropertyBar setColor(int color) {
        this.color = color;
        return this;
    }

    @Nullable
    public GuiIcon getBackgroundIcon() {
        return this.backgroundIcon;
    }

    public UIPropertyBar setBackgroundIcon(GuiIcon backgroundIcon) {
        this.backgroundIcon = backgroundIcon;
        this.backgroundIcon.setSize(ICON_SIZE, ICON_SIZE);
        return this;
    }

    @Nullable
    public GuiIcon getForegroundIcon() {
        return this.foregroundIcon;
    }

    public UIPropertyBar setForegroundIcon(GuiIcon foregroundIcon) {
        this.foregroundIcon = foregroundIcon;
        this.foregroundIcon.setSize(ICON_SIZE, ICON_SIZE);
        return this;
    }

    public float getAmount() {
        return this.amount;
    }

    public UIPropertyBar setAmount(float amount) {
        amount = Math.min(Math.max(amount, 0f), 1f);
        this.amount = amount;
        return this;
    }

    private int getConvertedFill() {
        final int shapeWidth = ((this.backgroundIcon == null && this.foregroundIcon == null) ? this.width : this.width - 2 - ICON_SIZE) - 2;
        return (int) ((this.amount) * shapeWidth / 1.0f);
    }
}
