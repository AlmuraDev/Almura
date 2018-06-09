/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.client.gui;

import com.almuradev.almura.shared.client.ui.FontColors;
import com.almuradev.almura.shared.client.ui.component.UIFormContainer;
import com.almuradev.almura.shared.client.ui.component.button.UIButtonBuilder;
import com.almuradev.almura.shared.client.ui.screen.SimpleScreen;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.component.UISlot;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UISelect;
import net.malisis.core.client.gui.component.interaction.UITextField;
import net.malisis.core.inventory.MalisisSlot;
import net.malisis.core.renderer.font.FontOptions;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;

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

    private World world;
    private EntityPlayer player;
    private BlockPos blockpos;
    private UITextField itemSearchField, sellerSearchField;
    // MOCK DATA - START
    public List<MockOffer> offers = new ArrayList<>();
    public List<MockOffer> currentResults = new ArrayList<>();
    // MOCK DATA - END

    @Inject private static PluginContainer container;

    public ExchangeGUI(EntityPlayer player, World worldIn, BlockPos blockpos) {
        this.player = player;
        this.world = worldIn;
        this.blockpos = blockpos;

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
        final UIFormContainer form = new UIFormContainer(this, 600, 400, "");
        form.setAnchor(Anchor.CENTER | Anchor.MIDDLE);
        form.setMovable(true);
        form.setClosable(true);
        form.setBorder(FontColors.WHITE, 1, 185);
        form.setBackgroundAlpha(215);
        form.setBottomPadding(3);
        form.setRightPadding(3);
        form.setTopPadding(20);
        form.setLeftPadding(3);

        final UILabel titleLabel = new UILabel(this, "Almura Exchange");
        titleLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        titleLabel.setPosition(0, -15, Anchor.CENTER | Anchor.TOP);

        // Item Search Area
        final UIFormContainer searchArea = new UIFormContainer(this, 295, 315, "");
        searchArea.setPosition(0, 10, Anchor.LEFT | Anchor.TOP);
        searchArea.setMovable(false);
        searchArea.setClosable(false);
        searchArea.setBorder(FontColors.WHITE, 1, 185);
        searchArea.setBackgroundAlpha(215);
        searchArea.setPadding(3, 3);

        final UILabel itemSearchLabel = new UILabel(this, "Item Name:");
        itemSearchLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).scale(1.1F).build());
        itemSearchLabel.setPosition(0, 0, Anchor.LEFT | Anchor.TOP);

        this.itemSearchField = new UITextField(this, "", false);
        this.itemSearchField.setSize(145, 0);
        this.itemSearchField.setPosition(itemSearchLabel.getWidth() + innerPadding, itemSearchLabel.getY(), Anchor.LEFT | Anchor.TOP);
        this.itemSearchField.setEditable(true);
        this.itemSearchField.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(false).build());

        // Seller Search Area
        final UILabel sellerSearchLabel = new UILabel(this, "Seller:");
        sellerSearchLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).scale(1.1F).build());
        sellerSearchLabel.setPosition(itemSearchField.getX() - sellerSearchLabel.getWidth() + innerPadding, itemSearchLabel.getY() + 17,
                Anchor.LEFT | Anchor.TOP);

        this.sellerSearchField = new UITextField(this, "", false);
        this.sellerSearchField.setSize(145, 0);
        this.sellerSearchField.setPosition(itemSearchField.getX(), sellerSearchLabel.getY(), Anchor.LEFT | Anchor.TOP);
        this.sellerSearchField.setEditable(true);
        this.sellerSearchField.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(false).build());

        // Sort combobox
        final UISelect<SortType> comboBoxSortType = new UISelect<>(this, 80, Arrays.asList(SortType.values()));
        comboBoxSortType.setLabelFunction(type -> type == null ? "" : type.displayName); // Because that's reasonable.
        comboBoxSortType.selectFirst();
        comboBoxSortType.setPosition(-innerPadding, sellerSearchLabel.getY(), Anchor.RIGHT | Anchor.TOP);

        // Search button
        final UIButton buttonSearch = new UIButtonBuilder(this)
                .width(80)
                .anchor(Anchor.RIGHT | Anchor.TOP)
                .position(-innerPadding, 0)
                .text("Search")
                .onClick(() -> {
                    final String itemName = itemSearchField.getText();
                    final String username = sellerSearchField.getText();
                    this.currentResults.clear();
                    System.out.println(String.format("Searching for criteria: itemname=%s, username=%s", itemName, username));
                    this.getResults(itemName, username, comboBoxSortType.getSelectedValue())
                            .forEach(o -> {
                                System.out.println(String.format("Found %d of %s for %1.2f/ea by %s submitted on %s", o.quantity, o.type.getName(),
                                        o.pricePer, o.username, formatter.format(o.instant)));
                                this.currentResults.add(o);
                            });
                    if (this.currentResults.isEmpty()) {
                        System.out.println(String.format("No offers found matching criteria: itemname=%s, username=%s", itemName, username));
                    }
                })
                .build("button.search");

        // Bottom Page Control - first button
        final UIButton buttonFirstPage = new UIButtonBuilder(this)
                .width(40)
                .anchor(Anchor.LEFT | Anchor.BOTTOM)
                .position(0,0)
                .text("First")
                .listener(this)
                .build("button.first");

        // Bottom Page Control - previous button
        final UIButton buttonPreviousPage = new UIButtonBuilder(this)
                .width(40)
                .anchor(Anchor.LEFT | Anchor.BOTTOM)
                .position(buttonFirstPage.getX() + buttonFirstPage.getWidth() + innerPadding,0)
                .text("Previous")
                .listener(this)
                .build("button.previous");

        // Bottom Page Control - last button
        final UIButton buttonLastPage = new UIButtonBuilder(this)
                .width(40)
                .anchor(Anchor.RIGHT | Anchor.BOTTOM)
                .position(0,0)
                .text("Last")
                .listener(this)
                .build("button.last");

        // Bottom Page Control - next button
        final UIButton buttonNextPage = new UIButtonBuilder(this)
                .width(40)
                .anchor(Anchor.RIGHT | Anchor.BOTTOM)
                .position(-(buttonLastPage.getX() + buttonLastPage.getWidth() + innerPadding),0)
                .text("Next")
                .listener(this)
                .build("button.next");

        // Add Elements of Search Area
        searchArea.add(itemSearchLabel, itemSearchField, sellerSearchLabel, sellerSearchField, buttonSearch, comboBoxSortType, buttonFirstPage,
                buttonPreviousPage, buttonNextPage, buttonLastPage);

        // Economy Pane
        final UIFormContainer economyActionArea = new UIFormContainer(this, 295, 50, "");
        economyActionArea.setPosition(0, searchArea.getY() + searchArea.getHeight() + innerPadding , Anchor.LEFT | Anchor.TOP);
        economyActionArea.setMovable(false);
        economyActionArea.setClosable(false);
        economyActionArea.setBorder(FontColors.WHITE, 1, 185);
        economyActionArea.setBackgroundAlpha(215);
        economyActionArea.setBottomPadding(3);
        economyActionArea.setRightPadding(3);
        economyActionArea.setTopPadding(3);
        economyActionArea.setLeftPadding(3);

        // Bottom Economy Pane - buyStack button
        final UIButton buttonBuyStack = new UIButtonBuilder(this)
                .width(40)
                .anchor(Anchor.LEFT | Anchor.BOTTOM)
                .position(0,0)
                .text("Buy Stack")
                .listener(this)
                .build("button.buyStack");

        // Bottom Economy Pane - buyStack button
        final UIButton buttonBuySingle = new UIButtonBuilder(this)
                .width(40)
                .anchor(Anchor.CENTER | Anchor.BOTTOM)
                .position(0,0)
                .text("Buy 1")
                .listener(this)
                .build("button.buySingle");

        // Bottom Economy Pane - buyStack button
        final UIButton buttonBuyQuantity = new UIButtonBuilder(this)
                .width(40)
                .anchor(Anchor.RIGHT | Anchor.BOTTOM)
                .position(0,0)
                .text("Buy Quantity")
                .listener(this)
                .build("button.buyQuantity");

        economyActionArea.add(buttonBuyStack, buttonBuySingle, buttonBuyQuantity);

        // Seller Inventory Area
        final UIFormContainer sellerInventoryArea = new UIFormContainer(this, 295, 30, "");
        sellerInventoryArea.setPosition(0, 10, Anchor.RIGHT | Anchor.TOP);
        sellerInventoryArea.setMovable(false);
        sellerInventoryArea.setClosable(false);
        sellerInventoryArea.setBorder(FontColors.WHITE, 1, 185);
        sellerInventoryArea.setBackgroundAlpha(215);
        sellerInventoryArea.setBottomPadding(3);
        sellerInventoryArea.setRightPadding(3);
        sellerInventoryArea.setTopPadding(3);
        sellerInventoryArea.setLeftPadding(3);

        final UISlot exchangeSlot1 = new UISlot(this, new MalisisSlot());
        exchangeSlot1.setPosition(0, 0, Anchor.LEFT | Anchor.MIDDLE);
        exchangeSlot1.setTooltip("Exchange Slot 1");

        final UISlot exchangeSlot2 = new UISlot(this, new MalisisSlot());
        exchangeSlot2.setPosition(exchangeSlot1.getX() + exchangeSlot1.getWidth() + 10, 0, Anchor.LEFT | Anchor.MIDDLE);
        exchangeSlot2.setTooltip("Exchange Slot 2");

        final UISlot exchangeSlot3 = new UISlot(this, new MalisisSlot());
        exchangeSlot3.setPosition(exchangeSlot2.getX() + exchangeSlot2.getWidth() + 10, 0, Anchor.LEFT | Anchor.MIDDLE);
        exchangeSlot3.setTooltip("Exchange Slot 3");

        final UISlot exchangeSlot4 = new UISlot(this, new MalisisSlot());
        exchangeSlot4.setPosition(exchangeSlot3.getX() + exchangeSlot3.getWidth() + 10, 0, Anchor.LEFT | Anchor.MIDDLE);
        exchangeSlot4.setTooltip("Exchange Slot 4");

        final UISlot exchangeSlot5 = new UISlot(this, new MalisisSlot());
        exchangeSlot5.setPosition(exchangeSlot4.getX() + exchangeSlot4.getWidth() + 10, 0, Anchor.LEFT | Anchor.MIDDLE);
        exchangeSlot5.setTooltip("Exchange Slot 5");

        final UISlot exchangeSlot6 = new UISlot(this, new MalisisSlot());
        exchangeSlot6.setPosition(exchangeSlot5.getX() + exchangeSlot5.getWidth() + 10, 0, Anchor.LEFT | Anchor.MIDDLE);
        exchangeSlot6.setTooltip("Exchange Slot 6");

        sellerInventoryArea.add(exchangeSlot1, exchangeSlot2, exchangeSlot3, exchangeSlot4, exchangeSlot5, exchangeSlot6);

        // Inventory Area Section (right pane)
        final UIFormContainer inventoryArea = new UIFormContainer(this, 295, 315, "");
        inventoryArea.setPosition(0, 45, Anchor.RIGHT | Anchor.TOP);
        inventoryArea.setMovable(false);
        inventoryArea.setClosable(false);
        inventoryArea.setBorder(FontColors.WHITE, 1, 185);
        inventoryArea.setBackgroundAlpha(215);
        inventoryArea.setBottomPadding(3);
        inventoryArea.setRightPadding(3);
        inventoryArea.setTopPadding(3);
        inventoryArea.setLeftPadding(3);


        // Bottom Economy Pane - buyStack button
        final UIButton buttonList = new UIButtonBuilder(this)
                .width(40)
                .anchor(Anchor.LEFT | Anchor.BOTTOM)
                .position(0,0)
                .text("List for Sale")
                .listener(this)
                .build("button.listForSaleButton");

        // Bottom Economy Pane - buyStack button
        final UIButton buttonSetPrice = new UIButtonBuilder(this)
                .width(40)
                .anchor(Anchor.CENTER | Anchor.BOTTOM)
                .position(0,0)
                .text("Set Price")
                .listener(this)
                .build("button.setPrice");

        // Bottom Economy Pane - buyStack button
        final UIButton buttonRemoveItem = new UIButtonBuilder(this)
                .width(40)
                .anchor(Anchor.RIGHT | Anchor.BOTTOM)
                .position(0,0)
                .text("Remove Item")
                .listener(this)
                .build("button.removeItem");

        inventoryArea.add(buttonList, buttonSetPrice, buttonRemoveItem);

        // Close button
        final UIButton buttonClose = new UIButtonBuilder(this)
                .width(40)
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .text(Text.of("almura.guide.button.close"))
                .onClick(this::close)
                .build("button.close");

        form.add(titleLabel, searchArea, economyActionArea, sellerInventoryArea, inventoryArea, buttonClose);

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
    public void update(int mouseX, int mouseY, float partialTick) {
        super.update(mouseX, mouseY, partialTick);
        if (this.unlockMouse && this.lastUpdate == 25) {
            Mouse.setGrabbed(false); // Force the mouse to be visible even though Mouse.isGrabbed() is false.  //#BugsUnited.
            this.unlockMouse = false; // Only unlock once per session.
        } else {
            ++this.lastUpdate;
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

    // MOCK DATA METHODS
    public List<MockOffer> getResults(String itemName, String username, SortType sort) {
        return this.offers
                .stream()
                .filter(o -> {
                    if (!itemName.isEmpty() && !o.type.getName().toLowerCase().contains(itemName.toLowerCase())) {
                        return false;
                    }

                    if (!username.isEmpty() && !o.username.toLowerCase().contains(username.toLowerCase())) {
                        return false;
                    }

                    return true;
                })
                .sorted(sort.comparator)
                .collect(Collectors.toList());
    }

    public MockOffer createMockOffer(ItemType type) {
        return new MockOffer(Instant.now(), type, ThreadLocalRandom.current().nextInt(0, 999), ThreadLocalRandom.current().nextDouble(0, 999), UUID
                .randomUUID(), "player" + ThreadLocalRandom.current().nextInt(0, 999));
    }

    // MOCK DATA OBJECTS
    public class MockOffer {
        public final Instant instant;
        public final ItemType type;
        public final int quantity;
        public final double pricePer;
        public final UUID playerUuid;
        public final String username;

        public MockOffer(final Instant instant, final ItemType type, final int quantity,
                         final double pricePer, final UUID playerUuid, final String username) {
            this.instant = instant;
            this.type = type;
            this.quantity = quantity;
            this.pricePer = pricePer;
            this.playerUuid = playerUuid;
            this.username = username;
        }
    }

    public enum SortType {
        OLDEST("Oldest", (a, b) -> a.instant.compareTo(b.instant)),
        NEWEST("Newest", (a, b) -> b.instant.compareTo(a.instant)),
        PRICE_ASC("Price (Asc.)", (a, b) -> Double.compare(a.pricePer, b.pricePer)),
        PRICE_DESC("Price (Desc.)", (a, b) -> Double.compare(b.pricePer, a.pricePer)),
        ITEM_ASC("Item (Asc.)", (a, b) -> a.type.getName().compareTo(b.type.getName())),
        ITEM_DESC("Item (Desc.)", (a, b) -> b.type.getName().compareTo(a.type.getName())),
        PLAYER_ASC("Player (Asc.)", (a, b) -> a.username.compareTo(b.username)),
        PLAYER_DESC("Player (Desc.)", (a, b) -> b.username.compareTo(a.username));

        public final String displayName;
        public final Comparator<MockOffer> comparator;
        SortType(String displayName, Comparator<MockOffer> comparator) {
            this.displayName = displayName;
            this.comparator = comparator;
        }
    }
}
