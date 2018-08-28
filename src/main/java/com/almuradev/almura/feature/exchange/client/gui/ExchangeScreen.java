/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.client.gui;

import com.almuradev.almura.feature.exchange.client.ClientExchangeManager;
import com.almuradev.almura.feature.exchange.Exchange;
import com.almuradev.almura.feature.notification.ClientNotificationManager;
import com.almuradev.almura.feature.notification.type.PopupNotification;
import com.almuradev.almura.shared.client.ui.FontColors;
import com.almuradev.almura.shared.client.ui.component.UIComplexImage;
import com.almuradev.almura.shared.client.ui.component.UIDynamicList;
import com.almuradev.almura.shared.client.ui.component.UIExpandingLabel;
import com.almuradev.almura.shared.client.ui.component.UIFormContainer;
import com.almuradev.almura.shared.client.ui.component.UISaneTooltip;
import com.almuradev.almura.shared.client.ui.component.button.UIButtonBuilder;
import com.almuradev.almura.shared.client.ui.component.container.UIContainer;
import com.almuradev.almura.shared.client.ui.component.dialog.MessageBoxButtons;
import com.almuradev.almura.shared.client.ui.component.dialog.UIMessageBox;
import com.almuradev.almura.shared.client.ui.screen.SimpleScreen;
import com.almuradev.almura.shared.feature.store.listing.ForSaleItem;
import com.almuradev.almura.shared.feature.store.listing.ListItem;
import com.almuradev.almura.shared.item.VirtualStack;
import com.almuradev.almura.shared.util.MathUtil;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UISelect;
import net.malisis.core.client.gui.component.interaction.UITextField;
import net.malisis.core.renderer.font.FontOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.slf4j.Logger;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.inject.Inject;

@SideOnly(Side.CLIENT)
public final class ExchangeScreen extends SimpleScreen {

    public static final DecimalFormat DEFAULT_DECIMAL_FORMAT = new DecimalFormat("#,##0.##");
    public static final double MILLION = 1000000.0;
    private static final double BILLION = 1000000000.0;
    private static final int minScreenWidth = 600;
    private static final int minScreenHeight = 370;
    private static final int innerPadding = 2;
    @Inject private static Logger logger;
    @Inject private static ClientNotificationManager clientNotificationManager;
    @Inject private static ClientExchangeManager clientExchangeManager;

    private final Exchange exchange;
    private final List<ForSaleItem> currentForSaleItemResults = new ArrayList<>();

    private UIButton buttonFirstPage, buttonPreviousPage, buttonNextPage, buttonLastPage, buttonBuyStack, buttonBuySingle, buttonBuyQuantity,
        buttonList;
    private UILabel labelSearchPage;
    private UITextField itemSearchField, sellerSearchField;
    private int currentPage;
    private int pages;

    public UIDynamicList<ListItem> listItemList;
    public UIDynamicList<ForSaleItem> forSaleList;

    public ExchangeScreen(final Exchange exchange) {
        this.exchange = exchange;
    }

