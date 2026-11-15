/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.client;

import com.almuradev.almura.feature.exchange.ExchangeMoney;
import com.almuradev.almura.feature.exchange.ListStatusType;
import com.almuradev.almura.feature.exchange.network.ExchangeMessages.ForSaleView;
import com.almuradev.almura.feature.exchange.network.ExchangeMessages.ServerboundBuy;
import com.almuradev.almura.feature.exchange.network.ExchangeMessages.ServerboundListStatus;
import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.control.attribute.popup.Closable;
import org.inspirenxe.strata.api.control.attribute.text.TextInput;
import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.button.CheckBox;
import org.inspirenxe.strata.api.control.button.CheckState;
import org.inspirenxe.strata.api.control.collection.ComboBox;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.control.field.TextBox;
import org.inspirenxe.strata.api.control.window.Window;
import org.inspirenxe.strata.api.layout.Anchor;
import org.inspirenxe.strata.api.layout.AnchorPanel;
import org.inspirenxe.strata.api.layout.HorizontalAlignment;
import org.inspirenxe.strata.api.layout.Orientation;
import org.inspirenxe.strata.api.layout.StackPanel;
import org.inspirenxe.strata.api.layout.VerticalAlignment;
import org.inspirenxe.strata.api.overlay.Overlay;
import org.inspirenxe.strata.api.text.Text;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * The exchange's small dialogs, as 3.5's screens: Enter a price (listing an item, or changing its price), Enter a Quantity (buying some of
 * an item) and Exchange Options.
 */
final class ExchangeDialogs {

    private static final float BUTTON_WIDTH = 40f, BUTTON_HEIGHT = 16f;
    private static final int WHITE = 0xFFFFFFFF;
    private static final Map<String, String> SORT_ORDERS = Map.of(
        ExchangeOptions.ITEM_NAME, "Item Name",
        ExchangeOptions.PRICE, "Price",
        ExchangeOptions.SELLER, "Seller");

    private ExchangeDialogs() {
    }

    /**
     * 3.5's Enter a price: a price each (up to two decimals, more than 0 and at most a trillion), then List (or Modify) or Cancel. Starts at
     * the current price, else the last one the item had.
     */
    static void price(final ComponentFactory factory, final String id, final int listItemRecNo, final ListStatusType type,
        final @Nullable BigDecimal startPrice) {
        final TextBox price = factory.textBox().setMaxLength(16).setText(startPrice == null ? "0" : startPrice.toPlainString());
        price.setSelectAllOnFocus(true);
        price.onEvent(TextInput.EDIT, event -> event.setValue(filterAmount(event.value())));
        price.setSize(65f, 16f);
        final Overlay[] overlay = new Overlay[1];
        final Button list = factory.button(Text.translatable("almura.feature.common.button." + (type == ListStatusType.LIST ? "list" : "modify")))
            .setSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        final Runnable done = () -> {
            if (!validPrice(price.text())) {
                return;
            }
            ClientExchange.send(new ServerboundListStatus(type, id, listItemRecNo, new BigDecimal(price.text()).toPlainString()));
            overlay[0].close();
        };
        list.setEnabled(validPrice(price.text()));
        price.onEvent(TextInput.EDITED, event -> list.setEnabled(validPrice(price.text())));
        price.onEvent(TextBox.SUBMITTED, event -> done.run());
        list.onEvent(Button.PRESSED, event -> done.run());
        final StackPanel column = column(factory)
            .add(factory.stackPanel(Orientation.HORIZONTAL).setSpacing(2f)
                .add(price.setAlignment(HorizontalAlignment.LEFT, VerticalAlignment.MIDDLE))
                .add(factory.label(Text.of("/ea").color(WHITE)).setAlignment(HorizontalAlignment.LEFT, VerticalAlignment.MIDDLE)))
            .add(buttons(factory, list, () -> overlay[0].close()));
        overlay[0] = factory.overlays().dialog(window(factory, Text.translatable("almura.feature.exchange.title.enter_a_price"), 120f).add(column));
    }

    /**
     * 3.5's Enter a Quantity: how many (1 to what's left), the price each and the total, then Buy or Cancel.
     */
    static void buyQuantity(final ComponentFactory factory, final ExchangeScreen screen, final ForSaleView item) {
        final TextBox quantity = factory.textBox().setMaxLength(9).setText("1");
        quantity.setSelectAllOnFocus(true);
        quantity.onEvent(TextInput.EDIT, event -> event.setValue(event.value().replaceAll("[^0-9]", "")));
        quantity.setSize(45f, 16f);
        final BigDecimal price = ExchangeMoney.parse(item.price());
        final Label per = factory.label(Text.of(ExchangeMoney.format(price)).color(WHITE));
        final Label total = factory.label("");
        final Overlay[] overlay = new Overlay[1];
        final Button buy = factory.button(Text.translatable("almura.feature.common.button.buy")).setSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        final Runnable update = () -> {
            final int value = parse(quantity.text());
            total.setText(Text.of(ExchangeMoney.format(price.multiply(BigDecimal.valueOf(Math.max(0, value))))).color(WHITE));
            buy.setEnabled(value > 0 && value <= item.quantity());
        };
        final Runnable done = () -> {
            final int value = parse(quantity.text());
            if (value > 0 && value <= item.quantity()) {
                ClientExchange.send(new ServerboundBuy(screen.id(), item.listItemRecNo(), value));
                overlay[0].close();
            }
        };
        quantity.onEvent(TextInput.EDITED, event -> update.run());
        quantity.onEvent(TextBox.SUBMITTED, event -> done.run());
        buy.onEvent(Button.PRESSED, event -> done.run());
        update.run();
        final StackPanel column = column(factory)
            .add(row(factory, factory.label(Text.translatable("almura.feature.common.text.quantity").append(Text.of(":")).color(WHITE)), quantity))
            .add(row(factory, factory.label(Text.of("Per:").color(WHITE)), per))
            .add(row(factory, factory.label(Text.translatable("almura.feature.common.text.total").append(Text.of(":")).color(WHITE)), total))
            .add(buttons(factory, buy, () -> overlay[0].close()));
        overlay[0] = factory.overlays().dialog(window(factory, Text.translatable("almura.feature.exchange.title.enter_a_quantity"), 130f)
            .add(column));
    }

