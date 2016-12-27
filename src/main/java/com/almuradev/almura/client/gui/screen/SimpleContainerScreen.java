/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.screen;

import com.almuradev.almura.client.gui.util.FontOptionsConstants;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.container.UIBackgroundContainer;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class SimpleContainerScreen extends SimpleScreen {

    private UIVanillaContainer container;
    private Text title = Text.EMPTY;

    public SimpleContainerScreen(@Nullable SimpleScreen parent, Text title) {
        super(parent);
        this.title = title;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void construct() {
        if (title != Text.EMPTY) {
            final UILabel titleLabel = new UILabel(this, TextSerializers.LEGACY_FORMATTING_CODE.serialize(title));
            titleLabel.setFontOptions(FontOptionsConstants.FRO_COLOR_WHITE);
            titleLabel.setPosition(0, 20, Anchor.TOP | Anchor.CENTER);

            this.addToScreen(titleLabel);
        }
        this.container = new UIVanillaContainer(this);
        this.container.setBackgroundAlpha(0);
        this.container.setPosition(0, 36);
        this.container.setSize(this.width, this.height - 102);
        this.container.setClipContent(true);
        this.addToScreen(container);
    }

    /**
     * Gets the primary container for the screen
     * @return The primary container
     */
    public UIBackgroundContainer getContainer() {
        return this.container;
    }

    /**
     * Gets the screen's title
     * @return The title
     */
    public Text getTitle() {
        return this.title;
    }

    /**
     * Sets the screen's title
     * @param title The title
     */
    public void setTitle(Text title) {
        this.title = title;
    }


    private static class UIVanillaContainer extends UIBackgroundContainer {

        private static final int CONTAINER_COLOR = org.spongepowered.api.util.Color.ofRgb(0, 0, 0).getRgb();
        private static final int BORDER_HEIGHT = 4;
        private final MalisisGui gui;

        private UIVanillaContainer(MalisisGui gui) {
            super(gui);
            this.gui = gui;
        }

        @Override
        public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
            super.drawBackground(renderer, mouseX, mouseY, partialTick);

            renderer.enableBlending();
            renderer.disableTextures();
            this.rp.usePerVertexAlpha.set(true);
            this.rp.usePerVertexColor.set(true);

            // Top border
            this.shape.resetState();
            this.shape.setSize(this.gui.width, BORDER_HEIGHT);
            this.shape.setPosition(0, -4);
            this.shape.getVertexes("Top").forEach(row -> row.setColor(CONTAINER_COLOR).setAlpha(255));
            this.shape.getVertexes("Bottom").forEach(row -> row.setColor(0).setAlpha(120));
            renderer.drawShape(shape, rp);

            // Middle
            this.shape.resetState();
            this.shape.setSize(this.gui.width, this.gui.height - 104);
            this.shape.setPosition(0, 0);
            this.shape.getVertexes("Top").forEach(row -> row.setColor(CONTAINER_COLOR).setAlpha(120));
            this.shape.getVertexes("Bottom").forEach(row -> row.setColor(0).setAlpha(120));
            renderer.drawShape(shape, rp);

            // Bottom border
            this.shape.resetState();
            this.shape.setSize(this.gui.width, BORDER_HEIGHT);
            this.shape.setPosition(0, this.gui.height - 104);
            this.shape.getVertexes("Top").forEach(row -> row.setColor(CONTAINER_COLOR).setAlpha(120));
            this.shape.getVertexes("Bottom").forEach(row -> row.setColor(0).setAlpha(255));
            renderer.drawShape(shape, rp);
            renderer.next();

            this.setSize(this.gui.width, this.gui.height - 104);

            renderer.enableTextures();
        }
    }
}
