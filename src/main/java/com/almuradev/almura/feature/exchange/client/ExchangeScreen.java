/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.client;

import com.almuradev.almura.feature.exchange.ExchangeMoney;
import com.almuradev.almura.feature.exchange.ExchangeSort;
import com.almuradev.almura.feature.exchange.ListStatusType;
import com.almuradev.almura.feature.exchange.network.ExchangeMessages.ExchangeInfo;
import com.almuradev.almura.feature.exchange.network.ExchangeMessages.ForSaleView;
import com.almuradev.almura.feature.exchange.network.ExchangeMessages.ListItemView;
import com.almuradev.almura.feature.exchange.network.ExchangeMessages.ServerboundBuy;
import com.almuradev.almura.feature.exchange.network.ExchangeMessages.ServerboundListStatus;
import com.almuradev.almura.feature.exchange.network.ExchangeMessages.ServerboundOfferRequest;
import com.almuradev.almura.feature.exchange.network.ExchangeMessages.ServerboundQuery;
import com.almuradev.almura.feature.menu.InGameScreens;
import com.almuradev.almura.feature.notification.PopupNotification;
import com.almuradev.almura.feature.notification.client.ClientNotifications;
import com.almuradev.almura.feature.menu.ListRows;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.world.item.ItemStack;
import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.component.HasContextMenu;
import org.inspirenxe.strata.api.control.attribute.item.Activatable;
import org.inspirenxe.strata.api.control.attribute.item.Selection;
import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.collection.ComboBox;
import org.inspirenxe.strata.api.control.collection.ListView;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.control.field.TextBox;
import org.inspirenxe.strata.api.control.menu.Menu;
import org.inspirenxe.strata.api.control.menu.MenuItem;
import org.inspirenxe.strata.api.control.window.Window;
import org.inspirenxe.strata.api.event.input.InputEvents;
import org.inspirenxe.strata.api.input.MouseButton;
import org.inspirenxe.strata.api.layout.Anchor;
import org.inspirenxe.strata.api.layout.AnchorPanel;
import org.inspirenxe.strata.api.layout.HorizontalAlignment;
import org.inspirenxe.strata.api.layout.VerticalAlignment;
import org.inspirenxe.strata.api.screen.Screen;
import org.inspirenxe.strata.api.text.Text;
import org.inspirenxe.strata.api.theme.Border;
import org.inspirenxe.strata.api.theme.Paint;
import org.inspirenxe.strata.minecraft.control.ItemDisplay;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * An exchange, as 3.5's {@code ExchangeScreen}: at the left the items for sale (searched by item and seller name, sorted by the box or the
 * column headings, ten a page) with the buy buttons under them; at the right, for a player who may list here, their items on the exchange,
 * with List (or Unlist) and +/- (the offer screen, moving items between their inventory and the exchange). Right click a row for its menu.
 */
final class ExchangeScreen implements Screen {

    // 3.5's sizes
    private static final float WIDTH = 600f, HEIGHT = 370f, HALF_WIDTH = 301f, PANEL_WIDTH = 295f, SEARCH_HEIGHT = 322f, BUY_HEIGHT = 21f;
    private static final float LIST_HEIGHT = 345f, BUTTON_HEIGHT = 16f, ROW_HEIGHT = 24f, FOR_SALE_ROW_HEIGHT = 24f, LABEL_WIDTH = 64f;
    private static final float HEADER_TOP = 39f, HEADER_HEIGHT = 12f, HEADER_ITEM_X = 24f, HEADER_RIGHT_INSET = 5f, MARGIN = 10f;
    private static final int PAGE_SIZE = 10;
    private static final int WHITE = 0xFFFFFFFF, GRAY = 0xFFAAAAAA, GOLD = 0xFFFFAA00, RED = 0xFFFF5555, DARK_GREEN = 0xFF00AA00;
    private static final Paint PANEL = Paint.solid(0xD7000000), LINE = Paint.solid(0xB9FFFFFF), LISTED = Paint.solid(DARK_GREEN);
    private static final Border PANEL_BORDER = Border.of(0xB9FFFFFF, 1f), OWN_BORDER = Border.of(0xFF55FFFF, 1f);

    private final ExchangeInfo exchange;
    private final int limit;
    private final boolean admin;
    private @Nullable ComponentFactory factory;
    private @Nullable ListView<ForSaleView> forSaleList;
    private @Nullable ListView<ListItemView> listItemList;
    private @Nullable TextBox itemName, sellerName;
    private @Nullable ComboBox<ExchangeSort> sort;
    private @Nullable Label pageLabel, limitLabel, noResults, headerItem, headerPrice, headerSeller;
    private @Nullable AnchorPanel headerPricePanel;
    private @Nullable Button firstPage, previousPage, nextPage, lastPage, buyOne, buyStack, buyQuantity, buyAll, list;
    private List<ForSaleView> forSale = List.of();
    private List<ListItemView> listItems = List.of();
    private int currentPage = 1, pages = 1;
    // The seller column's width on this page: prices end this far from the right, as 3.5
    private float sellerColumn = 40f;
    private boolean selecting;

