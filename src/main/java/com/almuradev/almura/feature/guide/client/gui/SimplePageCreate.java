/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.guide.client.gui;

import com.almuradev.almura.feature.guide.ClientPageManager;
import com.almuradev.almura.shared.client.ui.FontColors;
import com.almuradev.almura.shared.client.ui.component.UIForm;
import com.almuradev.almura.shared.client.ui.component.UITextBox;
import com.almuradev.almura.shared.client.ui.component.button.UIButtonBuilder;
import com.almuradev.almura.shared.client.ui.screen.SimpleScreen;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;
import javax.inject.Inject;

@SideOnly(Side.CLIENT)
public class SimplePageCreate extends SimpleScreen {

    private static final int padding = 4;

    @Inject
    private static ClientPageManager manager;

    private UITextBox tbId, tbIndex, tbName;

    public SimplePageCreate(@Nullable GuiScreen parent) {
        super(parent, true);
    }

    @Override
    public void construct() {
        this.guiscreenBackground = true;

        final UIForm form = new UIForm(this, 150, 125, I18n.format("almura.guide.create.form.title"));
        form.setZIndex(10);
        form.setBackgroundAlpha(255);

        // File name
        final UILabel labelId = new UILabel(this, I18n.format("almura.guide.label.id"));
        labelId.setAnchor(Anchor.TOP | Anchor.LEFT);
        labelId.setFontOptions(FontColors.WHITE_FO);

        this.tbId = new UITextBox(this, "");
        this.tbId.setAcceptsReturn(false);
        this.tbId.setAcceptsTab(false);
        this.tbId.setTabIndex(0);
        this.tbId.setOnEnter(tb -> this.save());
        this.tbId.setAnchor(Anchor.TOP | Anchor.LEFT);
        this.tbId.setPosition(0, SimpleScreen.getPaddedY(labelId, 1));
        this.tbId.setSize(UIComponent.INHERITED, 0);
        this.tbId.setFocused(true);
        this.tbId.setFilter(String::toLowerCase);

        // Index
        final UILabel labelIndex = new UILabel(this, I18n.format("almura.guide.label.index"));
        labelIndex.setAnchor(Anchor.TOP | Anchor.LEFT);
        labelIndex.setPosition(0, this.tbId.isVisible() ? SimpleScreen.getPaddedY(this.tbId, padding) : padding);
        labelIndex.setFontOptions(FontColors.WHITE_FO);

        this.tbIndex = new UITextBox(this, Integer.toString(0));
        this.tbIndex.setAcceptsReturn(false);
        this.tbIndex.setAcceptsTab(false);
        this.tbIndex.setTabIndex(1);
        this.tbIndex.setOnEnter(tb -> this.save());
        this.tbIndex.setAnchor(Anchor.TOP | Anchor.LEFT);
        this.tbIndex.setPosition(0, SimpleScreen.getPaddedY(labelIndex, 1));
        this.tbIndex.setSize(UIComponent.INHERITED, 0);
        this.tbIndex.setFilter(s -> s.replaceAll("[^\\d]", ""));

        // Title
        final UILabel labelName = new UILabel(this, I18n.format("almura.guide.label.name"));
        labelName.setAnchor(Anchor.TOP | Anchor.LEFT);
        labelName.setPosition(0, this.tbIndex.isVisible() ? SimpleScreen.getPaddedY(this.tbIndex, padding) : padding);
        labelName.setFontOptions(FontColors.WHITE_FO);

        this.tbName = new UITextBox(this, "");
        this.tbName.setAcceptsReturn(false);
        this.tbName.setAcceptsTab(false);
        this.tbName.setTabIndex(2);
        this.tbName.setOnEnter(tb -> this.save());
        this.tbName.setAnchor(Anchor.TOP | Anchor.LEFT);
        this.tbName.setPosition(0, SimpleScreen.getPaddedY(labelName, 1));
        this.tbName.setSize(UIComponent.INHERITED, 0);
        this.tbName.setFilter(s -> s.substring(0, Math.min(s.length(), 50)));

        // Save/Cancel
        final UIButton buttonSave = new UIButtonBuilder(this)
                .text(Text.of("almura.button.save"))
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .width(40)
                .onClick(this::save)
                .build("button.save");

        final UIButton buttonCancel = new UIButtonBuilder(this)
                .text(Text.of("almura.button.cancel"))
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .position(SimpleScreen.getPaddedX(buttonSave, 2, Anchor.RIGHT), 0)
                .width(40)
                .onClick(this::close)
                .build("button.cancel");

        form.add(labelId, this.tbId,
                 labelIndex, this.tbIndex,
                 labelName, this.tbName,
                 buttonCancel, buttonSave);

        addToScreen(form);

        this.tbId.focus();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private void save() {
        final String id = this.tbId.getText().trim();
        final String rawIndex = this.tbIndex.getText();
        final int index = rawIndex.isEmpty() ? 0 : Integer.valueOf(rawIndex);
        final String name = this.tbName.getText().trim();

        if (!id.isEmpty() && !rawIndex.isEmpty() && !name.isEmpty()) {
            manager.requestNewPage(id, index, name);
            close();
        }
    }
}
