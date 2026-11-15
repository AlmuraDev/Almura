/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.client;

import com.almuradev.almura.feature.exchange.ExchangeModifyType;
import com.almuradev.almura.feature.exchange.network.ExchangeMessages.ExchangeInfo;
import com.almuradev.almura.feature.exchange.network.ExchangeMessages.ServerboundModify;
import com.almuradev.almura.feature.menu.InGameScreens;
import net.minecraft.client.Minecraft;
import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.control.attribute.item.Activatable;
import org.inspirenxe.strata.api.control.attribute.item.Selection;
import org.inspirenxe.strata.api.control.attribute.text.TextInput;
import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.button.CheckBox;
import org.inspirenxe.strata.api.control.button.CheckState;
import org.inspirenxe.strata.api.control.collection.ListView;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.control.field.TextBox;
import org.inspirenxe.strata.api.control.window.MessageBox;
import org.inspirenxe.strata.api.control.window.Window;
import org.inspirenxe.strata.api.layout.Anchor;
import org.inspirenxe.strata.api.layout.AnchorPanel;
import org.inspirenxe.strata.api.layout.HorizontalAlignment;
import org.inspirenxe.strata.api.layout.VerticalAlignment;
import org.inspirenxe.strata.api.screen.Screen;
import org.inspirenxe.strata.api.text.Text;
import org.inspirenxe.strata.api.theme.Border;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;

/**
 * Exchange - Management, as 3.5's {@code IngameFeatureManagementScreen}: the exchanges at the left (+ to add one, - to delete the selected
 * one), and the selected one's ID, title, permission, creator and when it was made at the right, with Hidden, Open and Save.
 */
final class ExchangeManagementScreen implements Screen {

    private static final float WIDTH = 375f, HEIGHT = 215f, LIST_WIDTH = 120f, DETAILS_WIDTH = 244f, FIELD_WIDTH = 140f, FIELD_HEIGHT = 16f;
    private static final float ROW = 16f, BUTTON_HEIGHT = 16f, MARGIN = 10f;
    private static final int WHITE = 0xFFFFFFFF, GREEN = 0xFF55FF55, RED = 0xFFFF5555;
    private static final Border BOX_BORDER = Border.of(0xD7FFFFFF, 1f);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withZone(ZoneId.systemDefault());

    private @Nullable ComponentFactory factory;
    private @Nullable ListView<ExchangeInfo> list;
    private @Nullable TextBox id, name, permission, creatorName, creatorId, created;
    private @Nullable Label creatorNameLabel, creatorIdLabel, createdLabel;
    private @Nullable CheckBox hidden;
    private @Nullable Button open, save;
    private boolean selecting;

    @Override
    public Text title() {
        return Text.translatable("almura.feature.common.title.management", Text.translatable("almura.feature.exchange.name"));
    }

    @Override
    public Component<?> build(final ComponentFactory factory) {
        this.factory = factory;
        final ListView<ExchangeInfo> list = factory.<ExchangeInfo>listView(exchange -> factory.anchorPanel()
            .add(factory.label(Text.of(exchange.name()).color(WHITE)).setAnchor(Anchor.MIDDLE_LEFT).setMargin(4f, 0f, 0f, 0f))
            .setAnchor(Anchor.FILL).setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.STRETCH));
        list.setRowHeight(20f);
        list.onEvent(Selection.SELECTED, event -> {
            if (!this.selecting) {
                this.show(list.selected().orElse(null));
            }
        });
        // A double click opens it, as 3.5
        list.onEvent(Activatable.ACTIVATED, event -> this.openSelected());
        this.list = list;

        this.id = field(factory, true);
        this.name = field(factory, false);
        this.permission = field(factory, true);
        this.creatorName = field(factory, false).setReadOnly(true);
        this.creatorId = field(factory, false).setReadOnly(true);
        this.created = field(factory, false).setReadOnly(true);
        this.creatorNameLabel = label(factory, "almura.feature.common.text.creator_name");
        this.creatorIdLabel = label(factory, "almura.feature.common.text.creator_uuid");
        this.createdLabel = label(factory, "almura.feature.common.text.created");
        this.hidden = factory.checkBox(Text.translatable("almura.feature.common.text.hidden").color(WHITE));
        this.hidden.onEvent(CheckBox.CHANGED, event -> this.updateControls());
        this.save = ExchangeScreen.button(factory, Text.translatable("almura.button.save"), 40f, this::confirmSave);
        this.open = ExchangeScreen.button(factory, Text.translatable("almura.button.open"), 40f, this::openSelected);

