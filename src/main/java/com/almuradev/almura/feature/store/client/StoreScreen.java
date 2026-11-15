/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.store.client;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.menu.InGameScreens;
import com.almuradev.almura.feature.store.StoreNumbers;
import com.almuradev.almura.feature.store.StoreSide;
import com.almuradev.almura.feature.store.client.StoreItems.Finder;
import com.almuradev.almura.feature.store.network.StoreMessages.ItemView;
import com.almuradev.almura.feature.store.network.StoreMessages.ModifyCandidate;
import com.almuradev.almura.feature.store.network.StoreMessages.ServerboundDelistItems;
import com.almuradev.almura.feature.store.network.StoreMessages.ServerboundModifyItems;
import com.almuradev.almura.feature.store.network.StoreMessages.ServerboundTransaction;
import com.almuradev.almura.feature.store.network.StoreMessages.StoreInfo;
import com.almuradev.almura.feature.menu.ListRows;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.control.attribute.item.Activatable;
import org.inspirenxe.strata.api.control.attribute.item.Selection;
import org.inspirenxe.strata.api.control.attribute.text.TextInput;
import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.button.CheckBox;
import org.inspirenxe.strata.api.control.button.CheckState;
import org.inspirenxe.strata.api.control.collection.ComboBox;
import org.inspirenxe.strata.api.control.collection.ListView;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.control.field.TextBox;
import org.inspirenxe.strata.api.control.window.Window;
import org.inspirenxe.strata.api.event.input.InputEvents;
import org.inspirenxe.strata.api.geometry.Insets;
import org.inspirenxe.strata.api.input.MouseButton;
import org.inspirenxe.strata.api.layout.Anchor;
import org.inspirenxe.strata.api.layout.AnchorPanel;
import org.inspirenxe.strata.api.layout.HorizontalAlignment;
import org.inspirenxe.strata.api.layout.StackPanel;
import org.inspirenxe.strata.api.layout.VerticalAlignment;
import org.inspirenxe.strata.api.screen.Screen;
import org.inspirenxe.strata.api.text.Text;
import org.inspirenxe.strata.api.theme.Border;
import org.inspirenxe.strata.api.theme.Paint;
import org.inspirenxe.strata.api.theme.SpriteId;
import org.inspirenxe.strata.minecraft.MinecraftVisuals;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * A store, as 3.5's {@code StoreScreen}: its Buy and Sell tabs, each with its items searched by name and sorted (shop order, name or price),
 * and Buy/Sell 1, a stack, a quantity or all of the one selected. Store admins in edit mode also get the admin pane, to find items (in the
 * store, the inventory or a creative tab) and list, change or unlist them, and move arrows to arrange the shop order.
 */
final class StoreScreen implements Screen {

    // 3.5's sizes: the store pane, and the admin pane with the gap before it
    private static final float WIDTH = 257f, HEIGHT = 300f, STORE_WIDTH = 251f, ADMIN_WIDTH = 220f, ADMIN_GAP = 4f, MARGIN = 10f;
    private static final float TAB_WIDTH = 119f, TAB_HEIGHT = 16f, BUTTON_HEIGHT = 16f, ROW_HEIGHT = 24f, ICON = 16f;
    private static final float ARROW_WIDTH = 15f, ARROW_HEIGHT = 11f, ARROW_GAP = 4f, ARROW_ICON_WIDTH = 9f, ARROW_ICON_HEIGHT = 5f;
    // The rows' widths, for shortening names: the panes less their padding, borders and the lists' scroll bars
    private static final int STORE_ROW_WIDTH = 225, ADMIN_ROW_WIDTH = 196;
    private static final int WHITE = 0xFFFFFFFF, GRAY = 0xFFAAAAAA, GOLD = 0xFFFFAA00, RED = 0xFFFF5555;
    private static final int DARK_GREEN = 0xFF00AA00, DARK_RED = 0xFFAA0000;
    private static final Paint PANE = Paint.solid(0xFF000000), LINE = Paint.solid(0xB9FFFFFF);
    private static final Paint TAB = Paint.solid(0xFF1E1E1E), TAB_SELECTED = Paint.solid(0xFF000000);
    private static final Paint SOLD_OUT = Paint.solid(0xB4555555);
    private static final Border PANE_BORDER = Border.of(0xB9FFFFFF, 1f);
    private static final Paint ARROW_BUTTON = Paint.nineSlice(SpriteId.of(Almura.ID, "theme/button"), Insets.of(2f));
    private static final Paint ARROW_BUTTON_DISABLED = Paint.nineSlice(SpriteId.of(Almura.ID, "theme/button_disabled"), Insets.of(1f));
    private static final SpriteId ARROW_UP = SpriteId.of(Almura.ID, "guide/arrow_up");
    private static final SpriteId ARROW_DOWN = SpriteId.of(Almura.ID, "guide/arrow_down");

