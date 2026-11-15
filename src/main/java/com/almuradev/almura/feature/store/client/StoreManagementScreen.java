/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.store.client;

import com.almuradev.almura.feature.menu.InGameScreens;
import com.almuradev.almura.feature.store.network.StoreMessages.ModifyType;
import com.almuradev.almura.feature.store.network.StoreMessages.ServerboundModifyStore;
import com.almuradev.almura.feature.store.network.StoreMessages.StoreInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
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
 * Store management, as 3.5's ({@code IngameFeatureManagementScreen} for stores): the stores at the left (+ to add one, - to delete the one
 * selected), and the selected store's ID, title, permission and whether it is hidden to change, who made it and when, Open and Save.
 */
final class StoreManagementScreen implements Screen {

    // 3.5's sizes
    private static final float WIDTH = 375f, HEIGHT = 215f, LIST_WIDTH = 120f, DETAILS_WIDTH = 244f, FIELD_WIDTH = 140f;
    private static final float BUTTON_HEIGHT = 16f, SMALL_WIDTH = 12f, SMALL_HEIGHT = 15f, ROW_HEIGHT = 20f, MARGIN = 10f;
    private static final int WHITE = 0xFFFFFFFF, GREEN = 0xFF55FF55, RED = 0xFFFF5555;
    private static final Border BOX_BORDER = Border.of(0xD7FFFFFF, 1f);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withZone(ZoneId.systemDefault());

    private @Nullable ComponentFactory factory;
    private @Nullable ListView<StoreInfo> list;
    private @Nullable TextBox id, title, permission, creatorName, creatorId, created;
    private @Nullable Label creatorNameLabel, creatorIdLabel, createdLabel;
    private @Nullable CheckBox hidden;
    private @Nullable Button open, save;
    private boolean selecting;

    @Override
    public Text title() {
        return Text.translatable("almura.feature.common.title.management", Text.translatable("almura.feature.store.name"));
    }

    @Override
    public Component<?> build(final ComponentFactory factory) {
        this.factory = factory;
        final ListView<StoreInfo> list = factory.<StoreInfo>listView(store -> factory.anchorPanel()
            .add(factory.label(Text.of(store.name()).color(WHITE)).setHitTestVisible(false).setAnchor(Anchor.MIDDLE_LEFT).setMargin(4f, 0f, 0f, 0f))
            .setAnchor(Anchor.FILL).setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.STRETCH));
        list.setRowHeight(ROW_HEIGHT);
        list.onEvent(Selection.SELECTED, event -> {
            if (!this.selecting) {
                this.show(list.selected().orElse(null));
            }
        });
        // A double click opens the store, as 3.5
        list.onEvent(Activatable.ACTIVATED, event -> this.openStore());
        this.list = list;

        // The store's ID, title and permission: a-z, 0-9, _ and . (the ID only for a new store)
        this.id = field(factory, true);
        this.title = factory.textBox().setMaxLength(255);
        this.title.setSize(FIELD_WIDTH, BUTTON_HEIGHT);
        this.title.onEvent(TextInput.EDITED, event -> this.updateButtons());
        this.title.onEvent(TextBox.SUBMITTED, event -> this.save());
        this.permission = field(factory, true);
        this.creatorName = readOnly(factory);
        this.creatorId = readOnly(factory);
        this.created = readOnly(factory);
        this.creatorNameLabel = label(factory, "almura.feature.common.text.creator_name");
        this.creatorIdLabel = label(factory, "almura.feature.common.text.creator_uuid");
        this.createdLabel = label(factory, "almura.feature.common.text.created");
        this.hidden = factory.checkBox(Text.translatable("almura.feature.common.text.hidden").color(WHITE));
        this.hidden.onEvent(CheckBox.CHANGED, event -> this.updateButtons());
        this.open = button(factory, Text.translatable("almura.button.open"), 40f, this::openStore);
        this.save = button(factory, Text.translatable("almura.button.save"), 40f, this::save);

