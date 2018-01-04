/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.hud.screen.origin.component.panel;

import com.almuradev.almura.feature.hud.HeadUpDisplay;
import com.almuradev.almura.shared.client.ui.FontColors;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.text.serializer.TextSerializers;

import javax.inject.Inject;

@SideOnly(Side.CLIENT)
public class UIWorldPanel extends AbstractPanel {

    @Inject private static HeadUpDisplay hudData;

    private final UILabel compassLabel, worldLabel;

    public UIWorldPanel(MalisisGui gui, int width, int height) {
        super(gui, width, height);

        this.compassLabel = new UILabel(gui, "");
        this.compassLabel.setPosition(0, 1, Anchor.BOTTOM | Anchor.CENTER);

        this.worldLabel = new UILabel(gui, "");
        this.worldLabel.setPosition(0, 0, Anchor.TOP | Anchor.CENTER);
        this.worldLabel.setFontOptions(FontColors.WHITE_FO);

        this.add(this.compassLabel, this.worldLabel);
    }

    @Override
    public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
        if (this.client.player == null || this.client.player.world == null) {
            return;
        }
        super.drawForeground(renderer, mouseX, mouseY, partialTick);
        this.updateCompass();
        this.updateWorld();
    }

    @SuppressWarnings("deprecation")
    private void updateCompass() {
        this.compassLabel.setText(TextSerializers.LEGACY_FORMATTING_CODE.serialize(hudData.getCompass()));
        this.compassLabel.setPosition(0, 1, Anchor.BOTTOM | Anchor.CENTER);
    }

    private void updateWorld() {
        this.worldLabel.setText(hudData.worldName);
        this.worldLabel.setPosition(0, 0, Anchor.TOP | Anchor.CENTER);
        if (this.worldLabel.getWidth() + 15 <= this.compassLabel.getWidth() + 10) {
            this.width = this.compassLabel.getWidth() + 10;
        } else {
            this.width = this.worldLabel.getWidth() + 15;
        }
    }
}