        final AnchorPanel details = factory.anchorPanel().setBorder(BOX_BORDER).setPadding(4f);
        float y = 0f;
        for (final Component<?>[] pair : List.of(
            new Component<?>[] {label(factory, "almura.feature.common.text.id"), this.id},
            new Component<?>[] {label(factory, "almura.feature.common.text.title"), this.name},
            new Component<?>[] {label(factory, "almura.feature.common.text.permission"), this.permission},
            new Component<?>[] {this.creatorNameLabel, this.creatorName},
            new Component<?>[] {this.creatorIdLabel, this.creatorId},
            new Component<?>[] {this.createdLabel, this.created})) {
            details.add(pair[0].setAnchor(Anchor.TOP_LEFT).setMargin(0f, y + 4f, 0f, 0f))
                .add(pair[1].setAnchor(Anchor.TOP_RIGHT).setMargin(0f, y, 0f, 0f));
            y += ROW + 2f;
        }
        details.add(this.hidden.setAnchor(Anchor.BOTTOM_LEFT))
            .add(this.save.setAnchor(Anchor.BOTTOM_RIGHT))
            .add(this.open.setAnchor(Anchor.BOTTOM_RIGHT).setMargin(0f, 0f, 42f, 0f));

        final Button add = small(factory, "+", GREEN, () -> {
            list.clearSelection();
            this.show(null);
        });
        final Button delete = small(factory, "-", RED, this::confirmDelete);
        final Button close = ExchangeScreen.button(factory, Text.translatable("almura.button.close"), 40f,
            () -> Minecraft.getInstance().gui.screen().onClose());

        final AnchorPanel content = factory.anchorPanel()
            .add(factory.anchorPanel().setBorder(BOX_BORDER).setPadding(2f).add(list.setAnchor(Anchor.FILL))
                .setAnchor(Anchor.FILL).setMargin(1f, 0f, 0f, BUTTON_HEIGHT + 6f)
                .setAlignment(HorizontalAlignment.LEFT, VerticalAlignment.STRETCH).setWidth(LIST_WIDTH))
            .add(details.setAnchor(Anchor.FILL).setMargin(0f, 0f, 1f, BUTTON_HEIGHT + 6f)
                .setAlignment(HorizontalAlignment.RIGHT, VerticalAlignment.STRETCH).setWidth(DETAILS_WIDTH))
            .add(add.setAnchor(Anchor.BOTTOM_LEFT))
            .add(delete.setAnchor(Anchor.BOTTOM_LEFT).setMargin(17f, 0f, 0f, 0f))
            .add(close.setAnchor(Anchor.BOTTOM_RIGHT));