        final float row = BUTTON_HEIGHT + 2f;
        final AnchorPanel details = factory.anchorPanel().setBorder(BOX_BORDER).setPadding(4f)
            .add(label(factory, "almura.feature.common.text.id").setAnchor(Anchor.TOP_LEFT).setMargin(0f, 4f, 0f, 0f))
            .add(this.id.setAnchor(Anchor.TOP_RIGHT))
            .add(label(factory, "almura.feature.common.text.title").setAnchor(Anchor.TOP_LEFT).setMargin(0f, row + 4f, 0f, 0f))
            .add(this.title.setAnchor(Anchor.TOP_RIGHT).setMargin(0f, row, 0f, 0f))
            .add(label(factory, "almura.feature.common.text.permission").setAnchor(Anchor.TOP_LEFT).setMargin(0f, row * 2 + 4f, 0f, 0f))
            .add(this.permission.setAnchor(Anchor.TOP_RIGHT).setMargin(0f, row * 2, 0f, 0f))
            .add(this.creatorNameLabel.setAnchor(Anchor.TOP_LEFT).setMargin(0f, row * 3 + 4f, 0f, 0f))
            .add(this.creatorName.setAnchor(Anchor.TOP_RIGHT).setMargin(0f, row * 3, 0f, 0f))
            .add(this.creatorIdLabel.setAnchor(Anchor.TOP_LEFT).setMargin(0f, row * 4 + 4f, 0f, 0f))
            .add(this.creatorId.setAnchor(Anchor.TOP_RIGHT).setMargin(0f, row * 4, 0f, 0f))
            .add(this.createdLabel.setAnchor(Anchor.TOP_LEFT).setMargin(0f, row * 5 + 4f, 0f, 0f))
            .add(this.created.setAnchor(Anchor.TOP_RIGHT).setMargin(0f, row * 5, 0f, 0f))
            .add(this.hidden.setAnchor(Anchor.BOTTOM_LEFT).setMargin(0f, 0f, 0f, 2f))
            .add(this.save.setAnchor(Anchor.BOTTOM_RIGHT))
            .add(this.open.setAnchor(Anchor.BOTTOM_RIGHT).setMargin(0f, 0f, 42f, 0f));

        final Button add = small(factory, "+", GREEN, () -> {
            this.selecting = true;
            try {
                list.clearSelection();
            } finally {
                this.selecting = false;
            }
            this.show(null);
        });
        final Button delete = small(factory, "-", RED, this::confirmDelete);
        final AnchorPanel content = factory.anchorPanel()
            .add(factory.anchorPanel().setBorder(BOX_BORDER).setPadding(2f)
                .add(list.setAnchor(Anchor.FILL))
                .setAnchor(Anchor.FILL).setMargin(1f, 0f, 0f, BUTTON_HEIGHT + 6f)
                .setAlignment(HorizontalAlignment.LEFT, VerticalAlignment.STRETCH).setWidth(LIST_WIDTH))
            .add(details.setAnchor(Anchor.FILL).setMargin(0f, 0f, 1f, BUTTON_HEIGHT + 6f)
                .setAlignment(HorizontalAlignment.RIGHT, VerticalAlignment.STRETCH).setWidth(DETAILS_WIDTH))
            .add(add.setAnchor(Anchor.BOTTOM_LEFT))
            .add(delete.setAnchor(Anchor.BOTTOM_LEFT).setMargin(SMALL_WIDTH + 2f, 0f, 0f, 0f))
            .add(button(factory, Text.translatable("almura.button.close"), 40f, () -> Minecraft.getInstance().gui.screen().onClose())
                .setAnchor(Anchor.BOTTOM_RIGHT));

        final Minecraft minecraft = Minecraft.getInstance();
        final Window window = factory.window(this.title())
            .setMovable(false)
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
     * The stores again: keeps the one selected (or the ID typed for a new one), else selects the first.
     */
    void refresh() {
        if (this.list == null || this.id == null) {
            return;
        }
        final String keep = this.list.selected().map(StoreInfo::id).orElse(this.id.text().toLowerCase(Locale.ROOT));
        final List<StoreInfo> stores = ClientStore.stores();
        this.selecting = true;
        StoreInfo select = null;
        try {
            this.list.setItems(stores);
            this.list.refresh();
            select = stores.stream().filter(store -> store.id().equalsIgnoreCase(keep)).findAny()
                .orElse(stores.isEmpty() ? null : stores.getFirst());
            if (select != null) {
                this.list.selectItem(select);
            }
        } finally {
            this.selecting = false;
        }
        this.show(select);
    }

    /**
     * Shows a store's details, or (null) empty fields for a new store.
     */
    private void show(final @Nullable StoreInfo store) {
        if (this.id == null || this.title == null || this.permission == null || this.creatorName == null || this.creatorId == null
            || this.created == null || this.creatorNameLabel == null || this.creatorIdLabel == null || this.createdLabel == null
            || this.hidden == null || this.open == null) {
            return;
        }
        final boolean isNew = store == null;
        this.id.setText(isNew ? "" : store.id());
        this.id.setReadOnly(!isNew);
        this.title.setText(isNew ? "" : store.name());
        this.permission.setText(isNew ? "" : store.permission());
        this.creatorName.setText(isNew ? "" : store.creatorName().isEmpty()
            ? I18n.get("almura.feature.common.text.unknown") : store.creatorName());
        this.creatorId.setText(isNew ? "" : store.creator().toString());
        this.created.setText(isNew ? "" : DATE.format(Instant.ofEpochMilli(store.created())));
        // Shown from their start: setText leaves the caret at the end, scrolling a long value (the creator's UUID) to its end
        this.id.select(0, 0);
        this.title.select(0, 0);
        this.permission.select(0, 0);
        this.creatorName.select(0, 0);
        this.creatorId.select(0, 0);
        this.created.select(0, 0);
        for (final Component<?> detail : new Component<?>[] {this.creatorName, this.creatorId, this.created, this.creatorNameLabel,
            this.creatorIdLabel, this.createdLabel}) {
            detail.setVisible(!isNew);
        }
        this.hidden.setState(!isNew && store.hidden() ? CheckState.CHECKED : CheckState.UNCHECKED);
        this.open.setEnabled(!isNew);
        this.updateButtons();
    }