    ExchangeScreen(final ExchangeInfo exchange, final int limit, final boolean admin) {
        this.exchange = exchange;
        this.limit = limit;
        this.admin = admin;
    }

    String id() {
        return this.exchange.id();
    }

    @Override
    public Text title() {
        return Text.of(this.exchange.name());
    }

    @Override
    public Component<?> build(final ComponentFactory factory) {
        this.factory = factory;
        final Minecraft minecraft = Minecraft.getInstance();
        // As 3.5: the exchange needs room
        if (minecraft.getWindow().getGuiScaledWidth() < WIDTH || minecraft.getWindow().getGuiScaledHeight() < HEIGHT) {
            ClientNotifications.show(new PopupNotification("Exchange Error", "Screen area of: " + (int) HEIGHT + " x " + (int) WIDTH
                + " required.", 5));
        }
        this.sellerColumn = sellerColumnWidth(List.of());

        // ---- Search
        this.itemName = factory.textBox().setMaxLength(64);
        this.itemName.onEvent(TextBox.SUBMITTED, event -> this.searchFirstPage());
        this.itemName.setSize(110f, 16f);
        this.sellerName = factory.textBox().setMaxLength(16);
        this.sellerName.onEvent(TextBox.SUBMITTED, event -> this.searchFirstPage());
        this.sellerName.setSize(110f, 16f);
        final Button clear = factory.button(Text.of("X").color(RED)).setTextPadding(1f, 3f, 1f, 1f);
        clear.setMinSize(0f, 0f);
        clear.setSize(14f, 14f).setTooltip(Text.translatable("almura.feature.exchange.tooltip.clear_filters"));
        clear.onEvent(Button.PRESSED, event -> this.searchFor("", ""));
        final ComboBox<ExchangeSort> sort = factory.comboBox(type -> Text.translatable(type.key()));
        sort.setItems(List.of(ExchangeSort.values()));
        sort.selectItem(ExchangeOptions.defaultSortType());
        sort.setSize(78f, 14f);
        sort.onEvent(Selection.SELECTED, event -> {
            if (!this.selecting) {
                this.updateHeaders();
                this.searchFirstPage();
            }
        });
        this.sort = sort;
        final Button search = button(factory, Text.translatable("almura.feature.common.button.search"), 60f, this::searchFirstPage);
        final Button options = button(factory, Text.of("Options"), 46f, () -> ExchangeDialogs.options(factory, this));

        // Column headings: click to sort by one, ascending, then descending
        this.headerItem = header(factory);
        this.headerPrice = header(factory);
        this.headerSeller = header(factory);
        final AnchorPanel headerItemPanel = clickable(factory, this.headerItem, () -> this.sortBy(ExchangeSort.ITEM_NAME_ASC, ExchangeSort.ITEM_NAME_DESC));
        this.headerPricePanel = clickable(factory, this.headerPrice, () -> this.sortBy(ExchangeSort.PRICE_ASC, ExchangeSort.PRICE_DESC));
        final AnchorPanel headerSellerPanel = clickable(factory, this.headerSeller, () -> this.sortBy(ExchangeSort.SELLER_ASC, ExchangeSort.SELLER_DESC));
        this.updateHeaders();

        final ListView<ForSaleView> forSaleList = factory.<ForSaleView>listView(item -> this.forSaleRow(factory, item));
        forSaleList.setRowHeight(FOR_SALE_ROW_HEIGHT);
        ListRows.style(forSaleList);
        forSaleList.onEvent(Selection.SELECTED, event -> this.updateControls());
        // A double click buys one, as 3.5
        forSaleList.onEvent(Activatable.ACTIVATED, event -> this.buy(1));
        this.forSaleList = forSaleList;
        this.noResults = factory.label(Text.of("No results found.").color(GRAY));

        this.firstPage = button(factory, Text.of("|<"), 20f, () -> this.setPage(1));
        this.previousPage = button(factory, Text.of("<"), 20f, () -> this.setPage(this.currentPage - 1));
        this.nextPage = button(factory, Text.of(">"), 20f, () -> this.setPage(this.currentPage + 1));
        this.lastPage = button(factory, Text.of(">|"), 20f, () -> this.setPage(this.pages));
        this.pageLabel = factory.label("");

        final AnchorPanel searchPanel = panel(factory)
            .add(label(factory, "almura.feature.exchange.text.item_name").setAnchor(Anchor.TOP_LEFT).setMargin(0f, 3f, 0f, 0f))
            .add(this.itemName.setAnchor(Anchor.TOP_LEFT).setMargin(LABEL_WIDTH + 2f, 1f, 0f, 0f))
            .add(clear.setAnchor(Anchor.TOP_LEFT).setMargin(LABEL_WIDTH + 2f + 110f + 3f, 0f, 0f, 0f))
            .add(sort.setAnchor(Anchor.TOP_RIGHT).setMargin(0f, 0f, 1f, 0f))
            .add(label(factory, "almura.feature.exchange.text.seller").setAnchor(Anchor.TOP_LEFT).setMargin(0f, 20f, 0f, 0f))
            .add(this.sellerName.setAnchor(Anchor.TOP_LEFT).setMargin(LABEL_WIDTH + 2f, 18f, 0f, 0f))
            .add(options.setAnchor(Anchor.TOP_RIGHT).setMargin(0f, 17f, 64f, 0f))
            .add(search.setAnchor(Anchor.TOP_RIGHT).setMargin(0f, 17f, 2f, 0f))
            .add(line(factory, 36f, false))
            .add(headerItemPanel.setAnchor(Anchor.TOP_LEFT).setMargin(HEADER_ITEM_X, HEADER_TOP, 0f, 0f))
            .add(this.headerPricePanel.setAnchor(Anchor.TOP_RIGHT).setMargin(0f, HEADER_TOP, this.priceInset(), 0f))
            .add(headerSellerPanel.setAnchor(Anchor.TOP_RIGHT).setMargin(0f, HEADER_TOP, HEADER_RIGHT_INSET, 0f))
            .add(forSaleList.setAnchor(Anchor.FILL).setMargin(0f, HEADER_TOP + HEADER_HEIGHT, 0f, BUTTON_HEIGHT + 6f))
            .add(this.noResults.setAnchor(Anchor.TOP_CENTER).setMargin(0f, HEADER_TOP + HEADER_HEIGHT + 8f, 0f, 0f))
            .add(line(factory, BUTTON_HEIGHT + 3f, true))
            .add(this.firstPage.setAnchor(Anchor.BOTTOM_LEFT))
            .add(this.previousPage.setAnchor(Anchor.BOTTOM_LEFT).setMargin(22f, 0f, 0f, 0f))
            .add(this.pageLabel.setAnchor(Anchor.BOTTOM_CENTER).setMargin(0f, 0f, 0f, 4f))
            .add(this.nextPage.setAnchor(Anchor.BOTTOM_RIGHT).setMargin(0f, 0f, 22f, 0f))
            .add(this.lastPage.setAnchor(Anchor.BOTTOM_RIGHT));
        searchPanel.setAnchor(Anchor.TOP_LEFT).setSize(PANEL_WIDTH, SEARCH_HEIGHT);

        // ---- Buy
        this.buyOne = button(factory, Text.translatable("almura.feature.common.button.buy.one"), 60f, () -> this.buy(1));
        if (this.admin) {
            this.buyOne.setTooltip(Text.of("Hold Ctrl to delist (admin)"));
        }
        this.buyStack = button(factory, Text.translatable("almura.feature.common.button.buy.stack", 0), 60f, () -> this.selectedForSale()
            .ifPresent(item -> this.buy(item.item().getMaxStackSize())));
        this.buyQuantity = button(factory, Text.translatable("almura.feature.common.button.buy.quantity"), 60f, () -> this.selectedForSale()
            .ifPresent(item -> ExchangeDialogs.buyQuantity(factory, this, item)));
        this.buyAll = button(factory, Text.translatable("almura.feature.common.button.buy.all"), 50f, () -> this.selectedForSale()
            .ifPresent(item -> this.buy(item.quantity())));
        final AnchorPanel buyPanel = factory.anchorPanel().setBackground(PANEL).setBorder(PANEL_BORDER).setPadding(3f, 0f)
            .add(this.buyOne.setAnchor(Anchor.MIDDLE_LEFT))
            .add(this.buyStack.setAnchor(Anchor.MIDDLE_LEFT).setMargin(70f, 0f, 0f, 0f))
            .add(this.buyQuantity.setAnchor(Anchor.MIDDLE_RIGHT).setMargin(0f, 0f, 60f, 0f))
            .add(this.buyAll.setAnchor(Anchor.MIDDLE_RIGHT));
        buyPanel.setAnchor(Anchor.TOP_LEFT).setMargin(0f, SEARCH_HEIGHT + 2f, 0f, 0f).setSize(PANEL_WIDTH, BUY_HEIGHT);

        final AnchorPanel content = factory.anchorPanel().add(searchPanel).add(buyPanel);

        // ---- The player's items, if they may list here
        if (this.limit > 0) {
            final ListView<ListItemView> listItemList = factory.<ListItemView>listView(item -> this.listItemRow(factory, item));
            listItemList.setRowHeight(ROW_HEIGHT);
            ListRows.style(listItemList);
            listItemList.onEvent(Selection.SELECTED, event -> this.updateControls());
            listItemList.onEvent(Activatable.ACTIVATED, event -> this.list());
            this.listItemList = listItemList;
            this.list = button(factory, Text.translatable("almura.feature.common.button.list"), 40f, this::list);
            this.limitLabel = factory.label("");
            final Button offer = button(factory, Text.of("+").color(DARK_GREEN).append(Text.of("/").color(GRAY)).append(Text.of("-").color(RED)),
                30f, () -> ClientExchange.send(new ServerboundOfferRequest(this.exchange.id())));
            final AnchorPanel listPanel = panel(factory)
                .add(factory.label(Text.translatable("almura.feature.exchange.title.listable_items").color(WHITE))
                    .setAnchor(Anchor.TOP_CENTER).setMargin(0f, 2f, 0f, 0f))
                .add(line(factory, 14f, false))
                .add(listItemList.setAnchor(Anchor.FILL).setMargin(0f, 17f, 0f, BUTTON_HEIGHT + 6f))
                .add(line(factory, BUTTON_HEIGHT + 3f, true))
                .add(this.list.setAnchor(Anchor.BOTTOM_LEFT))
                .add(this.limitLabel.setAnchor(Anchor.BOTTOM_CENTER).setMargin(0f, 0f, 0f, 4f))
                .add(offer.setAnchor(Anchor.BOTTOM_RIGHT));
            listPanel.setAnchor(Anchor.TOP_RIGHT).setSize(PANEL_WIDTH, LIST_HEIGHT);
            content.add(listPanel);
        }

        final Window window = factory.window(this.title())
            .setMovable(true)
            .setResizable(false)
            .setCollapsible(false)
            .setMaximizable(false)
            .add(content.setAnchor(Anchor.FILL))
            .setSize(Math.min(this.limit > 0 ? WIDTH : HALF_WIDTH, minecraft.getWindow().getGuiScaledWidth() - MARGIN),
                Math.min(HEIGHT, minecraft.getWindow().getGuiScaledHeight() - MARGIN))
            .setAnchor(Anchor.CENTER);
        InGameScreens.closesScreen(window);
        this.showListItems(this.listItems);
        this.showForSale(this.forSale, this.forSale.size());
        this.search();
        return factory.anchorPanel().add(window).setAnchor(Anchor.FILL);
    }