        final Minecraft minecraft = Minecraft.getInstance();
        final Window window = factory.window(this.title())
            .setMovable(true)
            .setResizable(false)
            .setCollapsible(false)
            .setMaximizable(false)
            .add(content.setAnchor(Anchor.FILL))
            .setSize(Math.min(WIDTH, minecraft.getWindow().getGuiScaledWidth() - MARGIN),
                Math.min(HEIGHT, minecraft.getWindow().getGuiScaledHeight() - MARGIN))
            .setAnchor(Anchor.CENTER);
        InGameScreens.closesScreen(window);
        this.refresh();
        return factory.anchorPanel().add(window).setAnchor(Anchor.FILL);
    }

    /**
     * The exchanges again: keeps the one selected (or the one being added, once it exists), else the first.
     */
    void refresh() {
        if (this.list == null || this.id == null) {
            return;
        }
        final String last = this.list.selected().map(ExchangeInfo::id).orElse(this.id.text().toLowerCase(Locale.ROOT));
        final List<ExchangeInfo> exchanges = ClientExchange.exchanges();
        final ExchangeInfo select = exchanges.stream().filter(exchange -> exchange.id().equalsIgnoreCase(last)).findFirst()
            .orElse(exchanges.isEmpty() ? null : exchanges.getFirst());
        this.selecting = true;
        try {
            this.list.setItems(exchanges);
            this.list.refresh();
            if (select != null) {
                this.list.selectItem(select);
            }
        } finally {
            this.selecting = false;
        }
        this.show(select);
    }

    private void show(final @Nullable ExchangeInfo exchange) {
        if (this.id == null || this.name == null || this.permission == null || this.creatorName == null || this.creatorId == null
            || this.created == null || this.hidden == null || this.creatorNameLabel == null || this.creatorIdLabel == null
            || this.createdLabel == null || this.open == null) {
            return;
        }
        final boolean existing = exchange != null;
        this.id.setText(existing ? exchange.id() : "").setReadOnly(existing);
        this.name.setText(existing ? exchange.name() : "");
        this.permission.setText(existing ? exchange.permission() : "");
        this.creatorName.setText(existing ? (exchange.creatorName().isEmpty() ? "Unknown" : exchange.creatorName()) : "");
        this.creatorId.setText(existing ? exchange.creator().toString() : "");
        this.created.setText(existing ? DATE.format(Instant.ofEpochMilli(exchange.created())) : "");
        // Shown from their start: setText leaves the caret at the end, scrolling a long value (the creator's UUID) to its end
        this.id.select(0, 0);
        this.name.select(0, 0);
        this.permission.select(0, 0);
        this.creatorName.select(0, 0);
        this.creatorId.select(0, 0);
        this.created.select(0, 0);
        for (final Component<?> extra : List.of(this.creatorName, this.creatorId, this.created, this.creatorNameLabel, this.creatorIdLabel,
            this.createdLabel)) {
            extra.setVisible(existing);
        }
        this.hidden.setState(existing && exchange.hidden() ? CheckState.CHECKED : CheckState.UNCHECKED);
        this.open.setEnabled(existing);
        this.updateControls();
    }

    private void updateControls() {
        if (this.id == null || this.name == null || this.permission == null || this.save == null || this.list == null) {
            return;
        }
        boolean valid = !this.id.text().isEmpty() && !this.name.text().isEmpty() && !this.permission.text().isEmpty();
        // A new exchange needs an ID nobody has
        if (valid && this.list.selected().isEmpty()) {
            valid = ClientExchange.exchanges().stream().noneMatch(exchange -> exchange.id().equalsIgnoreCase(this.id.text()));
        }
        this.save.setEnabled(valid);
    }

    private void openSelected() {
        if (this.list != null) {
            this.list.selected().ifPresent(exchange -> ClientExchange.requestOpen(exchange.id()));
        }
    }

    private void confirmSave() {
        if (this.factory == null || this.list == null || this.save == null || !this.save.isEnabled() || this.id == null || this.name == null
            || this.permission == null || this.hidden == null) {
            return;
        }
        final boolean isNew = this.list.selected().isEmpty();
        final ServerboundModify change = new ServerboundModify(isNew ? ExchangeModifyType.ADD : ExchangeModifyType.MODIFY, this.id.text(),
            this.name.text(), this.permission.text(), this.hidden.state() == CheckState.CHECKED);
        this.ask(Text.translatable("almura.feature.common.text." + (isNew ? "add" : "modify") + "_feature",
            Text.translatable("almura.feature.exchange.name")), () -> ClientExchange.send(change));
    }

    private void confirmDelete() {
        if (this.list == null) {
            return;
        }
        this.list.selected().ifPresent(exchange -> this.ask(Text.translatable("almura.feature.common.text.delete_feature", exchange.id(),
            Text.translatable("almura.feature.exchange.name")),
            () -> ClientExchange.send(new ServerboundModify(ExchangeModifyType.DELETE, exchange.id(), "", "", false))));
    }

    private void ask(final Text message, final Runnable yes) {
        if (this.factory == null) {
            return;
        }
        this.factory.overlays().dialog(this.factory.messageBox(Text.translatable("almura.feature.common.title.are_you_sure"), message,
            MessageBox.Buttons.YES_NO).onEvent(MessageBox.ANSWERED, event -> {
                if (event.value().equals(MessageBox.Result.YES)) {
                    yes.run();
                }
            }));
    }

    /**
     * A field: the ID and permission take a-z, 0-9, _ and . only, lower-cased, as 3.5's.
     */
    private TextBox field(final ComponentFactory factory, final boolean idLike) {
        final TextBox field = factory.textBox().setMaxLength(255);
        if (idLike) {
            field.onEvent(TextInput.EDIT, event -> event.setValue(event.value().replaceAll("[^A-Za-z0-9_.]", "").toLowerCase(Locale.ROOT)));
        }
        field.onEvent(TextInput.EDITED, event -> this.updateControls());
        field.onEvent(TextBox.SUBMITTED, event -> this.confirmSave());
        field.setSize(FIELD_WIDTH, FIELD_HEIGHT);
        return field;
    }

    private static Label label(final ComponentFactory factory, final String key) {
        return factory.label(Text.translatable(key).append(Text.of(":")).color(WHITE));
    }

    private static Button small(final ComponentFactory factory, final String text, final int colour, final Runnable press) {
        final Button button = factory.button(Text.of(text).color(colour)).setTextPadding(2f, 4f, 2f, 2f);
        button.setMinSize(0f, 0f);
        button.setSize(15f, BUTTON_HEIGHT);
        return button.onEvent(Button.PRESSED, event -> press.run());
    }
}
