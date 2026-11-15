/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.client;

import com.almuradev.almura.feature.exchange.network.ExchangeMessages.Direction;
import com.almuradev.almura.feature.exchange.network.ExchangeMessages.ListItemView;
import com.almuradev.almura.feature.exchange.network.ExchangeMessages.Move;
import com.almuradev.almura.feature.exchange.network.ExchangeMessages.ServerboundMoveItems;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.control.attribute.item.Activatable;
import org.inspirenxe.strata.api.control.attribute.item.Selection;
import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.collection.ListView;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.control.display.ProgressBar;
import org.inspirenxe.strata.api.layout.Anchor;
import org.inspirenxe.strata.api.layout.AnchorPanel;
import org.inspirenxe.strata.api.layout.HorizontalAlignment;
import org.inspirenxe.strata.api.layout.Orientation;
import org.inspirenxe.strata.api.layout.StackPanel;
import org.inspirenxe.strata.api.layout.VerticalAlignment;
import org.inspirenxe.strata.api.overlay.Overlay;
import org.inspirenxe.strata.api.text.Text;
import org.inspirenxe.strata.api.theme.Border;
import org.inspirenxe.strata.api.theme.Paint;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 3.5's Offer: the player's inventory at the left and their unlisted items on the exchange at the right, with 1 (one), S (the stack), T
 * (every stack of that item) and A (everything) moving the other way from the side last picked from. Nothing moves until OK, which sends
 * what changed; the server takes and gives the items.
 */
final class ExchangeOfferDialog {

    private static final float WIDTH = 400f, HEIGHT = 290f, SIDE_WIDTH = 172f, MIDDLE_WIDTH = 38f, ROW_HEIGHT = 24f, BAR_HEIGHT = 14f;
    private static final float BUTTON_HEIGHT = 16f;
    private static final int WHITE = 0xFFFFFFFF;
    private static final Paint PANEL = Paint.solid(0xD7000000), LINE = Paint.solid(0xB9FFFFFF);
    private static final Border PANEL_BORDER = Border.of(0xB9FFFFFF, 1f);

    /**
     * Some of an item on one side.
     */
    private static final class Entry {

        final ItemStack item;
        int quantity;

        Entry(final ItemStack item, final int quantity) {
            this.item = item.copyWithCount(1);
            this.quantity = quantity;
        }
    }

    /**
     * How much of an item has moved onto the exchange (less than 0: back to the inventory).
     */
    private static final class Net {

        final ItemStack item;
        int toListing;

        Net(final ItemStack item) {
            this.item = item.copyWithCount(1);
        }
    }

    private final String id;
    private final int limit, listed;
    private final List<Entry> inventory = new ArrayList<>(), unlisted = new ArrayList<>();
    private final List<Net> moves = new ArrayList<>();
    private final ListView<Entry> inventoryList, unlistedList;
    private final ProgressBar inventoryBar, unlistedBar;
    private final Label direction;
    private final Button single, stack, type, all;
    // Where the buttons move to: the side not last picked from
    private boolean toUnlisted = true;
    private boolean selecting;

    ExchangeOfferDialog(final ComponentFactory factory, final String id, final int limit, final int listed, final List<ListItemView> pending) {
        this.id = id;
        this.limit = limit;
        this.listed = listed;
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            final Inventory playerInventory = minecraft.player.getInventory();
            for (int slot = 0; slot < Inventory.INVENTORY_SIZE; slot++) {
                final ItemStack item = playerInventory.getItem(slot);
                if (!item.isEmpty()) {
                    this.inventory.add(new Entry(item, item.getCount()));
                }
            }
        }
        pending.forEach(item -> this.unlisted.add(new Entry(item.item(), item.quantity())));

