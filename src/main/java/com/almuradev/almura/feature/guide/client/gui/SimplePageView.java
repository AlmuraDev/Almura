/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.guide.client.gui;

import com.almuradev.almura.feature.guide.ClientPageManager;
import com.almuradev.almura.feature.guide.Page;
import com.almuradev.almura.feature.guide.PageListEntry;
import com.almuradev.almura.shared.client.ui.component.UIForm;
import com.almuradev.almura.shared.client.ui.component.button.UIButtonBuilder;
import com.almuradev.almura.shared.client.ui.screen.SimpleScreen;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UISelect;
import net.malisis.core.client.gui.component.interaction.UITextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import javax.inject.Inject;

@SideOnly(Side.CLIENT)
public class SimplePageView extends SimpleScreen {

    private static final int innerPadding = 2;

    @Inject
    private static ClientPageManager manager;
    private int lastUpdate = 0;
    private boolean canAdd, canRemove, canModify;
    private boolean showRaw = false;
    private UIButton buttonRemove, buttonAdd, buttonDetails, buttonFormat, buttonSave;
    private UISelect<PageListEntry> pagesSelect;
    private UITextField contentField;

    public SimplePageView(boolean canAdd, boolean canRemove, boolean canModify) {
        this.canAdd = canAdd;
        this.canRemove = canRemove;
        this.canModify = canModify;
    }

    @Override
    public void construct() {
        guiscreenBackground = true;

        final UIForm form = new UIForm(this, 250, 225, I18n.format("almura.guide.view.form.title"));
        form.setAnchor(Anchor.CENTER | Anchor.MIDDLE);
        form.setMovable(true);
        form.setClosable(true);

        // Remove button
        this.buttonRemove = new UIButtonBuilder(this)
                .width(10)
                .text(Text.of(TextColors.RED, "-"))
                .tooltip(Text.of("almura.guide.view.button.remove.tooltip"))
                .visible(hasAnyPermission())
                .enabled(hasRemovePermission())
                .listener(this)
                .build("button.remove");

        // Details button
        this.buttonDetails = new UIButtonBuilder(this)
                .width(10)
                .text(Text.of(TextColors.YELLOW, "?"))
                .tooltip(Text.of(I18n.format("almura.guide.view.button.details.tooltip")))
                .anchor(Anchor.TOP | Anchor.RIGHT)
                .visible(hasAnyPermission())
                .listener(this)
                .build("button.details");

        // Add button
        this.buttonAdd = new UIButtonBuilder(this)
                .width(10)
                .x(SimpleScreen.getPaddedX(this.buttonDetails, 2, Anchor.RIGHT))
                .text(Text.of(TextColors.GREEN, "+"))
                .tooltip(Text.of("almura.guide.view.button.add.tooltip"))
                .anchor(Anchor.TOP | Anchor.RIGHT)
                .visible(hasAnyPermission())
                .enabled(hasAddPermission())
                .listener(this)
                .build("button.add");

        // Pages dropdown
        this.pagesSelect = new UISelect<>(this, SimpleScreen.getPaddedWidth(form));
        this.pagesSelect.setLabelFunction(PageListEntry::getName);
        this.pagesSelect.setName("combobox.pages");
        this.pagesSelect.setPosition(this.buttonRemove.isVisible() ? SimpleScreen.getPaddedX(this.buttonRemove, innerPadding) : 0, 0);
        this.pagesSelect.register(this);
        if (hasAnyPermission()) {
            this.pagesSelect.setSize(this.pagesSelect.getWidth() - this.buttonDetails.getWidth() - this.buttonAdd.getWidth() - this.buttonRemove
                    .getWidth() - (innerPadding * 4) + 2, 15);
        }

        // Formatted button
        this.buttonFormat = new UIButtonBuilder(this)
                .width(10)
                .anchor(Anchor.BOTTOM | Anchor.LEFT)
                .visible(this.hasAnyPermission())
                .listener(this)
                .build("button.format");

        // Content text field
        contentField = new UITextField(this, "", true);
        contentField.setSize(SimpleScreen.getPaddedWidth(form),
                SimpleScreen.getPaddedHeight(form) - this.pagesSelect.getHeight() - (innerPadding * 2) - this.buttonFormat.getHeight());
        contentField.setPosition(0, SimpleScreen.getPaddedY(this.pagesSelect, innerPadding));
        contentField.setEditable(this.hasModifyPermission());
        contentField.setOptions(0x555555, 0xc8c8c8, 0x00000);

        // Close button
        final UIButton buttonClose = new UIButtonBuilder(this)
                .width(40)
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .text(Text.of("almura.guide.button.close"))
                .listener(this)
                .build("button.close");

        // Save button
        this.buttonSave = new UIButtonBuilder(this)
                .width(40)
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .x(SimpleScreen.getPaddedX(buttonClose, innerPadding, Anchor.RIGHT))
                .text(Text.of("almura.guide.button.save"))
                .visible(hasAnyPermission())
                .enabled(hasModifyPermission())
                .listener(this)
                .build("button.save");


        form.add(this.buttonRemove, this.pagesSelect, this.buttonDetails, this.buttonAdd, this.buttonFormat, contentField, buttonClose, buttonSave);

        addToScreen(form);

        this.updateButtons();
    }

