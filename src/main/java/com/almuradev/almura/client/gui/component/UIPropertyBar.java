/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.component;

import com.almuradev.almura.client.gui.GuiConstants;
import com.almuradev.almura.client.gui.util.FontOptionsConstants;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.element.GuiShape;
import net.malisis.core.client.gui.element.SimpleGuiShape;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.renderer.font.MalisisFont;
import net.malisis.core.renderer.icon.GuiIcon;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import javax.annotation.Nullable;

public class UIPropertyBar extends UIComponent {
    private static final int ICON_SIZE = 9;

    private final GuiShape iconShape;
    private int color;
    private float amount = 1.0f;
    private Text text = Text.EMPTY;
    private MalisisFont font = MalisisFont.minecraftFont;
    private FontOptions fontOptions = FontOptions.builder().from(FontOptionsConstants.FRO_COLOR_WHITE).scale(0.65f).build();
    private GuiTexture spritesheet = GuiConstants.VANILLA_ICON_SPRITESHEET;

    @Nullable private GuiIcon backgroundIcon, foregroundIcon;

    public UIPropertyBar(MalisisGui gui, int width, int height) {
        super(gui);
        this.width = width;
        this.height = height;
        this.iconShape = new SimpleGuiShape();
    }

    @Override
    public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
        int shapeWidth = this.width;
        int shapeHeight = this.height;
        int shapeX = 0;

        if (this.backgroundIcon != null || this.foregroundIcon != null) {
            renderer.bindTexture(this.spritesheet);
        }

        if (this.backgroundIcon != null) {
            this.iconShape.resetState();
            this.iconShape.setSize(ICON_SIZE, ICON_SIZE);
            this.rp.icon.set(backgroundIcon);
            renderer.drawShape(this.iconShape, this.rp);
        }

        if (foregroundIcon != null) {
            this.iconShape.resetState();
            this.iconShape.setSize(ICON_SIZE, ICON_SIZE);
            this.rp.icon.set(foregroundIcon);
            renderer.drawShape(this.iconShape, this.rp);
        }

        if (backgroundIcon != null || foregroundIcon != null) {
            renderer.next();

            shapeWidth = this.width - ICON_SIZE - 2;
            shapeHeight = ICON_SIZE - 2;
            shapeX = ICON_SIZE + 2;
        }


        renderer.enableBlending();
        renderer.disableTextures();
        this.rp.usePerVertexAlpha.set(true);
        this.rp.usePerVertexColor.set(true);

        this.shape.resetState();
        this.shape.setSize(shapeWidth, shapeHeight);
        this.shape.setPosition(shapeX, 1);
        this.shape.getVertexes("TopLeft").get(0).setColor(0).setAlpha(180);
        this.shape.getVertexes("TopRight").get(0).setColor(0).setAlpha(180);
        this.shape.getVertexes("BottomLeft").get(0).setColor(0).setAlpha(180);
        this.shape.getVertexes("BottomRight").get(0).setColor(0).setAlpha(180);
        renderer.drawShape(this.shape, this.rp);

        this.shape.resetState();
        this.shape.setSize(this.getConvertedFill(), shapeHeight - 2);
        this.shape.setPosition(shapeX + 1, 2);
        this.shape.getVertexes("TopLeft").get(0).setColor(color).setAlpha(alpha);
        this.shape.getVertexes("TopRight").get(0).setColor(color).setAlpha(alpha);
        this.shape.getVertexes("BottomLeft").get(0).setColor(color).setAlpha(alpha);
        this.shape.getVertexes("BottomRight").get(0).setColor(color).setAlpha(alpha);
        renderer.drawShape(this.shape, this.rp);

        renderer.next();
        renderer.enableTextures();
    }

    public void setText(Text text) {
        this.text = text;
    }

    public Text getText() {
        return this.text;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
        if (text != Text.EMPTY) {
            final String rawText = TextSerializers.LEGACY_FORMATTING_CODE.serialize(text);
            int textWidth = (int) font.getStringWidth(rawText, fontOptions);
            int textHeight = (int) font.getStringHeight(fontOptions);

            int x = (((backgroundIcon != null || foregroundIcon != null) ? ICON_SIZE + 4 + width : width) - textWidth) / 2;
            int y = (((backgroundIcon != null || foregroundIcon != null) ? ICON_SIZE : height) / 2) - (textHeight / 2);

            if (x == 0) {
                x = 1;
            }
            if (y == 0) {
                y = 1;
            }
            renderer.drawText(font, rawText, x, y, zIndex, fontOptions);
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
        return ((this.backgroundIcon != null || this.foregroundIcon != null) ? ICON_SIZE : height);
    }

    private int getConvertedFill() {
        final int shapeWidth = ((this.backgroundIcon == null && this.foregroundIcon == null) ? this.width : this.width - 2 - ICON_SIZE) - 2;
        return (int) ((this.amount) * shapeWidth / 1.0f);
    }
}