    private @Nullable StoreInfo selected() {
        return this.list == null ? null : this.list.selected().orElse(null);
    }

    /**
     * Save, once an ID, title and permission are entered (and a new store's ID isn't taken).
     */
    private void updateButtons() {
        if (this.id == null || this.title == null || this.permission == null || this.save == null) {
            return;
        }
        boolean valid = !this.id.text().isEmpty() && !this.title.text().isBlank() && !this.permission.text().isEmpty();
        if (this.selected() == null && valid) {
            valid = ClientStore.stores().stream().noneMatch(store -> store.id().equalsIgnoreCase(this.id.text()));
        }
        this.save.setEnabled(valid);
    }

    private void openStore() {
        final StoreInfo store = this.selected();
        if (store != null) {
            ClientStore.requestOpen(store.id());
        }
    }

    private void save() {
        if (this.factory == null || this.save == null || !this.save.isEnabled() || this.id == null || this.title == null
            || this.permission == null || this.hidden == null) {
            return;
        }
        final boolean isNew = this.selected() == null;
        final ServerboundModifyStore change = new ServerboundModifyStore(isNew ? ModifyType.ADD : ModifyType.MODIFY, this.id.text(),
            this.title.text().trim(), this.permission.text(), this.hidden.state() == CheckState.CHECKED);
        this.factory.overlays().dialog(this.factory.messageBox(Text.translatable("almura.feature.common.title.are_you_sure"),
                Text.translatable("almura.feature.common.text." + (isNew ? "add" : "modify") + "_feature",
                    Text.translatable("almura.feature.store.name")), MessageBox.Buttons.YES_NO)
            .onEvent(MessageBox.ANSWERED, event -> {
                if (event.value().equals(MessageBox.Result.YES)) {
                    ClientStore.send(change);
                }
            }));
    }

    private void confirmDelete() {
        final StoreInfo store = this.selected();
        if (this.factory == null || store == null) {
            return;
        }
        this.factory.overlays().dialog(this.factory.messageBox(Text.translatable("almura.feature.common.title.are_you_sure"),
                Text.translatable("almura.feature.common.text.delete_feature", store.id(), Text.translatable("almura.feature.store.name")),
                MessageBox.Buttons.YES_NO)
            .onEvent(MessageBox.ANSWERED, event -> {
                if (event.value().equals(MessageBox.Result.YES)) {
                    ClientStore.send(new ServerboundModifyStore(ModifyType.DELETE, store.id(), store.name(), store.permission(), store.hidden()));
                }
            }));
    }

    // ---- Helpers

    /**
     * An ID or permission field: a-z, 0-9, _ and ., lower-cased.
     */
    private TextBox field(final ComponentFactory factory, final boolean submit) {
        final TextBox field = factory.textBox().setMaxLength(255);
        field.setSize(FIELD_WIDTH, BUTTON_HEIGHT);
        field.onEvent(TextInput.EDIT, event -> event.setValue(event.value().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_.]", "")));
        field.onEvent(TextInput.EDITED, event -> this.updateButtons());
        if (submit) {
            field.onEvent(TextBox.SUBMITTED, event -> this.save());
        }
        return field;
    }

    private static TextBox readOnly(final ComponentFactory factory) {
        final TextBox field = factory.textBox().setReadOnly(true);
        field.setSize(FIELD_WIDTH, BUTTON_HEIGHT);
        return field;
    }

    private static Label label(final ComponentFactory factory, final String key) {
        return factory.label(Text.translatable(key).append(Text.of(":")).color(WHITE));
    }

    private static Button button(final ComponentFactory factory, final Text text, final float width, final Runnable press) {
        return factory.button(text).setSize(width, BUTTON_HEIGHT).onEvent(Button.PRESSED, event -> press.run());
    }

    /**
     * As 3.5's + and -: narrow, one coloured character (as the Guide's).
     */
    private static Button small(final ComponentFactory factory, final String label, final int colour, final Runnable press) {
        final Button button = factory.button(Text.of(label).color(colour)).setTextPadding(2f, 4f, 2f, 2f);
        button.setMinSize(0f, 0f);
        button.setSize(SMALL_WIDTH, SMALL_HEIGHT);
        return button.onEvent(Button.PRESSED, event -> press.run());
    }
}
