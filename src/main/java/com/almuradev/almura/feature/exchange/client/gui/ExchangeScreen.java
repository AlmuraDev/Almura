/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.client.gui;

import com.almuradev.almura.feature.exchange.MockOffer;
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
import com.almuradev.almura.shared.util.MathUtil;
import com.google.common.collect.Lists;
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
import org.lwjgl.input.Mouse;
import org.slf4j.Logger;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.inject.Inject;

@SideOnly(Side.CLIENT)
public final class ExchangeScreen extends SimpleScreen {

    private static final DateTimeFormatter dateFormat = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withZone(ZoneId.systemDefault());
    private static final DecimalFormat defaultDecimalFormat = new DecimalFormat("#,##0.##");
    private static final double MILLION = 1000000.0;
    private static final double BILLION = 1000000000.0;
    private static final double TRILLION = 1000000000000.0;
    private static final int minScreenWidth = 600;
    private static final int minScreenHeight = 370;
    private static final int innerPadding = 2;

    private int lastUpdate = 0;
    private boolean unlockMouse = true;
    private UIButton buttonFirstPage, buttonPreviousPage, buttonNextPage, buttonLastPage, buttonBuyStack, buttonBuySingle, buttonBuyQuantity,
            buttonList, buttonSetPrice;
    private UILabel labelSearchPage;
    private UITextField itemSearchField, sellerSearchField;
    private UIDynamicList<MockOffer> resultsList, sellingList;
    private final List<MockOffer> allOffers = new ArrayList<>();
    private final List<MockOffer> currentResults = new ArrayList<>();
    private final List<MockOffer> playerOffers = new ArrayList<>();
    private int currentPage;
    private int pages;

    @Inject private static Logger logger;
    @Inject private static ClientNotificationManager clientNotificationManager;