    /**
     * How the store's items are sorted: shop order (as its admins arranged them), by name or by price.
     */
    private enum Sort {
        SHOP_ORDER("almura.feature.store.text.sort.shop_order",
            Comparator.<ItemView>comparingInt(ItemView::index).thenComparing(view -> view.stack().getHoverName().getString().toLowerCase(Locale.ROOT))),
        NAME_ASC("almura.feature.common.text.sort.item_display_name_asc",
            Comparator.comparing(view -> view.stack().getHoverName().getString().toLowerCase(Locale.ROOT))),
        NAME_DESC("almura.feature.common.text.sort.item_display_name_desc", NAME_ASC.comparator.reversed()),
        PRICE_ASC("almura.feature.common.text.sort.price_asc", Comparator.comparing(view -> StoreNumbers.parse(view.price()))),
        PRICE_DESC("almura.feature.common.text.sort.price_desc", PRICE_ASC.comparator.reversed());

        final String key;
        final Comparator<ItemView> comparator;

        Sort(final String key, final Comparator<ItemView> comparator) {
            this.key = key;
            this.comparator = comparator;
        }
    }

    private StoreInfo store;
    private final boolean admin;
    private StoreSide side = StoreSide.BUY;
    private @Nullable ComponentFactory factory;
    private @Nullable Window window;
    private @Nullable CheckBox editModeBox;
    private @Nullable AnchorPanel buyTab, sellTab, adminPane;
    private @Nullable Label buyTabLabel, sellTabLabel, adminTotal;
    private @Nullable TextBox search, adminSearch;
    private @Nullable ComboBox<Sort> sort;
    private @Nullable ComboBox<Finder> finder;
    private @Nullable ListView<ItemView> itemList;
    private @Nullable ListView<ItemStack> adminList;
    private @Nullable Button one, stack, quantity, all, list, unlist;
    private List<ItemStack> adminBase = List.of();
    // The list shows the whole side in shop order (as last searched), so its items may be moved up and down
    private boolean inShopOrder = true;
    private int selected = -1;

    StoreScreen(final StoreInfo store, final boolean admin) {
        this.store = store;
        this.admin = admin;
    }

    String storeId() {
        return this.store.id();
    }

    @Override
    public Text title() {
        return Text.of(this.store.name());
    }

