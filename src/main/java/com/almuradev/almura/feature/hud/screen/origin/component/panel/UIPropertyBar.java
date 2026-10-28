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
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.element.size.Size;
import net.malisis.core.client.gui.render.GuiIcon;
import net.malisis.core.client.gui.render.GuiRenderer;
import net.malisis.core.client.gui.render.GuiTexture;
import net.malisis.core.client.gui.render.shape.GuiShape;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.util.FontColors;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

/**
 * A bar filled to an amount, with optional icons (background and foreground, drawn over each other) on its left and a centered text.
 */
@SideOnly(Side.CLIENT)
public class UIPropertyBar extends UIComponent {

    private static final int ICON_SIZE = 9;

    private final int barWidth, barHeight;
    private final GuiShape backgroundIconShape, foregroundIconShape;
    private int barColor;
    private float amount = 1.0f;
    private String text = "";
    private FontOptions fontOptions = FontColors.WHITE_FO;
    private GuiTexture spritesheet = GuiConfig.SpriteSheet.VANILLA_ICON;

    @Nullable private GuiIcon backgroundIcon, foregroundIcon;

    public UIPropertyBar(final MalisisGui gui, final int width, final int height) {
        this.barWidth = width;
        this.barHeight = height;
        // Icons are a square as high as the bar
        this.setSize(Size.of(width, () -> this.hasIcons() ? ICON_SIZE : this.barHeight));

        this.backgroundIconShape = GuiShape.builder(this).size(s -> Size.of(ICON_SIZE, ICON_SIZE)).icon(() -> this.backgroundIcon).build();
        this.foregroundIconShape = GuiShape.builder(this).size(s -> Size.of(ICON_SIZE, ICON_SIZE)).icon(() -> this.foregroundIcon).build();

        this.setBackground(this::drawBar);
        this.setForeground(this::drawText);
    }

    private boolean hasIcons() {
        return this.backgroundIcon != null || this.foregroundIcon != null;
    }

    private void drawBar(final GuiRenderer renderer) {
        int barWidth = this.barWidth;
        int barHeight = this.barHeight;
        int barX = 0;

        // The icons carry their own texture
        if (this.backgroundIcon != null) {
            this.backgroundIconShape.render(renderer);
        }

        if (this.foregroundIcon != null) {
            this.foregroundIconShape.render(renderer);
        }

        if (this.hasIcons()) {
            renderer.next();

            barWidth = this.barWidth - ICON_SIZE - 2;
            barHeight = ICON_SIZE - 2;
            barX = ICON_SIZE + 2;
        }

        final int alpha = MathUtil.squashi(this.getAlpha() - 75, 0, 255);
        renderer.drawRectangle(barX, 1, 0, barWidth, barHeight, 0, alpha);
        renderer.drawRectangle(barX + 1, 2, 0, this.getConvertedFill(), barHeight - 2, this.barColor, this.getAlpha());
    }

    public String getText() {
        return this.text;
    }

    public void setText(final String text) {
        this.text = text;
    }

    @SuppressWarnings("deprecation")
    private void drawText(final GuiRenderer renderer) {
        if (!this.text.isEmpty()) {
            final int textWidth = (int) Fonts.MINECRAFT.getStringWidth(this.text, this.fontOptions);
            final int textHeight = (int) Fonts.MINECRAFT.getStringHeight(this.fontOptions);

            int x = ((this.hasIcons() ? ICON_SIZE + 4 + this.barWidth : this.barWidth) - textWidth) / 2;

            int y = (this.barHeight - textHeight) / 2;

            if (x == 0) {
                x = 1;
            }
            if (y == 0) {
                y = 1;
            }
            renderer.drawText(Fonts.MINECRAFT, this.text, x, y, 0, this.fontOptions);
        }
    }

    public int getBarColor() {
        return this.barColor;
    }

    /**
     * Sets the color of the filled part of the bar.
     *
     * @param color the color
     * @return this bar
     */
    public UIPropertyBar setBarColor(final int color) {
        this.barColor = color;
        return this;
    }

    @Nullable
    public GuiIcon getBackgroundIcon() {
        return this.backgroundIcon;
    }

    public UIPropertyBar setBackgroundIcon(final GuiIcon backgroundIcon) {
        this.backgroundIcon = backgroundIcon;
        return this;
    }

    @Nullable
    public GuiIcon getForegroundIcon() {
        return this.foregroundIcon;
    }

    public UIPropertyBar setForegroundIcon(final GuiIcon foregroundIcon) {
        this.foregroundIcon = foregroundIcon;
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

    /**
     * Kept for callers: icons carry their own texture now.
     */
    public UIPropertyBar setSpritesheet(final GuiTexture spritesheet) {
        this.spritesheet = spritesheet;
        return this;
    }

    @Override
    public UIPropertyBar setPosition(final int x, final int y) {
        return super.setPosition(x, y);
    }

    @Override
    public UIPropertyBar setPosition(final int x, final int y, final int anchor) {
        return super.setPosition(x, y, anchor);
    }

    private int getConvertedFill() {
        final int shapeWidth = (this.hasIcons() ? this.barWidth - 2 - ICON_SIZE : this.barWidth) - 2;
        return (int) ((this.amount) * shapeWidth / 1.0f);
    }

    public UIPropertyBar setFontOptions(final FontOptions fontOptions) {
        this.fontOptions = fontOptions;
        return this;
    }
}