    // ---- From the server

    void showListItems(final List<ListItemView> items) {
        this.listItems = List.copyOf(items);
        if (this.listItemList == null) {
            return;
        }
        final int selected = this.listItemList.selected().map(ListItemView::recNo).orElse(-1);
        this.selecting = true;
        try {
            this.listItemList.setItems(this.listItems);
            this.listItemList.refresh();
            this.listItems.stream().filter(item -> item.recNo() == selected).findFirst().ifPresent(this.listItemList::selectItem);
        } finally {
            this.selecting = false;
        }
        this.updateControls();
    }

    void showForSale(final List<ForSaleView> items, final int total) {
        this.forSale = List.copyOf(items);
        if (this.forSaleList == null) {
            return;
        }
        // Prices line up just left of the widest seller name on the page
        this.sellerColumn = sellerColumnWidth(this.forSale);
        if (this.headerPricePanel != null) {
            this.headerPricePanel.setMargin(0f, HEADER_TOP, this.priceInset(), 0f);
        }
        this.pages = Math.max(total - 1, 1) / PAGE_SIZE + 1;
        this.currentPage = this.currentPage <= this.pages ? this.currentPage : 1;
        final int selected = this.forSaleList.selected().map(ForSaleView::listItemRecNo).orElse(-1);
        this.selecting = true;
        try {
            this.forSaleList.setItems(this.forSale);
            this.forSaleList.refresh();
            this.forSale.stream().filter(item -> item.listItemRecNo() == selected).findFirst().ifPresent(this.forSaleList::selectItem);
        } finally {
            this.selecting = false;
        }
        this.updateControls();
    }

