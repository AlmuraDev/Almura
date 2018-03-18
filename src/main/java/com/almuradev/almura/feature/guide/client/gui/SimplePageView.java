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
import com.almuradev.almura.shared.client.ui.FontColors;
import com.almuradev.almura.shared.client.ui.component.UIFormContainer;
import com.almuradev.almura.shared.client.ui.component.button.UIButtonBuilder;
import com.almuradev.almura.shared.client.ui.component.dialog.MessageBoxButtons;
import com.almuradev.almura.shared.client.ui.component.dialog.MessageBoxConsumer;
import com.almuradev.almura.shared.client.ui.component.dialog.MessageBoxResult;
import com.almuradev.almura.shared.client.ui.component.dialog.UIMessageBox;
import com.almuradev.almura.shared.client.ui.screen.SimpleScreen;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UISelect;
import net.malisis.core.client.gui.component.interaction.UITextField;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.function.Consumer;

import javax.inject.Inject;

@SideOnly(Side.CLIENT)
public class SimplePageView extends SimpleScreen {

    private static final int innerPadding = 2;

    @Inject
    public static ClientPageManager manager;
    private int lastUpdate = 0;
    private boolean canAdd, canRemove, canModify;
    private boolean showRaw = false;
    private UIButton buttonRemove, buttonAdd, buttonDetails, buttonFormat, buttonSave;
    private UISelect<PageListEntry> pagesSelect;
    private UITextField contentField;
    @Inject private static PluginContainer container;
    private boolean unlockMouse = true;

    public SimplePageView(boolean canAdd, boolean canRemove, boolean canModify) {
        this.canAdd = canAdd;
        this.canRemove = canRemove;
        this.canModify = canModify;
    }

