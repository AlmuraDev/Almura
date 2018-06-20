/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.client.gui;

import com.almuradev.almura.feature.notification.ClientNotificationManager;
import com.almuradev.almura.feature.notification.type.PopupNotification;
import com.almuradev.almura.shared.client.ui.FontColors;
import com.almuradev.almura.shared.client.ui.component.UIFormContainer;
import com.almuradev.almura.shared.client.ui.component.UISaneTooltip;
import com.almuradev.almura.shared.client.ui.component.UISimpleList;
import com.almuradev.almura.shared.client.ui.component.button.UIButtonBuilder;
import com.almuradev.almura.shared.client.ui.screen.SimpleScreen;
import com.almuradev.almura.shared.util.MathUtil;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.UIBackgroundContainer;
import net.malisis.core.client.gui.component.container.UIListContainer;
import net.malisis.core.client.gui.component.decoration.UIImage;
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

import java.math.BigDecimal;
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
public final class ExchangeGUI extends SimpleScreen {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withZone(ZoneId.systemDefault());
    private static final int innerPadding = 2;
    private int lastUpdate = 0;
    private boolean unlockMouse = true;

    private UIButton buttonFirstPage, buttonPreviousPage, buttonNextPage, buttonLastPage, buttonBuyStack, buttonBuySingle, buttonBuyQuantity;
    private UILabel labelSearchPage;
    private UITextField itemSearchField, sellerSearchField;
    private UISimpleList<ListElementData<MockOffer>> resultsList;
    private UIFormContainer form;
    private final List<MockOffer> offers = new ArrayList<>();
    private final List<ListElementData<MockOffer>> currentResults = new ArrayList<>();
    private int currentPage;
    private int pages;
    private int screenWidth = 600;
    private int screenHeight = 370;

    @Inject private static Logger logger;
    @Inject private static ClientNotificationManager clientNotificationManager;

    public ExchangeGUI() {
        // ADD MOCK DATA
        offers.add(createMockOffer(ItemTypes.ACACIA_BOAT));
        offers.add(createMockOffer(ItemTypes.ACACIA_BOAT));
        offers.add(createMockOffer(ItemTypes.ACACIA_BOAT));
        offers.add(createMockOffer(ItemTypes.SNOWBALL));
        offers.add(createMockOffer(ItemTypes.WATER_BUCKET));
        offers.add(createMockOffer(ItemTypes.GOLDEN_AXE));
        offers.add(createMockOffer(ItemTypes.STONE));
        offers.add(createMockOffer(ItemTypes.PUMPKIN));
        offers.add(createMockOffer(ItemTypes.END_ROD));
        offers.add(createMockOffer(ItemTypes.APPLE));
        offers.add(createMockOffer(ItemTypes.BAKED_POTATO));
        offers.add(createMockOffer(ItemTypes.CARROT));
    }