    /**
     * Asks for the current page of the search, as it stands (3.5's search; also when the server says what's for sale changed).
     */
    void search() {
        if (this.itemName == null || this.sellerName == null || this.sort == null) {
            return;
        }
        ClientExchange.send(new ServerboundQuery(this.exchange.id(), this.itemName.text().trim(), this.sellerName.text().trim(),
            ExchangeOptions.hideOwnListings(), this.sort.selected().orElse(ExchangeSort.ITEM_NAME_ASC), (this.currentPage - 1) * PAGE_SIZE,
            PAGE_SIZE));
    }

    /**
     * The offer screen, with the player's unlisted items on the exchange.
     */
    void openOffer(final int limit) {
        if (this.factory == null) {
            return;
        }
        final int listed = (int) this.listItems.stream().filter(ListItemView::listed).count();
        new ExchangeOfferDialog(this.factory, this.exchange.id(), limit, listed, this.listItems.stream().filter(item -> !item.listed()).toList());
    }

    /**
     * The options changed: back to the default order, searched again.
     */
    void applyOptions() {
        if (this.sort == null) {
            return;
        }
        this.selecting = true;
        try {
            this.sort.selectItem(ExchangeOptions.defaultSortType());
        } finally {
            this.selecting = false;
        }
        this.updateHeaders();
        this.searchFirstPage();
    }