    @Override
    public Component<?> build(final ComponentFactory factory) {
        this.factory = factory;

        // ---- The store pane
        this.buyTabLabel = factory.label("");
        this.sellTabLabel = factory.label("");
        this.buyTab = this.tab(factory, StoreSide.BUY, this.buyTabLabel);
        this.sellTab = this.tab(factory, StoreSide.SELL, this.sellTabLabel);

        final float searchY = TAB_HEIGHT + 3f;
        final Label searchLabel = factory.label(Text.translatable("almura.feature.exchange.text.item_name").append(Text.of(":")).color(WHITE));
        final TextBox search = factory.textBox().setMaxLength(64);
        search.setSize(58f, BUTTON_HEIGHT);
        search.onEvent(TextBox.SUBMITTED, event -> this.search());
        this.search = search;
        final ComboBox<Sort> sort = factory.<Sort>comboBox(value -> Text.translatable(value.key));
        sort.setItems(List.of(Sort.values()));
        sort.selectItem(Sort.SHOP_ORDER);
        sort.setSize(78f, BUTTON_HEIGHT);
        sort.onEvent(Selection.SELECTED, event -> this.search());
        this.sort = sort;
        final Button searchButton = button(factory, Text.translatable("almura.feature.common.button.search"), 50f, this::search);

        final ListView<ItemView> itemList = factory.<ItemView>listView(view -> this.itemRow(factory, view));
        itemList.setRowHeight(ROW_HEIGHT);
        ListRows.style(itemList);
        itemList.onEvent(Selection.SELECTED, event -> {
            this.selected = itemList.selected().map(ItemView::recNo).orElse(-1);
            this.updateStoreButtons();
        });
        // A double click buys or sells one, as 3.5
        itemList.onEvent(Activatable.ACTIVATED, event -> this.transact(1));
        this.itemList = itemList;
        this.one = button(factory, Text.of(""), 60f, () -> this.transact(1));
        this.stack = button(factory, Text.of(""), 60f, () -> this.selectedView().ifPresent(view -> this.transact(view.stack().getMaxStackSize())));
        this.quantity = button(factory, Text.of(""), 60f, this::quantityDialog);
        this.all = button(factory, Text.of(""), 60f, this::transactAll);

        final AnchorPanel storePane = factory.anchorPanel().setBackground(PANE).setBorder(PANE_BORDER).setPadding(3f)
            .add(this.buyTab.setAnchor(Anchor.TOP_LEFT))
            .add(this.sellTab.setAnchor(Anchor.TOP_RIGHT))
            .add(line(factory, 0f, TAB_HEIGHT))
            .add(searchLabel.setAnchor(Anchor.TOP_LEFT).setMargin(1f, searchY + 4f, 0f, 0f))
            .add(search.setAnchor(Anchor.TOP_LEFT).setMargin(52f, searchY, 0f, 0f))
            .add(sort.setAnchor(Anchor.TOP_LEFT).setMargin(112f, searchY, 0f, 0f))
            .add(searchButton.setAnchor(Anchor.TOP_RIGHT).setMargin(0f, searchY, 0f, 0f))
            .add(factory.anchorPanel().setBorder(PANE_BORDER).setPadding(2f)
                .add(itemList.setAnchor(Anchor.FILL))
                .setAnchor(Anchor.FILL).setMargin(0f, searchY + BUTTON_HEIGHT + 2f, 0f, BUTTON_HEIGHT + 3f))
            .add(this.one.setAnchor(Anchor.BOTTOM_LEFT))
            .add(this.stack.setAnchor(Anchor.BOTTOM_LEFT).setMargin(62f, 0f, 0f, 0f))
            .add(this.all.setAnchor(Anchor.BOTTOM_RIGHT))
            .add(this.quantity.setAnchor(Anchor.BOTTOM_RIGHT).setMargin(0f, 0f, 62f, 0f));
        final AnchorPanel content = factory.anchorPanel()
            .add(storePane.setAnchor(Anchor.FILL).setAlignment(HorizontalAlignment.LEFT, VerticalAlignment.STRETCH).setWidth(STORE_WIDTH));

        // ---- The admin pane
        if (this.admin) {
            final ComboBox<Finder> finder = factory.<Finder>comboBox(Finder::name);
            finder.setItems(StoreItems.finders(this.store.id()));
            finder.setSize(184f, BUTTON_HEIGHT);
            finder.onEvent(Selection.SELECTED, event -> this.findItems());
            this.finder = finder;
            final TextBox adminSearch = factory.textBox().setMaxLength(64);
            adminSearch.setSize(ADMIN_WIDTH - 6f, BUTTON_HEIGHT);
            adminSearch.onEvent(TextInput.EDITED, event -> this.filterAdminItems());
            this.adminSearch = adminSearch;
            final ListView<ItemStack> adminList = factory.<ItemStack>listView(item -> this.adminRow(factory, item));
            adminList.setRowHeight(ROW_HEIGHT);
            ListRows.style(adminList);
            adminList.onEvent(Selection.SELECTED, event -> this.updateAdminButtons());
            adminList.onEvent(Activatable.ACTIVATED, event -> this.listOrModify());
            this.adminList = adminList;
            this.list = button(factory, Text.translatable("almura.feature.common.button.list"), 50f, this::listOrModify);
            this.unlist = button(factory, Text.translatable("almura.feature.common.button.unlist"), 50f, this::unlist);
            this.adminTotal = factory.label("");
            final AnchorPanel adminPane = factory.anchorPanel().setBackground(PANE).setBorder(PANE_BORDER).setPadding(3f)
                .add(factory.label(Text.of("Type:").color(WHITE)).setAnchor(Anchor.TOP_LEFT).setMargin(0f, 4f, 0f, 0f))
                .add(finder.setAnchor(Anchor.TOP_LEFT).setMargin(30f, 0f, 0f, 0f))
                .add(adminSearch.setAnchor(Anchor.TOP_LEFT).setMargin(0f, BUTTON_HEIGHT + 2f, 0f, 0f))
                .add(factory.anchorPanel().setBorder(PANE_BORDER).setPadding(2f)
                    .add(adminList.setAnchor(Anchor.FILL))
                    .setAnchor(Anchor.FILL).setMargin(0f, BUTTON_HEIGHT * 2 + 4f, 0f, BUTTON_HEIGHT + 3f))
                .add(this.list.setAnchor(Anchor.BOTTOM_LEFT))
                .add(this.unlist.setAnchor(Anchor.BOTTOM_LEFT).setMargin(52f, 0f, 0f, 0f))
                .add(this.adminTotal.setAnchor(Anchor.BOTTOM_RIGHT).setMargin(0f, 0f, 0f, 4f));
            this.adminPane = adminPane;
            content.add(adminPane.setAnchor(Anchor.FILL).setMargin(STORE_WIDTH + ADMIN_GAP, 0f, 0f, 0f)
                .setAlignment(HorizontalAlignment.LEFT, VerticalAlignment.STRETCH).setWidth(ADMIN_WIDTH));
        }

        final Window window = factory.window(this.title())
            .setMovable(true)
            .setResizable(false)
            .setCollapsible(false)
            .setMaximizable(false)
            .add(content.setAnchor(Anchor.FILL))
            .setAnchor(Anchor.CENTER);
        InGameScreens.closesScreen(window);
        this.window = window;
        final AnchorPanel root = factory.anchorPanel().add(window).setAnchor(Anchor.FILL);
        if (this.admin) {
            // Edit mode, at the left of the title bar: off shows the store as players see it
            final CheckBox editMode = factory.checkBox(Text.translatable("almura.feature.store.checkbox.edit_mode"))
                .setState(ClientStore.editMode() ? CheckState.CHECKED : CheckState.UNCHECKED);
            // A box as tall as its text (Strata's is the text's height plus 8), so it sits inside the title bar as 3.5's small one did
            editMode.setBoxSize(9f);
            editMode.setTooltip(Text.translatable("almura.feature.store.checkbox.edit_mode.tooltip"));
            editMode.onEvent(CheckBox.CHANGED, event -> this.setEditMode(event.value() == CheckState.CHECKED));
            this.editModeBox = editMode;
            // Above the window, which comes to the front when it is clicked and would otherwise cover it
            editMode.setZIndex(1);
            root.add(editMode.setAnchor(Anchor.TOP_LEFT));
            // At the left of the window's title bar, wherever the window is dragged to (Strata reports no move, so it is checked as the
            // window is drawn)
            window.onEvent(Component.PAINT, event -> this.placeEditMode());
            if (this.finder != null && !this.finder.items().isEmpty()) {
                this.finder.select(0);
            }
        }
        this.setEditMode(this.admin && ClientStore.editMode());
        this.refresh();
        return root;
    }

