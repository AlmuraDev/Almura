/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.guide.client.gui;

import com.almuradev.almura.feature.guide.ClientPageManager;
import com.almuradev.almura.feature.guide.PageListEntry;
import com.almuradev.almura.shared.client.ui.component.WYSIWYGTextBox;
import com.almuradev.almura.shared.util.TextUtil;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.BasicScreen;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.container.BasicForm;
import net.malisis.core.client.gui.component.container.BasicList;
import net.malisis.core.client.gui.component.container.dialog.BasicMessageBox;
import net.malisis.core.client.gui.component.container.dialog.MessageBoxButtons;
import net.malisis.core.client.gui.component.container.dialog.MessageBoxResult;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.button.builder.UIButtonBuilder;
import net.malisis.core.util.FontColors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.plugin.PluginContainer;

import javax.annotation.Nullable;
import javax.inject.Inject;

@SideOnly(Side.CLIENT)
public class GuidePageViewScreen extends BasicScreen {

    @Inject private static PluginContainer container;
    @Inject public static ClientPageManager manager;
    private boolean canAdd, canRemove, canModify;
    private float snapshotScrollOffset;
    private int snapshotCursorPositionX, snapshotCursorPositionY;
    private BasicList<PageListEntry> pagesList;
    private WYSIWYGTextBox tbContent;
    @Nullable private UIButton buttonRemove, buttonAdd, buttonDetails, buttonSave;

    public GuidePageViewScreen(final boolean canAdd, final boolean canRemove, final boolean canModify) {
        this.canAdd = canAdd;
        this.canRemove = canRemove;
        this.canModify = canModify;
    }

    @Override
    public void construct() {
        guiscreenBackground = false;
        Keyboard.enableRepeatEvents(true);

        final BasicForm form = new BasicForm(this, 582, 315, TextFormatting.WHITE + I18n.format("almura.guide.view.form.title"));

        // Page list
        this.pagesList = new BasicList<>(this, 156, BasicScreen.getPaddedHeight(form));
        this.pagesList.setBorder(0xFFFFFF, 1, 215);
        this.pagesList.setPadding(2, 2);
        this.pagesList.setPosition(1, 0);
        this.pagesList.setItemComponentSpacing(1);
        this.pagesList.setItemComponentFactory(PageListEntryItemComponent::new);
        this.pagesList.setItems(manager.getPageEntries());
        this.pagesList.setSelectConsumer(i -> {
            if (i == null) {
                this.tbContent.getTextBox().setText("");
            } else {
                if (manager.getPage() != null && !manager.getPage().getId().equalsIgnoreCase(i.getId())) {
                    this.clearCursorSnapshot();
                    this.restoreCursorSnapshot();
                }
                manager.requestPage(i.getId());
            }
        });

        if (this.canModify) {
            this.pagesList.setHeight(this.pagesList.getHeight() - 17);

            this.buttonAdd = new UIButtonBuilder(this)
                    .text(TextFormatting.GREEN + "+")
                    .onClick(() -> this.runScheduledTask("add"))
                    .anchor(Anchor.BOTTOM | Anchor.LEFT)
                    .position(0, 0)
                    .visible(this.canAdd)
                    .tooltip(I18n.format("almura.guide.view.button.add.tooltip"))
                    .build("button.add");

            this.buttonRemove = new UIButtonBuilder(this)
                    .text(TextFormatting.RED + "-")
                    .onClick(() -> this.runScheduledTask("remove"))
                    .anchor(Anchor.BOTTOM | Anchor.LEFT)
                    .position(BasicScreen.getPaddedX(this.buttonAdd, 2), 0)
                    .visible(this.canRemove)
                    .tooltip(I18n.format("almura.guide.view.button.remove.tooltip"))
                    .build("button.remove");

            this.buttonDetails = new UIButtonBuilder(this)
                    .text(TextFormatting.YELLOW + "?")
                    .onClick(() -> this.runScheduledTask("details"))
                    .anchor(Anchor.BOTTOM | Anchor.LEFT)
                    .visible(this.canModify)
                    .tooltip(I18n.format("almura.guide.view.button.details.tooltip"))
                    .build("button.details");
            this.buttonDetails.setPosition(BasicScreen.getPaddedX(this.pagesList, -this.buttonDetails.getWidth()) + 1, 0);
        }

        this.tbContent = new WYSIWYGTextBox(this, BasicScreen.getPaddedHeight(form), "");
        this.tbContent.setPosition(BasicScreen.getPaddedX(this.pagesList, 2), 0);
        this.tbContent.getTextBox().setEditable(this.canModify);
        this.tbContent.getTextBox().setOptions(0x555555, 0xc8c8c8, 0x00000);
        if (!this.canModify) {
            this.tbContent.hideToolbar();
        }

        final UIButton buttonClose = new UIButtonBuilder(this)
            .width(50)
            .anchor(Anchor.BOTTOM | Anchor.RIGHT)

            .text(I18n.format("almura.button.close"))
            .onClick(this::close)
            .build("button.close");

        form.add(this.pagesList, this.tbContent, this.buttonAdd, buttonRemove, buttonDetails, buttonClose);

        if (this.canModify) {
            this.tbContent.setHeight(BasicScreen.getPaddedHeight(form) - 17);

            this.buttonSave = new UIButtonBuilder(this)
                .width(50)
                .anchor(Anchor.RIGHT | Anchor.BOTTOM)
                .position(BasicScreen.getPaddedX(buttonClose, 2, Anchor.RIGHT), 0)
                //.position(-20,0)
                .text(I18n.format("almura.button.save"))
                .onClick(() -> {
                    if (manager.getPage() != null) {
                        if (!manager.preSnapshot.equalsIgnoreCase(this.tbContent.getTextBox().getText())) {
                            manager.getPage().setContent(this.tbContent.getTextBox().getText());
                            manager.requestSavePage();
                        }
                    }
                })
                .build("button.save");
            form.add(buttonSave);
        }

        this.addToScreen(form);
    }

