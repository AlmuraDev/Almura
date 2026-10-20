/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.worldload.client.gui;

import com.almuradev.almura.feature.worldload.client.ClientWorldLoadManager;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.BasicScreen;
import net.malisis.core.client.gui.component.container.BasicForm;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.decoration.UISeparator;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.button.builder.UIButtonBuilder;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.util.FontColors;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

import javax.annotation.Nullable;

/**
 * Popup shown while the server loads a world. Closed by the player, or by {@link ClientWorldLoadManager} once the load finishes.
 */
@SideOnly(Side.CLIENT)
public final class WorldLoadingScreen extends BasicScreen {

    private static final int FORM_WIDTH = 220;
    private static final int FORM_HEIGHT = 92;

    private final ClientWorldLoadManager manager;
    private UILabel worldLabel;
    private UILabel elapsedLabel;
    private long lastElapsedSeconds = -1;

    public WorldLoadingScreen(@Nullable final GuiScreen parent, final ClientWorldLoadManager manager) {
        // The screen that was open (if any) is drawn behind the popup and shown again when it closes
        super(parent, true);
        this.manager = manager;
    }

    @Override
    public void construct() {
        this.guiscreenBackground = false;

        final BasicForm form = new BasicForm(this, FORM_WIDTH, FORM_HEIGHT, "Loading World");
        form.setZIndex(10);
        form.setBackgroundAlpha(255);

        this.worldLabel = new UILabel(this, "");
        this.worldLabel.setFontOptions(FontOptions.builder().from(FontColors.GOLD_FO).shadow(true).build());
        this.worldLabel.setPosition(0, 6, Anchor.CENTER | Anchor.TOP);

        final UILabel infoLabel = new UILabel(this, "The server may pause until it's ready.");
        infoLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).build());
        infoLabel.setPosition(0, 20, Anchor.CENTER | Anchor.TOP);

        this.elapsedLabel = new UILabel(this, "");
        this.elapsedLabel.setFontOptions(FontOptions.builder().from(FontColors.GRAY_FO).shadow(true).build());
        this.elapsedLabel.setPosition(0, 34, Anchor.CENTER | Anchor.TOP);

        final UISeparator separator = new UISeparator(this);
        separator.setSize(form.getWidth() - 5, 1);
        separator.setPosition(0, -20, Anchor.BOTTOM | Anchor.CENTER);

        final UIButton buttonClose = new UIButtonBuilder(this)
                .text(I18n.format("almura.button.close"))
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .position(0, 0)
                .width(40)
                .listener(this)
                .build("button.close");

        form.add(this.worldLabel, infoLabel, this.elapsedLabel, separator, buttonClose);
        this.addToScreen(form);

        this.refresh();
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        this.refresh();
    }

    private void refresh() {
        if (this.worldLabel == null) {
            return;
        }

        final List<String> worlds = this.manager.getLoadingWorlds();
        this.worldLabel.setText(worlds.isEmpty() ? "Loading world..." : "Loading world: " + String.join(", ", worlds));

        final long seconds = Math.max(0, (System.currentTimeMillis() - this.manager.getLoadStartedAt()) / 1000);
        if (seconds != this.lastElapsedSeconds) {
            this.lastElapsedSeconds = seconds;
            this.elapsedLabel.setText("Please wait... (" + seconds + "s)");
        }
    }

    @Subscribe
    public void onUIButtonClickEvent(final UIButton.ClickEvent event) {
        if ("button.close".equalsIgnoreCase(event.getComponent().getName())) {
            this.close();
        }
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        // Escape, the close button or another screen replacing this one: don't reopen until the next world starts loading
        this.manager.dismiss();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