    // ---- From the server

    /**
     * New listings from the server: keeps the side and selection (or moves to the side with items), and searches again.
     */
    void refresh() {
        if (this.itemList == null) {
            return;
        }
        final boolean noBuy = ClientStore.items(this.store.id(), StoreSide.BUY).isEmpty();
        final boolean noSell = ClientStore.items(this.store.id(), StoreSide.SELL).isEmpty();
        if (this.side == StoreSide.BUY && noBuy && !noSell) {
            this.side = StoreSide.SELL;
        } else if (this.side == StoreSide.SELL && noSell) {
            this.side = StoreSide.BUY;
        }
        if (this.buyTab != null && this.sellTab != null) {
            if (noBuy) {
                this.buyTab.setTooltip(Text.translatable("almura.feature.store.text.no_items_for_sale"));
            } else {
                this.buyTab.clearTooltip();
            }
            if (noSell) {
                this.sellTab.setTooltip(Text.translatable("almura.feature.store.text.no_items_for_purchase"));
            } else {
                this.sellTab.clearTooltip();
            }
        }
        this.updateTabs();
        this.search();
        if (this.adminList != null) {
            // The rows show what is listed
            this.adminList.refresh();
            this.updateAdminButtons();
        }
    }

    /**
     * The stores changed: the store's name, or the store went.
     */
    void registryChanged() {
        final StoreInfo now = ClientStore.store(this.store.id()).orElse(null);
        if (now == null) {
            Minecraft.getInstance().gui.screen().onClose();
            return;
        }
        this.store = now;
        if (this.window != null) {
            this.window.setTitle(this.title());
        }
    }

    // ---- The store pane

    private AnchorPanel tab(final ComponentFactory factory, final StoreSide tabSide, final Label label) {
        label.setHitTestVisible(false);
        final AnchorPanel tab = factory.anchorPanel().add(label.setAnchor(Anchor.CENTER).setMargin(0f, 1f, 0f, 0f));
        tab.setSize(TAB_WIDTH, TAB_HEIGHT);
        tab.setBorder(PANE_BORDER);
        tab.onEvent(InputEvents.CLICK, event -> {
            if (event.button() == MouseButton.LEFT && !ClientStore.items(this.store.id(), tabSide).isEmpty()) {
                this.side = tabSide;
                this.updateTabs();
                this.search();
            }
        });
        return tab;
    }

