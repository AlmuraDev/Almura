/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.client.gui;

import com.almuradev.almura.FeatureSortTypes;
import com.almuradev.almura.asm.ClientStaticAccess;
import com.almuradev.almura.feature.FeatureSortType;
import com.almuradev.almura.feature.SortType;
import com.almuradev.almura.feature.exchange.Exchange;
import com.almuradev.almura.feature.exchange.ExchangeModule;
import com.almuradev.almura.feature.exchange.ListStatusType;
import com.almuradev.almura.feature.exchange.client.ClientExchangeManager;
import com.almuradev.almura.feature.exchange.listing.ForSaleItem;
import com.almuradev.almura.feature.exchange.listing.ListItem;
import com.almuradev.almura.feature.notification.ClientNotificationManager;
import com.almuradev.almura.feature.notification.type.PopupNotification;
import com.almuradev.almura.shared.client.ui.component.UIContextMenu;
import com.almuradev.almura.shared.client.ui.component.UIExpandingLabel;
import com.almuradev.almura.shared.client.ui.component.UISaneTooltip;
import com.almuradev.almura.shared.feature.FeatureConstants;
import com.almuradev.almura.shared.feature.filter.Direction;
import com.almuradev.almura.shared.item.VirtualStack;
import com.almuradev.almura.shared.util.MathUtil;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.BasicScreen;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.BasicContainer;
import net.malisis.core.client.gui.component.container.BasicForm;
import net.malisis.core.client.gui.component.container.BasicList;
import net.malisis.core.client.gui.component.decoration.BasicLine;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.BasicTextBox;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UISelect;
import net.malisis.core.client.gui.component.interaction.button.builder.UIButtonBuilder;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.util.FontColors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.*;

@SideOnly(Side.CLIENT)
public final class ExchangeScreen extends BasicScreen {

    private static final int minScreenWidth = 600;
    private static final int minScreenHeight = 370;
    private static final int innerPadding = 2;

    @Inject private static Logger logger;
    @Inject private static ClientNotificationManager notificationManager;
    @Inject private static ClientExchangeManager exchangeManager;

    private final Exchange axs;
    public static int limit;
    // Exchange admins may change the price of and delist any listing
    private final boolean isAdmin;

    private UIButton buttonFirstPage, buttonPreviousPage, buttonNextPage, buttonLastPage, buttonBuyStack, buttonBuyOne,
    buttonBuyAll, buttonBuyQuantity, buttonList;
    private UILabel labelSearchPage, labelLimit, noResultsLabel;
    private UISelect<SortType> comboBoxSortType;
    private BasicTextBox itemDisplayNameSearchBox, sellerSearchTextBox;
    private int currentPage = 1;
    private int pages;

    // Column headers above the for sale list
    // The header strip between the separator and the list; the header text is centred in it
    private static final int HEADER_HEIGHT = 12;
    private static final int HEADER_TEXT_OFFSET = 2;
    // UIExpandingLabel (the prices) is 5 wider than its text, so right aligned prices end 5 short of their position
    private static final int EXPANDING_LABEL_PADDING = 5;
    private static final int HEADER_ITEM_X = 24;
    private static final int HEADER_RIGHT_INSET = innerPadding + 3;
    private UILabel headerItemName, headerPrice, headerSeller;
    // Width of the widest seller name on the current page: prices are right aligned this far from the sellers
    private static int pageSellerWidth;

    public BasicList<ListItem> listItemList;
    public BasicList<ForSaleItem> forSaleList;

    public ExchangeScreen(final Exchange axs, final int limit, final boolean isAdmin) {
        this.axs = axs;
        ExchangeScreen.limit = limit;
        this.isAdmin = isAdmin;
    }