    // ---- Actions

    private void searchFirstPage() {
        this.currentPage = 1;
        this.search();
    }

    private void searchFor(final String item, final String seller) {
        if (this.itemName != null && this.sellerName != null) {
            this.itemName.setText(item);
            this.sellerName.setText(seller);
        }
        this.searchFirstPage();
    }

    private void setPage(final int page) {
        this.currentPage = Math.max(1, Math.min(page, this.pages));
        if (this.forSaleList != null) {
            this.forSaleList.clearSelection();
        }
        this.search();
    }

    private void sortBy(final ExchangeSort ascending, final ExchangeSort descending) {
        if (this.sort == null) {
            return;
        }
        final ExchangeSort current = this.sort.selected().orElse(null);
        this.selecting = true;
        try {
            this.sort.selectItem(current == ascending ? descending : ascending);
        } finally {
            this.selecting = false;
        }
        this.updateHeaders();
        this.searchFirstPage();
    }

    /**
     * Buys some of the selected item; an administrator holding Ctrl delists it instead, as 3.5.
     */
    void buy(final int quantity) {
        this.selectedForSale().ifPresent(item -> {
            if (this.admin && Minecraft.getInstance().hasControlDown()) {
                ClientExchange.send(new ServerboundListStatus(ListStatusType.ADMIN_DE_LIST, this.exchange.id(), item.listItemRecNo(), ""));
            } else {
                ClientExchange.send(new ServerboundBuy(this.exchange.id(), item.listItemRecNo(), quantity));
            }
        });
    }

    private void list() {
        if (this.listItemList == null || this.factory == null) {
            return;
        }
        this.listItemList.selected().ifPresent(item -> {
            if (item.listed()) {
                ClientExchange.send(new ServerboundListStatus(ListStatusType.DE_LIST, this.exchange.id(), item.recNo(), ""));
            } else {
                ExchangeDialogs.price(this.factory, this.exchange.id(), item.recNo(), ListStatusType.LIST,
                    item.lastKnownPrice().isEmpty() ? null : ExchangeMoney.parse(item.lastKnownPrice()));
            }
        });
    }

    private Optional<ForSaleView> selectedForSale() {
        return this.forSaleList == null ? Optional.empty() : this.forSaleList.selected();
    }

    // ---- Menus

