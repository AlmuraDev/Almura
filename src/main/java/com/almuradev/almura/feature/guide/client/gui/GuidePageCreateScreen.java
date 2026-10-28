/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.guide.client.gui;

import com.almuradev.almura.feature.guide.ClientPageManager;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.BasicScreen;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.BasicForm;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.BasicTextBox;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UICheckBox;
import net.malisis.core.client.gui.component.interaction.button.builder.UIButtonBuilder;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.util.FontColors;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * Creates a server guide (id, index and name) or a player guide of this player (just a name, the server picks the id). Players who may
 * create both choose with a check box.
 */
@SideOnly(Side.CLIENT)
public class GuidePageCreateScreen extends BasicScreen {

    private static final int padding = 4;

    @Inject
    private static ClientPageManager manager;

    private final boolean canAdd, canAddPlayer;
    private boolean playerGuide;
    private BasicTextBox tbId, tbIndex, tbName;

    public GuidePageCreateScreen(@Nullable GuiScreen parent, final boolean canAdd, final boolean canAddPlayer) {
        super(parent, true);
        this.canAdd = canAdd;
        this.canAddPlayer = canAddPlayer;
        // Without server guide permission it can only be a player guide
        this.playerGuide = !canAdd && canAddPlayer;
    }

    @Override
    public void construct() {
        this.guiscreenBackground = true;
        Keyboard.enableRepeatEvents(true);

        final boolean chooseType = this.canAdd && this.canAddPlayer;
        final int height = !this.canAdd ? 65 : chooseType ? 141 : 125;
        final BasicForm form = new BasicForm(150, height, I18n.format(this.playerGuide
                ? "almura.guide.create.form.title.player"
                : "almura.guide.create.form.title"));
        form.setZIndex(10);
        form.setBackgroundAlpha(255);

        // Server guide or player guide
        UICheckBox checkboxPlayerGuide = null;
        if (chooseType) {
            checkboxPlayerGuide = new UICheckBox(I18n.format("almura.guide.create.checkbox.player_guide"));
            checkboxPlayerGuide.setName("checkbox.player_guide");
            checkboxPlayerGuide.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).build());
            checkboxPlayerGuide.setTooltip(I18n.format("almura.guide.create.checkbox.player_guide.tooltip"));
            checkboxPlayerGuide.setPosition(0, 0, Anchor.TOP | Anchor.LEFT);
            checkboxPlayerGuide.register(this);
        }

        // File name
        final UILabel labelId = new UILabel(I18n.format("almura.guide.label.id"));
        labelId.setAnchor(Anchor.TOP | Anchor.LEFT);
        labelId.setPosition(0, checkboxPlayerGuide == null ? 0 : BasicScreen.getPaddedY(checkboxPlayerGuide, padding));
        labelId.setFontOptions(FontColors.WHITE_FO);
        labelId.setVisible(this.canAdd);

        this.tbId = new BasicTextBox("");
        this.tbId.setAcceptsReturn(false);
        this.tbId.setAcceptsTab(false);
        this.tbId.setTabIndex(0);
        this.tbId.setOnEnter(tb -> this.save());
        this.tbId.setAnchor(Anchor.TOP | Anchor.LEFT);
        this.tbId.setPosition(0, BasicScreen.getPaddedY(labelId, 1));
        this.tbId.setSize(UIComponent.INHERITED, 0);
        this.tbId.setFocused(true);
        // Ids are file names
        this.tbId.setFilter(s -> s.toLowerCase().replaceAll("[^a-z0-9_\\-]", ""));
        this.tbId.setVisible(this.canAdd);

        // Index
        final UILabel labelIndex = new UILabel(I18n.format("almura.guide.label.index"));
        labelIndex.setAnchor(Anchor.TOP | Anchor.LEFT);
        labelIndex.setPosition(0, this.tbId.isVisible() ? BasicScreen.getPaddedY(this.tbId, padding) : padding);
        labelIndex.setFontOptions(FontColors.WHITE_FO);
        labelIndex.setVisible(this.canAdd);

        this.tbIndex = new BasicTextBox(Integer.toString(0));
        this.tbIndex.setAcceptsReturn(false);
        this.tbIndex.setAcceptsTab(false);
        this.tbIndex.setTabIndex(1);
        this.tbIndex.setOnEnter(tb -> this.save());
        this.tbIndex.setAnchor(Anchor.TOP | Anchor.LEFT);
        this.tbIndex.setPosition(0, BasicScreen.getPaddedY(labelIndex, 1));
        this.tbIndex.setSize(UIComponent.INHERITED, 0);
        this.tbIndex.setFilter(s -> s.replaceAll("[^\\d]", ""));
        this.tbIndex.setVisible(this.canAdd);

        // Title
        final UILabel labelName = new UILabel(I18n.format("almura.guide.label.name"));
        labelName.setAnchor(Anchor.TOP | Anchor.LEFT);
        labelName.setPosition(0, this.tbIndex.isVisible() ? BasicScreen.getPaddedY(this.tbIndex, padding) : 0);
        labelName.setFontOptions(FontColors.WHITE_FO);

        this.tbName = new BasicTextBox("");
        this.tbName.setAcceptsReturn(false);
        this.tbName.setAcceptsTab(false);
        this.tbName.setTabIndex(2);
        this.tbName.setOnEnter(tb -> this.save());
        this.tbName.setAnchor(Anchor.TOP | Anchor.LEFT);
        this.tbName.setPosition(0, BasicScreen.getPaddedY(labelName, 1));
        this.tbName.setSize(UIComponent.INHERITED, 0);
        this.tbName.setFilter(s -> s.substring(0, Math.min(s.length(), 50)));

        // Save/Cancel
        final UIButton buttonSave = new UIButtonBuilder()
                .text(I18n.format("almura.button.save"))
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .width(40)
                .onClick(this::save)
                .build("button.save");

        final UIButton buttonCancel = new UIButtonBuilder()
                .text(I18n.format("almura.button.cancel"))
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .position(BasicScreen.getPaddedX(buttonSave, 2, Anchor.RIGHT), 0)
                .width(40)
                .onClick(this::close)
                .build("button.cancel");

        form.add(labelId, this.tbId,
                 labelIndex, this.tbIndex,
                 labelName, this.tbName,
                 buttonCancel, buttonSave);
        if (checkboxPlayerGuide != null) {
            form.add(checkboxPlayerGuide);
        }

        addToScreen(form);

        if (this.canAdd) {
            this.tbId.focus();
        } else {
            this.tbName.focus();
        }
    }

    @Subscribe
    public void onPlayerGuideChange(final UICheckBox.CheckEvent event) {
        this.playerGuide = event.getNewValue();
        // A player guide's id is picked by the server and it has no index
        this.tbId.setEditable(!this.playerGuide);
        this.tbIndex.setEditable(!this.playerGuide);
        if (this.playerGuide) {
            this.tbName.focus();
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private void save() {
        final String name = this.tbName.getText().trim();
        if (name.isEmpty()) {
            return;
        }

        if (this.playerGuide) {
            manager.requestNewPlayerPage(name);
            close();
            return;
        }

        final String id = this.tbId.getText().trim();
        final String rawIndex = this.tbIndex.getText();
        final int index = rawIndex.isEmpty() ? 0 : Integer.valueOf(rawIndex);

        if (!id.isEmpty() && !rawIndex.isEmpty()) {
            manager.requestNewPage(id, index, name);
            close();
        }
    }
}