    @Override
    public void construct() {
        guiscreenBackground = false;

        Keyboard.enableRepeatEvents(true);

        final UIFormContainer form = new UIFormContainer(this, 420, 225, TextFormatting.WHITE + I18n.format("almura.guide.view.form.title"));
        form.setAnchor(Anchor.CENTER | Anchor.MIDDLE);
        form.setMovable(true);
        form.setClosable(true);
        form.setBorder(FontColors.WHITE, 1, 185);
        form.setBackgroundAlpha(185);
        form.setBottomPadding(3);
        form.setRightPadding(3);
        form.setTopPadding(20);
        form.setLeftPadding(3);

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
                this.buttonDetails.getTooltip().setVisible(false);
                Sponge.getScheduler().createTaskBuilder().delayTicks(5).execute(delayedTask("pageDetails")).submit(container);
                break;

            case "button.format":
                this.showRaw = !this.showRaw;
                this.updateFormattingButton();

                final String currentContent = this.contentField.getText();

                if (showRaw) {
                    // Need to convert the content from sectional -> ampersand
                    this.contentField.setText(Page.asUglyText(currentContent));
                } else {
                    // Need to convert the content from ampersand -> sectional
                    this.contentField.setText(Page.asFriendlyText(currentContent));
                }
                break;
            case "button.add":
                this.buttonAdd.getTooltip().setVisible(false);
                Sponge.getScheduler().createTaskBuilder().delayTicks(5).execute(delayedTask("pageCreate")).submit(container);
                break;

            case "button.remove":
                this.buttonRemove.getTooltip().setVisible(false);
                if (manager.getPage() != null) {
                    Sponge.getScheduler().createTaskBuilder().delayTicks(5).execute(delayedTask("pageRemove")).submit(container);
                }
                break;
            case "button.save":
                if (manager.getPage() != null) {
                    if (!manager.preSnapshot.equalsIgnoreCase(this.contentField.getText())) {
                        manager.getPage().setContent(this.contentField.getText());
                        manager.requestSavePage();
                    }
                }
                break;
            case "button.close":
                if (canModify && manager.getPage() != null) {
                    if (!manager.preSnapshot.equalsIgnoreCase(this.contentField.getText())) {
                        // Save the postSnapshot to the manager so it can be accessed within manager.
                        manager.postSnapshot = this.contentField.getText();
                        UIMessageBox.showDialog(this, I18n.format("almura.guide.view.form.title"), "Changes detected to current guide, do you wish to save?"
                                , MessageBoxButtons.YES_NO_CANCEL, new MessageBoxConsumer() {
                                    @Override
                                    public void accept(MessageBoxResult messageBoxResult) {
                                        if (messageBoxResult == MessageBoxResult.YES) {
                                            manager.getPage().setContent(manager.postSnapshot);
                                            manager.requestSavePage();
                                        } else if (messageBoxResult == MessageBoxResult.CANCEL) {
                                            return;
                                        } else if (messageBoxResult == MessageBoxResult.NO) {
                                            Sponge.getScheduler().createTaskBuilder().delayTicks(5).execute(delayedTask("closeGUI")).submit(container);
                                        }
                                    }
                                });
                    } else {
                        close();
                    }
                } else {
                    close();
                }
                break;
        }
    }

    private Consumer<Task> delayedTask(String details) {
        return task -> {
            if (details.equalsIgnoreCase("pageDetails")) {
                new SimplePageDetails(this).display();
            }

            if (details.equalsIgnoreCase("pageCreate")) {
                new SimplePageCreate(this).display();
            }

            if (details.equalsIgnoreCase("closeGUI")) {
                if (Minecraft.getMinecraft().currentScreen instanceof SimplePageView) {
                    ((SimplePageView) Minecraft.getMinecraft().currentScreen).close();
                }
            }

            if (details.equalsIgnoreCase("pageRemove")) {
                // Todo: Disabled at the moment because I can't get the text within the messagebox to center.
                // Todo: Confirmation window is closing the Guide which is not intended.
                if (false) {
                    UIMessageBox.showDialog(this, I18n.format("almura.guide.view.form.title"), "Do you wish to delete the "
                            + "selected guide?", MessageBoxButtons.YES_NO, new MessageBoxConsumer() {
                        @Override
                        public void accept(MessageBoxResult messageBoxResult) {
                            if (messageBoxResult == MessageBoxResult.YES) {
                                manager.requestRemovePage(manager.getPage().getId());
                            }
                        }
                    });
                }
                new SimpleConfirmRemove(this).display();
            }
        };
    }

    @Override
    public void update(int mouseX, int mouseY, float partialTick) {
        super.update(mouseX, mouseY, partialTick);
        if (unlockMouse && this.lastUpdate == 25) {
            Mouse.setGrabbed(false); // Force the mouse to be visible even though Mouse.isGrabbed() is false.  //#BugsUnited.
            unlockMouse = false; // Only unlock once per session.
        }

        if (++this.lastUpdate > 100 && !showRaw && hasModifyPermission()) {

            // Todo: disabled, causing some issues with scroll.
            /*
            // Get the current cursor position so we can set it back after the refresh
            final int x = this.contentField.getCursorPosition().getXOffset();
            final int y = this.contentField.getCursorPosition().getYOffset();

            // Format current text.
            final String currentContent = this.contentField.getText();
            this.contentField.setText(Page.asFriendlyText(currentContent));

            // Set original cursor position prior to format.
            this.contentField.setCursorPosition(x, y);


            //Reset timer.
            this.lastUpdate = 0;

            //Enable tooltips

            if ((mc.currentScreen instanceof SimplePageView)) {
                this.buttonAdd.getTooltip().setVisible(true);
                this.buttonDetails.getTooltip().setVisible(true);
                this.buttonRemove.getTooltip().setVisible(true);
            } */
        }
    }

    @Override
    protected void keyTyped(char keyChar, int keyCode) {
        super.keyTyped(keyChar, keyCode);
        this.lastUpdate = 0; // Reset the timer when key is typed.
    }

    @Override
    protected void mouseClicked(int x, int y, int button) {
        super.mouseClicked(x, y, button);
        this.lastUpdate = 0; // Reset the timer when mouse is pressed.
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
            }
        }
    }

    public void refreshPageEntries(String switchToPage) {
        this.pagesSelect.setOptions(manager.getPageEntries());

        if (switchToPage != null) {
            manager.getPageEntries().stream().filter(en -> en.getId().equalsIgnoreCase(switchToPage)).findFirst().ifPresent(en -> this.pagesSelect.select(en));
        }
    }

    public void refreshPage() {
        this.contentField.setText(Page.asFriendlyText(manager.getPage() == null ? "" : manager.getPage().getContent()));
        manager.preSnapshot = this.contentField.getText();

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
        return false; // Can't stop the game otherwise the Sponge Scheduler also stops.
    }
}