    private Menu forSaleMenu(final ComponentFactory factory, final ForSaleView item) {
        if (this.forSaleList != null) {
            this.forSaleList.selectItem(item);
        }
        final String id = this.exchange.id();
        final int quantity = item.quantity();
        final int stackSize = item.item().getMaxStackSize();
        final Minecraft minecraft = Minecraft.getInstance();
        final boolean seller = minecraft.player != null && minecraft.player.getUUID().equals(item.seller());
        final Menu menu = factory.menu();
        menu.addItem(menuItem(factory, Text.translatable("almura.feature.common.button.buy.one"), true,
            () -> ClientExchange.send(new ServerboundBuy(id, item.listItemRecNo(), 1))));
        menu.addItem(menuItem(factory, Text.translatable("almura.feature.common.button.buy.stack", stackSize), quantity > 1 && quantity >= stackSize,
            () -> ClientExchange.send(new ServerboundBuy(id, item.listItemRecNo(), stackSize))));
        menu.addItem(menuItem(factory, Text.translatable("almura.feature.exchange.menu.buy_quantity"), quantity > 1,
            () -> ExchangeDialogs.buyQuantity(factory, this, item)));
        menu.addItem(menuItem(factory, Text.translatable("almura.feature.common.button.buy.all"), quantity > 1,
            () -> ClientExchange.send(new ServerboundBuy(id, item.listItemRecNo(), quantity))));
        menu.addSeparator();
        menu.addItem(menuItem(factory, Text.translatable("almura.feature.exchange.menu.search_item"), true,
            () -> this.searchFor(item.item().getHoverName().getString(), "")));
        menu.addItem(menuItem(factory, Text.translatable("almura.feature.exchange.menu.search_seller"), !item.sellerName().isEmpty(),
            () -> this.searchFor("", item.sellerName())));
        final BigDecimal price = ExchangeMoney.parse(item.price());
        if (seller) {
            menu.addSeparator();
            menu.addItem(menuItem(factory, Text.translatable("almura.feature.exchange.menu.change_price"), true,
                () -> ExchangeDialogs.price(factory, id, item.listItemRecNo(), ListStatusType.ADJUST_PRICE, price)));
            menu.addItem(menuItem(factory, Text.translatable("almura.feature.common.button.unlist"), true,
                () -> ClientExchange.send(new ServerboundListStatus(ListStatusType.DE_LIST, id, item.listItemRecNo(), ""))));
        } else if (this.admin) {
            menu.addSeparator();
            menu.addItem(menuItem(factory, Text.translatable("almura.feature.exchange.menu.admin_change_price"), true,
                () -> ExchangeDialogs.price(factory, id, item.listItemRecNo(), ListStatusType.ADMIN_ADJUST_PRICE, price)));
            menu.addItem(menuItem(factory, Text.translatable("almura.feature.exchange.menu.admin_delist"), true,
                () -> ClientExchange.send(new ServerboundListStatus(ListStatusType.ADMIN_DE_LIST, id, item.listItemRecNo(), ""))));
        }
        return menu;
    }

    private Menu listItemMenu(final ComponentFactory factory, final ListItemView item) {
        if (this.listItemList != null) {
            this.listItemList.selectItem(item);
        }
        final String id = this.exchange.id();
        final Menu menu = factory.menu();
        if (!item.listed()) {
            menu.addItem(menuItem(factory, Text.translatable("almura.feature.exchange.menu.list"), true,
                () -> ExchangeDialogs.price(factory, id, item.recNo(), ListStatusType.LIST,
                    item.lastKnownPrice().isEmpty() ? null : ExchangeMoney.parse(item.lastKnownPrice()))));
        } else {
            menu.addItem(menuItem(factory, Text.translatable("almura.feature.exchange.menu.change_price"), true,
                () -> ExchangeDialogs.price(factory, id, item.recNo(), ListStatusType.ADJUST_PRICE, ExchangeMoney.parse(item.price()))));
            menu.addItem(menuItem(factory, Text.translatable("almura.feature.common.button.unlist"), true,
                () -> ClientExchange.send(new ServerboundListStatus(ListStatusType.DE_LIST, id, item.recNo(), ""))));
        }
        return menu;
    }

    private static MenuItem menuItem(final ComponentFactory factory, final Text text, final boolean enabled, final Runnable action) {
        return factory.menuItem(text).setEnabled(enabled).setAction(action);
    }

    // ---- State

    private void updateControls() {
        if (this.buyOne == null || this.buyStack == null || this.buyQuantity == null || this.buyAll == null || this.firstPage == null
            || this.previousPage == null || this.nextPage == null || this.lastPage == null || this.pageLabel == null || this.noResults == null
            || this.forSaleList == null) {
            return;
        }
        final ForSaleView selected = this.forSaleList.selected().orElse(null);
        final boolean isSelected = selected != null;
        final int maxStack = isSelected ? selected.item().getMaxStackSize() : 0;
        this.buyOne.setEnabled(isSelected);
        this.buyQuantity.setEnabled(isSelected && selected.quantity() > 1);
        this.buyStack.setEnabled(isSelected && selected.quantity() > 1 && selected.quantity() >= maxStack);
        this.buyStack.setText(Text.translatable("almura.feature.common.button.buy.stack", maxStack));
        this.buyAll.setEnabled(isSelected && selected.quantity() > 1);
        this.firstPage.setEnabled(this.currentPage != 1);
        this.previousPage.setEnabled(this.currentPage != 1);
        this.nextPage.setEnabled(this.currentPage != this.pages);
        this.lastPage.setEnabled(this.currentPage != this.pages);
        this.pageLabel.setText(Text.of(this.currentPage + "/" + this.pages).color(WHITE));
        final boolean empty = this.forSale.isEmpty();
        this.forSaleList.setVisible(!empty);
        this.noResults.setVisible(empty);
        if (this.list != null && this.listItemList != null && this.limitLabel != null) {
            final ListItemView item = this.listItemList.selected().orElse(null);
            this.list.setEnabled(item != null);
            this.list.setText(Text.translatable("almura.feature.common.button." + (item != null && item.listed() ? "unlist" : "list")));
            this.limitLabel.setText(Text.of(this.listItems.size() + "/" + this.limit).color(WHITE));
        }
    }

