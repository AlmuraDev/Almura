/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.client.gui;

import com.almuradev.almura.feature.exchange.ExchangeConstants;
import com.almuradev.almura.feature.exchange.ListStatusType;
import com.almuradev.almura.feature.exchange.client.ClientExchangeManager;
import com.almuradev.almura.feature.exchange.Exchange;
import com.almuradev.almura.feature.notification.ClientNotificationManager;
import com.almuradev.almura.feature.notification.type.PopupNotification;
import com.almuradev.almura.shared.client.ui.FontColors;
import com.almuradev.almura.shared.client.ui.component.UIComplexImage;
import com.almuradev.almura.shared.client.ui.component.UIDynamicList;
import com.almuradev.almura.shared.client.ui.component.UIExpandingLabel;
import com.almuradev.almura.shared.client.ui.component.UIForm;
import com.almuradev.almura.shared.client.ui.component.UILine;
import com.almuradev.almura.shared.client.ui.component.UISaneTooltip;
import com.almuradev.almura.shared.client.ui.component.UITextBox;
import com.almuradev.almura.shared.client.ui.component.button.UIButtonBuilder;
import com.almuradev.almura.shared.client.ui.component.container.UIContainer;
import com.almuradev.almura.shared.client.ui.screen.SimpleScreen;
import com.almuradev.almura.shared.feature.store.StoreConstants;
import com.almuradev.almura.shared.feature.store.listing.ForSaleItem;
import com.almuradev.almura.shared.feature.store.listing.ListItem;
import com.almuradev.almura.shared.item.VirtualStack;
import com.almuradev.almura.shared.util.MathUtil;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UISelect;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.util.MouseButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.slf4j.Logger;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

@SideOnly(Side.CLIENT)
public final class ExchangeScreen extends SimpleScreen {

    private static final int minScreenWidth = 600;
    private static final int minScreenHeight = 370;
    private static final int innerPadding = 2;

    @Inject private static Logger logger;
    @Inject private static ClientNotificationManager notificationManager;
    @Inject private static ClientExchangeManager exchangeManager;

    private final Exchange axs;
    public final int limit;

    private UIButton buttonFirstPage, buttonPreviousPage, buttonNextPage, buttonLastPage, buttonBuyStack, buttonBuySingle, buttonBuyQuantity,
        buttonList;
    private UILabel labelSearchPage, labelLimit, noResultsLabel;
    private UISelect<SortType> comboBoxSortType;
    private UITextBox displayNameSearchTextBox, sellerSearchTextBox;
    private int currentPage = 1;
    private int pages;

    public UIDynamicList<ListItem> listItemList;
    public UIDynamicList<ForSaleItem> forSaleList;

    public ExchangeScreen(final Exchange axs, final int limit) {
        this.axs = axs;
        this.limit = limit;
    }

