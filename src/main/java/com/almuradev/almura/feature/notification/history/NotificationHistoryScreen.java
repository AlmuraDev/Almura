/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.notification.history;

import com.almuradev.almura.Almura;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmScreen;
import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.control.attribute.item.Selection;
import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.collection.ListView;
import org.inspirenxe.strata.api.control.display.Image;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.control.window.Window;
import org.inspirenxe.strata.api.layout.Anchor;
import org.inspirenxe.strata.api.layout.AnchorPanel;
import org.inspirenxe.strata.api.layout.HorizontalAlignment;
import org.inspirenxe.strata.api.layout.ScrollPanel;
import org.inspirenxe.strata.api.layout.StackPanel;
import org.inspirenxe.strata.api.layout.VerticalAlignment;
import org.inspirenxe.strata.api.screen.Screen;
import org.inspirenxe.strata.api.text.Text;
import org.inspirenxe.strata.api.text.TextOverflow;
import org.inspirenxe.strata.api.theme.SpriteId;
import org.inspirenxe.strata.minecraft.MinecraftText;
import org.jspecify.annotations.Nullable;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * The notifications the player has received, from the Features menu (F12), as on 1.12: newest first in a list on the left (when, and the
 * title or the message's first line), the selected one in full beside it (title, when and from which server, and the message), with Clear
 * History in the bottom left and Close in the bottom right.
 */
public final class NotificationHistoryScreen implements Screen {

    private static final SpriteId PANEL = SpriteId.of(Almura.ID, "theme/window");
    private static final DateTimeFormatter LIST_TIME = DateTimeFormatter.ofPattern("MMM d, h:mm a").withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter DETAIL_TIME = DateTimeFormatter.ofPattern("EEE, MMM d yyyy 'at' h:mm:ss a").withZone(ZoneId.systemDefault());

    // 1.12's sizes
    private static final float WIDTH = 420f, CONTENT_HEIGHT = 200f, LIST_WIDTH = 170f, ROW_HEIGHT = 24f, GAP = 6f, PADDING = 4f;
    private static final float BUTTON_HEIGHT = 16f, CLEAR_WIDTH = 80f, CLOSE_WIDTH = 40f, BOTTOM_GAP = 3f;
    private static final int WHITE = 0xFFFFFFFF, GRAY = 0xFFAAAAAA, DARK_GRAY = 0xFF555555;

    private @Nullable ListView<NotificationRecord> list;
    private @Nullable Label empty, title, received, message;
    private @Nullable Component<?> detailBody;
    private @Nullable Button clear;

    @Override
    public Text title() {
        return Text.of("Notifications");
    }

    @Override
    public Component<?> build(final ComponentFactory factory) {
        this.list = factory.<NotificationRecord>listView(record -> row(factory, record));
        this.list.setRowHeight(ROW_HEIGHT);
        this.list.onEvent(Selection.SELECTED, event -> this.show(this.list.selected().orElse(null)));
        this.empty = factory.label(Text.of("No notifications received yet.").color(GRAY));
        final AnchorPanel listArea = factory.anchorPanel()
            .add(this.list.setAnchor(Anchor.FILL))
            .add(this.empty.setAnchor(Anchor.TOP_CENTER).setMargin(0f, 20f, 0f, 0f));
        listArea.setSize(LIST_WIDTH, CONTENT_HEIGHT);

        // The selected notification: title, when and where from, a line, then the message (scrolling if long)
        this.title = factory.label("").setOverflow(TextOverflow.ELLIPSIS);
        this.received = factory.label("").setOverflow(TextOverflow.ELLIPSIS);
        this.message = factory.label("").setWrap(true);
        final ScrollPanel messageScroll = factory.scrollPanel();
        messageScroll.add(this.message.setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP));
        final StackPanel heading = factory.stackPanel().setSpacing(2f)
            .add(stretched(this.title))
            .add(stretched(this.received))
            .add(stretched(factory.separator()));
        final AnchorPanel body = factory.anchorPanel()
            .add(heading.setAnchor(Anchor.TOP_LEFT).setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP))
            .add(messageScroll.setAnchor(Anchor.FILL).setMargin(0f, 28f, 0f, 0f));
        this.detailBody = body;
        final Image background = factory.image(PANEL).setImageSize(1f, 1f).setScaling(Image.Scaling.STRETCH);
        final AnchorPanel detail = factory.anchorPanel()
            .add(background.setAnchor(Anchor.FILL))
            .add(body.setAnchor(Anchor.FILL).setMargin(PADDING));
        detail.setHeight(CONTENT_HEIGHT);

        this.clear = factory.button(Text.of("Clear History")).setSize(CLEAR_WIDTH, BUTTON_HEIGHT)
            .onEvent(Button.PRESSED, event -> this.confirmClear());
        final Button close = factory.button(Text.translatable("almura.button.close")).setSize(CLOSE_WIDTH, BUTTON_HEIGHT)
            .onEvent(Button.PRESSED, event -> Minecraft.getInstance().gui.screen().onClose());

        final AnchorPanel content = factory.anchorPanel()
            .add(listArea.setAnchor(Anchor.TOP_LEFT))
            .add(detail.setAnchor(Anchor.FILL).setMargin(LIST_WIDTH + GAP, 0f, 0f, BUTTON_HEIGHT + BOTTOM_GAP))
            .add(this.clear.setAnchor(Anchor.BOTTOM_LEFT))
            .add(close.setAnchor(Anchor.BOTTOM_RIGHT));
        content.setHeight(CONTENT_HEIGHT + BOTTOM_GAP + BUTTON_HEIGHT);

        final Window window = factory.window(this.title())
            .setClosable(false)
            .setMovable(false)
            .setResizable(false)
            .setCollapsible(false)
            .setMaximizable(false)
            .add(stretched(content))
            .setWidth(WIDTH)
            .setAnchor(Anchor.CENTER);

        this.refresh();
        return factory.anchorPanel().add(window).setAnchor(Anchor.FILL);
    }

    private void refresh() {
        final List<NotificationRecord> records = NotificationHistory.records();
        this.list.setItems(records);
        this.empty.setVisible(records.isEmpty());
        this.clear.setEnabled(!records.isEmpty());
        if (records.isEmpty()) {
            this.show(null);
        } else {
            this.list.selectItem(records.getFirst());
            this.show(records.getFirst());
        }
    }

    private void show(final @Nullable NotificationRecord record) {
        this.detailBody.setVisible(record != null);
        if (record == null) {
            return;
        }
        this.title.setText(record.title().isEmpty() ? Text.of("Notification").color(WHITE) : MinecraftText.of("§f" + record.title()));
        Text when = Text.of(DETAIL_TIME.format(record.received())).color(GRAY);
        if (!record.server().isEmpty()) {
            when = Text.concat(when, Text.of(" (" + record.server() + ")").color(DARK_GRAY));
        }
        this.received.setText(when);
        this.message.setText(MinecraftText.of("§f" + record.message()));
    }

    /**
     * Asks first, as clearing can't be undone, with vanilla's yes and no.
     */
    private void confirmClear() {
        final Minecraft minecraft = Minecraft.getInstance();
        final net.minecraft.client.gui.screens.Screen host = minecraft.gui.screen();
        minecraft.gui.setScreen(new ConfirmScreen(yes -> {
            if (yes) {
                NotificationHistory.clear();
            }
            minecraft.gui.setScreen(host);
        }, net.minecraft.network.chat.Component.literal("Clear History"),
            net.minecraft.network.chat.Component.literal("Delete all saved notifications? This can't be undone.")));
    }

    /**
     * A list row: when, then the title (or the message when there is none), its first line only.
     */
    private static Component<?> row(final ComponentFactory factory, final NotificationRecord record) {
        final String text = record.title().isEmpty() ? record.message() : record.title();
        final StackPanel lines = factory.stackPanel().setSpacing(1f)
            .add(line(factory, Text.of(LIST_TIME.format(record.received())).color(GRAY)))
            .add(line(factory, Text.of(firstLine(text)).color(WHITE)));
        lines.setHitTestVisible(false);
        return factory.anchorPanel().add(lines.setAnchor(Anchor.FILL).setMargin(2f, 2f, 2f, 0f)).setAnchor(Anchor.FILL);
    }

    private static Label line(final ComponentFactory factory, final Text text) {
        return factory.label(text).setOverflow(TextOverflow.ELLIPSIS).setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP);
    }

    private static String firstLine(final String text) {
        final String plain = ChatFormatting.stripFormatting(text);
        final String safe = plain == null ? "" : plain;
        final int newline = safe.indexOf('\n');
        return (newline >= 0 ? safe.substring(0, newline) : safe).trim();
    }

    private static <T extends Component<T>> T stretched(final T component) {
        return component.setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP);
    }
}