    private void updateHeaders() {
        if (this.headerItem == null || this.headerPrice == null || this.headerSeller == null || this.sort == null) {
            return;
        }
        final ExchangeSort current = this.sort.selected().orElse(null);
        setHeader(this.headerItem, "Item Name", current, ExchangeSort.ITEM_NAME_ASC, ExchangeSort.ITEM_NAME_DESC);
        setHeader(this.headerPrice, "Price", current, ExchangeSort.PRICE_ASC, ExchangeSort.PRICE_DESC);
        setHeader(this.headerSeller, "Seller", current, ExchangeSort.SELLER_ASC, ExchangeSort.SELLER_DESC);
    }

    private static void setHeader(final Label header, final String text, final @Nullable ExchangeSort current, final ExchangeSort ascending,
        final ExchangeSort descending) {
        if (current == ascending || current == descending) {
            header.setText(Text.of(text + (current == ascending ? " ▲" : " ▼")).color(WHITE));
        } else {
            header.setText(Text.of(text).color(GRAY));
        }
    }

    private float priceInset() {
        return HEADER_RIGHT_INSET + this.sellerColumn;
    }

    /**
     * As 3.5: the widest seller name on the page, never narrower than the Seller heading with its arrow, plus a gap.
     */
    private float sellerColumnWidth(final List<ForSaleView> items) {
        final Font font = Minecraft.getInstance().font;
        int widest = (int) (font.width("Seller ▲") * 0.9f);
        if (this.limit > 0) {
            for (final ForSaleView item : items) {
                widest = Math.max(widest, font.width(item.sellerName()));
            }
        }
        return widest + 8f;
    }

    // ---- Rows