    private void updateTabs() {
        if (this.buyTab == null || this.sellTab == null || this.buyTabLabel == null || this.sellTabLabel == null) {
            return;
        }
        this.buyTab.setBackground(this.side == StoreSide.BUY ? TAB_SELECTED : TAB);
        this.sellTab.setBackground(this.side == StoreSide.SELL ? TAB_SELECTED : TAB);
        this.buyTabLabel.setText(Text.translatable("almura.feature.common.button.buy").color(this.side == StoreSide.BUY ? WHITE : GRAY));
        this.sellTabLabel.setText(Text.translatable("almura.feature.common.button.sell").color(this.side == StoreSide.SELL ? WHITE : GRAY));
    }

    private void search() {
        if (this.itemList == null || this.search == null || this.sort == null) {
            return;
        }
        final String text = this.search.text().trim().toLowerCase(Locale.ROOT);
        final Sort order = this.sort.selected().orElse(Sort.SHOP_ORDER);
        this.inShopOrder = order == Sort.SHOP_ORDER && text.isEmpty();
        final List<ItemView> items = ClientStore.items(this.store.id(), this.side).stream()
            .filter(view -> text.isEmpty() || view.stack().getHoverName().getString().toLowerCase(Locale.ROOT).contains(text))
            .sorted(order.comparator)
            .toList();
        this.itemList.setItems(items);
        this.itemList.refresh();
        // The selected item if it's still there, else the first
        items.stream().filter(view -> view.recNo() == this.selected).findFirst()
            .or(() -> items.stream().findFirst())
            .ifPresentOrElse(this.itemList::selectItem, () -> this.selected = -1);
        this.updateStoreButtons();
    }

    private Optional<ItemView> selectedView() {
        return this.itemList == null ? Optional.empty() : this.itemList.selected();
    }

    private void updateStoreButtons() {
        if (this.one == null || this.stack == null || this.quantity == null || this.all == null) {
            return;
        }
        final String side = this.side.name().toLowerCase(Locale.ROOT);
        final ItemView view = this.selectedView().orElse(null);
        final int maxStack = view == null ? 64 : view.stack().getMaxStackSize();
        this.one.setText(Text.translatable("almura.feature.common.button." + side + ".one"));
        this.stack.setText(Text.translatable("almura.feature.common.button." + side + ".stack", maxStack));
        this.quantity.setText(Text.translatable("almura.feature.common.button." + side + ".quantity"));
        this.all.setText(Text.translatable("almura.feature.common.button." + side + ".all"));
        if (view == null) {
            this.one.setEnabled(false);
            this.stack.setEnabled(false);
            this.quantity.setEnabled(false);
            this.all.setEnabled(false);
            return;
        }
        final boolean available = view.quantity() != 0;
        if (this.side == StoreSide.SELL) {
            final int held = StoreItems.held(view.stack());
            this.one.setEnabled(available && held >= 1);
            this.stack.setEnabled(available && held >= maxStack);
            this.quantity.setEnabled(available && held > 1);
            this.all.setEnabled(available && held >= 1);
        } else {
            final boolean infinite = view.quantity() == StoreNumbers.UNLIMITED;
            this.one.setEnabled(infinite || view.quantity() > 0);
            this.stack.setEnabled(infinite || view.quantity() >= maxStack);
            this.quantity.setEnabled(infinite || view.quantity() > 0);
            this.all.setEnabled(view.quantity() >= 1);
        }
    }

    private void transact(final int amount) {
        final ItemView view = this.selectedView().orElse(null);
        if (view == null || view.quantity() == 0 || amount <= 0) {
            return;
        }
        // Don't try to sell what the player doesn't have
        if (this.side == StoreSide.SELL && StoreItems.held(view.stack()) == 0) {
            return;
        }
        ClientStore.send(new ServerboundTransaction(this.store.id(), this.side, view.recNo(), amount));
    }

    /**
     * Buy all that's left, or sell all the player holds (as much as the store still wants).
     */
    private void transactAll() {
        final ItemView view = this.selectedView().orElse(null);
        if (view == null) {
            return;
        }
        if (this.side == StoreSide.BUY) {
            this.transact(view.quantity());
        } else {
            final int held = StoreItems.held(view.stack());
            this.transact(view.quantity() == StoreNumbers.UNLIMITED ? held : Math.min(held, view.quantity()));
        }
    }

    private void quantityDialog() {
        final ItemView view = this.selectedView().orElse(null);
        if (this.factory != null && view != null) {
            StoreDialogs.quantity(this.factory, this.store.id(), view, this.side);
        }
    }

    // ---- Edit mode and the admin pane