    /**
     * 3.5's Exchange Options: leave the player's own listings out of searches, and the order a search starts in; saved and applied on
     * Close.
     */
    static void options(final ComponentFactory factory, final ExchangeScreen screen) {
        final CheckBox hideOwn = factory.checkBox("Hide my listed items in search")
            .setState(ExchangeOptions.hideOwnListings() ? CheckState.CHECKED : CheckState.UNCHECKED);
        final ComboBox<String> sort = factory.comboBox(value -> SORT_ORDERS.getOrDefault(value, value));
        sort.setItems(List.of(ExchangeOptions.ITEM_NAME, ExchangeOptions.PRICE, ExchangeOptions.SELLER));
        sort.selectItem(SORT_ORDERS.containsKey(ExchangeOptions.defaultSort()) ? ExchangeOptions.defaultSort() : ExchangeOptions.ITEM_NAME);
        sort.setSize(90f, 14f);
        final Overlay[] overlay = new Overlay[1];
        final Runnable close = () -> {
            ExchangeOptions.set(hideOwn.state() == CheckState.CHECKED, sort.selected().orElse(ExchangeOptions.ITEM_NAME));
            screen.applyOptions();
            overlay[0].close();
        };
        final StackPanel column = column(factory)
            .add(hideOwn.setAlignment(HorizontalAlignment.LEFT, VerticalAlignment.TOP))
            .add(factory.label(Text.of("Default sort order:").color(WHITE)).setAlignment(HorizontalAlignment.LEFT, VerticalAlignment.TOP))
            .add(sort.setAlignment(HorizontalAlignment.LEFT, VerticalAlignment.TOP))
            .add(factory.button(Text.translatable("almura.button.close")).setSize(50f, BUTTON_HEIGHT)
                .onEvent(Button.PRESSED, event -> close.run())
                .setAlignment(HorizontalAlignment.RIGHT, VerticalAlignment.TOP));
        final Window window = window(factory, Text.of("Exchange Options"), 200f);
        // Its x saves too, as leaving 3.5's did
        window.onEvent(Closable.CLOSE, event -> {
            ExchangeOptions.set(hideOwn.state() == CheckState.CHECKED, sort.selected().orElse(ExchangeOptions.ITEM_NAME));
            screen.applyOptions();
        });
        overlay[0] = factory.overlays().dialog(window.add(column));
    }

    // ---- Helpers

    static Window window(final ComponentFactory factory, final Text title, final float width) {
        return factory.window(title)
            .setClosable(true)
            .setMovable(true)
            .setResizable(false)
            .setCollapsible(false)
            .setMaximizable(false)
            .setWidth(width);
    }

    private static StackPanel column(final ComponentFactory factory) {
        final StackPanel column = factory.stackPanel().setSpacing(4f);
        column.setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP);
        return column;
    }

    private static AnchorPanel row(final ComponentFactory factory, final Label label, final Component<?> value) {
        final AnchorPanel row = factory.anchorPanel()
            .add(label.setAnchor(Anchor.MIDDLE_LEFT))
            .add(value.setAnchor(Anchor.MIDDLE_RIGHT));
        row.setHeight(14f).setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP);
        return row;
    }

    /**
     * Cancel, then the action, at the bottom right (3.5's order).
     */
    private static StackPanel buttons(final ComponentFactory factory, final Button action, final Runnable cancel) {
        return factory.stackPanel(Orientation.HORIZONTAL).setSpacing(2f)
            .add(factory.button(Text.translatable("almura.button.cancel")).setSize(BUTTON_WIDTH, BUTTON_HEIGHT)
                .onEvent(Button.PRESSED, event -> cancel.run()))
            .add(action)
            .setAlignment(HorizontalAlignment.RIGHT, VerticalAlignment.TOP);
    }

    private static boolean validPrice(final String text) {
        if (text.isEmpty()) {
            return false;
        }
        try {
            final BigDecimal value = new BigDecimal(text);
            return value.signum() > 0 && value.compareTo(ExchangeMoney.ONE_TRILLION) <= 0;
        } catch (final NumberFormatException e) {
            return false;
        }
    }

    private static int parse(final String text) {
        try {
            return Integer.parseInt(text);
        } catch (final NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Digits and one decimal point, at most two decimals, as 3.5's price box.
     */
    private static String filterAmount(final String text) {
        final StringBuilder filtered = new StringBuilder();
        boolean point = false;
        int decimals = 0;
        for (final char c : text.toCharArray()) {
            if (c == '.' && !point) {
                point = true;
                filtered.append(c);
            } else if (Character.isDigit(c) && (!point || decimals < 2) && filtered.length() < 16) {
                filtered.append(c);
                if (point) {
                    decimals++;
                }
            }
        }
        return filtered.toString();
    }
}
