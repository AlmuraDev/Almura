/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.guide.client.gui;

import com.almuradev.almura.feature.guide.ClientPageManager;
import com.almuradev.almura.shared.client.ui.component.UIForm;
import com.almuradev.almura.shared.client.ui.component.button.UIButtonBuilder;
import com.almuradev.almura.shared.client.ui.component.dialog.MessageBoxButtons;
import com.almuradev.almura.shared.client.ui.component.dialog.UIMessageBox;
import com.almuradev.almura.shared.client.ui.screen.SimpleScreen;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UITextField;
import net.malisis.core.renderer.font.FontOptions;
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
    private final FontOptions readOnlyFontOptions = FontOptions.builder().color(-3158579).shadow(true).build();

    private UITextField textFieldIndex, textFieldName;

    public SimplePageDetails(GuiScreen parent) {
        super(parent, true);
    }

    @Override
    public void construct() {
        this.guiscreenBackground = true;

        this.validatePage();

        assert manager.getPage() != null;

        final UIForm form = new UIForm(this, 150, 225, I18n.format("almura.guide.details.form.title"));
        form.setAnchor(Anchor.CENTER | Anchor.MIDDLE);
        form.setMovable(true);
        form.setClosable(true);
        form.setZIndex(50);

        // File name
        final UILabel labelId = new UILabel(this, I18n.format("almura.guide.label.id"));
        labelId.setAnchor(Anchor.TOP | Anchor.LEFT);

        final UITextField textFieldId = new UITextField(this, manager.getPage().getId());
        textFieldId.setAnchor(Anchor.TOP | Anchor.LEFT);
        textFieldId.setPosition(0, SimpleScreen.getPaddedY(labelId, 1));
        textFieldId.setSize(UIComponent.INHERITED, 0);
        textFieldId.setFontOptions(this.readOnlyFontOptions);
        textFieldId.setEditable(false);

        // Index
        final UILabel labelIndex = new UILabel(this, I18n.format("almura.guide.label.index"));
        labelIndex.setAnchor(Anchor.TOP | Anchor.LEFT);
        labelIndex.setPosition(0, SimpleScreen.getPaddedY(textFieldId, padding));

        this.textFieldIndex = new UITextField(this, Integer.toString(manager.getPage().getIndex()));
        this.textFieldIndex.setAnchor(Anchor.TOP | Anchor.LEFT);
        this.textFieldIndex.setPosition(0, SimpleScreen.getPaddedY(labelIndex, 1));
        this.textFieldIndex.setSize(UIComponent.INHERITED, 0);
        this.textFieldIndex.setFilter(s -> s.replaceAll("[^\\d]", ""));

        // Title
        final UILabel labelName = new UILabel(this, I18n.format("almura.guide.label.name"));
        labelName.setAnchor(Anchor.TOP | Anchor.LEFT);
        labelName.setPosition(0, SimpleScreen.getPaddedY(this.textFieldIndex, padding));

        this.textFieldName = new UITextField(this, manager.getPage().getName());
        this.textFieldName.setAnchor(Anchor.TOP | Anchor.LEFT);
        this.textFieldName.setPosition(0, SimpleScreen.getPaddedY(labelName, 1));
        this.textFieldName.setSize(UIComponent.INHERITED, 0);

        // Creator
        final UILabel labelCreator = new UILabel(this, I18n.format("almura.guide.label.creator"));
        labelCreator.setAnchor(Anchor.TOP | Anchor.LEFT);
        labelCreator.setPosition(0, SimpleScreen.getPaddedY(this.textFieldName, padding));

        // TODO: Show username in format of: Username (UUID)
        final UITextField textFieldCreator = new UITextField(this, manager.getPage().getCreator().toString());
        textFieldCreator.setAnchor(Anchor.TOP | Anchor.LEFT);
        textFieldCreator.setPosition(0, SimpleScreen.getPaddedY(labelCreator, 1));
        textFieldCreator.setSize(UIComponent.INHERITED, 0);
        textFieldCreator.setFontOptions(this.readOnlyFontOptions);
        textFieldCreator.setEditable(false);

        // Created
        final UILabel labelCreated = new UILabel(this, I18n.format("almura.guide.label.created"));
        labelCreated.setAnchor(Anchor.TOP | Anchor.LEFT);
        labelCreated.setPosition(0, SimpleScreen.getPaddedY(textFieldCreator, padding));

        final UITextField textFieldCreated = new UITextField(this, formatter.format(manager.getPage().getCreated()));
        textFieldCreated.setAnchor(Anchor.TOP | Anchor.LEFT);
        textFieldCreated.setPosition(0, SimpleScreen.getPaddedY(labelCreated, 1));
        textFieldCreated.setSize(UIComponent.INHERITED, 0);
        textFieldCreated.setFontOptions(this.readOnlyFontOptions);
        textFieldCreated.setEditable(false);

        // Last Modifier
        final UILabel labelLastModifier = new UILabel(this, I18n.format("almura.guide.label.last_modifier"));
        labelLastModifier.setAnchor(Anchor.TOP | Anchor.LEFT);
        labelLastModifier.setPosition(0, SimpleScreen.getPaddedY(textFieldCreated, padding));

        // TODO: Show username in format of: Username (UUID)
        final UITextField textFieldLastModifier = new UITextField(this, manager.getPage().getLastModifier().toString());
        textFieldLastModifier.setAnchor(Anchor.TOP | Anchor.LEFT);
        textFieldLastModifier.setPosition(0, SimpleScreen.getPaddedY(labelLastModifier, 1));
        textFieldLastModifier.setSize(UIComponent.INHERITED, 0);
        textFieldLastModifier.setFontOptions(this.readOnlyFontOptions);
        textFieldLastModifier.setEditable(false);

        // Last Modified
        final UILabel labelLastModified = new UILabel(this, I18n.format("almura.guide.label.last_modified"));
        labelLastModified.setAnchor(Anchor.TOP | Anchor.LEFT);
        labelLastModified.setPosition(0, SimpleScreen.getPaddedY(textFieldLastModifier, padding));

        final UITextField textFieldLastModified = new UITextField(this, formatter.format(manager.getPage().getLastModified()));
        textFieldLastModified.setAnchor(Anchor.TOP | Anchor.LEFT);
        textFieldLastModified.setPosition(0, SimpleScreen.getPaddedY(labelLastModified, 1));
        textFieldLastModified.setSize(UIComponent.INHERITED, 0);
        textFieldLastModified.setFontOptions(this.readOnlyFontOptions);
        textFieldLastModified.setEditable(false);

        // Save/Cancel
        final UIButton buttonSave = new UIButtonBuilder(this)
                .text(Text.of("almura.guide.button.close"))
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .width(40)
                .listener(this)
                .build("button.close");

        final UIButton buttonCancel = new UIButtonBuilder(this)
                .text(Text.of("almura.guide.button.save"))
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .position(SimpleScreen.getPaddedX(buttonSave, 2, Anchor.RIGHT), 0)
                .width(40)
                .listener(this)
                .build("button.save");

        form.add(labelId, textFieldId,
                 labelIndex, this.textFieldIndex,
                 labelName, this.textFieldName,
                 labelCreator, textFieldCreator,
                 labelCreated, textFieldCreated,
                 labelLastModifier, textFieldLastModifier,
                 labelLastModified, textFieldLastModified,
                 buttonSave, buttonCancel);

        addToScreen(form);
    }

    private void validatePage() {
        if (manager.getPage() == null) {
            UIMessageBox.showDialog(this,
                    I18n.format("almura.guide.dialog.error.page.title"),
                    I18n.format("almura.guide.dialog.error.page.content"),
                    MessageBoxButtons.OK);
            close();
        }
    }

    @Subscribe
    public void onButtonClick(UIButton.ClickEvent event) {
        switch (event.getComponent().getName().toLowerCase()) {
            case "button.close":
                close();
                break;
            case "button.save":
                final String rawIndex = textFieldIndex.getText();
                final int index = rawIndex.isEmpty() ? 0 : Integer.valueOf(rawIndex);

                this.validatePage();

                assert manager.getPage() != null;
                manager.getPage().setIndex(index);
                manager.getPage().setName(this.textFieldName.getText().trim());
                manager.requestSavePage();

                close();
                break;
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return true;
    }
}