        this.inventoryList = factory.<Entry>listView(entry -> row(factory, entry));
        this.unlistedList = factory.<Entry>listView(entry -> row(factory, entry));
        for (final ListView<Entry> list : List.of(this.inventoryList, this.unlistedList)) {
            list.setRowHeight(ROW_HEIGHT);
            list.onEvent(Selection.SELECTED, event -> this.onSelect(list));
            // A double click moves the stack, as 3.5
            list.onEvent(Activatable.ACTIVATED, event -> this.selected().ifPresent(entry -> this.move(entry, entry.quantity)));
        }
        this.inventoryBar = factory.progressBar();
        this.unlistedBar = factory.progressBar();
        this.direction = factory.label(Text.of("->").color(WHITE));
        this.single = small(factory, "1", "almura.feature.exchange.tooltip.move_single", () -> this.selected().ifPresent(entry -> this.move(entry, 1)));
        this.stack = small(factory, "S", "almura.feature.exchange.tooltip.move_stack", () -> this.selected().ifPresent(entry -> this.move(entry,
            entry.quantity)));
        this.type = small(factory, "T", "almura.feature.exchange.tooltip.move_type", () -> this.selected().ifPresent(this::moveType));
        this.all = small(factory, "A", "almura.feature.exchange.tooltip.move_all", this::moveAll);

        final StackPanel middle = factory.stackPanel().setSpacing(2f)
            .add(this.single.setAlignment(HorizontalAlignment.CENTER, VerticalAlignment.TOP))
            .add(this.stack.setAlignment(HorizontalAlignment.CENTER, VerticalAlignment.TOP))
            .add(this.direction.setAlignment(HorizontalAlignment.CENTER, VerticalAlignment.TOP).setMargin(0f, 4f, 0f, 4f))
            .add(this.type.setAlignment(HorizontalAlignment.CENTER, VerticalAlignment.TOP))
            .add(this.all.setAlignment(HorizontalAlignment.CENTER, VerticalAlignment.TOP));
        middle.setPadding(3f, 3f);
        middle.setBorder(PANEL_BORDER);

