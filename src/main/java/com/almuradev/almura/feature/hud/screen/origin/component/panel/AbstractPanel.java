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
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.element.Padding;
import net.malisis.core.client.gui.element.size.Size;
import net.malisis.core.client.gui.render.GuiRenderer;
import net.malisis.core.client.gui.render.shape.GuiShape;
import net.malisis.core.util.FontColors;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

/**
 * A HUD panel: the inventory panel background, a padding of 3, and text drawn directly in {@link #drawForeground}.
 */
@SideOnly(Side.CLIENT)
@SuppressWarnings("deprecation")
public abstract class AbstractPanel extends UIContainer {

    protected final Minecraft client = Minecraft.getMinecraft();
    /** The renderer, while {@link #drawBackground} and {@link #drawForeground} run. */
    protected GuiRenderer renderer;
    /** The size of the panel: set directly or with {@link #setSize(int, int)}. */
    protected int width, height;
    private final GuiShape panelShape;

    protected AbstractPanel(final MalisisGui gui, final int width, final int height) {
        this.width = width;
        this.height = height;
        this.setSize(Size.of(() -> this.width, () -> this.height));
        this.setPadding(Padding.of(3));
        this.panelShape = GuiShape.builder(this).icon(GuiConfig.Icon.VANILLA_CONTAINER_INVENTORY_ADVANCEMENT).border(5).build();

        this.setBackground(renderer -> {
            this.renderer = renderer;
            this.drawBackground(renderer, MalisisGui.MOUSE_POSITION.x(), MalisisGui.MOUSE_POSITION.y(), renderer.getPartialTick());
        });
        this.setForeground(renderer -> {
            this.renderer = renderer;
            this.drawForeground(renderer, MalisisGui.MOUSE_POSITION.x(), MalisisGui.MOUSE_POSITION.y(), renderer.getPartialTick());
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends UIComponent> T setSize(final int width, final int height) {
        this.width = width;
        this.height = height;
        return (T) this;
    }

    /**
     * Draws the panel background.
     */
    public void drawBackground(final GuiRenderer renderer, final int mouseX, final int mouseY, final float partialTick) {
        this.panelShape.render(renderer);
    }

    /**
     * Draws the components in the panel; overrides call this first, then draw their text.
     */
    public void drawForeground(final GuiRenderer renderer, final int mouseX, final int mouseY, final float partialTick) {
        this.content.render(renderer);
    }

    protected void drawProperty(final String key, final String value, final int x, final int y) {
        final Text text = Text.of(TextColors.WHITE, key, ": ", TextColors.GRAY, value);
        this.drawText(text, x, y);
    }

    protected void drawText(final Text text, final int x, final int y) {
        this.renderer.drawText(Fonts.MINECRAFT, TextSerializers.LEGACY_FORMATTING_CODE.serialize(text), x, y, this.zIndex, FontColors.WHITE_FO);
    }
}
