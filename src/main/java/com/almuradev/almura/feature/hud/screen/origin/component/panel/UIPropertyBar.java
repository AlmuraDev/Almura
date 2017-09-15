/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.feature.hud.screen.origin.component.panel;

import com.almuradev.shared.client.GuiConfig;
import com.almuradev.shared.client.ui.FontColors;
import com.almuradev.shared.client.ui.Fonts;
import com.almuradev.shared.util.MathUtil;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.element.GuiShape;
import net.malisis.core.client.gui.element.SimpleGuiShape;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.renderer.icon.GuiIcon;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class UIPropertyBar extends UIComponent {

    private static final int ICON_SIZE = 9;

    private final GuiShape iconShape;
    private int color;
    private float amount = 1.0f;
    private Text text = Text.EMPTY;
    private FontOptions fontOptions = FontColors.FRO_WHITE;
    private GuiTexture spritesheet = GuiConfig.SpriteSheet.VANILLA_ICON;

    @Nullable private GuiIcon backgroundIcon, foregroundIcon;

    public UIPropertyBar(MalisisGui gui, int width, int height) {
        super(gui);
        this.width = width;
        this.height = height;
        this.iconShape = new SimpleGuiShape();
    }

    @Override
    public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
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

        final int alpha = (int) MathUtil.squash(this.getAlpha() - 75, 0, 255);
        renderer.drawRectangle(barX, 1, this.zIndex, barWidth, barHeight, 0, alpha);
        renderer.drawRectangle(barX + 1, 2, this.zIndex, this.getConvertedFill(), barHeight - 2, this.color, this.getAlpha());
    }

    public Text getText() {
        return this.text;
    }

    public void setText(Text text) {
        this.text = text;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
        if (this.text != Text.EMPTY) {
            final String rawText = TextSerializers.LEGACY_FORMATTING_CODE.serialize(this.text);
            int textWidth = (int) Fonts.MINECRAFT.getStringWidth(rawText, this.fontOptions);
            int textHeight = (int) Fonts.MINECRAFT.getStringHeight(this.fontOptions);

            int x = (((this.backgroundIcon != null || this.foregroundIcon != null) ? ICON_SIZE + 4 + this.width : this.width) - textWidth) / 2;

            int y = (ICON_SIZE - textHeight) / 2;

            if (x == 0) {
                x = 1;
            }
            if (y == 0) {
                y = 1;
            }
            renderer.drawText(Fonts.MINECRAFT, rawText, x, y, this.zIndex, this.fontOptions);
        }
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

    public UIPropertyBar setIcons(GuiIcon backgroundIcon, GuiIcon foregroundIcon) {
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

    public UIPropertyBar setSpritesheet(GuiTexture spritesheet) {
        this.spritesheet = spritesheet;
        return this;
    }

    @Override
    public int getHeight() {
        return ((this.backgroundIcon != null || this.foregroundIcon != null) ? ICON_SIZE : this.height);
    }

    @Override
    public UIPropertyBar setPosition(int x, int y) {
        return (UIPropertyBar) super.setPosition(x, y);
    }

    @Override
    public UIPropertyBar setPosition(int x, int y, int anchor) {
        return (UIPropertyBar) super.setPosition(x, y, anchor);
    }

    private int getConvertedFill() {
        final int shapeWidth = ((this.backgroundIcon == null && this.foregroundIcon == null) ? this.width : this.width - 2 - ICON_SIZE) - 2;
        return (int) ((this.amount) * shapeWidth / 1.0f);
    }
}