        final Overlay[] overlay = new Overlay[1];
        final AnchorPanel content = factory.anchorPanel()
            .add(side(factory, "almura.feature.exchange.text.inventory", this.inventoryList, this.inventoryBar).setAnchor(Anchor.TOP_LEFT))
            .add(middle.setWidth(MIDDLE_WIDTH).setAnchor(Anchor.CENTER).setMargin(0f, 0f, 0f, BUTTON_HEIGHT))
            .add(side(factory, "almura.feature.exchange.text.unlisted_items", this.unlistedList, this.unlistedBar).setAnchor(Anchor.TOP_RIGHT))
            .add(factory.stackPanel(Orientation.HORIZONTAL).setSpacing(2f)
                .add(ExchangeScreen.button(factory, Text.translatable("almura.button.cancel"), 40f, () -> overlay[0].close()))
                .add(ExchangeScreen.button(factory, Text.translatable("almura.button.ok"), 40f, () -> {
                    this.send();
                    overlay[0].close();
                }))
                .setAnchor(Anchor.BOTTOM_RIGHT));
        content.setSize(WIDTH - 10f, HEIGHT);
        overlay[0] = factory.overlays().dialog(ExchangeDialogs.window(factory, Text.translatable("almura.feature.exchange.title.offer"), WIDTH)
            .add(content));
        this.refresh();
        // As 3.5: start on the first inventory item, else the first unlisted one
        if (!this.inventory.isEmpty()) {
            this.inventoryList.select(0);
        } else if (!this.unlisted.isEmpty()) {
            this.unlistedList.select(0);
        }
    }

    private AnchorPanel side(final ComponentFactory factory, final String title, final ListView<Entry> list, final ProgressBar bar) {
        final AnchorPanel side = factory.anchorPanel().setBackground(PANEL).setBorder(PANEL_BORDER).setPadding(3f)
            .add(factory.label(Text.translatable(title).color(WHITE)).setAnchor(Anchor.TOP_CENTER).setMargin(0f, 1f, 0f, 0f))
            .add(factory.anchorPanel().setBackground(LINE).setHeight(1f).setAnchor(Anchor.FILL).setMargin(0f, 12f, 0f, 0f)
                .setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP))
            .add(list.setAnchor(Anchor.FILL).setMargin(0f, 15f, 0f, BAR_HEIGHT + 5f))
            .add(factory.anchorPanel().setBackground(LINE).setHeight(1f).setAnchor(Anchor.FILL).setMargin(0f, 0f, 0f, BAR_HEIGHT + 2f)
                .setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.BOTTOM))
            .add(bar.setHeight(BAR_HEIGHT).setAnchor(Anchor.FILL).setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.BOTTOM));
        side.setSize(SIDE_WIDTH, HEIGHT - BUTTON_HEIGHT - 4f);
        return side;
    }

    private static Button small(final ComponentFactory factory, final String text, final String tooltip, final Runnable press) {
        final Button button = factory.button(Text.of(text)).setTextPadding(2f, 4f, 2f, 2f);
        button.setMinSize(0f, 0f);
        button.setSize(20f, BUTTON_HEIGHT).setTooltip(Text.translatable(tooltip, "right"));
        return button.onEvent(Button.PRESSED, event -> press.run());
    }

    private static Component<?> row(final ComponentFactory factory, final Entry entry) {
        return factory.anchorPanel()
            .add(ExchangeScreen.itemDisplay(factory, entry.item).setAnchor(Anchor.MIDDLE_LEFT).setMargin(2f, 0f, 0f, 0f))
            .add(factory.label(ExchangeScreen.nameAndQuantity(entry.item, entry.quantity, SIDE_WIDTH - 6f - 8f - 26f))
                .setAnchor(Anchor.MIDDLE_LEFT).setMargin(22f, 0f, 0f, 0f))
            .setAnchor(Anchor.FILL).setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.STRETCH);
    }

    // ---- Moving

    private void onSelect(final ListView<Entry> list) {
        if (this.selecting || list.selected().isEmpty()) {
            this.updateButtons();
            return;
        }
        // Picking from one side moves to the other
        this.toUnlisted = list == this.inventoryList;
        this.selecting = true;
        try {
            (list == this.inventoryList ? this.unlistedList : this.inventoryList).clearSelection();
        } finally {
            this.selecting = false;
        }
        this.updateButtons();
    }

    private Optional<Entry> selected() {
        return (this.toUnlisted ? this.inventoryList : this.unlistedList).selected();
    }

    private void moveType(final Entry picked) {
        for (final Entry entry : new ArrayList<>(this.toUnlisted ? this.inventory : this.unlisted)) {
            if (ExchangeScreen.same(entry.item, picked.item)) {
                this.move(entry, entry.quantity, false);
            }
        }
        this.refresh();
    }

    private void moveAll() {
        for (final Entry entry : new ArrayList<>(this.toUnlisted ? this.inventory : this.unlisted)) {
            this.move(entry, entry.quantity, false);
        }
        this.refresh();
    }

    private void move(final Entry entry, final int count) {
        this.move(entry, count, true);
    }

    /**
     * Moves up to {@code count} of an entry to the other side, as much as fits: the exchange takes any amount of an item it already shows
     * and new items up to the listing limit; the inventory fills its stacks of the item, then empty slots of its 36.
     */
    private void move(final Entry entry, final int count, final boolean refresh) {
        final List<Entry> source = this.toUnlisted ? this.inventory : this.unlisted;
        final List<Entry> target = this.toUnlisted ? this.unlisted : this.inventory;
        int amount = Math.min(count, entry.quantity);
        if (amount <= 0 || !source.contains(entry)) {
            return;
        }
        if (this.toUnlisted) {
            Entry same = null;
            for (final Entry other : target) {
                if (ExchangeScreen.same(other.item, entry.item)) {
                    same = other;
                    break;
                }
            }
            if (same == null) {
                if (target.size() + this.listed >= this.limit) {
                    return;
                }
                same = new Entry(entry.item, 0);
                target.add(same);
            }
            same.quantity += amount;
        } else {
            final int max = entry.item.getMaxStackSize();
            int left = amount;
            for (final Entry other : target) {
                if (left <= 0) {
                    break;
                }
                if (ExchangeScreen.same(other.item, entry.item) && other.quantity < max) {
                    final int added = Math.min(left, max - other.quantity);
                    other.quantity += added;
                    left -= added;
                }
            }
            while (left > 0 && target.size() < Inventory.INVENTORY_SIZE) {
                final int added = Math.min(left, max);
                target.add(new Entry(entry.item, added));
                left -= added;
            }
            amount -= left;
            if (amount <= 0) {
                return;
            }
        }
        entry.quantity -= amount;
        if (entry.quantity <= 0) {
            source.remove(entry);
        }
        this.net(entry.item).toListing += this.toUnlisted ? amount : -amount;
        if (refresh) {
            this.refresh();
        }
    }

    private Net net(final ItemStack item) {
        for (final Net net : this.moves) {
            if (ExchangeScreen.same(net.item, item)) {
                return net;
            }
        }
        final Net net = new Net(item);
        this.moves.add(net);
        return net;
    }

    /**
     * Sends what changed, once per item: onto the exchange, or back.
     */
    private void send() {
        final List<Move> moves = new ArrayList<>();
        for (final Net net : this.moves) {
            if (net.toListing > 0) {
                moves.add(new Move(Direction.TO_LISTING, net.item, net.toListing));
            } else if (net.toListing < 0) {
                moves.add(new Move(Direction.TO_INVENTORY, net.item, -net.toListing));
            }
        }
        if (!moves.isEmpty()) {
            ClientExchange.send(new ServerboundMoveItems(this.id, moves));
        }
    }

    // ---- State

    private void refresh() {
        final Entry picked = this.selected().orElse(null);
        this.selecting = true;
        try {
            this.inventoryList.setItems(this.inventory);
            this.inventoryList.refresh();
            this.unlistedList.setItems(this.unlisted);
            this.unlistedList.refresh();
            final List<Entry> source = this.toUnlisted ? this.inventory : this.unlisted;
            final ListView<Entry> sourceList = this.toUnlisted ? this.inventoryList : this.unlistedList;
            if (picked != null && source.contains(picked)) {
                sourceList.selectItem(picked);
            } else if (!source.isEmpty()) {
                sourceList.select(0);
            }
        } finally {
            this.selecting = false;
        }
        bar(this.inventoryBar, this.inventory.size(), Inventory.INVENTORY_SIZE);
        bar(this.unlistedBar, this.unlisted.size() + this.listed, this.limit);
        this.updateButtons();
    }

    private static void bar(final ProgressBar bar, final int used, final int limit) {
        bar.setRange(0, Math.max(1, limit)).setValue(Math.min(used, limit)).setText(Text.of(used + "/" + limit));
    }

    private void updateButtons() {
        final boolean picked = this.selected().isPresent();
        final String side = this.toUnlisted ? "right" : "left";
        this.direction.setText(Text.of(this.toUnlisted ? "->" : "<-").color(WHITE));
        for (final Button button : List.of(this.single, this.stack, this.type, this.all)) {
            button.setEnabled(picked);
        }
        this.single.setTooltip(Text.translatable("almura.feature.exchange.tooltip.move_single", side));
        this.stack.setTooltip(Text.translatable("almura.feature.exchange.tooltip.move_stack", side));
        this.type.setTooltip(Text.translatable("almura.feature.exchange.tooltip.move_type", side));
        this.all.setTooltip(Text.translatable("almura.feature.exchange.tooltip.move_all", side));
    }
}
