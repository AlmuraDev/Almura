/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.hud.screen.origin.component.panel;

import com.almuradev.almura.shared.client.GuiConfig;
import com.almuradev.almura.shared.client.ui.Fonts;
import com.almuradev.almura.shared.util.MathUtil;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.element.GuiShape;
import net.malisis.core.client.gui.element.SimpleGuiShape;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.renderer.icon.GuiIcon;
import net.malisis.core.util.FontColors;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class UIPropertyBar extends UIComponent {

    private static final int ICON_SIZE = 9;

    private final GuiShape iconShape;
    private int color;
    private float amount = 1.0f;
    private String text = "";
    private FontOptions fontOptions = FontColors.WHITE_FO;
    private GuiTexture spritesheet = GuiConfig.SpriteSheet.VANILLA_ICON;

    @Nullable private GuiIcon backgroundIcon, foregroundIcon;

    public UIPropertyBar(final MalisisGui gui, final int width, final int height) {
        super(gui);
        this.width = width;
        this.height = height;
        this.iconShape = new SimpleGuiShape();
    }

    @Override
    public void drawBackground(final GuiRenderer renderer, final int mouseX, final int mouseY, final float partialTick) {
        int barWidth = this.width;
        int barHeight = this.height;
        int barX = 0;

        if (this.backgroundIcon != null || this.foregroundIcon != null) {
            renderer.bindTexture(this.spritesheet);
        }

        if (this.backgroundIcon != null) {
            this.iconShape.resetState();
            this.iconShape.setSize(ICON_SIZE, ICON_SIZE);
            this.rp.icon.set(this.backgroundIcon);
            renderer.drawShape(this.iconShape, this.rp);
        }

        if (this.foregroundIcon != null) {
            this.iconShape.resetState();
            this.iconShape.setSize(ICON_SIZE, ICON_SIZE);
            this.rp.icon.set(this.foregroundIcon);
            renderer.drawShape(this.iconShape, this.rp);
        }

        if (this.backgroundIcon != null || this.foregroundIcon != null) {
            renderer.next();

            barWidth = this.width - ICON_SIZE - 2;
            barHeight = ICON_SIZE - 2;
            barX = ICON_SIZE + 2;
        }

        final int alpha = MathUtil.squashi(this.getAlpha() - 75, 0, 255);
        renderer.drawRectangle(barX, 1, getZIndex(), barWidth, barHeight, 0, alpha);
        renderer.drawRectangle(barX + 1, 2, getZIndex(), this.getConvertedFill(), barHeight - 2, this.color, this.getAlpha());
    }

    public String getText() {
        return this.text;
    }

    public void setText(final String text) {
        this.text = text;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void drawForeground(final GuiRenderer renderer, final int mouseX, final int mouseY, final float partialTick) {
        if (!this.text.isEmpty()) {
            final int textWidth = (int) Fonts.MINECRAFT.getStringWidth(this.text, this.fontOptions);
            final int textHeight = (int) Fonts.MINECRAFT.getStringHeight(this.fontOptions);

            int x = (((this.backgroundIcon != null || this.foregroundIcon != null) ? ICON_SIZE + 4 + this.width : this.width) - textWidth) / 2;

            int y = (this.height - textHeight) / 2;

            if (x == 0) {
                x = 1;
            }
            if (y == 0) {
                y = 1;
            }
            renderer.drawText(Fonts.MINECRAFT, this.text, x, y, getZIndex(), this.fontOptions);
        }
    }

    public int getColor() {
        return this.color;
    }

    public UIPropertyBar setColor(final int color) {
        this.color = color;
        return this;
    }

    @Nullable
    public GuiIcon getBackgroundIcon() {
        return this.backgroundIcon;
    }

    public UIPropertyBar setBackgroundIcon(final GuiIcon backgroundIcon) {
        this.backgroundIcon = backgroundIcon;
        this.backgroundIcon.setSize(ICON_SIZE, ICON_SIZE);
        return this;
    }

    @Nullable
    public GuiIcon getForegroundIcon() {
        return this.foregroundIcon;
    }

    public UIPropertyBar setForegroundIcon(final GuiIcon foregroundIcon) {
        this.foregroundIcon = foregroundIcon;
        this.foregroundIcon.setSize(ICON_SIZE, ICON_SIZE);
        return this;
    }

    public UIPropertyBar setIcons(final GuiIcon backgroundIcon, final GuiIcon foregroundIcon) {
        this.setBackgroundIcon(backgroundIcon);
        return this.setForegroundIcon(foregroundIcon);
    }

    public float getAmount() {
        return this.amount;
    }

    public UIPropertyBar setAmount(float amount) {
        amount = Math.min(Math.max(amount, 0f), 1f);
        this.amount = amount;
        return this;
    }

    public GuiTexture getSpritesheet() {
        return this.spritesheet;
    }

    public UIPropertyBar setSpritesheet(final GuiTexture spritesheet) {
        this.spritesheet = spritesheet;
        return this;
    }

    @Override
    public int getHeight() {
        return ((this.backgroundIcon != null || this.foregroundIcon != null) ? ICON_SIZE : this.height);
    }

    @Override
    public UIPropertyBar setPosition(final int x, final int y) {
        return (UIPropertyBar) super.setPosition(x, y);
    }

    @Override
    public UIPropertyBar setPosition(final int x, final int y, final int anchor) {
        return (UIPropertyBar) super.setPosition(x, y, anchor);
    }

    private int getConvertedFill() {
        final int shapeWidth = ((this.backgroundIcon == null && this.foregroundIcon == null) ? this.width : this.width - 2 - ICON_SIZE) - 2;
        return (int) ((this.amount) * shapeWidth / 1.0f);
    }

    public UIPropertyBar setFontOptions(final FontOptions fontOptions) {
        this.fontOptions = fontOptions;
        return this;
    }
}