    @Override
    public void construct() {
        guiscreenBackground = false;
        Keyboard.enableRepeatEvents(true);

        // Detect if screen area is large enough to display.
        if (minScreenWidth > resolution.getScaledWidth() || minScreenHeight > resolution.getScaledHeight()) {
            clientNotificationManager.queuePopup(new PopupNotification(Text.of("Exchange Error"),
                Text.of("Screen area of: " + minScreenHeight + " x " + minScreenWidth + " required."), 5));
            this.close();
        }

        // Main Panel
        final UIFormContainer form = new UIFormContainer(this, minScreenWidth, minScreenHeight, "");
        form.setAnchor(Anchor.CENTER | Anchor.MIDDLE);
        form.setMovable(true);
        form.setClosable(true);
        form.setTitle("Exchange");
        form.setBorder(FontColors.WHITE, 1, 185);
        form.setBackgroundAlpha(215);
        form.setBottomPadding(3);
        form.setRightPadding(3);
        form.setTopPadding(20);
        form.setLeftPadding(3);

        // Item Search Area
        final UIFormContainer searchArea = new UIFormContainer(this, 295, 323, "");
        searchArea.setPosition(0, 0, Anchor.LEFT | Anchor.TOP);
        searchArea.setMovable(false);
        searchArea.setClosable(false);
        searchArea.setBorder(FontColors.WHITE, 1, 185);
        searchArea.setBackgroundAlpha(215);
        searchArea.setPadding(3, 3);

        final UILabel itemSearchLabel = new UILabel(this, "Item Name:");
        itemSearchLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).scale(1.1F).build());
        itemSearchLabel.setPosition(0, 4, Anchor.LEFT | Anchor.TOP);

        this.itemSearchField = new UITextField(this, "", false);
        this.itemSearchField.setSize(145, 0);
        this.itemSearchField.setPosition(itemSearchLabel.getWidth() + innerPadding, 2, Anchor.LEFT | Anchor.TOP);
        this.itemSearchField.setEditable(true);
        this.itemSearchField.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(false).build());

        // Seller Search Area
        final UILabel sellerSearchLabel = new UILabel(this, "Seller:");
        sellerSearchLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).scale(1.1F).build());
        sellerSearchLabel.setPosition(itemSearchField.getX() - sellerSearchLabel.getWidth() - 1,
            getPaddedY(itemSearchLabel, 7), Anchor.LEFT | Anchor.TOP);

        this.sellerSearchField = new UITextField(this, "", false);
        this.sellerSearchField.setSize(145, 0);
        this.sellerSearchField.setPosition(this.itemSearchField.getX(), getPaddedY(this.itemSearchField, 4), Anchor.LEFT | Anchor.TOP);
        this.sellerSearchField.setEditable(true);
        this.sellerSearchField.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(false).build());

        // Sort combobox
        final UISelect<SortType> comboBoxSortType = new UISelect<>(this, 78, Arrays.asList(SortType.values()));
        comboBoxSortType.setLabelFunction(type -> type == null ? "" : type.displayName); // Because that's reasonable.
        comboBoxSortType.select(SortType.PRICE_ASC);
        comboBoxSortType.setPosition(-(innerPadding + 1), this.sellerSearchField.getY(), Anchor.RIGHT | Anchor.TOP);

        this.forSaleList = new UIDynamicList<>(this, searchArea.getWidth() - 10, 250);
        this.forSaleList.setPosition(innerPadding, getPaddedY(this.sellerSearchField, 8));
        this.forSaleList.setItemComponentFactory((g, e) -> new ForSaleItemComponent(this, e));
        this.forSaleList.setItemComponentSpacing(1);
        this.forSaleList.setCanDeselect(true);
        this.forSaleList.setSelectConsumer((i) -> this.updateControls());

        // Search button
        final UIButton buttonSearch = new UIButtonBuilder(this)
            .width(80)
            .anchor(Anchor.RIGHT | Anchor.TOP)
            .position(-innerPadding, 1)
            .text("Search")
            .onClick(() -> {
                final String itemName = this.itemSearchField.getText();
                final String username = this.sellerSearchField.getText();

                this.currentForSaleItemResults.clear();

                logger.info(String.format("Searching for criteria: itemname=%s, playerName=%s", itemName, username));

                final List<ForSaleItem> items =
                        this.exchange.getForSaleItems().values().stream().flatMap(List::stream).collect(Collectors.toList());
                this.pages = (Math.max(items.size() - 1, 1) / 10) + 1;

                this.setPage(1);

                if (this.currentForSaleItemResults.isEmpty()) {
                    final String message = String.format("No offers found matching criteria: itemname=%s, playerName=%s", itemName, username);
                    UIMessageBox.showDialog(this, "", message, MessageBoxButtons.OK);
                }
            })
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

        // Add Elements of Search Area
        searchArea.add(itemSearchLabel, this.itemSearchField, sellerSearchLabel, this.sellerSearchField, buttonSearch, comboBoxSortType,
            this.buttonFirstPage, this.buttonPreviousPage, this.buttonNextPage, this.buttonLastPage, this.forSaleList, this.labelSearchPage);

        // Economy Pane
        final UIFormContainer economyActionArea = new UIFormContainer(this, 295, 20, "");
        economyActionArea.setPosition(0, getPaddedY(searchArea, innerPadding), Anchor.LEFT | Anchor.TOP);
        economyActionArea.setMovable(false);
        economyActionArea.setClosable(false);
        economyActionArea.setBorder(FontColors.WHITE, 1, 185);
        economyActionArea.setBackgroundAlpha(215);
        economyActionArea.setPadding(2, 0);

        // Bottom Economy Pane - buyStack button
        this.buttonBuyStack = new UIButtonBuilder(this)
            .width(60)
            .anchor(Anchor.LEFT | Anchor.MIDDLE)
            .position(0, 0)
            .text("Buy Stack")
            .enabled(false)
            .build("button.buy.stack");

        // Bottom Economy Pane - buyStack button
        this.buttonBuySingle = new UIButtonBuilder(this)
            .width(60)
            .anchor(Anchor.CENTER | Anchor.MIDDLE)
            .position(0, 0)
            .text("Buy 1")
            .enabled(false)
            .build("button.buy.single");

        // Bottom Economy Pane - buyStack button
        this.buttonBuyQuantity = new UIButtonBuilder(this)
            .width(60)
            .anchor(Anchor.RIGHT | Anchor.MIDDLE)
            .position(0, 0)
            .text("Buy Quantity")
            .enabled(false)
            .build("button.buy.quantity");

        economyActionArea.add(this.buttonBuyStack, this.buttonBuySingle, this.buttonBuyQuantity);

        // Inventory Area Section (right pane)
        final UIFormContainer inventoryArea = new UIFormContainer(this, 295, 345, "");
        inventoryArea.setPosition(0, 0, Anchor.RIGHT | Anchor.TOP);
        inventoryArea.setMovable(false);
        inventoryArea.setClosable(false);
        inventoryArea.setBorder(FontColors.WHITE, 1, 185);
        inventoryArea.setBackgroundAlpha(215);
        inventoryArea.setPadding(3, 3);

        this.listItemList = new UIDynamicList<>(this, searchArea.getWidth() - 10, 315);
        this.listItemList.setPosition(innerPadding, innerPadding);
        this.listItemList.setItemComponentFactory(ListItemComponent::new);
        this.listItemList.setItemComponentSpacing(1);
        this.listItemList.setCanDeselect(true);
        this.listItemList.setSelectConsumer((i) -> this.updateControls());

        // Bottom Economy Pane - buyStack button
        this.buttonList = new UIButtonBuilder(this)
            .width(40)
            .anchor(Anchor.LEFT | Anchor.BOTTOM)
            .position(0, 0)
            .text("List")
            .enabled(false)
            .onClick(() -> {
                final ListItem offer = this.listItemList.getSelectedItem();
                if (offer != null) {
                    // TODO: Grinch: Handle list/delist
                    // TODO: Grinch: Prompt for price if listing
                }
                this.updateControls();
            })
            .build("button.list");

        // Bottom Economy Pane - buyStack button
        final UIButton buttonRemoveItem = new UIButtonBuilder(this)
            .width(30)
            .anchor(Anchor.RIGHT | Anchor.BOTTOM)
            .position(0, 0)
            .text(Text.of(TextColors.DARK_GREEN, "+", TextColors.GRAY, "/", TextColors.RED, "-"))
            .enabled(true)
            .onClick(() -> new ExchangeOfferScreen(this, this.exchange).display())
            .build("button.add_remove");

        inventoryArea.add(this.listItemList, this.buttonList, buttonRemoveItem);

        form.add(searchArea, economyActionArea, inventoryArea);

        addToScreen(form);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false; // Can't stop the game otherwise the Sponge Scheduler also stops.
    }

    @SuppressWarnings("deprecation")
    private void setPage(int page) {
        page = MathUtil.squashi(page, 1, this.pages);

        this.currentPage = page;

        this.buttonFirstPage.setEnabled(page != 1);
        this.buttonPreviousPage.setEnabled(page != 1);
        this.buttonNextPage.setEnabled(page != this.pages);
        this.buttonLastPage.setEnabled(page != this.pages);

        this.labelSearchPage.setText(TextSerializers.LEGACY_FORMATTING_CODE.serialize(Text.of(TextColors.WHITE, page, "/", pages)));

        this.forSaleList.setSelectedItem(null); // Clear selection
        this.forSaleList.setItems(this.currentForSaleItemResults.stream().skip((page - 1) * 10).limit(10).collect(Collectors.toList()));
    }

    private void updateControls() {

        // Results list
        final boolean isResultSelected = this.forSaleList.getSelectedItem() != null;
        // TODO: Logic for buying buttons

        // Selling list
        final boolean isSellingItemSelected = this.listItemList.getSelectedItem() != null;

        this.buttonList.setEnabled(isSellingItemSelected);

        if (isSellingItemSelected && this.listItemList.getSelectedItem().getForSaleItem().isPresent()) {
            this.buttonList.setText("Unlist");
        } else {
            buttonList.setText("List");
        }
    }

    public List<ForSaleItem> getResults(String itemName, String username, SortType sort) {
        return this.exchange.getForSaleItems().values()
                .stream()
                .flatMap(List::stream)
                .filter(o -> {
                    final String itemDisplayName = o.asRealStack().getDisplayName().toLowerCase();
                    final String itemSellerName = o.getListItem().getSellerName().orElse("").toLowerCase();

                    if (!itemName.isEmpty() && !itemDisplayName.contains(itemName.toLowerCase())) {
                        return false;
                    }

                    return username.isEmpty() || itemSellerName.contains(username.toLowerCase());
                })
                .sorted(sort.comparator)
                .collect(Collectors.toList());
    }

    public static String withSuffix(int value) {
        if (value < MILLION) {
            return DEFAULT_DECIMAL_FORMAT.format(value);
        } else if (value < BILLION) {
            return DEFAULT_DECIMAL_FORMAT.format(value / MILLION) + "m";
        }
        return DEFAULT_DECIMAL_FORMAT.format(value / BILLION) + "t";
    }

    public void refreshListItems() {
        final List<ListItem> listItems = this.exchange.getListItemsFor(Minecraft.getMinecraft().player.getUniqueID()).orElse(null);
        if (listItems == null) {
            this.listItemList.clearItems();
        } else {
            this.listItemList.setItems(listItems);
        }
    }

    public static class ExchangeItemComponent<T extends VirtualStack> extends UIDynamicList.ItemComponent<T> {

        private UIComplexImage image;
        private UIExpandingLabel itemLabel;

        public ExchangeItemComponent(final MalisisGui gui, final T stack) {
            super(gui, stack);
        }

        @SuppressWarnings("deprecation")
        @Override
        protected void construct(final MalisisGui gui) {
            this.setSize(0, 24);

            // Add components
            final net.minecraft.item.ItemStack fakeStack = item.asRealStack().copy();
            fakeStack.setCount(1);
            final EntityPlayer player = Minecraft.getMinecraft().player;
            final boolean useAdvancedTooltips = Minecraft.getMinecraft().gameSettings.advancedItemTooltips;

            this.image = new UIComplexImage(gui, fakeStack);
            this.image.setPosition(0, 0, Anchor.LEFT | Anchor.MIDDLE);
            this.image.setTooltip(new UISaneTooltip(gui, String.join("\n", fakeStack.getTooltip(player, useAdvancedTooltips
                ? ITooltipFlag.TooltipFlags.ADVANCED
                : ITooltipFlag.TooltipFlags.NORMAL))));

            final FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
            final int maxItemTextWidth = fontRenderer.getStringWidth("999999999999999999") + 4;

            // Limit item name to prevent over drawing
            String displayName = fakeStack.getDisplayName();
            if (fontRenderer.getStringWidth(displayName) > maxItemTextWidth) {
                final StringBuilder displayNameBuilder = new StringBuilder();
                for (char c : fakeStack.getDisplayName().toCharArray()) {
                    final int textWidth = fontRenderer.getStringWidth(displayNameBuilder.toString() + c);
                    if (textWidth > maxItemTextWidth) {
                        displayNameBuilder.replace(displayNameBuilder.length() - 3, displayNameBuilder.length(), "...");
                        break;
                    }
                    displayNameBuilder.append(c);
                }
                displayName = displayNameBuilder.toString();
            }
            this.itemLabel = new UIExpandingLabel(gui, TextSerializers.LEGACY_FORMATTING_CODE.serialize(
                Text.of(TextColors.WHITE, displayName, TextColors.GRAY, " x ", withSuffix(item.getQuantity()))));
            this.itemLabel.setPosition(getPaddedX(this.image, 4), 0, Anchor.LEFT | Anchor.MIDDLE);

            if (item.getQuantity() >= (int) MILLION) {
                this.itemLabel.setTooltip(new UISaneTooltip(gui, DEFAULT_DECIMAL_FORMAT.format(item.getQuantity())));
            }

            this.add(this.image, this.itemLabel);
        }
    }

    public static final class ListItemComponent extends ExchangeItemComponent<ListItem> {

        private UIContainer<?> statusContainer;
        private UIExpandingLabel priceLabel;

        private ListItemComponent(final MalisisGui gui, final ListItem item) {
            super(gui, item);
        }

        @SuppressWarnings("deprecation")
        @Override
        protected void construct(final MalisisGui gui) {
            super.construct(gui);

            this.statusContainer = new UIContainer<>(gui, 5, this.height - (this.borderSize * 2));
            this.statusContainer.setPosition(2, -2, Anchor.TOP | Anchor.RIGHT);
            this.statusContainer.setColor(FontColors.DARK_GREEN);

            final ForSaleItem forSaleItem = this.item.getForSaleItem().orElse(null);

            // TODO Grinch, I'm sure you want to handle this differently.
            if (forSaleItem != null) {
                this.priceLabel = new UIExpandingLabel(gui, Text.of(TextColors.GOLD, forSaleItem.getPrice(), TextColors.GRAY, "/ea"));
                this.priceLabel.setFontOptions(this.priceLabel.getFontOptions().toBuilder().scale(0.8f).build());
                this.priceLabel.setPosition(-(this.statusContainer.getWidth() + 6), 0, Anchor.RIGHT | Anchor.MIDDLE);
                this.priceLabel.setTooltip("Total: " +
                    DEFAULT_DECIMAL_FORMAT.format(BigDecimal.valueOf(this.item.getQuantity()).multiply(forSaleItem.getPrice())));

                this.add(this.statusContainer, this.priceLabel);
            } else {
                this.add(this.statusContainer);
            }
        }

        @Override
        public void draw(final GuiRenderer renderer, final int mouseX, final int mouseY, final float partialTick) {
            this.statusContainer.setAlpha(this.item.getForSaleItem().isPresent() ? 255 : 0);
            super.draw(renderer, mouseX, mouseY, partialTick);
        }
    }

    public static final class ForSaleItemComponent extends ExchangeItemComponent<ForSaleItem> {

        private UILabel sellerLabel;
        private UIExpandingLabel priceLabel;
        private final ForSaleItem forSaleItem;

        private ForSaleItemComponent(final MalisisGui gui, final ForSaleItem forSaleItem) {
            super(gui, forSaleItem);
            this.forSaleItem = forSaleItem;
        }

        @SuppressWarnings("deprecation")
        @Override
        protected void construct(final MalisisGui gui) {
            super.construct(gui);

            final int maxPlayerTextWidth = Minecraft.getMinecraft().fontRenderer.getStringWidth("9999999999999999");

            this.sellerLabel = new UILabel(gui, TextSerializers.LEGACY_FORMATTING_CODE.serialize(
                Text.of(TextColors.GRAY, TextStyles.ITALIC, "PLAYER_NAME"))); // TODO: Real player name
            this.sellerLabel.setPosition(-innerPadding, 0, Anchor.RIGHT | Anchor.MIDDLE);

            this.priceLabel = new UIExpandingLabel(gui, Text.of(TextColors.GOLD, this.forSaleItem.getPrice(), TextColors.GRAY, "/ea"));
            this.priceLabel.setFontOptions(this.priceLabel.getFontOptions().toBuilder().scale(0.8f).build());
            this.priceLabel.setPosition(-maxPlayerTextWidth + 6, 0, Anchor.RIGHT | Anchor.MIDDLE);
            this.priceLabel.setTooltip("Total: " +
                DEFAULT_DECIMAL_FORMAT.format(BigDecimal.valueOf(item.getQuantity()).multiply(this.forSaleItem.getPrice())));

            this.add(this.sellerLabel, this.priceLabel);
        }
    }

    public enum SortType {
        OLDEST("Oldest", (a, b) -> a.getCreated().compareTo(b.getCreated())),
        NEWEST("Newest", (a, b) -> b.getCreated().compareTo(a.getCreated())),
        PRICE_ASC("Price (Asc.)", (a, b) -> a.getPrice().compareTo(b.getPrice())),
        PRICE_DESC("Price (Desc.)", (a, b) -> b.getPrice().compareTo(a.getPrice())),
        ITEM_ASC("Item (Asc.)", (a, b) -> a.asRealStack().getDisplayName().compareTo(b.asRealStack().getDisplayName())),
        ITEM_DESC("Item (Desc.)", (a, b) -> b.asRealStack().getDisplayName().compareTo(a.asRealStack().getDisplayName())),
        PLAYER_ASC("Player (Asc.)", (a, b) -> a.getListItem().getSellerName().orElse("").compareTo(b.getListItem().getSellerName().orElse(""))),
        PLAYER_DESC("Player (Desc.)", (a, b) -> b.getListItem().getSellerName().orElse("").compareTo(a.getListItem().getSellerName().orElse("")));

        public final String displayName;
        public final Comparator<ForSaleItem> comparator;
        SortType(final String displayName, final Comparator<ForSaleItem> comparator) {
            this.displayName = displayName;
            this.comparator = comparator;
        }
    }
}