    /**
     * Shows the admin pane (widening the window for it), or hides it so the store looks as it does to players.
     */
    private void setEditMode(final boolean on) {
        if (this.admin) {
            ClientStore.setEditMode(on);
        }
        final boolean editMode = this.admin && on;
        if (this.adminPane != null) {
            this.adminPane.setVisible(editMode);
        }
        final Minecraft minecraft = Minecraft.getInstance();
        final float width = Math.min(WIDTH + (editMode ? ADMIN_WIDTH + ADMIN_GAP : 0f), minecraft.getWindow().getGuiScaledWidth() - MARGIN);
        final float height = Math.min(HEIGHT, minecraft.getWindow().getGuiScaledHeight() - MARGIN);
        if (this.window != null) {
            this.window.setSize(width, height);
        }
        // The rows show their move arrows in edit mode
        if (this.itemList != null) {
            this.itemList.refresh();
        }
    }

    /**
     * Keeps Edit mode at the left of the window's title bar.
     */
    private void placeEditMode() {
        if (this.editModeBox == null || this.window == null) {
            return;
        }
        final float x = this.window.bounds().x() + 4f, y = this.window.bounds().y() + 3f;
        if (this.editModeBox.margin().left() != x || this.editModeBox.margin().top() != y) {
            this.editModeBox.setMargin(x, y, 0f, 0f);
        }
    }

    private boolean editing() {
        return this.admin && ClientStore.editMode();
    }

    private void findItems() {
        if (this.finder == null) {
            return;
        }
        this.adminBase = this.finder.selected().map(found -> found.items().get()).orElse(List.of()).stream()
            .sorted(Comparator.comparing(item -> item.getHoverName().getString()))
            .toList();
        this.filterAdminItems();
    }

    private void filterAdminItems() {
        if (this.adminList == null || this.adminSearch == null || this.adminTotal == null) {
            return;
        }
        final String text = this.adminSearch.text().trim().toLowerCase(Locale.ROOT);
        final List<ItemStack> items = this.adminBase.stream()
            .filter(item -> text.isEmpty() || item.getHoverName().getString().toLowerCase(Locale.ROOT).contains(text))
            .toList();
        this.adminList.setItems(items);
        this.adminList.refresh();
        if (!items.isEmpty()) {
            this.adminList.select(0);
        }
        this.adminTotal.setText(Text.translatable("almura.feature.common.text.total").append(Text.of(": " + items.size())).color(WHITE));
        this.updateAdminButtons();
    }

    private Optional<ItemView> listed(final StoreSide listedSide, final @Nullable ItemStack item) {
        if (item == null) {
            return Optional.empty();
        }
        return ClientStore.items(this.store.id(), listedSide).stream()
            .filter(view -> ItemStack.isSameItemSameComponents(view.stack(), item)).findAny();
    }

    private void updateAdminButtons() {
        if (this.adminList == null || this.list == null || this.unlist == null) {
            return;
        }
        final ItemStack item = this.adminList.selected().orElse(null);
        final boolean listed = this.listed(StoreSide.BUY, item).isPresent() || this.listed(StoreSide.SELL, item).isPresent();
        this.list.setEnabled(item != null);
        this.list.setText(Text.translatable("almura.feature.common.button." + (listed ? "modify" : "list")));
        this.unlist.setEnabled(listed);
    }

    private void listOrModify() {
        final ItemStack item = this.adminList == null ? null : this.adminList.selected().orElse(null);
        if (this.factory == null || item == null) {
            return;
        }
        StoreDialogs.list(this.factory, this.store.id(), item, this.listed(StoreSide.BUY, item).orElse(null),
            this.listed(StoreSide.SELL, item).orElse(null));
    }

    private void unlist() {
        final ItemStack item = this.adminList == null ? null : this.adminList.selected().orElse(null);
        for (final StoreSide listedSide : StoreSide.values()) {
            this.listed(listedSide, item).ifPresent(view ->
                ClientStore.send(new ServerboundDelistItems(this.store.id(), listedSide, List.of(view.recNo()))));
        }
    }