    public ExchangeScreen() {
        // ADD MOCK DATA
        final List<List<MockOffer>> testList = Lists.newArrayList(this.allOffers, this.playerOffers);

        for (List<MockOffer> list : testList) {
            list.add(createMockOffer(ItemTypes.ACACIA_BOAT));
            list.add(createMockOffer(ItemTypes.ACACIA_BOAT));
            list.add(createMockOffer(ItemTypes.ACACIA_BOAT));
            list.add(createMockOffer(ItemTypes.SNOWBALL));
            list.add(createMockOffer(ItemTypes.WATER_BUCKET));
            list.add(createMockOffer(ItemTypes.GOLDEN_AXE));
            list.add(createMockOffer(ItemTypes.STONE));
            list.add(createMockOffer(ItemTypes.PUMPKIN));
            list.add(createMockOffer(ItemTypes.END_ROD));
            list.add(createMockOffer(ItemTypes.APPLE));
            list.add(createMockOffer(ItemTypes.BAKED_POTATO));
            list.add(createMockOffer(ItemTypes.CARROT));

        }
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

        this.resultsList = new UIDynamicList<>(this, searchArea.getWidth() - 10, 250);
        this.resultsList.setPosition(innerPadding, getPaddedY(sellerSearchField, 8));
        this.resultsList.setItemComponentFactory((g, e) -> new ResultItemComponent(this, e));
        this.resultsList.setItemComponentSpacing(1);
        this.resultsList.setCanDeselect(true);
        this.resultsList.setSelectConsumer((i) -> this.updateControls());

        // Search button
        final UIButton buttonSearch = new UIButtonBuilder(this)
                .width(80)
                .anchor(Anchor.RIGHT | Anchor.TOP)
                .position(-innerPadding, 1)
                .text("Search")
                .onClick(() -> {
                    final String itemName = itemSearchField.getText();
                    final String username = sellerSearchField.getText();

                    this.currentResults.clear();

                    logger.info(String.format("Searching for criteria: itemname=%s, playerName=%s", itemName, username));

                    this.currentResults.addAll(this.getResults(itemName, username, comboBoxSortType.getSelectedValue()));
                    this.pages = (Math.max(this.currentResults.size() - 1, 1) / 10) + 1;

                    this.setPage(1);

                    if (this.currentResults.isEmpty()) {
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
                this.buttonFirstPage, this.buttonPreviousPage, this.buttonNextPage, this.buttonLastPage, this.resultsList, this.labelSearchPage);

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

        this.sellingList = new UIDynamicList<>(this, searchArea.getWidth() - 10, 315);
        this.sellingList.setPosition(innerPadding, innerPadding);
        this.sellingList.setItemComponentFactory(ListingItemComponent::new);
        this.sellingList.setItems(this.playerOffers);
        this.sellingList.setItemComponentSpacing(1);
        this.sellingList.setCanDeselect(true);
        this.sellingList.setSelectConsumer((i) -> this.updateControls());

        // Bottom Economy Pane - buyStack button
        this.buttonList = new UIButtonBuilder(this)
                .width(40)
                .anchor(Anchor.LEFT | Anchor.BOTTOM)
                .position(0, 0)
                .text("List")
                .enabled(false)
                .onClick(() -> {
                    final MockOffer offer = this.sellingList.getSelectedItem();
                    if (offer != null) {
                        offer.listed = !offer.listed;
                    }
                    this.updateControls();
                })
                .build("button.list");

        // Bottom Economy Pane - buyStack button
        this.buttonSetPrice = new UIButtonBuilder(this)
                .width(40)
                .anchor(Anchor.CENTER | Anchor.BOTTOM)
                .position(0, 0)
                .text("Set Price")
                .enabled(false)
                .build("button.setprice");

        // Bottom Economy Pane - buyStack button
        final UIButton buttonRemoveItem = new UIButtonBuilder(this)
                .width(30)
                .anchor(Anchor.RIGHT | Anchor.BOTTOM)
                .position(0, 0)
                .text(Text.of(TextColors.DARK_GREEN, "+", TextColors.GRAY, "/", TextColors.RED, "-"))
                .enabled(true)
                .onClick(() -> new ExchangeOfferScreen(this).display())
                .build("button.add_remove");

        inventoryArea.add(sellingList, buttonList, buttonSetPrice, buttonRemoveItem);

        form.add(searchArea, economyActionArea, inventoryArea);

        addToScreen(form);
    }

    protected Consumer<Task> openWindow(String details) {  // Scheduler
        return task -> {
            if (details.equalsIgnoreCase("purchaseRequest")) {
                // Packet to send request.
            }
        };
    }

    @Override
    protected void keyTyped(char keyChar, int keyCode) {
        super.keyTyped(keyChar, keyCode);
        this.lastUpdate = 0; // Reset the timer when key is typed.
    }

    @Override
    protected void mouseClicked(int x, int y, int button) {
        super.mouseClicked(x, y, button);
        this.lastUpdate = 0; // Reset the timer when mouse is pressed.
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

        this.resultsList.setSelectedItem(null); // Clear selection
        this.resultsList.setItems(this.currentResults.stream().skip((page - 1) * 10).limit(10).collect(Collectors.toList()));
    }

    private void updateControls() {

        // Results list
        final boolean isResultSelected = this.resultsList.getSelectedItem() != null;
        // TODO: Logic for buying buttons

        // Selling list
        final boolean isSellingItemSelected = this.sellingList.getSelectedItem() != null;

        this.buttonList.setEnabled(isSellingItemSelected);
        this.buttonSetPrice.setEnabled(isSellingItemSelected);

        if (isSellingItemSelected && this.sellingList.getSelectedItem().listed) {
            this.buttonList.setText("Unlist");
        } else {
            buttonList.setText("List");
        }
    }

    public List<MockOffer> getResults(String itemName, String username, SortType sort) {
        return this.allOffers
                .stream()
                .filter(o -> {
                    final String itemDisplayName = o.item.getTranslation().get().toLowerCase();

                    if (!itemName.isEmpty() && !itemDisplayName.contains(itemName.toLowerCase())) {
                        return false;
                    }

                    if (!username.isEmpty() && !o.playerName.toLowerCase().contains(username.toLowerCase())) {
                        return false;
                    }

                    return true;
                })
                .sorted(sort.comparator)
                .collect(Collectors.toList());
    }

    public MockOffer createMockOffer(final ItemType type) {
        final long quantity = ThreadLocalRandom.current().nextLong(1, 500000);
        final BigDecimal pricePer = BigDecimal.valueOf(ThreadLocalRandom.current().nextInt(1, 10000));
        return new MockOffer(Instant.now(),
                             ItemStack.of(type, 1), quantity, pricePer, UUID.randomUUID(), "player" + ThreadLocalRandom.current().nextInt(0, 999));
    }

    public static String withSuffix(long value) {
        if (value < 1000000) {
            return defaultDecimalFormat.format(value);
        } else if (value < BILLION) {
            return defaultDecimalFormat.format(value / MILLION) + "m";
        } else if (value < TRILLION) {
            return defaultDecimalFormat.format(value / BILLION) + "b";
        }

        return defaultDecimalFormat.format(value / TRILLION) + "t";
    }

    public static class ExchangeItemComponent<T> extends UIDynamicList.ItemComponent<T> {

        private static final int BORDER_COLOR = org.spongepowered.api.util.Color.ofRgb(128, 128, 128).getRgb();
        private static final int INNER_COLOR = org.spongepowered.api.util.Color.ofRgb(0, 0, 0).getRgb();
        private static final int INNER_HOVER_COLOR = org.spongepowered.api.util.Color.ofRgb(40, 40, 40).getRgb();
        private static final int INNER_SELECTED_COLOR = org.spongepowered.api.util.Color.ofRgb(65, 65, 65).getRgb();

        public ExchangeItemComponent(final MalisisGui gui, final T item) {
            super(gui, item);

            // Set padding
            this.setPadding(3, 3);

            // Set colors
            this.setColor(INNER_COLOR);
            this.setBorder(BORDER_COLOR, 1, 255);

            // Set default size
            this.setSize(0, 24);

            this.construct(gui, item);
        }

        protected void construct(final MalisisGui gui, final T item) {}

        @Override
        public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
            if (this.parent instanceof UIDynamicList) {
                final UIDynamicList parent = (UIDynamicList) this.parent;

                final int width = parent.getWidth() - (parent.getScrollBar().isEnabled() ? parent.getScrollBar().getRawWidth() + 5 : 0);

                this.setSize(width, getHeight());

                if (parent.getSelectedItem() == this.item) {
                    this.setColor(INNER_SELECTED_COLOR);
                } else if (this.isHovered()) {
                    this.setColor(INNER_HOVER_COLOR);
                } else {
                    this.setColor(INNER_COLOR);
                }

                super.drawBackground(renderer, mouseX, mouseY, partialTick);
            }
        }
    }

    public static class BaseItemComponent extends ExchangeItemComponent<MockOffer> {

        private UIComplexImage image;
        private UIExpandingLabel itemLabel;

        public BaseItemComponent(final MalisisGui gui, final MockOffer offer) {
            super(gui, offer);
        }

        @SuppressWarnings("deprecation")
        @Override
        protected void construct(final MalisisGui gui, final MockOffer offer) {
            // Add components
            final net.minecraft.item.ItemStack fakeStack = ItemStackUtil.toNative(offer.item.copy());
            fakeStack.setCount(1);
            final EntityPlayer player = Minecraft.getMinecraft().player;
            final boolean useAdvancedTooltips = Minecraft.getMinecraft().gameSettings.advancedItemTooltips;

            this.image = new UIComplexImage(gui, fakeStack);
            this.image.setPosition(0, 0, Anchor.LEFT | Anchor.MIDDLE);
            this.image.setTooltip(new UISaneTooltip(gui, String.join("\n", fakeStack.getTooltip(player, useAdvancedTooltips
                    ? ITooltipFlag.TooltipFlags.ADVANCED
                    : ITooltipFlag.TooltipFlags.NORMAL))));

            final FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
            final int maxItemTextWidth = fontRenderer.getStringWidth("999999999999999999");

            // Limit item name to prevent over drawing
            final StringBuilder itemTextBuilder = new StringBuilder();
            for (char c : offer.item.getTranslation().get().toCharArray()) {
                final int textWidth = fontRenderer.getStringWidth(itemTextBuilder.toString() + c);
                if (textWidth > maxItemTextWidth + 4) {
                    itemTextBuilder.replace(itemTextBuilder.length() - 3, itemTextBuilder.length(), "...");
                    break;
                }
                itemTextBuilder.append(c);
            }
            this.itemLabel = new UIExpandingLabel(gui, TextSerializers.LEGACY_FORMATTING_CODE.serialize(
                    Text.of(TextColors.WHITE, itemTextBuilder.toString(), TextColors.GRAY, " x ", withSuffix(offer.quantity))), true);
            this.itemLabel.setPosition(getPaddedX(this.image, 4), 0, Anchor.LEFT | Anchor.MIDDLE);

            if (offer.quantity >= (long) MILLION) {
                this.itemLabel.setTooltip(new UISaneTooltip(gui, defaultDecimalFormat.format(offer.quantity)));
            }

            this.add(this.image, this.itemLabel);
        }

        @Override
        public void draw(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
            super.draw(renderer, mouseX, mouseY, partialTick);
        }
    }

    public static final class ResultItemComponent extends BaseItemComponent {

        private UILabel sellerLabel;
        private UIExpandingLabel priceLabel;

        private ResultItemComponent(MalisisGui gui, MockOffer offer) {
            super(gui, offer);
        }

        @SuppressWarnings("deprecation")
        @Override
        protected void construct(final MalisisGui gui, final MockOffer offer) {
            super.construct(gui, offer);

            final int maxPlayerTextWidth = Minecraft.getMinecraft().fontRenderer.getStringWidth("9999999999999999");

            this.sellerLabel = new UILabel(gui, TextSerializers.LEGACY_FORMATTING_CODE.serialize(
                    Text.of(TextColors.GRAY, TextStyles.ITALIC, offer.playerName)));
            this.sellerLabel.setPosition(-innerPadding, 0, Anchor.RIGHT | Anchor.MIDDLE);

            this.priceLabel = new UIExpandingLabel(gui, TextSerializers.LEGACY_FORMATTING_CODE.serialize(
                    Text.of(TextColors.GOLD, offer.pricePer, TextColors.GRAY, "/ea")), true);
            this.priceLabel.setFontOptions(this.priceLabel.getFontOptions().toBuilder().scale(0.8f).build());
            this.priceLabel.setPosition(-maxPlayerTextWidth + 6, 0, Anchor.RIGHT | Anchor.MIDDLE);
            this.priceLabel.setTooltip("Total: " + defaultDecimalFormat.format(BigDecimal.valueOf(offer.quantity).multiply(offer.pricePer)));

            this.add(this.sellerLabel, this.priceLabel);
        }
    }

    public static final class ListingItemComponent extends BaseItemComponent {

        private UIContainer<?> statusContainer;
        private UIExpandingLabel priceLabel;
        private MockOffer item;

        private ListingItemComponent(MalisisGui gui, MockOffer offer) {
            super(gui, offer);
            this.item = offer;
        }

        @SuppressWarnings("deprecation")
        @Override
        protected void construct(final MalisisGui gui, final MockOffer offer) {
            super.construct(gui, offer);

            this.statusContainer = new UIContainer<>(gui, 5, this.height - (this.borderSize * 2));
            this.statusContainer.setPosition(2, -2, Anchor.TOP | Anchor.RIGHT);
            this.statusContainer.setColor(FontColors.DARK_GREEN);

            this.priceLabel = new UIExpandingLabel(gui, TextSerializers.LEGACY_FORMATTING_CODE.serialize(
                    Text.of(TextColors.GOLD, offer.pricePer, TextColors.GRAY, "/ea")), true);
            this.priceLabel.setFontOptions(this.priceLabel.getFontOptions().toBuilder().scale(0.8f).build());
            this.priceLabel.setPosition(-(this.statusContainer.getWidth() + 6), 0, Anchor.RIGHT | Anchor.MIDDLE);
            this.priceLabel.setTooltip("Total: " + defaultDecimalFormat.format(BigDecimal.valueOf(offer.quantity).multiply(offer.pricePer)));

            this.add(this.statusContainer, this.priceLabel);
        }

        @Override
        public void draw(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
            this.statusContainer.setAlpha(this.item.listed ? 255 : 0);
            super.draw(renderer, mouseX, mouseY, partialTick);
        }
    }

    public enum SortType {
        OLDEST("Oldest", (a, b) -> a.instant.compareTo(b.instant)),
        NEWEST("Newest", (a, b) -> b.instant.compareTo(a.instant)),
        PRICE_ASC("Price (Asc.)", (a, b) -> a.pricePer.compareTo(b.pricePer)),
        PRICE_DESC("Price (Desc.)", (a, b) -> b.pricePer.compareTo(a.pricePer)),
        ITEM_ASC("Item (Asc.)", (a, b) -> a.item.getTranslation().get().compareTo(b.item.getTranslation().get())),
        ITEM_DESC("Item (Desc.)", (a, b) -> b.item.getTranslation().get().compareTo(a.item.getTranslation().get())),
        PLAYER_ASC("Player (Asc.)", (a, b) -> a.playerName.compareTo(b.playerName)),
        PLAYER_DESC("Player (Desc.)", (a, b) -> b.playerName.compareTo(a.playerName));

        public final String displayName;
        public final Comparator<MockOffer> comparator;
        SortType(String displayName, Comparator<MockOffer> comparator) {
            this.displayName = displayName;
            this.comparator = comparator;
        }
    }
}