    @Subscribe
    public void onUIButtonClickEvent(UIButton.ClickEvent event) {
        switch (event.getComponent().getName().toLowerCase()) {
            case "button.details":
                new SimplePageDetails(this).display();
                break;
            case "button.format":
                this.showRaw = !this.showRaw;
                this.updateFormattingButton();

                final String currentContent = this.contentField.getText();
                if (showRaw) {
                    // Need to convert the content from sectional -> ampersand
                    this.contentField.setText(Page.asFriendlyText(currentContent));
                } else {
                    // Need to convert the content from ampersand -> sectional
                    this.contentField.setText(Page.asUglyText(currentContent));
                }
                break;
            case "button.add":
                new SimplePageCreate(this).display();
                break;
            case "button.remove":
                if (manager.getPage() != null) {
                    manager.requestRemovePage(manager.getPage().getId());
                }
                break;
            case "button.save":
                if (manager.getPage() != null) {
                    manager.getPage().setContent(this.contentField.getText());
                    manager.requestSavePage();
                }
                break;
            case "button.close":
                close();
                break;
        }
    }

    @Override
    public void update(int mouseX, int mouseY, float partialTick) {
        super.update(mouseX, mouseY, partialTick);
        if (++this.lastUpdate > 60) { // 2.5 second delay
            final String currentContent = this.contentField.getText();
            this.contentField.setText(Page.asFriendlyText(currentContent));
            this.lastUpdate = 0;
        }
    }

    @Override
    protected void keyTyped(char keyChar, int keyCode) {
        super.keyTyped(keyChar, keyCode);
        this.lastUpdate = 0; // Reset the timer when key is typed.
    }

    @Subscribe
    public void onComboBoxSelect(UISelect.SelectEvent event) {
        switch (event.getComponent().getName().toLowerCase()) {
            case "combobox.pages": {
                if (event.getNewValue() == null) {
                    this.contentField.setText("");
                } else {
                    final PageListEntry entry = (PageListEntry) event.getNewValue();
                    manager.requestPage(entry.getId());
                }
                this.updateButtons();
            }
        }
    }

    public void refreshPageEntries() {
        final PageListEntry oldEntry = this.pagesSelect.getSelectedValue();

        this.pagesSelect.setOptions(manager.getPageEntries());

        if (oldEntry == null) {
            this.pagesSelect.selectFirst();
            this.refreshPage();

            return;
        }

        final PageListEntry newEntry = manager.getPageEntries()
                .stream()
                .filter(p -> p.getId().equalsIgnoreCase(oldEntry.getId()))
                .findFirst().orElse(null);
        if (newEntry != null) {
            this.pagesSelect.select(newEntry);
        } else {
            this.pagesSelect.selectFirst();
        }

        this.refreshPage();
    }

    public void refreshPage() {
        this.contentField.setText(manager.getPage() == null ? "" : manager.getPage().getContent());
        this.updateButtons();
    }

    private boolean hasAnyPermission() {
        return this.hasModifyPermission() || this.hasRemovePermission() || this.hasAddPermission();
    }

    private boolean hasModifyPermission() {
        return this.canModify;
    }

    private boolean hasRemovePermission() {
        return this.canRemove;
    }

    private boolean hasAddPermission() {
        return this.canAdd;
    }

    private void updateButtons() {
        this.updateFormattingButton();
        this.buttonFormat.setEnabled((this.hasAnyPermission() && manager.getPage() != null));
        this.buttonRemove.setEnabled((this.hasRemovePermission() && manager.getPage() != null));
        this.buttonAdd.setEnabled(this.hasAddPermission());
        this.buttonSave.setEnabled((this.hasModifyPermission() && manager.getPage() != null));
        this.buttonDetails.setEnabled((this.hasModifyPermission() && manager.getPage() != null));
    }

    private void updateFormattingButton() {
        this.buttonFormat.setText(this.showRaw ? I18n.format("almura.guide.view.button.raw")
                : TextFormatting.ITALIC + I18n.format("almura.guide.view.button.formatted"));
        this.buttonFormat.setTooltip(this.showRaw ? I18n.format("almura.guide.view.button.raw.tooltip")
                : I18n.format("almura.guide.view.button.formatted.tooltip"));
    }

    @Override
    public boolean doesGuiPauseGame() {
        return true;
    }
}