    /**
     * Moves an item one place up (-1) or down (1) the shop order of its side: swaps its index with its neighbour's. If the indexes repeat,
     * so the order can't be told apart, they are numbered 1, 2, 3... first. The server saves it and sends everyone the new order.
     */
    private void move(final ItemView item, final int direction) {
        if (!this.inShopOrder) {
            return;
        }
        final List<ItemView> order = ClientStore.items(this.store.id(), this.side).stream().sorted(Sort.SHOP_ORDER.comparator).toList();
        int from = -1;
        for (int i = 0; i < order.size(); i++) {
            if (order.get(i).recNo() == item.recNo()) {
                from = i;
            }
        }
        final int to = from + direction;
        if (from < 0 || to < 0 || to >= order.size()) {
            return;
        }
        final int[] indexes = new int[order.size()];
        final boolean renumber = order.stream().mapToInt(ItemView::index).distinct().count() < order.size();
        for (int i = 0; i < order.size(); i++) {
            indexes[i] = renumber ? i + 1 : order.get(i).index();
        }
        final int swap = indexes[from];
        indexes[from] = indexes[to];
        indexes[to] = swap;
        final List<ModifyCandidate> changed = new ArrayList<>();
        for (int i = 0; i < order.size(); i++) {
            final ItemView view = order.get(i);
            if (indexes[i] != view.index()) {
                changed.add(new ModifyCandidate(view.recNo(), view.quantity(), indexes[i], view.price()));
            }
        }
        ClientStore.send(new ServerboundModifyItems(this.store.id(), this.side, changed));
    }

    // ---- Rows

