/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.store.client;

import com.almuradev.almura.feature.menu.AlmuraScreens;
import com.almuradev.almura.feature.store.network.StoreMessages.ItemView;
import com.almuradev.almura.feature.store.network.StoreMessages.ModifyCandidate;
import com.almuradev.almura.feature.store.network.StoreMessages.ServerboundDelistItems;
import com.almuradev.almura.feature.store.network.StoreMessages.ServerboundListItem;
import com.almuradev.almura.feature.store.network.StoreMessages.ServerboundModifyItems;
import com.almuradev.almura.feature.store.network.StoreMessages.ServerboundTransaction;
import com.almuradev.almura.feature.store.StoreNumbers;
import com.almuradev.almura.feature.store.StoreSide;
import net.minecraft.world.item.ItemStack;
import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.control.attribute.text.TextInput;
import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.button.CheckBox;
import org.inspirenxe.strata.api.control.button.CheckState;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.control.field.TextBox;
import org.inspirenxe.strata.api.control.window.Window;
import org.inspirenxe.strata.api.layout.Anchor;
import org.inspirenxe.strata.api.layout.AnchorPanel;
import org.inspirenxe.strata.api.layout.HorizontalAlignment;
import org.inspirenxe.strata.api.layout.Orientation;
import org.inspirenxe.strata.api.layout.VerticalAlignment;
import org.inspirenxe.strata.api.overlay.Overlay;
import org.inspirenxe.strata.api.text.Text;
import org.inspirenxe.strata.api.theme.Border;
import org.inspirenxe.strata.api.theme.Paint;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

/**
 * The store's dialogs, as 3.5's: List (an item's buying and selling price and quantity, for store admins) and Enter a Quantity (to buy or
 * sell).
 */
final class StoreDialogs {

    private static final float BUTTON_HEIGHT = 16f, FIELD_WIDTH = 50f;
    private static final int WHITE = 0xFFFFFFFF, TOTAL = 0xFF999999;
    private static final Paint BOX = Paint.solid(0xFF000000), LINE = Paint.solid(0xB9FFFFFF);
    private static final Border BOX_BORDER = Border.of(0xB9FFFFFF, 1f);

    private StoreDialogs() {
    }

    /**
     * One side of the List dialog: its price each, quantity, Infinite and the total.
     */
    private static final class SideBox {

        final TextBox price, quantity;
        final CheckBox infinite;
        final Label totalLabel, total;
        final AnchorPanel panel;

        SideBox(final ComponentFactory factory, final String titleKey, final @Nullable ItemView listed, final Runnable changed) {
            this.price = factory.textBox().setText(listed == null ? "" : listed.price());
            this.price.setSize(FIELD_WIDTH, BUTTON_HEIGHT);
            this.price.onEvent(TextInput.EDIT, event -> event.setValue(StoreNumbers.filterNumber(event.value(), 2, false)));
            this.price.onEvent(TextInput.EDITED, event -> changed.run());
            this.quantity = factory.textBox();
            this.quantity.setSize(FIELD_WIDTH, BUTTON_HEIGHT);
            this.quantity.onEvent(TextInput.EDIT, event -> event.setValue(StoreNumbers.filterNumber(event.value(), 0, this.infinite())));
            this.quantity.onEvent(TextInput.EDITED, event -> changed.run());
            this.infinite = factory.checkBox(Text.translatable("almura.feature.common.text.infinite").color(WHITE)).setBoxSize(AlmuraScreens.CHECKBOX_SIZE);
            this.infinite.onEvent(CheckBox.CHANGED, event -> {
                final boolean on = event.value() == CheckState.CHECKED;
                this.quantity.setReadOnly(on);
                this.quantity.setText(on ? "-1" : "1");
                changed.run();
            });
            this.totalLabel = factory.label(Text.translatable("almura.feature.common.text.total").append(Text.of(":")).color(WHITE));
            this.total = factory.label("");
            final float column = 4f + FIELD_WIDTH;
            this.panel = factory.anchorPanel().setBackground(BOX).setBorder(BOX_BORDER).setPadding(2f)
                .add(factory.label(Text.translatable(titleKey).color(WHITE)).setAnchor(Anchor.TOP_CENTER).setMargin(0f, 2f, 0f, 0f))
                .add(factory.anchorPanel().setBackground(LINE).setHeight(1f).setAnchor(Anchor.FILL).setMargin(-1f, 13f, -1f, 0f)
                    .setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP))
                .add(factory.label(Text.translatable("almura.feature.common.text.price_per").append(Text.of(":")).color(WHITE))
                    .setAnchor(Anchor.TOP_RIGHT).setMargin(0f, 20f, column, 0f))
                .add(this.price.setAnchor(Anchor.TOP_RIGHT).setMargin(0f, 16f, 2f, 0f))
                .add(factory.label(Text.translatable("almura.feature.common.text.quantity").append(Text.of(":")).color(WHITE))
                    .setAnchor(Anchor.TOP_RIGHT).setMargin(0f, 38f, column, 0f))
                .add(this.quantity.setAnchor(Anchor.TOP_RIGHT).setMargin(0f, 34f, 2f, 0f))
                .add(this.infinite.setAnchor(Anchor.TOP_RIGHT).setMargin(0f, 53f, 2f, 0f))
                .add(this.totalLabel.setAnchor(Anchor.BOTTOM_RIGHT).setMargin(0f, 0f, column, 2f))
                .add(this.total.setAnchor(Anchor.BOTTOM_RIGHT).setMargin(0f, 0f, 2f, 2f));
            if (listed != null) {
                if (listed.quantity() == StoreNumbers.UNLIMITED) {
                    this.infinite.setState(CheckState.CHECKED);
                    this.quantity.setReadOnly(true);
                    this.quantity.setText("-1");
                } else {
                    this.quantity.setText(Integer.toString(listed.quantity()));
                }
            }
        }