    private void runScheduledTask(final String details) {
        // Null this so we don't see it drawing on any screens we open
        this.tooltipComponent = null;

        Sponge.getScheduler().createTaskBuilder().delayTicks(5).execute(task -> {
            switch (details.toUpperCase()) {
                case "ADD":
                    new GuidePageCreateScreen(this).display();
                    break;
                case "DETAILS":
                    new GuidePageDetailsScreen(this).display();
                    break;
                case "REMOVE":
                    if (manager.getPage() == null) {
                        return;
                    }

                    BasicMessageBox.showDialog(this, I18n.format("almura.guide.view.form.title"), "Do you wish to delete the selected guide?",
                            MessageBoxButtons.YES_NO,
                            messageBoxResult -> {
                                if (messageBoxResult == MessageBoxResult.YES && manager.getPage() != null) {
                                    manager.requestRemovePage(manager.getPage().getId());
                                }
                            });
                    break;
                case "CLOSE":
                    if (Minecraft.getMinecraft().currentScreen instanceof GuidePageViewScreen) {
                        ((GuidePageViewScreen) Minecraft.getMinecraft().currentScreen).close();
                    }
                    break;
            }
        }).submit(container);
    }

    public void close() {
        if (!canModify || manager.getPage() != null) {
            super.close();
            return;
        }

        if (manager.preSnapshot.equals(this.tbContent.getTextBox().getText())) {
            super.close();
            return;
        }

        // Save the postSnapshot to the manager so it can be accessed within manager.
        manager.postSnapshot = this.tbContent.getTextBox().getText();
        BasicMessageBox.showDialog(this, I18n.format("almura.guide.view.form.title"), "Changes detected to current guide, do you wish to save?",
                MessageBoxButtons.YES_NO_CANCEL, messageBoxResult -> {
                    if (messageBoxResult == MessageBoxResult.YES) {
                        manager.getPage().setContent(manager.postSnapshot);
                        manager.requestSavePage();
                    } else {
                        this.runScheduledTask("close");
                    }
                });
    }