    @Override
    public void construct() {
        guiscreenBackground = false;

        // Detect if screen area is large enough to display.
        if (minScreenWidth > this.resolution.getScaledWidth() || minScreenHeight > this.resolution.getScaledHeight()) {
            notificationManager.handlePopup(new PopupNotification("Exchange Error", "Screen area of: " + minScreenHeight + " x " +
                minScreenWidth + " required.", 5));
            this.close();
            return;
        }

        // Main Panel
        final BasicForm form = new BasicForm(minScreenWidth, minScreenHeight, this.getExchange().getName());

        // Search section
        final BasicContainer<?> searchContainer = new BasicContainer<>(295, 322);
        searchContainer.setPosition(0, 0, Anchor.LEFT | Anchor.TOP);
        searchContainer.setColor(0);
        searchContainer.setBorder(FontColors.WHITE, 1, 185);
        searchContainer.setBackgroundAlpha(215);
        searchContainer.setPadding(3, 3);

        final UILabel itemSearchLabel = new UILabel(I18n.format("almura.feature.exchange.text.item_name") + ":");
        itemSearchLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).scale(1.1F).build());
        itemSearchLabel.setPosition(0, 4, Anchor.LEFT | Anchor.TOP);

        this.itemDisplayNameSearchBox = new BasicTextBox("");
        this.itemDisplayNameSearchBox.setAcceptsReturn(false);
        this.itemDisplayNameSearchBox.setAcceptsTab(false);
        this.itemDisplayNameSearchBox.setTabIndex(0);
        this.itemDisplayNameSearchBox.setOnEnter((tb) -> this.search());
        this.itemDisplayNameSearchBox.setSize(110, 0);
        this.itemDisplayNameSearchBox.setPosition(itemSearchLabel.getWidth() + innerPadding, 2, Anchor.LEFT | Anchor.TOP);
        this.itemDisplayNameSearchBox.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(false).build());

        final UILabel sellerSearchLabel = new UILabel(I18n.format("almura.feature.exchange.text.seller") + ":");
        sellerSearchLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).scale(1.1F).build());
        sellerSearchLabel.setPosition(itemDisplayNameSearchBox.getX() - sellerSearchLabel.getWidth() - 1,
            getPaddedY(itemSearchLabel, 7), Anchor.LEFT | Anchor.TOP);

        this.sellerSearchTextBox = new BasicTextBox("");
        this.sellerSearchTextBox.setAcceptsReturn(false);
        this.sellerSearchTextBox.setAcceptsTab(false);
        this.sellerSearchTextBox.setTabIndex(1);
        this.sellerSearchTextBox.setOnEnter(tb -> this.search());
        this.sellerSearchTextBox.setSize(110, 0);
        this.sellerSearchTextBox.setPosition(this.itemDisplayNameSearchBox.getX(), getPaddedY(this.itemDisplayNameSearchBox, 4), Anchor.LEFT | Anchor.TOP);
        this.sellerSearchTextBox.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(false).build());

        // Sort combobox
        // Sort combobox
        this.comboBoxSortType = new UISelect<>(78);
        this.comboBoxSortType.setOptions(Arrays.asList(
          FeatureSortTypes.CREATED_ASC,
          FeatureSortTypes.CREATED_DESC,
          FeatureSortTypes.PRICE_ASC,
          FeatureSortTypes.PRICE_DESC,
          FeatureSortTypes.ITEM_DISPLAY_NAME_ASC,
          FeatureSortTypes.ITEM_DISPLAY_NAME_DESC,
          ExchangeSortTypes.SELLER_NAME_ASC,
          ExchangeSortTypes.SELLER_NAME_DESC
        ));
        this.comboBoxSortType.setLabelFunction(type -> type == null ? "" : type.getName()); // Because that's reasonable.
        this.comboBoxSortType.select(defaultSortType());
        this.comboBoxSortType.setPosition(-(innerPadding + 1), 2, Anchor.RIGHT | Anchor.TOP);
        this.comboBoxSortType.register(this);

        // Search button
        final UIButton buttonSearch = new UIButtonBuilder()
                .width(60)
                .anchor(Anchor.RIGHT | Anchor.TOP)
                .position(-innerPadding, this.sellerSearchTextBox.getY() - 1)
                .text(I18n.format("almura.feature.common.button.search"))
                .onClick(this::search)
                .build("button.search");

        // Options button
        final UIButton buttonOptions = new UIButtonBuilder()
                .width(46)
                .anchor(Anchor.RIGHT | Anchor.TOP)
                .position(-(innerPadding + 62), this.sellerSearchTextBox.getY() - 1)
                .text("Options")
                .onClick(() -> new ExchangeOptionsScreen(this).display())
                .build("button.options");

        // Clear button: empties the item name and seller filters and searches again
        final UIButton buttonClearFilters = new UIButtonBuilder()
                .anchor(Anchor.LEFT | Anchor.TOP)
                // Text boxes draw their border a pixel above their position: line up with it
                .position(BasicScreen.getPaddedX(this.itemDisplayNameSearchBox, 3), this.itemDisplayNameSearchBox.getY() - 1)
                .text(TextFormatting.RED + "X")
                .tooltip(I18n.format("almura.feature.exchange.tooltip.clear_filters"))
                .onClick(() -> this.searchFor("", ""))
                .build("button.clear_filters");
        // Buttons draw 2 smaller than their size (right and bottom): 14 shows as the text box's 12, with the X centred in it
        buttonClearFilters.setAutoSize(false);
        buttonClearFilters.setSize(14, 14);

        // Separator
        final BasicContainer<?> forSaleTopLine = new BasicContainer<>(searchContainer.getRawWidth() - 2, 1);
        forSaleTopLine.setColor(FontColors.WHITE);
        forSaleTopLine.setBackgroundAlpha(185);
        forSaleTopLine.setPosition(0, BasicScreen.getPaddedY(this.sellerSearchTextBox, 4), Anchor.CENTER | Anchor.TOP);

        // Column headers: click to sort by the column, ascending, click again for descending
        final int headerTop = BasicScreen.getPaddedY(forSaleTopLine, 1);
        final int headerY = headerTop + HEADER_TEXT_OFFSET;
        this.headerItemName = this.createHeader("Item Name");
        this.headerItemName.setPosition(HEADER_ITEM_X, headerY, Anchor.LEFT | Anchor.TOP);
        this.headerSeller = this.createHeader("Seller");
        this.headerSeller.setPosition(-HEADER_RIGHT_INSET, headerY, Anchor.RIGHT | Anchor.TOP);
        this.headerPrice = this.createHeader("Price");
        this.headerPrice.setPosition(-(priceColumnInset() + EXPANDING_LABEL_PADDING), headerY, Anchor.RIGHT | Anchor.TOP);
        this.updateHeaders();

        this.forSaleList = new BasicList<>(UIComponent.INHERITED, 242);
        this.forSaleList.setPosition(0, headerTop + HEADER_HEIGHT);
        this.forSaleList.setItemComponentFactory(ForSaleItemComponent::new);
        this.forSaleList.setItemComponentSpacing(1);
        this.forSaleList.setSelectConsumer((i) -> this.updateControls());

        // No results label
        this.noResultsLabel = new UILabel("No results found.");
        this.noResultsLabel.setPosition(0, this.forSaleList.getY() + 8, Anchor.TOP | Anchor.CENTER);
        this.noResultsLabel.setFontOptions(FontColors.GRAY_FO);
        this.noResultsLabel.setVisible(false);

        // Bottom Page Control - first button
        this.buttonFirstPage = new UIButtonBuilder()
            .size(20)
            .anchor(Anchor.LEFT | Anchor.BOTTOM)
            .position(0, 0)
            .text("|<")
            .enabled(false)
            .onClick(() -> setPage(1))
            .build("button.first");

        // Bottom Page Control - previous button
        this.buttonPreviousPage = new UIButtonBuilder()
            .size(20)
            .anchor(Anchor.LEFT | Anchor.BOTTOM)
            .position(getPaddedX(this.buttonFirstPage, innerPadding), 0)
            .text("<")
            .enabled(false)
            .onClick(() -> setPage(--this.currentPage))
            .build("button.previous");

        // Search pages label
        this.labelSearchPage = new UILabel("");
        this.labelSearchPage.setPosition(0, -4, Anchor.CENTER | Anchor.BOTTOM);

        // Bottom Page Control - last button
        this.buttonLastPage = new UIButtonBuilder()
            .size(20)
            .anchor(Anchor.RIGHT | Anchor.BOTTOM)
            .position(0,0)
            .text(">|")
            .enabled(false)
            .onClick(() -> setPage(this.pages))
            .build("button.last");

        // Bottom Page Control - next button
        this.buttonNextPage = new UIButtonBuilder()
            .size(20)
            .anchor(Anchor.RIGHT | Anchor.BOTTOM)
            .position(getPaddedX(this.buttonLastPage, innerPadding, Anchor.RIGHT),0)
            .text(">")
            .enabled(false)
            .onClick(() -> setPage(++this.currentPage))
            .build("button.next");

        // Separator
        final BasicLine forSaleBottomLine = new BasicLine(searchContainer.getRawWidth() - 2);
        forSaleBottomLine.setPosition(0, BasicScreen.getPaddedY(this.buttonFirstPage, 2, Anchor.BOTTOM), Anchor.CENTER | Anchor.BOTTOM);

        // Add Elements of Search Area
        searchContainer.add(this.headerItemName, this.headerPrice, this.headerSeller, buttonOptions, buttonClearFilters, itemSearchLabel, this.itemDisplayNameSearchBox, sellerSearchLabel, this.sellerSearchTextBox, buttonSearch,
                this.comboBoxSortType, this.buttonFirstPage, this.buttonPreviousPage, this.buttonNextPage, this.buttonLastPage, this.forSaleList,
                this.noResultsLabel, this.labelSearchPage, forSaleTopLine, forSaleBottomLine);

        // Buy container
        final BasicContainer<?> buyContainer = new BasicContainer(295, 21);
        buyContainer.setPosition(0, getPaddedY(searchContainer, innerPadding), Anchor.LEFT | Anchor.TOP);
        buyContainer.setColor(0);
        buyContainer.setBorder(FontColors.WHITE, 1, 185);
        buyContainer.setBackgroundAlpha(215);
        buyContainer.setPadding(3, 0);

        this.buttonBuyOne = new UIButtonBuilder()
                .width(60)
                .anchor(Anchor.LEFT | Anchor.MIDDLE)
                .position(0, 0)
                .text(I18n.format("almura.feature.common.button.buy.one"))
                .enabled(false)
                .onClick(() -> this.buy(1))
                .build("button.buy.single");

        this.buttonBuyStack = new UIButtonBuilder()
                .width(60)
                .anchor(Anchor.LEFT | Anchor.MIDDLE)
                .position(BasicScreen.getPaddedX(this.buttonBuyOne, 10), 0)
                .text(I18n.format("almura.feature.common.button.buy.stack", 0))
                .enabled(false)
                .onClick(() -> {
                    final ForSaleItem forSaleItem = this.forSaleList.getSelectedItem();
                    if (forSaleItem != null) {
                        this.buy(forSaleItem.asRealStack().getMaxStackSize());
                    }
                })
                .build("button.buy.stack");

        this.buttonBuyAll = new UIButtonBuilder()
                .width(50)
                .anchor(Anchor.RIGHT | Anchor.MIDDLE)
                .position(0, 0)
                .text(I18n.format("almura.feature.common.button.buy.all"))
                .enabled(false)
                .onClick(() -> {
                    final ForSaleItem forSaleItem = this.forSaleList.getSelectedItem();
                    if (forSaleItem != null) {
                        this.buy(forSaleItem.getQuantity());
                    }
                })
                .build("button.buy.all");

        this.buttonBuyQuantity = new UIButtonBuilder()
                .width(60)
                .anchor(Anchor.RIGHT | Anchor.MIDDLE)
                .position(BasicScreen.getPaddedX(this.buttonBuyAll, 10, Anchor.RIGHT), 0)
                .text(I18n.format("almura.feature.common.button.buy.quantity"))
                .enabled(false)
                .onClick(() -> {
                    final ForSaleItem forSaleItem = this.forSaleList.getSelectedItem();
                    if (forSaleItem != null) {
                        new ExchangeBuyQuantityScreen(this, this.axs, forSaleItem).display();
                    }
                })
                .build("button.buy.quantity");

        buyContainer.add(this.buttonBuyOne, this.buttonBuyStack, this.buttonBuyQuantity, this.buttonBuyAll);

        // Listable Items section
        final BasicContainer<?> listableItemsContainer = new BasicContainer(295, 345);
        listableItemsContainer.setPosition(0, 0, Anchor.RIGHT | Anchor.TOP);
        listableItemsContainer.setBorder(FontColors.WHITE, 1, 185);
        listableItemsContainer.setColor(0);
        listableItemsContainer.setBackgroundAlpha(215);
        listableItemsContainer.setPadding(3, 3);

        final UILabel listableItemsLabel = new UILabel(I18n.format("almura.feature.exchange.title.listable_items"));
        listableItemsLabel.setPosition(0, 2, Anchor.CENTER | Anchor.TOP);
        listableItemsLabel.setFontOptions(FontColors.WHITE_FO);

        final BasicLine listTopLine = new BasicLine(listableItemsContainer.getRawWidth() - 2);
        listTopLine.setPosition(0, BasicScreen.getPaddedY(listableItemsLabel, 3), Anchor.TOP | Anchor.CENTER);

        this.listItemList = new BasicList<>(UIComponent.INHERITED, 302);
        this.listItemList.setPosition(0, BasicScreen.getPaddedY(listTopLine, 2));
        this.listItemList.setItemComponentFactory(ListItemComponent::new);
        this.listItemList.setItemComponentSpacing(1);
        this.listItemList.setSelectConsumer((i) -> this.updateControls());

        this.buttonList = new UIButtonBuilder()
            .width(40)
            .anchor(Anchor.LEFT | Anchor.BOTTOM)
            .position(0, 0)
            .text(I18n.format("almura.feature.common.button.list"))
            .enabled(false)
            .onClick(this::list)
            .build("button.list");

        this.labelLimit = new UILabel("");
        this.labelLimit.setPosition(0, -2, Anchor.CENTER | Anchor.BOTTOM);

        final UIButton buttonOffer = new UIButtonBuilder()
            .width(30)
            .anchor(Anchor.RIGHT | Anchor.BOTTOM)
            .position(0, 0)
            .text(TextFormatting.DARK_GREEN + "+" + TextFormatting.GRAY + "/" + TextFormatting.RED + "-")
            .enabled(true)
            .onClick(() -> exchangeManager.requestExchangeSpecificOfferGui(this.axs.getId()))
            .build("button.offer");

        // Separator
        final BasicLine listBottomLine = new BasicLine(searchContainer.getRawWidth() - 2);
        listBottomLine.setPosition(0, BasicScreen.getPaddedY(this.buttonList, 2, Anchor.BOTTOM), Anchor.CENTER | Anchor.BOTTOM);

        listableItemsContainer.add(listableItemsLabel, listTopLine, this.listItemList, this.buttonList, this.labelLimit, buttonOffer, listBottomLine);

        if (limit > 0) {
            // Display full Exchange window with List Contianer
            form.add(searchContainer, buyContainer, listableItemsContainer);
        } else {
            // Display only half the Exchange Window, no List Container because the player can't list things here
            form.setWidth(301);
            form.add(searchContainer, buyContainer);
        }
        addToScreen(form);
        this.itemDisplayNameSearchBox.focus();

        // Query with this screen's sort and options (hide own listings)
        this.search();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (this.buttonBuyOne != null) {
            if (this.isAdmin && isCtrlKeyDown()) {
                this.buttonBuyOne.setText(I18n.format("almura.feature.common.button.delist"));
            } else {
                this.buttonBuyOne.setText(I18n.format("almura.feature.common.button.buy.one"));
            }
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false; // Can't stop the game otherwise the Sponge Scheduler also stops.
    }

    public Exchange getExchange() {
        return this.axs;
    }

    private void setPage(int page) {
        page = MathUtil.squashi(page, 1, this.pages);

        this.axs.clearForSaleItems();
        this.forSaleList.clearItems();

        this.currentPage = page;

        this.forSaleList.setSelectedItem(null); // Clear selection

        exchangeManager.queryForSaleItemsCached(this.axs.getId(), (this.currentPage - 1) * 10, 10);
    }

    private UILabel createHeader(final String text) {
        final UILabel header = new UILabel(text);
        header.setFontOptions(FontOptions.builder().from(FontColors.GRAY_FO).shadow(true).scale(0.9F).build());
        return header;
    }

    /**
     * @return How far from the right edge of a row the prices end: past the sellers (widest on the page) and a gap
     */
    private static int priceColumnInset() {
        return HEADER_RIGHT_INSET + (pageSellerWidth > 0 ? pageSellerWidth + 8 : 0);
    }

    /**
     * Shows which column the list is sorted by (white, with an arrow for the direction); the others are gray.
     */
    private void updateHeaders() {
        if (this.headerItemName == null || this.comboBoxSortType == null) {
            return;
        }
        final SortType sort = this.comboBoxSortType.getSelectedValue();
        this.setHeader(this.headerItemName, "Item Name", sort, FeatureSortTypes.ITEM_DISPLAY_NAME_ASC.getTypeId());
        this.setHeader(this.headerPrice, "Price", sort, FeatureSortTypes.PRICE_ASC.getTypeId());
        this.setHeader(this.headerSeller, "Seller", sort, ExchangeSortTypes.SELLER_NAME_ASC.getTypeId());
    }

    private void setHeader(final UILabel header, final String text, @Nullable final SortType sort, final String typeId) {
        final boolean active = sort != null && sort.getTypeId().equals(typeId);
        header.setText(active ? TextFormatting.WHITE + text + (sort.getDirection() == Direction.ASCENDING ? " ▲" : " ▼") : text);
    }

    /**
     * Sorts by a column: ascending, or descending when it already is sorted ascending by it.
     */
    private void sortBy(final SortType ascending, final SortType descending) {
        final SortType current = this.comboBoxSortType.getSelectedValue();
        this.comboBoxSortType.select(current == ascending ? descending : ascending);
        this.updateHeaders();
        this.search();
    }

    @Override
    protected void mouseClicked(final int x, final int y, final int button) {
        if (button == 0 && this.headerItemName != null) {
            if (this.headerItemName.isInsideBounds(x, y)) {
                this.sortBy(FeatureSortTypes.ITEM_DISPLAY_NAME_ASC, FeatureSortTypes.ITEM_DISPLAY_NAME_DESC);
                return;
            }
            if (this.headerPrice.isInsideBounds(x, y)) {
                this.sortBy(FeatureSortTypes.PRICE_ASC, FeatureSortTypes.PRICE_DESC);
                return;
            }
            if (this.headerSeller.isInsideBounds(x, y)) {
                this.sortBy(ExchangeSortTypes.SELLER_NAME_ASC, ExchangeSortTypes.SELLER_NAME_DESC);
                return;
            }
        }
        super.mouseClicked(x, y, button);
    }

    @Subscribe
    public void onSortSelected(final UISelect.SelectEvent<SortType> event) {
        // The dropdown changes before this event's value is applied, refresh the headers once it is
        Minecraft.getMinecraft().addScheduledTask(this::updateHeaders);
    }

    /**
     * @return The ascending sort for the "default sort order" option
     */
    static SortType defaultSortType() {
        switch (ClientStaticAccess.configAdapter.get().general.exchangeDefaultSort) {
            case "price":
                return FeatureSortTypes.PRICE_ASC;
            case "seller":
                return ExchangeSortTypes.SELLER_NAME_ASC;
            default:
                return FeatureSortTypes.ITEM_DISPLAY_NAME_ASC;
        }
    }

    /**
     * Called when the options change: sorts by the default sort order and searches again.
     */
    void applyOptions() {
        this.comboBoxSortType.select(defaultSortType());
        this.updateHeaders();
        this.search();
    }

    private void search() {
        final Map<String, Object> filterQueryMap = new HashMap<>();
        filterQueryMap.put("item_display_name", this.itemDisplayNameSearchBox.getText());
        filterQueryMap.put("seller_name", this.sellerSearchTextBox.getText());
        if (ClientStaticAccess.configAdapter.get().general.exchangeHideOwnListings) {
            // Option: hide my listed items in search
            filterQueryMap.put("not_seller", Minecraft.getMinecraft().player.getUniqueID().toString());
        }

        final Map<String, String> sortQueryMap = new HashMap<>();
        sortQueryMap.put(this.comboBoxSortType.getSelectedValue().getTypeId(), this.comboBoxSortType.getSelectedValue().getDirection().getId());

        final StringBuilder filterQueryBuilder = new StringBuilder();
        for (final Map.Entry<String, Object> entry : filterQueryMap.entrySet()) {
            if (entry.getValue().toString().isEmpty()) {
                continue;
            }
            filterQueryBuilder.append(ExchangeModule.ID).append("_").append(entry.getKey()).append(FeatureConstants.EQUALITY).append(entry.getValue())
              .append(FeatureConstants.DELIMITER);
        }

        // Not needed to be done this way in current form but adds native support if we allow multiple sorting patterns
        final StringBuilder sortQueryBuilder = new StringBuilder();
        for (final Map.Entry<String, String> entry : sortQueryMap.entrySet()) {
            if (entry.getValue().isEmpty()) {
                continue;
            }
            sortQueryBuilder.append(ExchangeModule.ID).append("_").append(entry.getKey()).append(FeatureConstants.EQUALITY).append(entry.getValue())
              .append(FeatureConstants.DELIMITER);
        }

        final String filterQuery = filterQueryBuilder.toString().isEmpty() ? null : filterQueryBuilder.toString();
        final String sortQuery = sortQueryBuilder.toString().isEmpty() ? null : sortQueryBuilder.toString();

        exchangeManager.queryForSaleItemsFor(this.axs.getId(), filterQuery, sortQuery, (this.currentPage - 1) * 10, 10);
    }

    private void buy(final int value) {
        final ForSaleItem forSaleItem = this.forSaleList.getSelectedItem();
        if (forSaleItem != null) {
            if (this.isAdmin && isCtrlKeyDown()) {
                exchangeManager.modifyListStatus(ListStatusType.ADMIN_DE_LIST, this.axs.getId(), forSaleItem.getListItem().getRecord(), null);
            } else {
                exchangeManager.purchase(this.axs.getId(), forSaleItem.getListItem().getRecord(), value);
            }
        }
    }

    private void list() {
        final ListItem item = this.listItemList.getSelectedItem();
        if (item != null) {
            final boolean de_list = item.getForSaleItem().isPresent();

            if (de_list) {
                exchangeManager.modifyListStatus(ListStatusType.DE_LIST, this.axs.getId(), item.getRecord(), null);
            } else {
                new ExchangeListPriceScreen(this, this.axs, item).display();
            }
        }
    }

    /**
     * The right click menu of a for sale row: buying, searching for the item or seller and, for the seller or an exchange admin, changing
     * the price or delisting.
     */
    private UIContextMenu forSaleMenu(final ForSaleItem item) {
        this.forSaleList.setSelectedItem(item, false);

        final ListItem listItem = item.getListItem();
        final String exchangeId = this.axs.getId();
        final int quantity = item.getQuantity();
        final int stackSize = item.asRealStack().getMaxStackSize();
        final boolean isSeller = Minecraft.getMinecraft().player.getUniqueID().equals(listItem.getSeller());
        final String sellerName = listItem.getSellerName().orElse("");

        final UIContextMenu menu = new UIContextMenu(this)
            .add(I18n.format("almura.feature.common.button.buy.one"), () -> exchangeManager.purchase(exchangeId, listItem.getRecord(), 1))
            .add(I18n.format("almura.feature.common.button.buy.stack", stackSize),
                () -> exchangeManager.purchase(exchangeId, listItem.getRecord(), stackSize), () -> quantity > 1 && quantity >= stackSize)
            .add(I18n.format("almura.feature.exchange.menu.buy_quantity"), () -> new ExchangeBuyQuantityScreen(this, this.axs, item).display(),
                () -> quantity > 1)
            .add(I18n.format("almura.feature.common.button.buy.all"), () -> exchangeManager.purchase(exchangeId, listItem.getRecord(), quantity),
                () -> quantity > 1)
            .separator()
            .add(I18n.format("almura.feature.exchange.menu.search_item"), () -> this.searchFor(item.asRealStack().getDisplayName(), ""))
            .add(I18n.format("almura.feature.exchange.menu.search_seller"), () -> this.searchFor("", sellerName), () -> !sellerName.isEmpty());

        if (isSeller) {
            menu.separator()
                .add(I18n.format("almura.feature.exchange.menu.change_price"), () -> new ExchangeListPriceScreen(this, this.axs, listItem,
                    ListStatusType.ADJUST_PRICE, item.getPrice()).display())
                .add(I18n.format("almura.feature.common.button.unlist"),
                    () -> exchangeManager.modifyListStatus(ListStatusType.DE_LIST, exchangeId, listItem.getRecord(), null));
        } else if (this.isAdmin) {
            menu.separator()
                .add(I18n.format("almura.feature.exchange.menu.admin_change_price"), () -> new ExchangeListPriceScreen(this, this.axs, listItem,
                    ListStatusType.ADMIN_ADJUST_PRICE, item.getPrice()).display())
                .add(I18n.format("almura.feature.exchange.menu.admin_delist"),
                    () -> exchangeManager.modifyListStatus(ListStatusType.ADMIN_DE_LIST, exchangeId, listItem.getRecord(), null));
        }
        return menu;
    }

    /**
     * The right click menu of a listable item: list it, or change its price or unlist it when it is listed.
     */
    private UIContextMenu listItemMenu(final ListItem item) {
        this.listItemList.setSelectedItem(item, false);

        final UIContextMenu menu = new UIContextMenu(this);
        final ForSaleItem forSaleItem = item.getForSaleItem().orElse(null);
        if (forSaleItem == null) {
            menu.add(I18n.format("almura.feature.exchange.menu.list"), () -> new ExchangeListPriceScreen(this, this.axs, item).display());
        } else {
            menu.add(I18n.format("almura.feature.exchange.menu.change_price"), () -> new ExchangeListPriceScreen(this, this.axs, item,
                    ListStatusType.ADJUST_PRICE, forSaleItem.getPrice()).display())
                .add(I18n.format("almura.feature.common.button.unlist"),
                    () -> exchangeManager.modifyListStatus(ListStatusType.DE_LIST, this.axs.getId(), item.getRecord(), null));
        }
        return menu;
    }

    /**
     * Searches from the first page for an item name and/or seller (an empty value matches everything).
     */
    private void searchFor(final String itemName, final String sellerName) {
        this.itemDisplayNameSearchBox.setText(itemName);
        this.sellerSearchTextBox.setText(sellerName);
        this.currentPage = 1;
        this.search();
    }

    private void updateControls() {

        // Results list
        final ForSaleItem currentForSaleItem = this.forSaleList.getSelectedItem();
        final boolean isResultSelected = currentForSaleItem != null;
        this.buttonBuyOne.setEnabled(isResultSelected);
        this.buttonBuyQuantity.setEnabled(isResultSelected && currentForSaleItem.getQuantity() > 1);

        final int maxStackSize = currentForSaleItem != null ? currentForSaleItem.asRealStack().getMaxStackSize() : 0;
        this.buttonBuyStack.setEnabled(isResultSelected
                && currentForSaleItem.getQuantity() > 1
                && currentForSaleItem.getQuantity() >= maxStackSize);
        this.buttonBuyStack.setText(I18n.format("almura.feature.common.button.buy.stack", maxStackSize));
        this.buttonBuyAll.setEnabled(isResultSelected && currentForSaleItem.getQuantity() > 1);

        // Selling list
        final boolean isSellingItemSelected = this.listItemList.getSelectedItem() != null;
        final boolean isListed = isSellingItemSelected && this.listItemList.getSelectedItem().getForSaleItem().isPresent();
        this.buttonList.setEnabled(isSellingItemSelected);
        this.buttonList.setText(I18n.format("almura.feature.common.button." + (isListed ? "unlist" : "list")));

        // Update page buttons/labels
        this.buttonFirstPage.setEnabled(this.currentPage != 1);
        this.buttonPreviousPage.setEnabled(this.currentPage != 1);
        this.buttonNextPage.setEnabled(this.currentPage != this.pages);
        this.buttonLastPage.setEnabled(this.currentPage != this.pages);

        this.labelSearchPage.setText(TextFormatting.WHITE + String.valueOf(this.currentPage) + "/" + this.pages);

        // Update all items
        this.listItemList.getComponents()
                .stream()
                .filter(c -> c instanceof ListItemComponent)
                .map(c -> (ListItemComponent) c)
                .forEach(ListItemComponent::update);

        // Update the limit label
        this.labelLimit.setText(TextFormatting.WHITE + String.valueOf(this.listItemList.getItems().size()) + "/" + this.limit);

        // Hide the list and show the label if there are no items to present
        final boolean isEmpty = this.forSaleList.getItems().isEmpty();
        this.forSaleList.setVisible(!isEmpty);
        this.noResultsLabel.setVisible(isEmpty);
    }

    public void refreshListItems() {
        if (this.listItemList == null) {
            return;
        }

        final ListItem currentItem = this.listItemList.getSelectedItem() == null ? null : this.listItemList.getSelectedItem().copy();

        this.listItemList.clearItems();

        final List<ListItem> listItems = this.axs.getListItemsFor(Minecraft.getMinecraft().player.getUniqueID()).orElse(null);
        if (listItems != null && !listItems.isEmpty()) {
            this.listItemList.setItems(listItems);
        }

        // Attempt to re-select the same item
        if (currentItem != null) {
            this.listItemList.setSelectedItem(this.listItemList.getItems()
                    .stream()
                    .filter(i -> i.getRecord() == currentItem.getRecord())
                    .findFirst()
                    .orElse(null));
        }

        this.updateControls();
    }

    public void refreshForSaleItemResults(@Nullable final List<ForSaleItem> forSaleItems, final int preLimitCount) {
        if (this.forSaleList == null) {
            return;
        }

        final ForSaleItem currentItem = this.forSaleList.getSelectedItem() == null ? null : this.forSaleList.getSelectedItem().copy();

        this.forSaleList.clearItems();

        // Line the prices up just left of the widest seller name on this page, before the rows are built
        pageSellerWidth = 0;
        if (forSaleItems != null && limit > 0) {
            final FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
            for (final ForSaleItem item : forSaleItems) {
                pageSellerWidth = Math.max(pageSellerWidth, fontRenderer.getStringWidth(
                        item.getListItem().getSellerName().orElse(I18n.format("almura.feature.common.text.unknown"))));
            }
        }
        if (this.headerPrice != null) {
            this.headerPrice.setPosition(-(priceColumnInset() + EXPANDING_LABEL_PADDING), this.headerPrice.getY(), Anchor.RIGHT | Anchor.TOP);
        }

        if (forSaleItems != null) {
            this.forSaleList.addItems(forSaleItems);
        }

        this.pages = (Math.max(preLimitCount - 1, 1) / 10) + 1;

        // If we can go back to the same page, try it. Otherwise default back to the first.
        this.currentPage = this.currentPage <= this.pages ? this.currentPage : 1;

        // Attempt to re-select the same item
        if (currentItem != null) {
            this.forSaleList.setSelectedItem(this.forSaleList.getItems()
                    .stream()
                    .filter(i -> i.getRecord() == currentItem.getRecord())
                    .findFirst()
                    .orElse(null));
        }

        this.updateControls();
    }

    public static class ExchangeItemComponent<T extends VirtualStack> extends BasicList.ItemComponent<T> {

        protected UIImage image;
        protected UIExpandingLabel itemLabel;
        protected int itemNameSpaceAvailable;

        public ExchangeItemComponent(final MalisisGui gui, final BasicList<T> parent, final T item) {
            super(gui, parent, item);
        }

        @Override
        protected void construct(final MalisisGui gui) {
            this.setSize(0, 24);

            // Default available space
            this.itemNameSpaceAvailable = this.getWidth();

            // Add components
            final ItemStack fakeStack = this.item.asRealStack().copy();
            fakeStack.setCount(1);
            final EntityPlayer player = Minecraft.getMinecraft().player;
            final boolean useAdvancedTooltips = Minecraft.getMinecraft().gameSettings.advancedItemTooltips;

            this.image = new UIImage(fakeStack);
            this.image.setPosition(0, 0, Anchor.LEFT | Anchor.MIDDLE);
            this.image.setTooltip(new UISaneTooltip(gui, String.join("\n", fakeStack.getTooltip(player, useAdvancedTooltips
                ? ITooltipFlag.TooltipFlags.ADVANCED
                : ITooltipFlag.TooltipFlags.NORMAL))));

            this.itemLabel = new UIExpandingLabel(gui, "");
            this.itemLabel.setPosition(getPaddedX(this.image, 4), 0, Anchor.LEFT | Anchor.MIDDLE);

            // Exact value
            if (this.item.getQuantity() >= (int) FeatureConstants.ONE_MILLION) {
                this.itemLabel.setTooltip(new UISaneTooltip(gui, FeatureConstants.CURRENCY_DECIMAL_FORMAT.format(this.item.getQuantity())));
            }

            this.add(this.image, this.itemLabel);
        }

        protected void refreshDisplayName() {
            // Limit item name to prevent over drawing
            final ItemStack fakeStack = this.item.asRealStack().copy();
            final FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;

            StringBuilder displayName = new StringBuilder();
            for (final char c : fakeStack.getDisplayName().toCharArray()) {
                if (fontRenderer.getStringWidth(displayName.toString() + c + " x " + this.item.getQuantity()) > this.itemNameSpaceAvailable) {
                    displayName = new StringBuilder(displayName.toString().substring(0, Math.max(displayName.length() - 3, 0)) + "...");
                    break;
                }
                displayName.append(c);
            }

            this.itemLabel.setText(TextFormatting.WHITE + displayName.toString() + TextFormatting.GRAY + " x "
              + FeatureConstants.withSuffix(this.item.getQuantity()));
        }
    }

    public static final class ListItemComponent extends ExchangeItemComponent<ListItem> {

        private BasicContainer<?> listedIndicatorContainer;
        private UIExpandingLabel priceLabel;

        public ListItemComponent(final MalisisGui gui, final BasicList<ListItem> parent, final ListItem item) {
            super(gui, parent, item);
            this.setOnDoubleClickConsumer(i -> ((ExchangeScreen) getGui()).list());
            UIContextMenu.attach(this, c -> ((ExchangeScreen) getGui()).listItemMenu(this.item));
        }

        @Override
        protected void construct(final MalisisGui gui) {
            super.construct(gui);

            this.listedIndicatorContainer = new BasicContainer<>(5, this.height - (this.getLeftBorderSize() + this.getRightBorderSize()));
            this.listedIndicatorContainer.setVisible(false);
            this.listedIndicatorContainer.setPosition(2, -2, Anchor.TOP | Anchor.RIGHT);
            this.listedIndicatorContainer.setColor(FontColors.DARK_GREEN);

            this.priceLabel = new UIExpandingLabel(gui, "");
            this.priceLabel.setVisible(false);
            this.priceLabel.setFontOptions(this.priceLabel.getFontOptions().toBuilder().scale(0.8f).build());
            this.priceLabel.setPosition(-(this.listedIndicatorContainer.getWidth() + 6), 0, Anchor.RIGHT | Anchor.MIDDLE);

            this.add(this.listedIndicatorContainer, this.priceLabel);

            this.update();
        }

        public void update() {
            final boolean listed = this.item.getForSaleItem().isPresent();

            // Update visibility
            this.listedIndicatorContainer.setVisible(listed);
            this.priceLabel.setVisible(listed);

            if (listed) {
                final BigDecimal forSalePrice = this.item.getForSaleItem().map(ForSaleItem::getPrice).orElse(BigDecimal.valueOf(0));
                final double forSaleDoublePrice = forSalePrice.doubleValue();

                this.priceLabel.setText(TextFormatting.GOLD + FeatureConstants.withSuffix(forSaleDoublePrice) + TextFormatting.GRAY + "/ea");
                this.priceLabel.setPosition(-(this.listedIndicatorContainer.getWidth() + 6), 0, Anchor.RIGHT | Anchor.MIDDLE);

                // Exact value
                this.priceLabel.setTooltip(I18n.format("almura.feature.exchange.tooltip.total")
                        + ": " + FeatureConstants.CURRENCY_DECIMAL_FORMAT.format(this.item.getQuantity() * forSaleDoublePrice));
            }

            this.itemNameSpaceAvailable = this.getWidth()
                    - (this.priceLabel.isVisible() ? this.priceLabel.getWidth() : 0)
                    - (this.image.isVisible() ? this.image.getWidth() : 0)
                    - (this.listedIndicatorContainer.isVisible() ? this.listedIndicatorContainer.getWidth() : 0)
                    - 16;

            this.refreshDisplayName();
        }
    }

    public static final class ForSaleItemComponent extends ExchangeItemComponent<ForSaleItem> {

        private UILabel sellerLabel;
        private UIExpandingLabel priceLabel;

        public ForSaleItemComponent(final MalisisGui gui, final BasicList<ForSaleItem> parent, final ForSaleItem item) {
            super(gui, parent, item);
            this.setOnDoubleClickConsumer(i -> ((ExchangeScreen) getGui()).buy(1));
            UIContextMenu.attach(this, c -> ((ExchangeScreen) getGui()).forSaleMenu(this.item));
        }

        @Override
        protected void construct(final MalisisGui gui) {
            super.construct(gui);
            // A pixel shorter than the other lists' rows, making room for the column headers
            this.setSize(0, 23);

            final boolean isSeller = Minecraft.getMinecraft().player.getUniqueID().equals(item.getListItem().getSeller());

            this.sellerLabel = new UILabel(TextFormatting.GRAY + "" + TextFormatting.ITALIC
              + this.item.getListItem().getSellerName().orElse(I18n.format("almura.feature.common.text.unknown")));
            this.sellerLabel.setPosition(-innerPadding, 0, Anchor.RIGHT | Anchor.MIDDLE);

            final double price = this.item.getPrice().doubleValue();

            // Prices end just left of the widest seller name on the page (lined up, leaving the item name the rest of the row)
            final int maxPlayerTextWidth = pageSellerWidth > 0 ? pageSellerWidth + 8 : 0;
            this.priceLabel = new UIExpandingLabel(gui, TextFormatting.GOLD + FeatureConstants.withSuffix(price) + TextFormatting.GRAY + "/ea");
            this.priceLabel.setFontOptions(this.priceLabel.getFontOptions().toBuilder().scale(0.8f).build());
            this.priceLabel.setPosition(-(innerPadding + maxPlayerTextWidth), 0, Anchor.RIGHT | Anchor.MIDDLE);

            // Exact value
            this.priceLabel.setTooltip(I18n.format("almura.feature.exchange.tooltip.total")
                    + ": " + FeatureConstants.CURRENCY_DECIMAL_FORMAT.format(this.item.getListItem().getQuantity() * price));

            this.itemNameSpaceAvailable = this.getWidth()
                    - (this.priceLabel.isVisible() ? this.priceLabel.getWidth() : 0)
                    - (this.image.isVisible() ? this.image.getWidth() : 0)
                    - maxPlayerTextWidth
                    - 10;

            if (limit > 0 && !isSeller) {
                this.add(this.sellerLabel, this.priceLabel);
            } else {
                this.add(this.priceLabel);
            }

            if (isSeller) {
                this.setBorder(FontColors.AQUA, 1, 255);
            }

            this.refreshDisplayName();
        }
    }

    private static final class ExchangeSortTypes {
        public static final SortType SELLER_NAME_ASC =
          new FeatureSortType("seller_name", I18n.format("almura.feature.exchange.text.sort.seller_name_asc"), Direction.ASCENDING);
        public static final SortType SELLER_NAME_DESC =
          new FeatureSortType("seller_name", I18n.format("almura.feature.exchange.text.sort.seller_name_desc"), Direction.DESCENDING);
    }
}