        boolean infinite() {
            return this.infinite.state() == CheckState.CHECKED;
        }

        /**
         * @return whether a price (more than 0) and a quantity are entered
         */
        boolean valid() {
            final BigDecimal price = StoreNumbers.parse(this.price.text());
            final String quantity = this.quantity.text();
            return price.signum() > 0 && !quantity.isEmpty() && !quantity.equals("-") && !quantity.equals("0");
        }

        int quantityValue() {
            try {
                return Integer.parseInt(this.quantity.text());
            } catch (final NumberFormatException e) {
                return 0;
            }
        }

        void updateTotal() {
            final boolean shown = this.valid() && !this.infinite();
            this.totalLabel.setVisible(!this.infinite());
            this.total.setVisible(!this.infinite());
            this.total.setText(shown ? Text.of(StoreNumbers.withSuffix(StoreNumbers.parse(this.price.text()).doubleValue() * this.quantityValue()))
                .color(TOTAL) : Text.of(""));
        }
    }

    /**
     * 3.5's List: the item's buying and selling sides side by side; a side with a price and quantity is listed (or changed), an emptied side
     * of a listed item is unlisted.
     */
    static void list(final ComponentFactory factory, final String storeId, final ItemStack item, final @Nullable ItemView buying,
        final @Nullable ItemView selling) {
        final Button[] list = new Button[1];
        final SideBox[] boxes = new SideBox[2];
        final Runnable changed = () -> {
            if (boxes[0] == null || boxes[1] == null) {
                return;
            }
            boxes[0].updateTotal();
            boxes[1].updateTotal();
            list[0].setEnabled(boxes[0].valid() || boxes[1].valid());
        };
        final boolean modifying = buying != null || selling != null;
        list[0] = factory.button(Text.translatable(modifying ? "almura.feature.common.button.modify" : "almura.feature.common.text.list"))
            .setSize(FIELD_WIDTH, BUTTON_HEIGHT);
        list[0].setEnabled(false);
        boxes[0] = new SideBox(factory, "almura.feature.common.button.buy", buying, changed);
        boxes[1] = new SideBox(factory, "almura.feature.common.button.sell", selling, changed);
        changed.run();
        final Overlay[] overlay = new Overlay[1];
        list[0].onEvent(Button.PRESSED, event -> {
            if (!list[0].isEnabled()) {
                return;
            }
            apply(storeId, StoreSide.BUY, item, buying, boxes[0]);
            apply(storeId, StoreSide.SELL, item, selling, boxes[1]);
            overlay[0].close();
        });
        final float half = (250f - 8f) / 2f - 1f;
        final AnchorPanel content = factory.anchorPanel()
            .add(boxes[0].panel.setAnchor(Anchor.FILL).setMargin(0f, 0f, 0f, BUTTON_HEIGHT + 4f)
                .setAlignment(HorizontalAlignment.LEFT, VerticalAlignment.STRETCH).setWidth(half))
            .add(boxes[1].panel.setAnchor(Anchor.FILL).setMargin(0f, 0f, 0f, BUTTON_HEIGHT + 4f)
                .setAlignment(HorizontalAlignment.RIGHT, VerticalAlignment.STRETCH).setWidth(half))
            .add(factory.stackPanel(Orientation.HORIZONTAL).setSpacing(2f)
                .add(factory.button(Text.translatable("almura.button.cancel")).setSize(FIELD_WIDTH, BUTTON_HEIGHT)
                    .onEvent(Button.PRESSED, event -> overlay[0].close()))
                .add(list[0])
                .setAnchor(Anchor.BOTTOM_RIGHT));
        content.setHeight(104f);
        overlay[0] = factory.overlays().dialog(window(factory, Text.translatable("almura.feature.common.text.list"), 250f)
            .add(content.setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP)));
    }

    private static void apply(final String storeId, final StoreSide side, final ItemStack item, final @Nullable ItemView listed,
        final SideBox box) {
        if (box.valid()) {
            final String price = StoreNumbers.parse(box.price.text()).toPlainString();
            if (listed != null) {
                ClientStore.send(new ServerboundModifyItems(storeId, side,
                    List.of(new ModifyCandidate(listed.recNo(), box.quantityValue(), listed.index(), price))));
            } else {
                // At the bottom of the shop order
                final int index = ClientStore.items(storeId, side).stream().mapToInt(ItemView::index).max().orElse(0) + 1;
                ClientStore.send(new ServerboundListItem(storeId, side, item.copyWithCount(1), box.quantityValue(), index, price));
            }
        } else if (listed != null) {
            ClientStore.send(new ServerboundDelistItems(storeId, side, List.of(listed.recNo())));
        }
    }

    /**
     * 3.5's Enter a Quantity: how many to buy or sell (up to what's left, and for selling what the player holds), the price each and the
     * total.
     */
    static void quantity(final ComponentFactory factory, final String storeId, final ItemView view, final StoreSide side) {
        final int left = view.quantity() == StoreNumbers.UNLIMITED ? 9999 : view.quantity();
        final int max = side == StoreSide.BUY ? left : Math.min(left, StoreItems.held(view.stack()));
        final BigDecimal price = StoreNumbers.parse(view.price());
        final TextBox quantity = factory.textBox().setText("1");
        quantity.setSize(45f, BUTTON_HEIGHT);
        quantity.setSelectAllOnFocus(true);
        final Label total = factory.label("");
        final Button transact = factory.button(Text.translatable("almura.feature.common.button." + side.name().toLowerCase(Locale.ROOT)))
            .setSize(40f, BUTTON_HEIGHT);
        final Runnable changed = () -> {
            final int value = StoreNumbers.parse(quantity.text()).intValue();
            total.setText(Text.of(StoreNumbers.currency(price.doubleValue() * value)).color(WHITE));
            transact.setEnabled(value > 0 && value <= max);
        };
        quantity.onEvent(TextInput.EDIT, event -> {
            final String digits = event.value().replaceAll("[^\\d]", "");
            if (digits.isEmpty()) {
                event.setValue("");
                return;
            }
            final long value = Long.parseLong(digits.length() > 9 ? digits.substring(0, 9) : digits);
            event.setValue(Long.toString(Math.max(1, Math.min(value, Math.max(1, max)))));
        });
        quantity.onEvent(TextInput.EDITED, event -> changed.run());
        final Overlay[] overlay = new Overlay[1];
        final Runnable done = () -> {
            final int value = StoreNumbers.parse(quantity.text()).intValue();
            if (value > 0 && value <= max) {
                ClientStore.send(new ServerboundTransaction(storeId, side, view.recNo(), value));
                overlay[0].close();
            }
        };
        quantity.onEvent(TextBox.SUBMITTED, event -> done.run());
        transact.onEvent(Button.PRESSED, event -> done.run());
        changed.run();
        final AnchorPanel content = factory.anchorPanel()
            .add(factory.label(Text.translatable("almura.feature.common.text.quantity").append(Text.of(":")).color(WHITE))
                .setAnchor(Anchor.TOP_LEFT).setMargin(0f, 4f, 0f, 0f))
            .add(quantity.setAnchor(Anchor.TOP_RIGHT))
            .add(factory.label(Text.of("Per:").color(WHITE)).setAnchor(Anchor.TOP_LEFT).setMargin(0f, 26f, 0f, 0f))
            .add(factory.label(Text.of(StoreNumbers.currency(price.doubleValue())).color(WHITE)).setAnchor(Anchor.TOP_RIGHT)
                .setMargin(0f, 26f, 0f, 0f))
            .add(factory.label(Text.translatable("almura.feature.common.text.total").append(Text.of(":")).color(WHITE))
                .setAnchor(Anchor.TOP_LEFT).setMargin(0f, 39f, 0f, 0f))
            .add(total.setAnchor(Anchor.TOP_RIGHT).setMargin(0f, 39f, 0f, 0f))
            .add(factory.stackPanel(Orientation.HORIZONTAL).setSpacing(2f)
                .add(factory.button(Text.translatable("almura.button.cancel")).setSize(40f, BUTTON_HEIGHT)
                    .onEvent(Button.PRESSED, event -> overlay[0].close()))
                .add(transact)
                .setAnchor(Anchor.BOTTOM_RIGHT));
        content.setHeight(74f);
        overlay[0] = factory.overlays().dialog(window(factory, Text.translatable("almura.feature.exchange.title.enter_a_quantity"), 130f)
            .add(content.setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP)));
    }

    private static Window window(final ComponentFactory factory, final Text title, final float width) {
        return factory.window(title)
            .setClosable(true)
            .setMovable(true)
            .setResizable(false)
            .setCollapsible(false)
            .setMaximizable(false)
            .setWidth(width);
    }
}