    @Override
    public void construct() {
        guiscreenBackground = false;
        Keyboard.enableRepeatEvents(true);

        // Main Panel
        form = new UIFormContainer(this, screenWidth, screenHeight, "");
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

        this.resultsList = new UISimpleList<>(this, searchArea.getWidth() - 10, 250);
        this.resultsList.setPosition(innerPadding, getPaddedY(sellerSearchField, 8));
        this.resultsList.setComponentFactory((g, e) -> new ResultListElement(this, e));
        this.resultsList.setElementSpacing(1);
        this.resultsList.register(this);

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

                    this.getResults(itemName, username, comboBoxSortType.getSelectedValue())
                            .forEach(r -> this.currentResults.add(new ListElementData<>(this.resultsList, r)));
                    this.pages = (Math.max(this.currentResults.size() - 1, 1) / 10) + 1;

                    this.setPage(1);

                    if (this.currentResults.isEmpty()) {
                        logger.info(String.format("No offers found matching criteria: itemname=%s, playerName=%s", itemName, username));
                    } else {
                        this.currentResults.forEach(r -> logger.info(String.format("Found %d of %s for %1.2f/ea by %s submitted on %s",
                                r.tag.item.getQuantity(),
                                r.tag.item.getTranslation().get(),
                                r.tag.pricePer,
                                r.tag.playerName,
                                formatter.format(r.tag.instant))));
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

        economyActionArea.add(buttonBuyStack, buttonBuySingle, buttonBuyQuantity);

        // Inventory Area Section (right pane)
        final UIFormContainer inventoryArea = new UIFormContainer(this, 295, 345, "");
        inventoryArea.setPosition(0, 0, Anchor.RIGHT | Anchor.TOP);
        inventoryArea.setMovable(false);
        inventoryArea.setClosable(false);
        inventoryArea.setBorder(FontColors.WHITE, 1, 185);
        inventoryArea.setBackgroundAlpha(215);
        inventoryArea.setPadding(3, 3);

        // Bottom Economy Pane - buyStack button
        final UIButton buttonList = new UIButtonBuilder(this)
                .width(40)
                .anchor(Anchor.LEFT | Anchor.BOTTOM)
                .position(0,0)
                .text("List")
                .enabled(false)
                .build("button.list");

        // Bottom Economy Pane - buyStack button
        final UIButton buttonSetPrice = new UIButtonBuilder(this)
                .width(40)
                .anchor(Anchor.CENTER | Anchor.BOTTOM)
                .position(0,0)
                .text("Set Price")
                .enabled(false)
                .build("button.setprice");

        // Bottom Economy Pane - buyStack button
        final UIButton buttonRemoveItem = new UIButtonBuilder(this)
                .width(30)
                .anchor(Anchor.RIGHT | Anchor.BOTTOM)
                .position(0,0)
                .text(Text.of(TextColors.DARK_GREEN, "+", TextColors.GRAY, "/", TextColors.RED, "-"))
                .enabled(false)
                .build("button.remove");

        inventoryArea.add(buttonList, buttonSetPrice, buttonRemoveItem);

        this.form.add(searchArea, economyActionArea, inventoryArea);

        // Detect if screen area is large enough to display.
        if (screenWidth > resolution.getScaledWidth() || screenHeight > resolution.getScaledHeight()) {
            clientNotificationManager.queuePopup(new PopupNotification(Text.of("Exchange Error"), Text.of("Screen area of: " + screenHeight + " x " + screenWidth + " required."), 5));
            this.close();
        } else {
            addToScreen(form);
        }
    }

    protected Consumer<Task> openWindow(String details) {  // Scheduler
        return task -> {
            if (details.equalsIgnoreCase("purchaseRequest")) {
                // Packet to send request.
            }
        };
    }

    @Override
    public void update(int mouseX, int mouseY, float partialTick) {
        super.update(mouseX, mouseY, partialTick);
        if (this.unlockMouse && this.lastUpdate == 25) {
            Mouse.setGrabbed(false); // Force the mouse to be visible even though Mouse.isGrabbed() is false.  //#BugsUnited.
            this.unlockMouse = false; // Only unlock once per session.
        } else if (this.unlockMouse) {
            this.lastUpdate++;
        }
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

    @Subscribe
    private void onElementSelect(UIListContainer.SelectEvent<ListElementData> event) {
        final boolean isSelected = event.getNewValue() != null;
        this.buttonBuyStack.setEnabled(isSelected);
        this.buttonBuySingle.setEnabled(isSelected);
        this.buttonBuyQuantity.setEnabled(isSelected);
    }

    private void setPage(int page) {
        page = MathUtil.squashi(page, 1, this.pages);

        this.currentPage = page;

        this.buttonFirstPage.setEnabled(page != 1);
        this.buttonPreviousPage.setEnabled(page != 1);
        this.buttonNextPage.setEnabled(page != this.pages);
        this.buttonLastPage.setEnabled(page != this.pages);

        this.labelSearchPage.setText(TextSerializers.LEGACY_FORMATTING_CODE.serialize(Text.of(TextColors.WHITE, page, "/", pages)));

        this.resultsList.select(null); // Clear selection
        this.resultsList.setElements(this.currentResults.stream().skip((page - 1) * 10).limit(10).collect(Collectors.toList()));
    }

    public List<MockOffer> getResults(String itemName, String username, SortType sort) {
        return this.offers
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
        final int quantity = ThreadLocalRandom.current().nextInt(1, 10000);
        final BigDecimal pricePer = BigDecimal.valueOf(ThreadLocalRandom.current().nextInt(0, 10000));
        return new MockOffer(Instant.now(),
                             ItemStack.of(type, quantity), pricePer, UUID.randomUUID(), "player" + ThreadLocalRandom.current().nextInt(0, 999));
    }

    public class MockOffer {
        public final Instant instant;
        public final ItemStack item;
        public final BigDecimal pricePer;
        public final UUID playerUuid;
        public final String playerName;

        public MockOffer(final Instant instant, final ItemStack item, final BigDecimal pricePer, final UUID playerUuid, final String playerName) {
            this.instant = instant;
            this.item = item;
            this.pricePer = pricePer;
            this.playerUuid = playerUuid;
            this.playerName = playerName;
        }
    }

    @SuppressWarnings("unchecked, deprecation")
    private static class ExchangeListElement<T> extends UIBackgroundContainer {

        private static final int BORDER_COLOR = org.spongepowered.api.util.Color.ofRgb(128, 128, 128).getRgb();
        private static final int INNER_COLOR = org.spongepowered.api.util.Color.ofRgb(0, 0, 0).getRgb();
        private static final int INNER_HOVER_COLOR = org.spongepowered.api.util.Color.ofRgb(40, 40, 40).getRgb();
        private static final int INNER_SELECTED_COLOR = org.spongepowered.api.util.Color.ofRgb(65, 65, 65).getRgb();

        private final ListElementData data;

        private ExchangeListElement(final MalisisGui gui, final ListElementData data) {
            super(gui);

            this.data = data;

            this.parent = this.data.parent;

            // Set padding
            this.setPadding(3, 3);

            // Set colors
            this.setColor(INNER_COLOR);
            this.setBorder(BORDER_COLOR, 1, 255);

            this.setSize(0, 24);

            this.construct(gui, data);
        }

        protected void construct(final MalisisGui gui, final ListElementData<T> data) {}

        @Override
        public boolean onClick(int x, int y) {
            final UIComponent component = getComponentAt(x, y);
            if (this.equals(component)) {
                final UIListContainer<ListElementData<T>> parent = this.data.parent;

                // Deselect the item if we click it again, select it otherwise.
                parent.select(parent.isSelected(this.data) ? null : this.data);
            }
            return true;
        }

        @Override
        public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
            if (this.parent instanceof UISimpleList) {
                final UISimpleList parent = (UISimpleList) this.parent;

                final int width = parent.getContentWidth() - (parent.getScrollBar().isEnabled() ? parent.getScrollBar().getRawWidth() + 1 : 0);

                setSize(width, getHeight());

                if (parent.isSelected(this.data)) {
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

    private static final class ResultListElement extends ExchangeListElement<MockOffer> {

        private UIImage image;
        private UILabel itemLabel, priceLabel, sellerLabel;

        private ResultListElement(final MalisisGui gui, final ListElementData data) {
            super(gui, data);
        }

        @Override
        protected void construct(final MalisisGui gui, final ListElementData<MockOffer> data) {
            // Add components
            final net.minecraft.item.ItemStack fakeStack = (net.minecraft.item.ItemStack) (Object) ItemStack.of(data.tag.item.getType(), 1);
            final EntityPlayer player = Minecraft.getMinecraft().player;
            final boolean useAdvancedTooltips = Minecraft.getMinecraft().gameSettings.advancedItemTooltips;

            this.image = new UIImage(gui, fakeStack);
            this.image.setPosition(0, 0, Anchor.LEFT | Anchor.MIDDLE);
            this.image.setTooltip(new UISaneTooltip(gui, String.join("\n", fakeStack.getTooltip(player, useAdvancedTooltips
                    ? ITooltipFlag.TooltipFlags.ADVANCED
                    : ITooltipFlag.TooltipFlags.NORMAL))));

            final FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
            final int maxItemTextWidth =  fontRenderer.getStringWidth("999999999999999999");
            final int maxPlayerTextWidth = fontRenderer.getStringWidth("9999999999999999");

            // Limit item name to prevent over drawing
            final StringBuilder itemTextBuilder = new StringBuilder();
            for (char c : (data.tag.item.getTranslation().get()).toCharArray()) {
                final int textWidth = fontRenderer.getStringWidth(itemTextBuilder.toString() + c + " x " + data.tag.item.getQuantity());
                if (textWidth > maxItemTextWidth + 4) {
                    itemTextBuilder.replace(itemTextBuilder.length() - 3, itemTextBuilder.length(), "...");
                    break;
                }
                itemTextBuilder.append(c);
            }
            this.itemLabel = new UILabel(gui, TextSerializers.LEGACY_FORMATTING_CODE.serialize(
                    Text.of(TextColors.WHITE, itemTextBuilder.toString(),
                            TextColors.GRAY, " x ", data.tag.item.getQuantity())
            ));
            this.itemLabel.setPosition(getPaddedX(this.image, 4), 0, Anchor.LEFT | Anchor.MIDDLE);

            this.sellerLabel = new UILabel(gui, TextSerializers.LEGACY_FORMATTING_CODE.serialize(
                    Text.of(TextColors.GRAY, TextStyles.ITALIC, data.tag.playerName)
            ));
            this.sellerLabel.setPosition(-innerPadding, 0, Anchor.RIGHT | Anchor.MIDDLE);

            this.priceLabel = new UILabel(gui, TextSerializers.LEGACY_FORMATTING_CODE.serialize(
                    Text.of(TextColors.GOLD, data.tag.pricePer, TextColors.GRAY, "/ea")
            ));
            this.priceLabel.setFontOptions(this.priceLabel.getFontOptions().toBuilder().scale(0.8f).build());
            this.priceLabel.setPosition(-maxPlayerTextWidth + 6, 0, Anchor.RIGHT | Anchor.MIDDLE);

            this.add(this.image, this.itemLabel, this.priceLabel, this.sellerLabel);
        }
    }

    private static final class ListElementData<T> {
        public final UISimpleList<ListElementData<T>> parent;
        public final T tag;

        private ListElementData(final UISimpleList<ListElementData<T>> parent, final T tag) {
            this.parent = parent;
            this.tag = tag;
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
