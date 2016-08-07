/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.components;

import com.almuradev.almura.client.gui.util.FontRenderOptionsConstants;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.UIBackgroundContainer;
import net.malisis.core.client.gui.component.decoration.UILabel;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

public class UIVanillaContainer extends UIBackgroundContainer {

    private final MalisisGui gui;
    private final Text title;
    private UIBackgroundContainer bottomBorderContainer, middleContentContainer, topBorderContainer, wrappingContainer;

    public UIVanillaContainer(MalisisGui gui) {
        this(gui, Text.EMPTY);
    }

    public UIVanillaContainer(MalisisGui gui, Text title) {
        super(gui);
        this.gui = gui;
        this.title = title;
        this.construct();
    }

    @SuppressWarnings("deprecation")
    private void construct() {
        this.setBackgroundAlpha(0);
        this.setClipContent(false);

        this.topBorderContainer = new UIBackgroundContainer(this.gui, UIComponent.INHERITED, 4);
        this.topBorderContainer.setColor(org.spongepowered.api.util.Color.ofRgb(0, 0, 0).getRgb());
        this.topBorderContainer.setTopAlpha(255);
        this.topBorderContainer.setBottomAlpha(120);
        //
        //        this.middleContentContainer = new UIBackgroundContainer(this.gui, UIComponent.INHERITED, this.gui.height - 104);
        //        this.middleContentContainer.setPosition(0, SimpleGui.getPaddedY(topBorderContainer, 0));
        //        this.middleContentContainer.setColor(org.spongepowered.api.util.Color.ofRgb(0, 0, 0).getRgb());
        //        this.middleContentContainer.setBackgroundAlpha(120);
        //        this.middleContentContainer.setPadding(4, 10);
        //
        //        this.bottomBorderContainer = new UIBackgroundContainer(this.gui, UIComponent.INHERITED, 4);
        //        this.bottomBorderContainer.setPosition(0, this.gui.height - 86);
        //        this.bottomBorderContainer.setColor(org.spongepowered.api.util.Color.ofRgb(0, 0, 0).getRgb());
        //        this.bottomBorderContainer.setTopAlpha(120);
        //        this.bottomBorderContainer.setBottomAlpha(255);
        //
        //        this.wrappingContainer = new UIBackgroundContainer(this.gui, UIComponent.INHERITED, topBorderContainer.getHeight() +
        // middleContentContainer
        //                .getHeight() + bottomBorderContainer.getHeight());
        //        this.wrappingContainer.setBackgroundAlpha(152);
        //        this.wrappingContainer.setClipContent(true);

        //        this.wrappingContainer.add(topBorderContainer, middleContentContainer, bottomBorderContainer);

        if (title != Text.EMPTY) {
            final UILabel titleLabel = new UILabel(this.gui, TextSerializers.LEGACY_FORMATTING_CODE.serialize(title));
            titleLabel.setFontRenderOptions(FontRenderOptionsConstants.FRO_COLOR_WHITE);
            titleLabel.setPosition(0, 20, Anchor.TOP | Anchor.CENTER);

            //            this.wrappingContainer.setPosition(0, SimpleGui.getPaddedY(titleLabel, 3));
            this.add(titleLabel);
        }

        //        this.add(wrappingContainer);
    }

    public UIBackgroundContainer getContentContainer() {
        return this.wrappingContainer;
    }

    @Override
    public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
        super.drawBackground(renderer, mouseX, mouseY, partialTick);

        shape.getVertexes("TopLeft").get(0).setColor(0).setAlpha(255);
        shape.getVertexes("TopRight").get(0).setColor(0).setAlpha(255);
        shape.getVertexes("BottomLeft").get(0).setColor(0).setAlpha(120);
        shape.getVertexes("BottomRight").get(0).setColor(0).setAlpha(120);
        //        renderer.drawRectangle(0, SimpleGui.getPaddedY(this.topBorderContainer, 0), 0, this.gui.height, this.gui.height - 104,
        //                org.spongepowered.api.util.Color.ofRgb(0, 0, 0).getRgb(), 120);
    }

    @Override
    public void draw(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
        //bottomBorderContainer.setPosition(0, SimpleGui.getPaddedY(this.middleContentContainer, 0));
        //this.wrappingContainer.setSize(UIComponent.INHERITED, topBorderContainer.getHeight() + middleContentContainer.getHeight() +
        //        bottomBorderContainer.getHeight());
        super.draw(renderer, mouseX, mouseY, partialTick);
    }
}