    /**
     * An item for sale: the item, its name and how many, the price each (the total in its tooltip) and the seller; the player's own outlined
     * in aqua.
     */
    private Component<?> forSaleRow(final ComponentFactory factory, final ForSaleView item) {
        final Minecraft minecraft = Minecraft.getInstance();
        final boolean own = minecraft.player != null && minecraft.player.getUUID().equals(item.seller());
        final double price = ExchangeMoney.parse(item.price()).doubleValue();
        final Label priceLabel = factory.label(Text.of(ExchangeMoney.withSuffix(price)).color(GOLD).append(Text.of("/ea").color(GRAY)))
            .setTextScale(0.8f);
        priceLabel.setTooltip(Text.translatable("almura.feature.exchange.tooltip.total").append(Text.of(": "
            + ExchangeMoney.format(item.quantity() * price))));
        final float priceWidth = minecraft.font.width(ExchangeMoney.withSuffix(price) + "/ea") * 0.8f;
        final float nameSpace = PANEL_WIDTH - 6f - 8f - 22f - priceWidth - this.sellerColumn - 10f;
        final AnchorPanel row = factory.anchorPanel()
            .add(itemDisplay(factory, item.item()).setAnchor(Anchor.MIDDLE_LEFT).setMargin(2f, 0f, 0f, 0f))
            .add(factory.label(nameAndQuantity(item.item(), item.quantity(), nameSpace)).setAnchor(Anchor.MIDDLE_LEFT)
                .setMargin(22f, 0f, 0f, 0f))
            .add(priceLabel.setAnchor(Anchor.MIDDLE_RIGHT).setMargin(0f, 0f, 2f + this.sellerColumn, 0f));
        if (this.limit > 0 && !own) {
            row.add(factory.label(Text.of(item.sellerName()).color(GRAY).italic()).setAnchor(Anchor.MIDDLE_RIGHT).setMargin(0f, 0f, 2f, 0f));
        }
        ListRows.box(row, this.forSaleList != null && this.forSaleList.selected().filter(item::equals).isPresent(),
            own ? OWN_BORDER : ListRows.BORDER);
        row.setContextMenu(factory.menu());
        row.onEvent(HasContextMenu.SHOW_CONTEXT_MENU, event -> event.setValue(this.forSaleMenu(factory, item)));
        return row.setAnchor(Anchor.FILL).setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.STRETCH);
    }

    /**
     * One of the player's items on the exchange: the item, its name and how many, and when listed a green mark with the price each.
     */
    private Component<?> listItemRow(final ComponentFactory factory, final ListItemView item) {
        final Minecraft minecraft = Minecraft.getInstance();
        float used = 0f;
        final AnchorPanel row = factory.anchorPanel()
            .add(itemDisplay(factory, item.item()).setAnchor(Anchor.MIDDLE_LEFT).setMargin(2f, 0f, 0f, 0f));
        if (item.listed()) {
            final double price = ExchangeMoney.parse(item.price()).doubleValue();
            final Label priceLabel = factory.label(Text.of(ExchangeMoney.withSuffix(price)).color(GOLD).append(Text.of("/ea").color(GRAY)))
                .setTextScale(0.8f);
            priceLabel.setTooltip(Text.translatable("almura.feature.exchange.tooltip.total").append(Text.of(": "
                + ExchangeMoney.format(item.quantity() * price))));
            used = minecraft.font.width(ExchangeMoney.withSuffix(price) + "/ea") * 0.8f + 11f;
            row.add(factory.anchorPanel().setBackground(LISTED).setWidth(5f).setAnchor(Anchor.FILL).setMargin(0f, 2f, 2f, 2f)
                    .setAlignment(HorizontalAlignment.RIGHT, VerticalAlignment.STRETCH))
                .add(priceLabel.setAnchor(Anchor.MIDDLE_RIGHT).setMargin(0f, 0f, 11f, 0f));
        }
        final float nameSpace = PANEL_WIDTH - 6f - 8f - 22f - used - 16f;
        row.add(factory.label(nameAndQuantity(item.item(), item.quantity(), nameSpace)).setAnchor(Anchor.MIDDLE_LEFT).setMargin(22f, 0f, 0f, 0f));
        ListRows.box(row, this.listItemList != null && this.listItemList.selected().filter(item::equals).isPresent());
        row.setContextMenu(factory.menu());
        row.onEvent(HasContextMenu.SHOW_CONTEXT_MENU, event -> event.setValue(this.listItemMenu(factory, item)));
        return row.setAnchor(Anchor.FILL).setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.STRETCH);
    }

    static Component<?> itemDisplay(final ComponentFactory factory, final ItemStack item) {
        return new ItemDisplay(factory, item.copyWithCount(1)).setSize(16f, 16f);
    }

    /**
     * The item's name, cut short with ... to fit, then x and how many (with m, b or t past a million), as 3.5.
     */
    static Text nameAndQuantity(final ItemStack item, final int quantity, final float space) {
        final Font font = Minecraft.getInstance().font;
        final String suffix = " x " + ExchangeMoney.withSuffix(quantity);
        String name = item.getHoverName().getString();
        if (font.width(name + suffix) > space) {
            final StringBuilder cut = new StringBuilder();
            for (final char c : name.toCharArray()) {
                if (font.width(cut.toString() + c + "..." + suffix) > space) {
                    break;
                }
                cut.append(c);
            }
            name = cut + "...";
        }
        return Text.of(name).color(WHITE).append(Text.of(suffix).color(GRAY));
    }

    // ---- Helpers

    private static AnchorPanel panel(final ComponentFactory factory) {
        return factory.anchorPanel().setBackground(PANEL).setBorder(PANEL_BORDER).setPadding(3f);
    }

    private static Label label(final ComponentFactory factory, final String key) {
        return factory.label(Text.translatable(key).append(Text.of(":")).color(WHITE)).setTextScale(1.1f).setWidth(LABEL_WIDTH)
            .setTextAlignment(HorizontalAlignment.RIGHT);
    }

    private static Label header(final ComponentFactory factory) {
        return factory.label("").setTextScale(0.9f);
    }

    private static AnchorPanel clickable(final ComponentFactory factory, final Label label, final Runnable click) {
        final AnchorPanel panel = factory.anchorPanel().add(label.setAnchor(Anchor.TOP_LEFT));
        panel.onEvent(InputEvents.CLICK, event -> {
            if (event.button() == MouseButton.LEFT) {
                click.run();
            }
        });
        return panel;
    }

    static Button button(final ComponentFactory factory, final Text text, final float width, final Runnable press) {
        return factory.button(text).setSize(width, BUTTON_HEIGHT).onEvent(Button.PRESSED, event -> press.run());
    }

    private static Component<?> line(final ComponentFactory factory, final float offset, final boolean fromBottom) {
        final AnchorPanel line = factory.anchorPanel().setBackground(LINE).setHeight(1f).setAnchor(Anchor.FILL);
        return fromBottom
            ? line.setMargin(0f, 0f, 0f, offset).setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.BOTTOM)
            : line.setMargin(0f, offset, 0f, 0f).setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP);
    }

    static boolean same(final @Nullable ItemStack a, final @Nullable ItemStack b) {
        return a != null && b != null && ItemStack.isSameItemSameComponents(a, b);
    }
}
