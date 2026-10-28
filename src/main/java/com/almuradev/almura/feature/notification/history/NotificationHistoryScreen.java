/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.notification.history;

import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.BasicScreen;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.container.BasicContainer;
import net.malisis.core.client.gui.component.container.BasicForm;
import net.malisis.core.client.gui.component.container.BasicList;
import net.malisis.core.client.gui.component.container.dialog.BasicMessageBox;
import net.malisis.core.client.gui.component.container.dialog.MessageBoxButtons;
import net.malisis.core.client.gui.component.container.dialog.MessageBoxResult;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.decoration.UISeparator;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.button.builder.UIButtonBuilder;
import net.malisis.core.util.FontColors;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * Lets the player review the notifications they received: a list on the left, the selected message on the right.
 */
@SideOnly(Side.CLIENT)
public final class NotificationHistoryScreen extends BasicScreen {

    private static final int FORM_WIDTH = 420;
    private static final int FORM_HEIGHT = 250;
    private static final int LIST_WIDTH = 170;
    private static final DateTimeFormatter LIST_TIME = DateTimeFormatter.ofPattern("MMM d, h:mm a").withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter DETAIL_TIME = DateTimeFormatter.ofPattern("EEE, MMM d yyyy 'at' h:mm:ss a").withZone(ZoneId.systemDefault());

    @Inject private static ClientNotificationHistory history;

    private BasicList<NotificationRecord> recordList;
    private UILabel emptyLabel, titleLabel, receivedLabel, messageLabel;
    private UISeparator detailSeparator;
    private UIButton clearButton;

    public NotificationHistoryScreen(@Nullable final GuiScreen parent) {
        super(parent);
    }

    @Override
    public void construct() {
        this.guiscreenBackground = false;

        final BasicForm form = new BasicForm(FORM_WIDTH, FORM_HEIGHT, "Notifications");

        // Received notifications, newest first
        this.recordList = new BasicList<>(LIST_WIDTH, BasicScreen.getPaddedHeight(form) - 22);
        this.recordList.setPosition(0, 0);
        this.recordList.setItemComponentSpacing(1);
        this.recordList.setItemComponentFactory(RecordItemComponent::new);
        this.recordList.setSelectConsumer(this::showRecord);

        this.emptyLabel = new UILabel(TextFormatting.GRAY + "No notifications received yet.");
        this.emptyLabel.setFontOptions(FontColors.WHITE_FO);
        this.emptyLabel.setPosition((LIST_WIDTH - this.emptyLabel.getWidth()) / 2, 20);

        // Selected notification
        final int detailX = LIST_WIDTH + 6;
        final int detailWidth = BasicScreen.getPaddedWidth(form) - detailX;
        final BasicContainer<?> detail = new BasicContainer<>(detailWidth, BasicScreen.getPaddedHeight(form) - 22);
        detail.setPosition(detailX, 0);
        detail.setColor(0x000000);
        detail.setBorder(0x808080, 1, 255);
        detail.setPadding(4, 4);

        this.titleLabel = new UILabel("");
        this.titleLabel.setFontOptions(FontColors.WHITE_FO);
        this.titleLabel.setPosition(0, 0);

        this.receivedLabel = new UILabel("");
        this.receivedLabel.setFontOptions(FontColors.WHITE_FO);
        this.receivedLabel.setPosition(0, 11);

        this.detailSeparator = new UISeparator(this);
        this.detailSeparator.setSize(BasicScreen.getPaddedWidth(detail), 1);
        this.detailSeparator.setPosition(0, 23);

        this.messageLabel = new UILabel(true);
        this.messageLabel.setFontOptions(FontColors.WHITE_FO);
        this.messageLabel.setPosition(0, 28);
        this.messageLabel.setSize(BasicScreen.getPaddedWidth(detail), BasicScreen.getPaddedHeight(detail) - 28);

        detail.add(this.titleLabel, this.receivedLabel, this.detailSeparator, this.messageLabel);

        this.clearButton = new UIButtonBuilder()
                .width(80)
                .anchor(Anchor.BOTTOM | Anchor.LEFT)
                .text("Clear History")
                .onClick(this::confirmClear)
                .build("button.clear");

        final UIButton closeButton = new UIButtonBuilder()
                .width(40)
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .text(I18n.format("almura.button.close"))
                .onClick(this::close)
                .build("button.close");

        form.add(this.recordList, this.emptyLabel, detail, this.clearButton, closeButton);
        addToScreen(form);

        this.refresh();
    }

    private void refresh() {
        final List<NotificationRecord> records = history.getRecords();
        this.recordList.setItems(records);
        this.emptyLabel.setVisible(records.isEmpty());
        this.clearButton.setEnabled(!records.isEmpty());

        if (records.isEmpty()) {
            this.showRecord(null);
        } else {
            this.recordList.setSelectedItem(records.get(0));
            this.showRecord(records.get(0));
        }
    }

    private void showRecord(@Nullable final NotificationRecord record) {
        final boolean present = record != null;
        this.titleLabel.setVisible(present);
        this.receivedLabel.setVisible(present);
        this.detailSeparator.setVisible(present);
        this.messageLabel.setVisible(present);
        if (!present) {
            return;
        }

        this.titleLabel.setText(record.getTitle().isEmpty() ? TextFormatting.WHITE + "Notification" : record.getTitle());
        final String server = record.getServer().isEmpty() ? "" : TextFormatting.DARK_GRAY + " (" + record.getServer() + ")";
        this.receivedLabel.setText(TextFormatting.GRAY + DETAIL_TIME.format(record.getReceived()) + server);
        this.messageLabel.setText(TextFormatting.WHITE + record.getMessage());
    }

    private void confirmClear() {
        BasicMessageBox.showDialog(this, "Clear History", "Delete all saved notifications? This can't be undone.", MessageBoxButtons.YES_NO,
                result -> {
                    if (result == MessageBoxResult.YES) {
                        history.clear();
                        this.refresh();
                    }
                });
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false; // Can't stop the game otherwise the Sponge Scheduler also stops.
    }

    private static final class RecordItemComponent extends BasicList.ItemComponent<NotificationRecord> {

        RecordItemComponent(final MalisisGui gui, final BasicList<NotificationRecord> parent, final NotificationRecord item) {
            super(gui, parent, item);
            this.setHeight(24);
        }

        @Override
        protected void construct(final MalisisGui gui) {
            super.construct(gui);

            final UILabel timeLabel = new UILabel(TextFormatting.GRAY + LIST_TIME.format(this.item.getReceived()));
            timeLabel.setFontOptions(FontColors.WHITE_FO);
            timeLabel.setPosition(2, 0);

            // First line of the title (or message when there's no title), cut to fit the row
            final String text = this.item.getTitle().isEmpty() ? this.item.getMessage() : this.item.getTitle();
            final UILabel titleLabel = new UILabel(TextFormatting.WHITE + firstLine(text));
            titleLabel.setFontOptions(FontColors.WHITE_FO);
            titleLabel.setPosition(2, 10);

            this.add(timeLabel, titleLabel);
        }

        private static String firstLine(final String text) {
            final String plain = TextFormatting.getTextWithoutFormattingCodes(text);
            final int newline = plain.indexOf('\n');
            final String line = (newline >= 0 ? plain.substring(0, newline) : plain).trim();
            return line.length() > 27 ? line.substring(0, 26) + "..." : line;
        }
    }
}
