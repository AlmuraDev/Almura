/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.component.hud;

import com.almuradev.almura.Constants;
import com.almuradev.almura.client.gui.util.FontOptionsConstants;
import com.almuradev.almura.util.MathUtil;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.element.GuiShape;
import net.malisis.core.client.gui.element.SimpleGuiShape;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.renderer.font.MalisisFont;
import net.malisis.core.renderer.icon.GuiIcon;
import net.minecraft.client.renderer.GlStateManager;
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
    private MalisisFont font = MalisisFont.minecraftFont;
    private FontOptions fontOptions = FontOptionsConstants.FRO_COLOR_WHITE;
    private GuiTexture spritesheet = Constants.Gui.SPRITE_SHEET_VANILLA_ICON;

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

        final int alpha = (int) MathUtil.squash(this.getAlpha() - 75, 0, 255);
        this.shape.getVertexes("TopLeft").get(0).setColor(0).setAlpha(alpha);
        this.shape.getVertexes("TopRight").get(0).setColor(0).setAlpha(alpha);
        this.shape.getVertexes("BottomLeft").get(0).setColor(0).setAlpha(alpha);
        this.shape.getVertexes("BottomRight").get(0).setColor(0).setAlpha(alpha);
        renderer.drawShape(this.shape, this.rp);

        this.shape.resetState();
        this.shape.setSize(this.getConvertedFill(), shapeHeight - 2);
        this.shape.setPosition(shapeX + 1, 2);
        this.shape.getVertexes("TopLeft").get(0).setColor(this.color).setAlpha(this.getAlpha());
        this.shape.getVertexes("TopRight").get(0).setColor(this.color).setAlpha(this.getAlpha());
        this.shape.getVertexes("BottomLeft").get(0).setColor(this.color).setAlpha(this.getAlpha());
        this.shape.getVertexes("BottomRight").get(0).setColor(this.color).setAlpha(this.getAlpha());
        renderer.drawShape(this.shape, this.rp);

        renderer.next();
        renderer.disableBlending();
        renderer.enableTextures();
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
            int textWidth = (int) this.font.getStringWidth(rawText, this.fontOptions);
            int textHeight = (int) this.font.getStringHeight(this.fontOptions);

            int x = (((this.backgroundIcon != null || this.foregroundIcon != null) ? ICON_SIZE + 4 + this.width : this.width) - textWidth) / 2;
            int y = (((this.backgroundIcon != null || this.foregroundIcon != null) ? ICON_SIZE : this.height) / 2) - (textHeight / 2);

            if (x == 0) {
                x = 1;
            }
            if (y == 0) {
                y = 1;
            }
            renderer.drawText(this.font, rawText, x, y, this.zIndex, this.fontOptions);
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
