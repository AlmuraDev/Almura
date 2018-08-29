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
import com.almuradev.almura.shared.client.ui.UIConstants;
import com.almuradev.almura.shared.client.ui.component.UIForm;
import com.almuradev.almura.shared.client.ui.component.UITextBox;
import com.almuradev.almura.shared.client.ui.component.button.UIButtonBuilder;
import com.almuradev.almura.shared.client.ui.component.dialog.MessageBoxButtons;
import com.almuradev.almura.shared.client.ui.component.dialog.UIMessageBox;
import com.almuradev.almura.shared.client.ui.screen.SimpleScreen;
import com.google.inject.Inject;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import org.spongepowered.api.text.Text;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class SimplePageDetails extends SimpleScreen {

    private static final int padding = 4;

    @Inject
    private static ClientPageManager manager;

    private final DateTimeFormatter formatter = DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.SHORT)
            .withLocale(Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getJavaLocale())
            .withZone(ZoneId.systemDefault());

    private UITextBox tbIndex, tbName;

    SimplePageDetails(GuiScreen parent) {
        super(parent, true);
    }

    @Override
    public void construct() {
        this.guiscreenBackground = true;

        this.validatePage();

        assert manager.getPage() != null;

        final UIForm form = new UIForm(this, 150, 225, I18n.format("almura.guide.details.form.title"));
        form.setZIndex(10);
        form.setBackgroundAlpha(255);

        // File name
        final UILabel labelId = new UILabel(this, I18n.format("almura.guide.label.id"));
        labelId.setAnchor(Anchor.TOP | Anchor.LEFT);
        labelId.setFontOptions(FontColors.WHITE_FO);

        final UITextBox tbId = new UITextBox(this, manager.getPage().getId());
        tbId.setAnchor(Anchor.TOP | Anchor.LEFT);
        tbId.setPosition(0, SimpleScreen.getPaddedY(labelId, 1));
        tbId.setSize(UIComponent.INHERITED, 0);
        tbId.setEditable(false);

        // Index
        final UILabel labelIndex = new UILabel(this, I18n.format("almura.guide.label.index"));
        labelIndex.setAnchor(Anchor.TOP | Anchor.LEFT);
        labelIndex.setPosition(0, SimpleScreen.getPaddedY(tbId, padding));
        labelIndex.setFontOptions(FontColors.WHITE_FO);

        this.tbIndex = new UITextBox(this, Integer.toString(manager.getPage().getIndex()));
        this.tbIndex.setAcceptsReturn(false);
        this.tbIndex.setAcceptsTab(false);
        this.tbIndex.setTabIndex(0);
        this.tbIndex.setOnEnter(tb -> this.save());
        this.tbIndex.setAnchor(Anchor.TOP | Anchor.LEFT);
        this.tbIndex.setPosition(0, SimpleScreen.getPaddedY(labelIndex, 1));
        this.tbIndex.setSize(UIComponent.INHERITED, 0);
        this.tbIndex.setFilter(s -> s.replaceAll("[^\\d]", ""));

        // Title
        final UILabel labelName = new UILabel(this, I18n.format("almura.guide.label.name"));
        labelName.setAnchor(Anchor.TOP | Anchor.LEFT);
        labelName.setPosition(0, SimpleScreen.getPaddedY(this.tbIndex, padding));
        labelName.setFontOptions(FontColors.WHITE_FO);

        this.tbName = new UITextBox(this, manager.getPage().getName());
        this.tbName.setAcceptsReturn(false);
        this.tbName.setAcceptsTab(false);
        this.tbName.setTabIndex(0);
        this.tbName.setOnEnter(tb -> this.save());
        this.tbName.setAnchor(Anchor.TOP | Anchor.LEFT);
        this.tbName.setPosition(0, SimpleScreen.getPaddedY(labelName, 1));
        this.tbName.setSize(UIComponent.INHERITED, 0);

        // Creator
        final UILabel labelCreator = new UILabel(this, I18n.format("almura.guide.label.creator"));
        labelCreator.setAnchor(Anchor.TOP | Anchor.LEFT);
        labelCreator.setPosition(0, SimpleScreen.getPaddedY(this.tbName, padding));
        labelCreator.setFontOptions(FontColors.WHITE_FO);

        final UITextBox tbCreator = new UITextBox(this, manager.getPage().getCreator().toString());
        tbCreator.setAnchor(Anchor.TOP | Anchor.LEFT);
        tbCreator.setPosition(0, SimpleScreen.getPaddedY(labelCreator, 1));
        tbCreator.setSize(UIComponent.INHERITED, 0);
        tbCreator.setEditable(false);

        // Created
        final UILabel labelCreated = new UILabel(this, I18n.format("almura.guide.label.created"));
        labelCreated.setAnchor(Anchor.TOP | Anchor.LEFT);
        labelCreated.setPosition(0, SimpleScreen.getPaddedY(tbCreator, padding));
        labelCreated.setFontOptions(FontColors.WHITE_FO);

        final UITextBox tbCreated = new UITextBox(this, formatter.format(manager.getPage().getCreated()));
        tbCreated.setAnchor(Anchor.TOP | Anchor.LEFT);
        tbCreated.setPosition(0, SimpleScreen.getPaddedY(labelCreated, 1));
        tbCreated.setSize(UIComponent.INHERITED, 0);
        tbCreated.setEditable(false);

        // Last Modifier
        final UILabel labelLastModifier = new UILabel(this, I18n.format("almura.guide.label.last_modifier"));
        labelLastModifier.setAnchor(Anchor.TOP | Anchor.LEFT);
        labelLastModifier.setPosition(0, SimpleScreen.getPaddedY(tbCreated, padding));
        labelLastModifier.setFontOptions(FontColors.WHITE_FO);

        final UITextBox tbLastModifier = new UITextBox(this, manager.getPage().getLastModifier().toString());
        tbLastModifier.setAnchor(Anchor.TOP | Anchor.LEFT);
        tbLastModifier.setPosition(0, SimpleScreen.getPaddedY(labelLastModifier, 1));
        tbLastModifier.setSize(UIComponent.INHERITED, 0);
        tbLastModifier.setEditable(false);

        // Last Modified
        final UILabel labelLastModified = new UILabel(this, I18n.format("almura.guide.label.last_modified"));
        labelLastModified.setAnchor(Anchor.TOP | Anchor.LEFT);
        labelLastModified.setPosition(0, SimpleScreen.getPaddedY(tbLastModifier, padding));
        labelLastModified.setFontOptions(FontColors.WHITE_FO);

        final UITextBox tbLastModified = new UITextBox(this, formatter.format(manager.getPage().getLastModified()));
        tbLastModified.setAnchor(Anchor.TOP | Anchor.LEFT);
        tbLastModified.setPosition(0, SimpleScreen.getPaddedY(labelLastModified, 1));
        tbLastModified.setSize(UIComponent.INHERITED, 0);
        tbLastModified.setEditable(false);

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

        form.add(labelId, tbId,
                 labelIndex, this.tbIndex,
                 labelName, this.tbName,
                 labelCreator, tbCreator,
                 labelCreated, tbCreated,
                 labelLastModifier, tbLastModifier,
                 labelLastModified, tbLastModified,
                 buttonSave, buttonCancel);

        addToScreen(form);

        this.tbIndex.focus();
        this.tbIndex.selectAll();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private boolean validatePage() {
        if (manager.getPage() == null) {
            UIMessageBox.showDialog(this,
                    I18n.format("almura.guide.dialog.error.page.title"),
                    I18n.format("almura.guide.dialog.error.page.content"),
                    MessageBoxButtons.OK);
            this.close();

            return false;
        }

        return true;
    }

    private void save() {
        final String rawIndex = tbIndex.getText();
        final int index = rawIndex.isEmpty() ? 0 : Integer.valueOf(rawIndex);

        if (this.validatePage() && manager.getPage() != null) {
            manager.getPage().setIndex(index);
            manager.getPage().setName(this.tbName.getText().trim());
            manager.requestSavePage();

            this.close();
        }
    }
}