    @Override
    public void construct() {
        guiscreenBackground = false;
        Keyboard.enableRepeatEvents(true);

        // Detect if screen area is large enough to display.
        if (minScreenWidth > this.resolution.getScaledWidth() || minScreenHeight > this.resolution.getScaledHeight()) {
            notificationManager.handlePopup(new PopupNotification("Exchange Error", "Screen area of: " + minScreenHeight + " x " +
                minScreenWidth + " required.", 5));
            this.close();
            return;
        }

        // Main Panel
        final UIForm form = new UIForm(this, minScreenWidth, minScreenHeight, this.getExchange().getName());

        // Search section
        final UIContainer<?> searchContainer = new UIContainer(this, 295, 322);
        searchContainer.setPosition(0, 0, Anchor.LEFT | Anchor.TOP);
        searchContainer.setColor(0);
        searchContainer.setBorder(FontColors.WHITE, 1, 185);
        searchContainer.setBackgroundAlpha(215);
        searchContainer.setPadding(3, 3);

        final UILabel itemSearchLabel = new UILabel(this, I18n.format("almura.text.exchange.item_name") + ":");
        itemSearchLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).scale(1.1F).build());
        itemSearchLabel.setPosition(0, 4, Anchor.LEFT | Anchor.TOP);

        this.displayNameSearchTextBox = new UITextBox(this, "");
        this.displayNameSearchTextBox.setTabIndex(0);
        this.displayNameSearchTextBox.setOnEnter((tb) -> this.search());
        this.displayNameSearchTextBox.setSize(145, 0);
        this.displayNameSearchTextBox.setPosition(itemSearchLabel.getWidth() + innerPadding, 2, Anchor.LEFT | Anchor.TOP);
        this.displayNameSearchTextBox.setEditable(true);
        this.displayNameSearchTextBox.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(false).build());

        final UILabel sellerSearchLabel = new UILabel(this, I18n.format("almura.text.exchange.seller") + ":");
        sellerSearchLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).scale(1.1F).build());
        sellerSearchLabel.setPosition(displayNameSearchTextBox.getX() - sellerSearchLabel.getWidth() - 1,
            getPaddedY(itemSearchLabel, 7), Anchor.LEFT | Anchor.TOP);

        this.sellerSearchTextBox = new UITextBox(this, "");
        this.sellerSearchTextBox.setTabIndex(1);
        this.sellerSearchTextBox.setOnEnter((tb) -> this.search());
        this.sellerSearchTextBox.setSize(145, 0);
        this.sellerSearchTextBox.setPosition(this.displayNameSearchTextBox.getX(), getPaddedY(this.displayNameSearchTextBox, 4), Anchor.LEFT | Anchor.TOP);
        this.sellerSearchTextBox.setEditable(true);
        this.sellerSearchTextBox.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(false).build());

        // Sort combobox
        this.comboBoxSortType = new UISelect<>(this, 78, Arrays.asList(SortType.values()));
        this.comboBoxSortType.setLabelFunction(type -> type == null ? "" : type.displayName); // Because that's reasonable.
        this.comboBoxSortType.select(SortType.PRICE_ASC);
        this.comboBoxSortType.setPosition(-(innerPadding + 1), this.sellerSearchTextBox.getY(), Anchor.RIGHT | Anchor.TOP);

        // Separator
        final UIContainer<?> forSaleTopLine = new UIContainer<>(this, searchContainer.getRawWidth() - 2, 1);
        forSaleTopLine.setColor(FontColors.WHITE);
        forSaleTopLine.setBackgroundAlpha(185);
        forSaleTopLine.setPosition(0, SimpleScreen.getPaddedY(this.sellerSearchTextBox, 4), Anchor.CENTER | Anchor.TOP);

        this.forSaleList = new UIDynamicList<>(this, UIComponent.INHERITED, 254);
        this.forSaleList.setPosition(0, SimpleScreen.getPaddedY(forSaleTopLine, 2));
        this.forSaleList.setItemComponentFactory((s, t, u) -> new ForSaleItemComponent(this, this.forSaleList, u));
        this.forSaleList.setItemComponentSpacing(1);
        this.forSaleList.setSelectConsumer((i) -> this.updateControls());

        // No results label
        this.noResultsLabel = new UILabel(this, "No results found.");
        this.noResultsLabel.setPosition(0, this.forSaleList.getY() + 8, Anchor.TOP | Anchor.CENTER);
        this.noResultsLabel.setFontOptions(FontColors.GRAY_FO);
        this.noResultsLabel.setVisible(false);

        // Search button
        final UIButton buttonSearch = new UIButtonBuilder(this)
            .width(80)
            .anchor(Anchor.RIGHT | Anchor.TOP)
            .position(-innerPadding, 1)
            .text(I18n.format("almura.button.exchange.search"))
            .onClick(this::search)
            .build("button.search");

        // Bottom Page Control - first button
        this.buttonFirstPage = new UIButtonBuilder(this)
            .size(20)
            .anchor(Anchor.LEFT | Anchor.BOTTOM)
            .position(0, 0)
            .text("|<")
            .enabled(false)
            .onClick(() -> setPage(1))
            .build("button.first");

        // Bottom Page Control - previous button
        this.buttonPreviousPage = new UIButtonBuilder(this)
            .size(20)
            .anchor(Anchor.LEFT | Anchor.BOTTOM)
            .position(getPaddedX(this.buttonFirstPage, innerPadding), 0)
            .text("<")
            .enabled(false)
            .onClick(() -> setPage(--this.currentPage))
            .build("button.previous");

        // Search pages label
        this.labelSearchPage = new UILabel(this, "");
        this.labelSearchPage.setPosition(0, -4, Anchor.CENTER | Anchor.BOTTOM);

        // Bottom Page Control - last button
        this.buttonLastPage = new UIButtonBuilder(this)
            .size(20)
            .anchor(Anchor.RIGHT | Anchor.BOTTOM)
            .position(0,0)
            .text(">|")
            .enabled(false)
            .onClick(() -> setPage(this.pages))
            .build("button.last");

        // Bottom Page Control - next button
        this.buttonNextPage = new UIButtonBuilder(this)
            .size(20)
            .anchor(Anchor.RIGHT | Anchor.BOTTOM)
            .position(getPaddedX(this.buttonLastPage, innerPadding, Anchor.RIGHT),0)
            .text(">")
            .enabled(false)
            .onClick(() -> setPage(++this.currentPage))
            .build("button.next");

        // Separator
        final UILine forSaleBottomLine = new UILine(this, searchContainer.getRawWidth() - 2);
        forSaleBottomLine.setPosition(0, SimpleScreen.getPaddedY(this.buttonFirstPage, 2, Anchor.BOTTOM), Anchor.CENTER | Anchor.BOTTOM);

        // Add Elements of Search Area
        searchContainer.add(itemSearchLabel, this.displayNameSearchTextBox, sellerSearchLabel, this.sellerSearchTextBox, buttonSearch,
                this.comboBoxSortType, this.buttonFirstPage, this.buttonPreviousPage, this.buttonNextPage, this.buttonLastPage, this.forSaleList,
                this.noResultsLabel, this.labelSearchPage, forSaleTopLine, forSaleBottomLine);

        // Buy container
        final UIContainer<?> buyContainer = new UIContainer(this, 295, 21);
        buyContainer.setPosition(0, getPaddedY(searchContainer, innerPadding), Anchor.LEFT | Anchor.TOP);
        buyContainer.setColor(0);
        buyContainer.setBorder(FontColors.WHITE, 1, 185);
        buyContainer.setBackgroundAlpha(215);
        buyContainer.setPadding(3, 0);

        this.buttonBuyStack = new UIButtonBuilder(this)
                .width(60)
                .anchor(Anchor.LEFT | Anchor.MIDDLE)
                .position(0, 0)
                .text(I18n.format("almura.button.exchange.buy.stack"))
                .enabled(false)
                .onClick(() -> {
                    final ForSaleItem forSaleItem = this.forSaleList.getSelectedItem();
                    if (forSaleItem != null) {
                        this.buy(forSaleItem.getQuantity());
                    }
                })
                .build("button.buy.stack");

        this.buttonBuySingle = new UIButtonBuilder(this)
                .width(60)
                .anchor(Anchor.CENTER | Anchor.MIDDLE)
                .position(0, 0)
                .text(I18n.format("almura.button.exchange.buy.single"))
                .enabled(false)
                .onClick(() -> this.buy(1))
                .build("button.buy.single");

        this.buttonBuyQuantity = new UIButtonBuilder(this)
                .width(60)
                .anchor(Anchor.RIGHT | Anchor.MIDDLE)
                .position(0, 0)
                .text(I18n.format("almura.button.exchange.buy.quantity"))
                .enabled(false)
                .onClick(() -> {
                    final ForSaleItem forSaleItem = this.forSaleList.getSelectedItem();
                    if (forSaleItem != null) {
                        new ExchangeBuyQuantityScreen(this, this.axs, forSaleItem).display();
                    }
                })
                .build("button.buy.quantity");

        buyContainer.add(this.buttonBuyStack, this.buttonBuySingle, this.buttonBuyQuantity);

        // Listable Items section
        final UIContainer<?> listableItemsContainer = new UIContainer(this, 295, 345);
        listableItemsContainer.setPosition(0, 0, Anchor.RIGHT | Anchor.TOP);
        listableItemsContainer.setBorder(FontColors.WHITE, 1, 185);
        listableItemsContainer.setColor(0);
        listableItemsContainer.setBackgroundAlpha(215);
        listableItemsContainer.setPadding(3, 3);

        final UILabel listableItemsLabel = new UILabel(this, I18n.format("almura.title.exchange.listable_items"));
        listableItemsLabel.setPosition(0, 2, Anchor.CENTER | Anchor.TOP);
        listableItemsLabel.setFontOptions(FontColors.WHITE_FO);

        final UILine listTopLine = new UILine(this, listableItemsContainer.getRawWidth() - 2);
        listTopLine.setPosition(0, SimpleScreen.getPaddedY(listableItemsLabel, 3), Anchor.TOP | Anchor.CENTER);

        this.listItemList = new UIDynamicList<>(this, UIComponent.INHERITED, 302);
        this.listItemList.setPosition(0, SimpleScreen.getPaddedY(listTopLine, 2));
        this.listItemList.setItemComponentFactory(ListItemComponent::new);
        this.listItemList.setItemComponentSpacing(1);
        this.listItemList.setSelectConsumer((i) -> this.updateControls());

        this.buttonList = new UIButtonBuilder(this)
            .width(40)
            .anchor(Anchor.LEFT | Anchor.BOTTOM)
            .position(0, 0)
            .text(I18n.format("almura.button.exchange.list"))
            .enabled(false)
            .onClick(this::list)
            .build("button.list");

        this.labelLimit = new UILabel(this, "");
        this.labelLimit.setPosition(0, -2, Anchor.CENTER | Anchor.BOTTOM);

        final UIButton buttonOffer = new UIButtonBuilder(this)
            .width(30)
            .anchor(Anchor.RIGHT | Anchor.BOTTOM)
            .position(0, 0)
            .text(Text.of(TextColors.DARK_GREEN, "+", TextColors.GRAY, "/", TextColors.RED, "-"))
            .enabled(true)
            .onClick(() -> exchangeManager.requestExchangeSpecificOfferGui(this.axs.getId()))
            .build("button.offer");

        // Separator
        final UILine listBottomLine = new UILine(this, searchContainer.getRawWidth() - 2);
        listBottomLine.setPosition(0, SimpleScreen.getPaddedY(this.buttonList, 2, Anchor.BOTTOM), Anchor.CENTER | Anchor.BOTTOM);

        listableItemsContainer.add(listableItemsLabel, listTopLine, this.listItemList, this.buttonList, this.labelLimit, buttonOffer, listBottomLine);

        form.add(searchContainer, buyContainer, listableItemsContainer);

        addToScreen(form);

        this.displayNameSearchTextBox.focus();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false; // Can't stop the game otherwise the Sponge Scheduler also stops.
    }

    public Exchange getExchange() {
        return this.axs;
    }

    @SuppressWarnings("deprecation")
    private void setPage(int page) {
        page = MathUtil.squashi(page, 1, this.pages);

        this.axs.clearForSaleItems();
        this.forSaleList.clearItems();

        this.currentPage = page;

        this.forSaleList.setSelectedItem(null); // Clear selection

        exchangeManager.queryForSaleItemsCached(this.axs.getId(), (this.currentPage - 1) * 10, 10);
    }

    private void search() {
        final Map<String, Object> filterQueryMap = new HashMap<>();
        filterQueryMap.put("display_name", this.displayNameSearchTextBox.getText());
        filterQueryMap.put("seller_name", this.sellerSearchTextBox.getText());

        final Map<String, String> sortQueryMap = new HashMap<>();
        sortQueryMap.put(this.comboBoxSortType.getSelectedValue().id, this.comboBoxSortType.getSelectedValue().direction);

        final StringBuilder filterQueryBuilder = new StringBuilder();
        for (Map.Entry<String, Object> entry : filterQueryMap.entrySet()) {
            if (entry.getValue().toString().isEmpty()) {
                continue;
            }
            filterQueryBuilder.append(entry.getKey()).append(StoreConstants.EQUALITY).append(entry.getValue()).append(StoreConstants.DELIMETER);
        }

        // Not needed to be done this way in current form but adds native support if we allow multiple sorting patterns
        final StringBuilder sortQueryBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : sortQueryMap.entrySet()) {
            if (entry.getValue().isEmpty()) {
                continue;
            }
            sortQueryBuilder.append(entry.getKey()).append(StoreConstants.EQUALITY).append(entry.getValue()).append(StoreConstants.DELIMETER);
        }

        final String filterQuery = filterQueryBuilder.toString().isEmpty() ? null : filterQueryBuilder.toString();
        final String sortQuery = sortQueryBuilder.toString().isEmpty() ? null : sortQueryBuilder.toString();

        exchangeManager.queryForSaleItemsFor(this.axs.getId(), filterQuery, sortQuery, (this.currentPage - 1) * 10, 10);
    }

    private void buy(final int value) {
        final ForSaleItem forSaleItem = this.forSaleList.getSelectedItem();
        if (forSaleItem != null) {
            exchangeManager.purchase(this.axs.getId(), forSaleItem.getListItem().getRecord(), value);
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

    @SuppressWarnings("deprecation")
    private void updateControls() {

        // Results list
        final ForSaleItem currentForSaleItem = this.forSaleList.getSelectedItem();
        final boolean isResultSelected = currentForSaleItem != null;
        this.buttonBuySingle.setEnabled(isResultSelected);
        this.buttonBuyQuantity.setEnabled(isResultSelected && currentForSaleItem.getQuantity() > 1);
        this.buttonBuyStack.setEnabled(isResultSelected && currentForSaleItem.getQuantity() > 1);

        // Selling list
        final boolean isSellingItemSelected = this.listItemList.getSelectedItem() != null;
        final boolean isListed = isSellingItemSelected && this.listItemList.getSelectedItem().getForSaleItem().isPresent();
        this.buttonList.setEnabled(isSellingItemSelected);
        this.buttonList.setText(I18n.format("almura.button.exchange." + (isListed ? "unlist" : "list")));

        // Update page buttons/labels
        this.buttonFirstPage.setEnabled(this.currentPage != 1);
        this.buttonPreviousPage.setEnabled(this.currentPage != 1);
        this.buttonNextPage.setEnabled(this.currentPage != this.pages);
        this.buttonLastPage.setEnabled(this.currentPage != this.pages);

        this.labelSearchPage.setText(TextSerializers.LEGACY_FORMATTING_CODE.serialize(Text.of(TextColors.WHITE, this.currentPage, "/", this.pages)));

        // Update all items
        this.listItemList.getComponents()
                .stream()
                .filter(c -> c instanceof ListItemComponent)
                .map(c -> (ListItemComponent) c)
                .forEach(ListItemComponent::update);

        // Update the limit label
        this.labelLimit.setText(TextSerializers.LEGACY_FORMATTING_CODE
                .serialize(Text.of(TextColors.WHITE, this.listItemList.getItems().size(), "/", this.limit)));

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

    public void refreshForSaleItemResults(int preLimitCount) {
        if (this.forSaleList == null) {
            return;
        }

        final ForSaleItem currentItem = this.forSaleList.getSelectedItem() == null ? null : this.forSaleList.getSelectedItem().copy();

        this.forSaleList.clearItems();

        final List<ForSaleItem> forSaleItems = this.axs.getForSaleItems().entrySet().stream().map(Map.Entry::getValue).flatMap(List::stream)
            .collect(Collectors.toList());

        if (!forSaleItems.isEmpty()) {
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

    public static class ExchangeItemComponent<T extends VirtualStack> extends UIDynamicList.ItemComponent<T> {

        protected UIImage image;
        protected UIExpandingLabel itemLabel;
        protected int itemNameSpaceAvailable;

        public ExchangeItemComponent(MalisisGui gui, UIDynamicList<T> parent, T item) {
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

            this.image = new UIComplexImage(gui, fakeStack);
            this.image.setPosition(0, 0, Anchor.LEFT | Anchor.MIDDLE);
            this.image.setTooltip(new UISaneTooltip(gui, String.join("\n", fakeStack.getTooltip(player, useAdvancedTooltips
                ? ITooltipFlag.TooltipFlags.ADVANCED
                : ITooltipFlag.TooltipFlags.NORMAL))));

            this.itemLabel = new UIExpandingLabel(gui, "");
            this.itemLabel.setPosition(getPaddedX(this.image, 4), 0, Anchor.LEFT | Anchor.MIDDLE);

            // Exact value
            if (this.item.getQuantity() >= (int) ExchangeConstants.MILLION) {
                this.itemLabel.setTooltip(new UISaneTooltip(gui, ExchangeConstants.CURRENCY_DECIMAL_FORMAT.format(this.item.getQuantity())));
            }

            this.add(this.image, this.itemLabel);
        }

        @SuppressWarnings("deprecation")
        protected void refreshDisplayName() {
            // Limit item name to prevent over drawing
            final ItemStack fakeStack = this.item.asRealStack().copy();
            final FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;

            StringBuilder displayName = new StringBuilder();
            for (char c : fakeStack.getDisplayName().toCharArray()) {
                if (fontRenderer.getStringWidth(displayName.toString() + c + " x " + this.item.getQuantity()) > this.itemNameSpaceAvailable) {
                    displayName = new StringBuilder(displayName.toString().substring(0, Math.max(displayName.length() - 3, 0)) + "...");
                    break;
                }
                displayName.append(c);
            }

            this.itemLabel.setText(TextSerializers.LEGACY_FORMATTING_CODE.serialize(
                    Text.of(TextColors.WHITE, displayName.toString(), TextColors.GRAY, " x ",
                            ExchangeConstants.withSuffix(this.item.getQuantity()))));
        }
    }

    public static final class ListItemComponent extends ExchangeItemComponent<ListItem> {

        private UIContainer<?> listedIndicatorContainer;
        private UIExpandingLabel priceLabel;

        public ListItemComponent(MalisisGui gui, UIDynamicList<ListItem> parent, ListItem item) {
            super(gui, parent, item);
        }

        @Override
        protected void construct(final MalisisGui gui) {
            super.construct(gui);

            this.listedIndicatorContainer = new UIContainer<>(gui, 5, this.height - (this.borderSize * 2));
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

        @SuppressWarnings("deprecation")
        public void update() {
            final boolean listed = this.item.getForSaleItem().isPresent();

            // Update visibility
            this.listedIndicatorContainer.setVisible(listed);
            this.priceLabel.setVisible(listed);

            if (listed) {
                final BigDecimal forSalePrice = this.item.getForSaleItem().map(ForSaleItem::getPrice).orElse(BigDecimal.valueOf(0));
                final double forSaleDoublePrice = forSalePrice.doubleValue();

                this.priceLabel.setText(TextSerializers.LEGACY_FORMATTING_CODE
                        .serialize(Text.of(TextColors.GOLD, ExchangeConstants.withSuffix(forSaleDoublePrice), TextColors.GRAY, "/ea")));
                this.priceLabel.setPosition(-(this.listedIndicatorContainer.getWidth() + 6), 0, Anchor.RIGHT | Anchor.MIDDLE);

                // Exact value
                this.priceLabel.setTooltip(I18n.format("almura.tooltip.exchange.total")
                        + ": " + ExchangeConstants.CURRENCY_DECIMAL_FORMAT.format(this.item.getQuantity() * forSaleDoublePrice));
            }

            this.itemNameSpaceAvailable = this.getWidth()
                    - (this.priceLabel.isVisible() ? this.priceLabel.getWidth() : 0)
                    - (this.image.isVisible() ? this.image.getWidth() : 0)
                    - (this.listedIndicatorContainer.isVisible() ? this.listedIndicatorContainer.getWidth() : 0)
                    - 12;

            this.refreshDisplayName();
        }

        @Override
        public boolean onDoubleClick(int x, int y, MouseButton button) {
            if (button != MouseButton.LEFT) {
                return super.onDoubleClick(x, y, button);
            }

            final UIComponent<?> componentAt = this.getComponentAt(x, y);
            final UIComponent<?> parentComponentAt = componentAt == null ? null : componentAt.getParent();
            if (!(componentAt instanceof UIDynamicList.ItemComponent) && !(parentComponentAt instanceof UIDynamicList.ItemComponent)) {
                return super.onDoubleClick(x, y, button);
            }

            ((ExchangeScreen) getGui()).list();

            return true;
        }
    }

    public static final class ForSaleItemComponent extends ExchangeItemComponent<ForSaleItem> {

        private UILabel sellerLabel;
        private UIExpandingLabel priceLabel;

        public ForSaleItemComponent(MalisisGui gui, UIDynamicList<ForSaleItem> parent, ForSaleItem item) {
            super(gui, parent, item);
        }

        @SuppressWarnings("deprecation")
        @Override
        protected void construct(final MalisisGui gui) {
            super.construct(gui);

            final int maxPlayerTextWidth = Minecraft.getMinecraft().fontRenderer.getStringWidth("9999999999999999");

            this.sellerLabel = new UILabel(gui, TextSerializers.LEGACY_FORMATTING_CODE.serialize(
                Text.of(TextColors.GRAY, TextStyles.ITALIC,
                        this.item.getListItem().getSellerName().orElse(I18n.format("almura.text.exchange.unknown")))));
            this.sellerLabel.setPosition(-innerPadding, 0, Anchor.RIGHT | Anchor.MIDDLE);

            final double price = this.item.getPrice().doubleValue();

            this.priceLabel = new UIExpandingLabel(gui, Text.of(TextColors.GOLD, ExchangeConstants.withSuffix(price), TextColors.GRAY, "/ea"));
            this.priceLabel.setFontOptions(this.priceLabel.getFontOptions().toBuilder().scale(0.8f).build());
            this.priceLabel.setPosition(-maxPlayerTextWidth + 6, 0, Anchor.RIGHT | Anchor.MIDDLE);

            // Exact value
            this.priceLabel.setTooltip(I18n.format("almura.tooltip.exchange.total")
                    + ": " + ExchangeConstants.CURRENCY_DECIMAL_FORMAT.format(this.item.getListItem().getQuantity() * price));

            this.itemNameSpaceAvailable = this.getWidth()
                    - (this.priceLabel.isVisible() ? this.priceLabel.getWidth() : 0)
                    - (this.image.isVisible() ? this.image.getWidth() : 0)
                    - maxPlayerTextWidth
                    - 6;

            this.add(this.sellerLabel, this.priceLabel);

            this.refreshDisplayName();
        }

        @Override
        public boolean onDoubleClick(int x, int y, MouseButton button) {
            if (button != MouseButton.LEFT) {
                return super.onDoubleClick(x, y, button);
            }

            final UIComponent<?> componentAt = this.getComponentAt(x, y);
            final UIComponent<?> parentComponentAt = componentAt == null ? null : componentAt.getParent();
            if (!(componentAt instanceof UIDynamicList.ItemComponent) && !(parentComponentAt instanceof UIDynamicList.ItemComponent)) {
                return super.onDoubleClick(x, y, button);
            }

            ((ExchangeScreen) getGui()).buy(1);

            return true;
        }
    }

    public enum SortType {
        OLDEST(I18n.format("almura.text.exchange.sort.oldest"), "created", "asc"),
        NEWEST(I18n.format("almura.text.exchange.sort.newest"), "created", "desc"),
        PRICE_ASC(I18n.format("almura.text.exchange.sort.price_ascending"), "price", "asc"),
        PRICE_DESC(I18n.format("almura.text.exchange.sort.price_descending"), "price", "desc"),
        DISPLAY_NAME_ASC(I18n.format("almura.text.exchange.sort.item_ascending"), "display_name", "asc"),
        DISPLAY_NAME_DESC(I18n.format("almura.text.exchange.sort.item_descending"), "display_name", "desc"),
        SELLER_NAME_ASC(I18n.format("almura.text.exchange.sort.player_ascending"), "seller_name", "asc"),
        SELLER_NAME_DESC(I18n.format("almura.text.exchange.sort.player_descending"), "seller_name", "desc");

        public final String displayName, id, direction;
        SortType(final String displayName, final String id, final String direction) {
            this.displayName = displayName;
            this.id = id;
            this.direction = direction;
        }
    }
}