    public void refreshPageEntries(final String switchToPage) {
        this.pagesList.setItems(manager.getPageEntries());

        if (switchToPage != null) {
            manager.getPageEntries()
                    .stream()
                    .filter(en -> en.getId().equalsIgnoreCase(switchToPage))
                    .findFirst()
                    .ifPresent(en -> this.pagesList.setSelectedItem(en));
        }
    }

    public void refreshPage() {
        this.snapshotCursor();
        this.tbContent.getTextBox().setText(TextUtil.asFriendlyText(manager.getPage() == null ? "" : manager.getPage().getContent()));
        this.tbContent.getTextBox().getCursorPosition().jumpToBeginning();
        manager.preSnapshot = this.tbContent.getTextBox().getText();

        this.updateButtons();
        this.restoreCursorSnapshot();
        this.clearCursorSnapshot();
    }

    private void snapshotCursor() {
        this.snapshotScrollOffset = this.tbContent.getTextBox().getScrollbar().getOffset();
        this.snapshotCursorPositionX = this.tbContent.getTextBox().getCursorPosition().getXOffset();
        this.snapshotCursorPositionY = this.tbContent.getTextBox().getCursorPosition().getYOffset();
    }

    private void restoreCursorSnapshot() {
        this.tbContent.getTextBox().focus();
        this.tbContent.getTextBox().getScrollbar().scrollTo(this.snapshotScrollOffset);
        this.tbContent.getTextBox().getCursorPosition().setPosition(this.snapshotCursorPositionX, this.snapshotCursorPositionY);
    }

    private void clearCursorSnapshot() {
        this.snapshotScrollOffset = 0;
        this.snapshotCursorPositionX = 0;
        this.snapshotCursorPositionY = 0;
    }

    private void updateButtons() {
        if (this.buttonAdd != null) {
            this.buttonAdd.setEnabled(this.canAdd);
        }

        if (this.buttonRemove != null) {
            this.buttonRemove.setEnabled((this.canRemove && manager.getPage() != null));
        }

        if (this.buttonDetails != null) {
            this.buttonDetails.setEnabled((this.canModify && manager.getPage() != null));
        }

        if (this.buttonSave != null) {
            this.buttonSave.setEnabled((this.canModify && manager.getPage() != null));
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false; // Can't stop the game otherwise the Sponge Scheduler also stops.
    }

    private static final class PageListEntryItemComponent extends BasicList.ItemComponent<PageListEntry> {

        PageListEntryItemComponent(final MalisisGui gui, final BasicList<PageListEntry> parent, final PageListEntry item) {
            super(gui, parent, item);
            this.setHeight(25);
            this.setOnDoubleClickConsumer(i -> {
                final GuidePageViewScreen parentScreen = (GuidePageViewScreen) this.getGui();
                if (parentScreen.canModify) {
                    parentScreen.runScheduledTask("details");
                }
            });
        }

        @Override
        protected void construct(final MalisisGui gui) {
            super.construct(gui);

            final UIImage icon = null;//this.item.getIcon() == null ? null : new UIImage(gui, this.item.getIcon());
            if (icon != null) {
                icon.setPosition(2, 0, Anchor.MIDDLE | Anchor.LEFT);
                this.add(icon);
            }

            // Ensure all text lines up if one happens to have an image
            final int xPos = 2; //manager.getPageEntries().stream().anyMatch(entry -> entry.getIcon() != null);

            final String[] splitName = this.item.getName().split(" - ");
            final UILabel categoryLabel = new UILabel(gui, splitName[0]);
            categoryLabel.setPosition(xPos, 0, Anchor.MIDDLE | Anchor.LEFT);
            categoryLabel.setFontOptions(FontColors.WHITE_FO);
            this.add(categoryLabel);

            if (splitName.length > 1) {
                categoryLabel.setAnchor(Anchor.TOP | Anchor.LEFT);

                final UILabel nameLabel = new UILabel(gui, splitName[1]);
                nameLabel.setPosition(xPos, BasicScreen.getPaddedY(categoryLabel, 2));
                nameLabel.setFontOptions(FontColors.GRAY_FO);
                this.add(nameLabel);
            }
        }
    }
}