    /**
     * A listing: its item, name (with how many are left), price each at the right (with the move arrows in edit mode), and Sold Out over it
     * when there are none left.
     */
    private Component<?> itemRow(final ComponentFactory factory, final ItemView view) {
        final boolean arrows = this.editing();
        final String priceText = StoreNumbers.withSuffix(StoreNumbers.parse(view.price()).doubleValue());
        final Label price = factory.label(Text.of(priceText).color(GOLD).append(Text.of("/ea").color(GRAY))).setTextScale(0.8f);
        if (view.quantity() > 0) {
            price.setTooltip(Text.translatable("almura.feature.exchange.tooltip.total").append(Text.of(": "
                + StoreNumbers.currency(view.quantity() * StoreNumbers.parse(view.price()).doubleValue()))));
        }
        final String suffix = view.quantity() > 0 ? " x " + StoreNumbers.withSuffix(view.quantity()) : "";
        final int priceWidth = (int) (Minecraft.getInstance().font.width(priceText + "/ea") * 0.8f);
        final int nameWidth = STORE_ROW_WIDTH - priceWidth - (int) ICON - (arrows ? (int) (ARROW_WIDTH + ARROW_GAP) : 0) - 16;
        final Label name = factory.label(Text.of(StoreItems.fitName(view.stack(), suffix, nameWidth)).color(WHITE)
            .append(Text.of(suffix).color(GRAY)));
        final AnchorPanel row = factory.anchorPanel()
            .add(factory.image(MinecraftVisuals.item(view.stack())).setImageSize(ICON, ICON).setSize(ICON, ICON)
                .setTooltip(StoreItems.tooltip(view.stack())).setAnchor(Anchor.MIDDLE_LEFT).setMargin(2f, 0f, 0f, 0f))
            .add(name.setAnchor(Anchor.MIDDLE_LEFT).setMargin(ICON + 6f, 0f, 0f, 0f))
            .add(price.setAnchor(Anchor.MIDDLE_RIGHT).setMargin(0f, 0f, arrows ? ARROW_WIDTH + ARROW_GAP : 6f, 0f));
        if (view.quantity() == 0) {
            final AnchorPanel soldOut = factory.anchorPanel().setBackground(SOLD_OUT)
                .add(factory.label(Text.translatable("almura.feature.store.text.soldout").color(RED)).setTextScale(2f)
                    .setAnchor(Anchor.CENTER).setMargin(0f, 1f, 0f, 0f));
            soldOut.setHitTestVisible(false);
            row.add(soldOut.setAnchor(Anchor.FILL));
        }
        if (arrows) {
            // After the sold out cover, so they stay clickable on sold out items
            final List<ItemView> order = this.itemList == null ? List.of() : this.itemList.items();
            final boolean first = order.isEmpty() || order.getFirst().recNo() == view.recNo();
            final boolean last = order.isEmpty() || order.getLast().recNo() == view.recNo();
            final StackPanel stackedArrows = factory.stackPanel().setSpacing(-1f)
                .add(this.arrow(factory, ARROW_UP, view, -1, this.inShopOrder && !first))
                .add(this.arrow(factory, ARROW_DOWN, view, 1, this.inShopOrder && !last));
            row.add(stackedArrows.setAnchor(Anchor.MIDDLE_RIGHT).setMargin(0f, 0f, 2f, 0f));
        }
        ListRows.box(row, this.itemList != null && this.itemList.selected().filter(view::equals).isPresent());
        return row.setAnchor(Anchor.FILL).setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.STRETCH);
    }

    private Component<?> arrow(final ComponentFactory factory, final SpriteId icon, final ItemView view, final int direction,
        final boolean enabled) {
        final AnchorPanel arrow = factory.anchorPanel()
            .setBackground(enabled ? ARROW_BUTTON : ARROW_BUTTON_DISABLED)
            .add(factory.image(icon).setImageSize(ARROW_ICON_WIDTH, ARROW_ICON_HEIGHT).setSize(ARROW_ICON_WIDTH, ARROW_ICON_HEIGHT)
                .setOpacity(enabled ? 1f : 0.35f).setAnchor(Anchor.CENTER));
        arrow.setSize(ARROW_WIDTH, ARROW_HEIGHT);
        arrow.setTooltip(this.inShopOrder
            ? Text.translatable(direction < 0 ? "almura.feature.store.button.move_up" : "almura.feature.store.button.move_down")
            : Text.translatable("almura.feature.store.button.move.shop_order_only"));
        if (enabled) {
            arrow.onEvent(InputEvents.CLICK, event -> {
                if (event.button() == MouseButton.LEFT) {
                    this.move(view, direction);
                }
            });
        }
        return arrow;
    }

    /**
     * An item to list: its item and name, its buying (B:) and selling (S:) prices if listed, and a strip at the right, green or red at the top
     * for buying and at the bottom for selling.
     */
    private Component<?> adminRow(final ComponentFactory factory, final ItemStack item) {
        final ItemView buying = this.listed(StoreSide.BUY, item).orElse(null);
        final ItemView selling = this.listed(StoreSide.SELL, item).orElse(null);
        final boolean listed = buying != null || selling != null;
        final StackPanel prices = factory.stackPanel().setSpacing(1f);
        int priceWidth = 0;
        for (final ItemView view : new ItemView[] {buying, selling}) {
            if (view == null) {
                continue;
            }
            final String label = (view == buying ? "B: " : "S: ");
            final String amount = StoreNumbers.withSuffix(StoreNumbers.parse(view.price()).doubleValue());
            prices.add(factory.label(Text.of(label).color(GRAY).append(Text.of(amount).color(GOLD)).append(Text.of("/ea").color(GRAY)))
                .setTextScale(0.8f).setAlignment(HorizontalAlignment.RIGHT, VerticalAlignment.TOP));
            priceWidth = Math.max(priceWidth, (int) (Minecraft.getInstance().font.width(label + amount + "/ea") * 0.8f));
        }
        final int nameWidth = ADMIN_ROW_WIDTH - priceWidth - (int) ICON - (listed ? 5 : 0) - 24;
        final AnchorPanel row = factory.anchorPanel()
            .add(factory.image(MinecraftVisuals.item(item)).setImageSize(ICON, ICON).setSize(ICON, ICON)
                .setTooltip(StoreItems.tooltip(item)).setAnchor(Anchor.MIDDLE_LEFT).setMargin(2f, 0f, 0f, 0f))
            .add(factory.label(Text.of(StoreItems.fitName(item, "", nameWidth)).color(WHITE))
                .setAnchor(Anchor.MIDDLE_LEFT).setMargin(ICON + 6f, 0f, 0f, 0f))
            .add(prices.setAnchor(Anchor.MIDDLE_RIGHT).setMargin(0f, 0f, listed ? 11f : 6f, 0f));
        if (listed) {
            final StackPanel strip = factory.stackPanel()
                .add(factory.anchorPanel().setBackground(Paint.solid(buying != null ? DARK_GREEN : DARK_RED)).setSize(5f, ROW_HEIGHT / 2f - 2f))
                .add(factory.anchorPanel().setBackground(Paint.solid(selling != null ? DARK_GREEN : DARK_RED)).setSize(5f, ROW_HEIGHT / 2f - 2f));
            row.add(strip.setAnchor(Anchor.MIDDLE_RIGHT).setMargin(0f, 0f, 2f, 0f));
        }
        row.setHitTestVisible(true);
        ListRows.box(row, this.adminList != null && this.adminList.selected().filter(selected -> selected == item).isPresent());
        return row.setAnchor(Anchor.FILL).setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.STRETCH);
    }

    // ---- Helpers

    private static Button button(final ComponentFactory factory, final Text text, final float width, final Runnable press) {
        return factory.button(text).setSize(width, BUTTON_HEIGHT).onEvent(Button.PRESSED, event -> press.run());
    }

    private static Component<?> line(final ComponentFactory factory, final float x, final float y) {
        return factory.anchorPanel().setBackground(LINE).setHeight(1f).setAnchor(Anchor.FILL).setMargin(x, y, 0f, 0f)
            .setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP);
    }
}
