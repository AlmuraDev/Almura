/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.store.client.gui;

import com.almuradev.almura.asm.ClientStaticAccess;
import com.almuradev.almura.Almura;
import com.almuradev.almura.FeatureSortTypes;
import com.almuradev.almura.feature.FeatureSortType;
import com.almuradev.almura.feature.SortType;
import com.almuradev.almura.feature.store.SideType;
import com.almuradev.almura.feature.store.Store;
import com.almuradev.almura.feature.store.StoreItemSegmentType;
import com.almuradev.almura.feature.store.network.ServerboundModifyItemsPacket;
import com.almuradev.almura.shared.feature.filter.Direction;
import com.almuradev.almura.feature.store.StoreModule;
import com.almuradev.almura.feature.store.client.ClientStoreManager;
import com.almuradev.almura.feature.store.listing.BuyingItem;
import com.almuradev.almura.feature.store.listing.SellingItem;
import com.almuradev.almura.feature.store.listing.StoreItem;
import com.almuradev.almura.shared.client.ui.component.UIExpandingLabel;
import com.almuradev.almura.shared.client.ui.component.UISaneTooltip;
import com.almuradev.almura.shared.feature.FeatureConstants;
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
import net.malisis.core.client.gui.render.GuiTexture;
import net.minecraft.util.ResourceLocation;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.decoration.UITooltip;
import net.malisis.core.client.gui.component.interaction.BasicTextBox;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UICheckBox;
import net.malisis.core.client.gui.component.interaction.UISelect;
import net.malisis.core.client.gui.component.interaction.UITextField;
import net.malisis.core.client.gui.component.interaction.button.builder.UIButtonBuilder;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.util.FontColors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.inject.Inject;

@SideOnly(Side.CLIENT)
public class StoreScreen extends BasicScreen {

    @Inject private static ClientStoreManager storeManager;
    private static final int defaultTabColor = 0x1E1E1E;
    private static final int hoveredTabColor = 0x3C3C3C;
    private static final int innerPadding = 2;
    private static final Map<ItemFinder, Object> itemFinderRelationshipMap = new LinkedHashMap<>();
    // The admin pane and the gap before it
    private static final int ADMIN_PANE_WIDTH = 224;
    private static final SortType SHOP_ORDER =
      new FeatureSortType("index", I18n.format("almura.feature.store.text.sort.shop_order"), Direction.ASCENDING);
    private static final ResourceLocation ARROW_UP = new ResourceLocation(Almura.ID, "textures/gui/guide_arrow_up.png");
    private static final ResourceLocation ARROW_DOWN = new ResourceLocation(Almura.ID, "textures/gui/guide_arrow_down.png");
    // Move arrow buttons, black outline included; the price moves left by their width and a gap
    private static final int ARROW_BUTTON_WIDTH = 15, ARROW_BUTTON_HEIGHT = 11, ARROW_GAP = 4;

    private final Store store;
    private final boolean isAdmin;
    private final int selectedTabColor = 0;
    private final int tabWidth = 119;
    private final int tabHeight = 16;
    private final List<ItemStack> adminBaseList = new ArrayList<>();

    private SideType currentSide = SideType.BUY;
    private UIButton buttonTransactStack, buttonTransactOne, buttonTransactAll, buttonTransactQuantity, buttonAdminList, buttonAdminUnlist;
    private BasicContainer<?> buyTabContainer, sellTabContainer;
    private BasicList<StoreItem> itemList;
    private BasicList<ItemStack> adminItemList;
    private UILabel typeLabel, adminListTotalLabel, buyTabLabel, sellTabLabel;
    private BasicLine thisDoesNotExistLine;
    private UISelect<ItemFinder> locationSelect;
    private BasicTextBox itemDisplayNameSearchBox;
    private BasicTextBox adminSearchTextBox;
    private UISelect<SortType> comboBoxSortType;
    // Admins: the admin pane, shown in edit mode (otherwise the store looks as it does to players)
    private BasicForm form;
    private BasicContainer<?> adminContainer;
    private int playerFormWidth;
    private boolean editMode;
    // The list shows every item of the side in shop order (as last searched), so its items can be moved up and down
    private boolean listInShopOrder;

    public StoreScreen(final Store store, final boolean isAdmin) {
        this.store = store;
        this.isAdmin = isAdmin;

        if (itemFinderRelationshipMap.isEmpty()) {
            // Always clear inventory and store items
            itemFinderRelationshipMap.put(ItemFinders.STORE_ITEM_FINDER, this.store);
            itemFinderRelationshipMap.put(ItemFinders.INVENTORY_ITEM_FINDER, Minecraft.getMinecraft().player);

            // Generate an item finder for each creativetab
            Arrays
              .stream(CreativeTabs.CREATIVE_TAB_ARRAY)
              .filter(tab -> tab != CreativeTabs.INVENTORY && tab != CreativeTabs.SEARCH && tab != CreativeTabs.HOTBAR)
              .sorted(Comparator.comparing(t -> I18n.format(t.getTranslationKey())))
              .forEach(tab -> {
                final NonNullList<ItemStack> itemList = NonNullList.create();
                itemFinderRelationshipMap.put(new ItemFinder<CreativeTabs>(tab.getTabLabel().toLowerCase(), tab.getTranslationKey(), (innerTab) -> {
                    ForgeRegistries.ITEMS.getValuesCollection().stream().map(ItemStack::new).forEach((i -> i.getItem().getSubItems(innerTab, itemList)));
                    return itemList;
                }, true), tab);
            });
        } else {
            itemFinderRelationshipMap.replace(ItemFinders.STORE_ITEM_FINDER, this.store);
            itemFinderRelationshipMap.replace(ItemFinders.INVENTORY_ITEM_FINDER, Minecraft.getMinecraft().player);
        }
    }

    @Override
    public void construct() {
        guiscreenBackground = false;

        final BasicForm form = new BasicForm(257, 300, this.store.getName());
        form.setBackgroundAlpha(255);

        final BasicContainer<?> storeContainer = new BasicContainer<>(251, UIComponent.INHERITED);
        storeContainer.setBorder(FontColors.WHITE, 1, 185);
        storeContainer.setPadding(3);
        storeContainer.setColor(0);

        // Buy tab
        final int leftOffset = storeContainer.getLeftPadding() - storeContainer.getLeftBorderSize();
        this.buyTabContainer = new BasicContainer<>(this.tabWidth, this.tabHeight);
        this.buyTabContainer.setBorders(FontColors.WHITE, 185, 1, 1, 1, 0);
        this.buyTabContainer.setPosition(2, 2);
        this.buyTabContainer.setColor(defaultTabColor);
        this.buyTabContainer.attachData(SideType.BUY);

        // Buy tab label
        this.buyTabLabel = new UILabel(I18n.format("almura.feature.common.button.buy"));
        this.buyTabLabel.setPosition(0, 1, Anchor.MIDDLE | Anchor.CENTER);
        this.buyTabLabel.attachData(this.buyTabContainer.getData()); // Always use the same data as their parent
        this.buyTabContainer.add(this.buyTabLabel);

        // Sell tab
        this.sellTabContainer = new BasicContainer<>(this.tabWidth, this.tabHeight);
        this.sellTabContainer.setBorders(FontColors.WHITE, 185, 1, 1, 1, 0);
        this.sellTabContainer.setColor(defaultTabColor);
        this.sellTabContainer.setPosition(-2, 2, Anchor.TOP | Anchor.RIGHT);
        this.sellTabContainer.attachData(SideType.SELL);

        // Sell tab label
        this.sellTabLabel = new UILabel(I18n.format("almura.feature.common.button.sell"));
        this.sellTabLabel.setPosition(0, 1, Anchor.MIDDLE | Anchor.CENTER);
        this.sellTabLabel.attachData(this.sellTabContainer.getData()); // Always use the same data as their parent
        this.sellTabContainer.add(this.sellTabLabel);

        // Doesn't exist.
        this.thisDoesNotExistLine = new BasicLine(this.tabWidth - 2);
        this.thisDoesNotExistLine.setBackgroundAlpha(255);
        this.thisDoesNotExistLine.setColor(selectedTabColor);

        // Bottom tab line
        final BasicLine tabContainerLineBottom = new BasicLine(
                storeContainer.getWidth() - (storeContainer.getLeftBorderSize() + storeContainer.getRightBorderSize()), 1);
        tabContainerLineBottom.setPosition(-leftOffset, BasicScreen.getPaddedY(this.buyTabContainer, 0));

        // Search Y
        final int searchY = BasicScreen.getPaddedY(tabContainerLineBottom, 2);

        // Item Display Name Search Label
        final UILabel itemSearchLabel = new UILabel(I18n.format("almura.feature.exchange.text.item_name") + ":");
        itemSearchLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).build());
        itemSearchLabel.setPosition(1, searchY + 3, Anchor.LEFT | Anchor.TOP);

        // Item Display Name Search Text Box
        this.itemDisplayNameSearchBox = new BasicTextBox("");
        this.itemDisplayNameSearchBox.setAcceptsReturn(false);
        this.itemDisplayNameSearchBox.setAcceptsTab(false);
        this.itemDisplayNameSearchBox.setTabIndex(0);
        this.itemDisplayNameSearchBox.setOnEnter((tb) -> this.search());
        this.itemDisplayNameSearchBox.setSize(60, 0);
        this.itemDisplayNameSearchBox.setPosition(BasicScreen.getPaddedX(itemSearchLabel, innerPadding), searchY + 1, Anchor.LEFT | Anchor.TOP);
        this.itemDisplayNameSearchBox.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(false).build());

        // Sort combobox
        this.comboBoxSortType = new UISelect<>(78);
        this.comboBoxSortType.setPosition(BasicScreen.getPaddedX(this.itemDisplayNameSearchBox, innerPadding), searchY + 1);
        this.comboBoxSortType.setOptions(Arrays.asList(
          SHOP_ORDER,
          FeatureSortTypes.ITEM_DISPLAY_NAME_ASC,
          FeatureSortTypes.ITEM_DISPLAY_NAME_DESC,
          FeatureSortTypes.PRICE_ASC,
          FeatureSortTypes.PRICE_DESC
        ));
        this.comboBoxSortType.setOptionsWidth(UISelect.LONGEST_OPTION);
        this.comboBoxSortType.setLabelFunction(type -> type == null ? "" : type.getName()); // Because that's reasonable.
        // The order store admins arrange with the move arrows
        this.comboBoxSortType.select(SHOP_ORDER);

        // Search button
        final UIButton buttonSearch = new UIButtonBuilder()
          .width(50)
          .anchor(Anchor.RIGHT | Anchor.TOP)
          .position(0, searchY)
          .text(I18n.format("almura.feature.common.button.search"))
          .onClick(this::search)
          .build("button.search");

        // List
        this.itemList = new BasicList<>(UIComponent.INHERITED, BasicScreen.getPaddedHeight(form) - this.buyTabContainer.getHeight()
          - tabContainerLineBottom.getHeight() - buttonSearch.getHeight() - 30);
        this.itemList.setPosition(0, BasicScreen.getPaddedY(buttonSearch, 2));
        this.itemList.setSelectConsumer(i -> this.updateStoreControls());
        this.itemList.setItemComponentSpacing(1);
        this.itemList.setBorder(FontColors.WHITE, 1, 185);
        this.itemList.setPadding(2);
        this.itemList.setItemComponentFactory(StoreItemComponent::new);

        this.buttonTransactOne = new UIButtonBuilder()
                .width(60)
                .anchor(Anchor.BOTTOM | Anchor.LEFT)
                .enabled(false)
                .onClick(() -> this.transact(1))
                .build("button.transact.single");

        this.buttonTransactStack = new UIButtonBuilder()
                .width(60)
                .x(BasicScreen.getPaddedX(this.buttonTransactOne, 2))
                .anchor(Anchor.BOTTOM | Anchor.LEFT)
                .enabled(false)
                .onClick(() -> {
                    if (this.itemList.getSelectedItem() == null) {
                        return;
                    }
                    final int value = this.itemList.getSelectedItem().asRealStack().getMaxStackSize();
                    this.transact(value);
                })
                .build("button.transact.stack");

        this.buttonTransactAll = new UIButtonBuilder()
                .width(60)
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .enabled(false)
                .onClick(() -> {
                    final StoreItem selectedItem = this.itemList.getSelectedItem();
                    if (selectedItem == null) {
                        return;
                    }

                    // Determine the value to transact
                    final int value;
                    if (this.currentSide == SideType.BUY) { // If we are buying then base the value on the quantity remaining for the item to purchase
                        value = this.itemList.getSelectedItem().getQuantity();
                    } else { // If we're selling then base it on how many we have in our main inventory
                        value = Minecraft.getMinecraft().player.inventory.mainInventory
                          .stream()
                          .filter(i -> ItemStack.areItemsEqual(i, selectedItem.asRealStack()))
                          .mapToInt(ItemStack::getCount)
                          .sum();
                    }
                    this.transact(value);
                })
                .build("button.transact.all");

        this.buttonTransactQuantity = new UIButtonBuilder()
                .width(60)
                .x(BasicScreen.getPaddedX(this.buttonTransactAll, 2, Anchor.RIGHT))
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .enabled(false)
                .onClick(() -> {
                    final StoreItem selectedItem = this.itemList.getSelectedItem();
                    if (selectedItem == null) {
                        return;
                    }

                    new StoreTransactQuantityScreen(this, this.store, selectedItem, this.currentSide).display();
                })
                .build("button.transact.quantity");

        storeContainer.add(this.buyTabContainer, this.sellTabContainer, tabContainerLineBottom, this.thisDoesNotExistLine, itemSearchLabel,
          this.itemDisplayNameSearchBox, this.comboBoxSortType, buttonSearch, this.itemList, this.buttonTransactOne, this.buttonTransactStack,
          this.buttonTransactQuantity, this.buttonTransactAll);

        form.add(storeContainer);

        // Logic for admin pane
        if (this.isAdmin) {
            this.form = form;
            this.playerFormWidth = form.getWidth();

            // Create admin pane
            final BasicContainer<?> adminContainer = new BasicContainer(220, UIComponent.INHERITED);
            this.adminContainer = adminContainer;
            adminContainer.setPosition(BasicScreen.getPaddedX(storeContainer, 4), 0);
            adminContainer.setBorder(FontColors.WHITE, 1, 185);
            adminContainer.setPadding(3);
            adminContainer.setColor(0);

            // Buy tab label
            this.typeLabel = new UILabel(TextFormatting.WHITE + "Type: ");
            this.typeLabel.setPosition(0, 2, Anchor.TOP | Anchor.LEFT);

            // Item location selection
            this.locationSelect = new UISelect<>(UIComponent.INHERITED);
            this.locationSelect.setOptions(itemFinderRelationshipMap.keySet());
            this.locationSelect.setLabelFunction(o -> I18n.format(o.translationKey));
            this.locationSelect.setPosition(30, 0);
            this.locationSelect.setSize(184,12);
            this.locationSelect.maxDisplayedOptions(24);
            this.locationSelect.register(this);

            // Search text box
            this.adminSearchTextBox = new BasicTextBox("");
            this.adminSearchTextBox.setSize(UIComponent.INHERITED, this.locationSelect.getHeight());
            this.adminSearchTextBox.setPosition(0, BasicScreen.getPaddedY(this.locationSelect, 2));
            this.adminSearchTextBox.register(this);

            // Item list
            this.adminItemList = new BasicList<>(UIComponent.INHERITED,
                    BasicScreen.getPaddedHeight(form) - this.locationSelect.getHeight() - this.adminSearchTextBox.getHeight() - 28);
            this.adminItemList.setItemComponentFactory(AdminItemComponent::new);
            this.adminItemList.setItemComponentSpacing(1);
            this.adminItemList.setPosition(0, BasicScreen.getPaddedY(this.adminSearchTextBox, 2));
            this.adminItemList.setPadding(2);
            this.adminItemList.setBorder(FontColors.WHITE, 1, 185);
            this.adminItemList.setSelectConsumer(i -> this.updateAdminControls());

            this.buttonAdminList = new UIButtonBuilder()
                    .width(50)
                    .anchor(Anchor.BOTTOM | Anchor.LEFT)
                    .enabled(false)
                    .onClick(this::listOrModify)
                    .text(I18n.format("almura.feature.common.button.list"))
                    .build("button.list");
            this.buttonAdminUnlist = new UIButtonBuilder()
                    .width(50)
                    .anchor(Anchor.BOTTOM | Anchor.LEFT)
                    .position(BasicScreen.getPaddedX(this.buttonAdminList, 2), 0)
                    .enabled(false)
                    .onClick(this::unlist)
                    .text(I18n.format("almura.feature.common.button.unlist"))
                    .build("button.unlist");

            this.adminListTotalLabel = new UILabel(TextFormatting.WHITE + I18n.format("almura.feature.common.text.total") + ": ");
            this.adminListTotalLabel.setPosition(0, -2, Anchor.BOTTOM | Anchor.RIGHT);

            adminContainer.add(this.typeLabel, this.locationSelect, this.adminSearchTextBox, this.adminItemList, this.buttonAdminList,
                               this.buttonAdminUnlist, this.adminListTotalLabel);

            form.add(adminContainer);

            // Edit mode, at the left of the title bar: off shows the store as players see it
            final UICheckBox editMode = new UICheckBox(I18n.format("almura.feature.store.checkbox.edit_mode"));
            editMode.setName("checkbox.edit_mode");
            editMode.setFontOptions(FontColors.WHITE_FO.toBuilder().shadow(false).build());
            editMode.setTooltip(I18n.format("almura.feature.store.checkbox.edit_mode.tooltip"));
            editMode.setPosition(0, -16, Anchor.TOP | Anchor.LEFT);
            editMode.setChecked(ClientStaticAccess.configAdapter.get().general.storeEditMode);
            editMode.register(this);
            form.add(editMode);

            // Select the first item
            this.locationSelect.selectFirst();

            this.setEditMode(editMode.isChecked());
        }

        addToScreen(form);
    }

    @Subscribe
    private void onEditModeChange(final UICheckBox.CheckEvent event) {
        if (!"checkbox.edit_mode".equals(event.getComponent().getName())) {
            return;
        }
        ClientStaticAccess.configAdapter.get().general.storeEditMode = event.getNewValue();
        ClientStaticAccess.configAdapter.save();
        this.setEditMode(event.getNewValue());
    }

    /**
     * Shows the admin pane (and widens the window for it), or hides it so the store looks as it does to players.
     */
    private void setEditMode(final boolean editMode) {
        this.editMode = editMode;
        this.adminContainer.setVisible(editMode);
        this.form.setWidth(this.playerFormWidth + (editMode ? ADMIN_PANE_WIDTH : 0));
        // The rows show their move arrows in edit mode
        if (this.itemList != null) {
            this.search();
        }
    }

    /**
     * Moves an item one place up (-1) or down (1) the shop order of its side: swaps its index with its neighbour's. If the indexes repeat,
     * so the order can't be told apart, they are numbered 1, 2, 3... first. The server saves it and sends everyone the new order.
     */
    private void moveItem(final StoreItem item, final int direction) {
        if (!this.listInShopOrder) {
            return;
        }
        final List<StoreItem> order = new ArrayList<>(this.itemList.getItems());
        int from = -1;
        for (int i = 0; i < order.size(); i++) {
            if (order.get(i).getRecord() == item.getRecord()) {
                from = i;
            }
        }
        final int to = from + direction;
        if (from < 0 || to < 0 || to >= order.size()) {
            return;
        }

        final int[] indexes = new int[order.size()];
        final boolean renumber = order.stream().mapToInt(StoreItem::getIndex).distinct().count() < order.size();
        for (int i = 0; i < order.size(); i++) {
            indexes[i] = renumber ? i + 1 : order.get(i).getIndex();
        }
        final int swap = indexes[from];
        indexes[from] = indexes[to];
        indexes[to] = swap;

        final List<ServerboundModifyItemsPacket.ModifyCandidate> changed = new ArrayList<>();
        for (int i = 0; i < order.size(); i++) {
            final StoreItem storeItem = order.get(i);
            if (indexes[i] != storeItem.getIndex()) {
                changed.add(new ServerboundModifyItemsPacket.ModifyCandidate(storeItem.getRecord(), storeItem.getQuantity(), indexes[i],
                    storeItem.getPrice()));
            }
        }
        storeManager.requestModifyItems(this.store.getId(), this.currentSide == SideType.BUY ? StoreItemSegmentType.BUYING
          : StoreItemSegmentType.SELLING, changed);
    }

    @Override
    protected void mouseClicked(final int x, final int y, final int button) {
        this.getTabContainer(x, y).ifPresent(tab -> {
            final SideType targetSide = (SideType) tab.getData();;

            if (targetSide == SideType.BUY && this.store.getBuyingItems().size() == 0) {
                return;
            }

            if (targetSide == SideType.SELL && this.store.getSellingItems().size() == 0) {
                return;
            }

            this.currentSide = targetSide;
            this.refresh(false);
        });
        super.mouseClicked(x, y, button);
    }

    @Override
    public void drawScreen(final int mouseX, final int mouseY, final float partialTick) {
        // Update colors
        this.updateTabs(this.buyTabContainer, this.buyTabLabel);
        this.updateTabs(this.sellTabContainer, this.sellTabLabel);

        // Hover logic
        this.getTabContainer(mouseX, mouseY).ifPresent(tab -> {
            final SideType tabType = (SideType) tab.getData();

            if (tabType == SideType.BUY && this.store.getBuyingItems().size() == 0) {
                return;
            }

            if (tabType == SideType.SELL && this.store.getSellingItems().size() == 0) {
                return;
            }

            if (this.currentSide != tabType) {
                tab.setColor(hoveredTabColor);
            }
        });

        super.drawScreen(mouseX, mouseY, partialTick);
    }

    @Subscribe
    private void onTextChange(final ComponentEvent.ValueChange<UITextField, String> event) {
        if (this.adminSearchTextBox.equals(event.getComponent())) {
            if (this.adminSearchTextBox.getText().isEmpty()) {
                return;
            }

            this.adminItemList.setItems(this.adminBaseList
                    .stream()
                    .filter(i -> i.getDisplayName().toLowerCase().contains(event.getNewValue().toLowerCase()))
                    .sorted(Comparator.comparing(ItemStack::getDisplayName))
                    .collect(Collectors.toList()));

            this.adminListTotalLabel.setText(TextFormatting.WHITE + I18n.format("almura.feature.common.text.total") + ": "
              + this.adminItemList.getItems().size());
        }
    }

    @Subscribe
    private void onSelect(final UISelect.SelectEvent<ItemFinder> event) {
        this.createAdminControls(event.getNewValue());
    }

    public void refresh(final boolean createControls) {
        if (this.currentSide == SideType.BUY && this.store.getBuyingItems().size() == 0) {
            // Determine if we stay on buy or move to sell
            this.currentSide = this.store.getSellingItems().size() > 0 ? SideType.SELL : SideType.BUY;
        } else if (this.currentSide == SideType.SELL && this.store.getSellingItems().size() == 0) {
            this.currentSide = SideType.BUY;
        }

        this.buyTabContainer.setTooltip(this.store.getBuyingItems().size() == 0
          ? new UITooltip(I18n.format("almura.feature.store.text.no_items_for_sale"))
          : null);
        this.sellTabContainer.setTooltip(this.store.getSellingItems().size() == 0
          ? new UITooltip(I18n.format("almura.feature.store.text.no_items_for_purchase"))
          : null);

        this.search();

        if (createControls && this.isAdmin) {
            this.createAdminControls(this.locationSelect.getSelectedValue());
        }

        this.updateStoreControls();
        this.updateAdminControls();
    }

    private void search() {
        final Map<String, Object> filterQueryMap = new HashMap<>();
        filterQueryMap.put("item_display_name", this.itemDisplayNameSearchBox.getText());

        final Map<String, String> sortQueryMap = new HashMap<>();
        sortQueryMap.put(this.comboBoxSortType.getSelectedValue().getTypeId(), this.comboBoxSortType.getSelectedValue().getDirection().getId());

        final StringBuilder filterQueryBuilder = new StringBuilder();
        for (final Map.Entry<String, Object> entry : filterQueryMap.entrySet()) {
            if (entry.getValue().toString().isEmpty()) {
                continue;
            }
            filterQueryBuilder.append(StoreModule.ID).append("_").append(entry.getKey()).append(FeatureConstants.EQUALITY).append(entry.getValue())
              .append(FeatureConstants.DELIMITER);
        }

        // Not needed to be done this way in current form but adds native support if we allow multiple sorting patterns
        final StringBuilder sortQueryBuilder = new StringBuilder();
        for (final Map.Entry<String, String> entry : sortQueryMap.entrySet()) {
            if (entry.getValue().isEmpty()) {
                continue;
            }
            sortQueryBuilder.append(StoreModule.ID).append("_").append(entry.getKey()).append(FeatureConstants.EQUALITY)
              .append(entry.getValue()).append(FeatureConstants.DELIMITER);
        }

        final String filterQuery = filterQueryBuilder.toString().isEmpty() ? null : filterQueryBuilder.toString();
        final String sortQuery = sortQueryBuilder.toString().isEmpty() ? null : sortQueryBuilder.toString();

        // Store current selection
        final int currentRecord = this.itemList.getSelectedItem() != null ? this.itemList.getSelectedItem().getRecord() : -1;

        this.listInShopOrder = this.comboBoxSortType.getSelectedValue() == SHOP_ORDER && filterQuery == null;

        // Update the item list to have the current filtered items
        this.itemList.setItems(storeManager.filterLocalItems(this.store.getId(), filterQuery, sortQuery, this.currentSide));

        // Select the current item if available, otherwise the first item, last but not least, null if nothing is available.
        this.itemList.setSelectedItem(this.itemList.getItems()
          .stream()
          .filter(i -> i.getRecord() == currentRecord)
          .findFirst()
          .orElse(this.itemList.getItems().stream().findFirst().orElse(null)));
    }

    private void transact(final int value) {
        final StoreItem selectedItem = this.itemList.getSelectedItem();
        if (selectedItem == null) {
            return;
        }

        if (selectedItem.getQuantity() == 0) {
            return;
        }

        if (this.currentSide == SideType.BUY) {
            storeManager.buy(this.store.getId(), selectedItem.getRecord(), value);
        } else {
            // Don't attempt to sell if the player doesn't have that item
            if (Minecraft.getMinecraft().player.inventory.mainInventory
                  .stream()
                  .noneMatch((i -> ItemStack.areItemsEqual(i,selectedItem.asRealStack())))) {
                return;
            }
            storeManager.sell(this.store.getId(), selectedItem.getRecord(), value);
        }
    }

    private void listOrModify() {
        final ItemStack selectedItem = this.adminItemList.getSelectedItem();
        if (selectedItem == null) {
            return;
        }

        final Optional<BuyingItem> buyingItem = this.store.getBuyingItems()
                .stream()
                .filter(i -> StoreScreen.isStackEqualIgnoreSize(i.asRealStack(), selectedItem))
                .findAny();
        final Optional<SellingItem> sellingItem = this.store.getSellingItems()
                .stream()
                .filter(i -> StoreScreen.isStackEqualIgnoreSize(i.asRealStack(), selectedItem))
                .findAny();

        if (buyingItem.isPresent() || sellingItem.isPresent()) {
            new StoreListScreen(this, this.store, buyingItem.orElse(null), sellingItem.orElse(null), selectedItem).display();
        } else {
            new StoreListScreen(this, this.store, selectedItem).display();
        }
    }

    private void unlist() {
        final ItemStack selectedItem = this.adminItemList.getSelectedItem();
        if (selectedItem == null) {
            return;
        }

        this.store.getBuyingItems()
          .stream()
          .filter(i -> StoreScreen.isStackEqualIgnoreSize(i.asRealStack(), selectedItem))
          .findAny()
          .ifPresent(i -> storeManager.requestDelistBuyingItem(this.store.getId(), i.getRecord()));
        this.store.getSellingItems()
          .stream()
          .filter(i -> StoreScreen.isStackEqualIgnoreSize(i.asRealStack(), selectedItem))
          .findAny()
          .ifPresent(i -> storeManager.requestDelistSellingItem(this.store.getId(), i.getRecord()));
    }

    /**
     * Gets the tab container if present at the x and y position on screen
     *
     * @param x The x position
     * @param y The y position
     * @return The tab container for that location if present, empty otherwise
     */
    private Optional<BasicContainer> getTabContainer(final int x, final int y) {
        final UIComponent componentAt = this.getComponentAt(x, y);
        final UIComponent componentAtParent = componentAt != null ? componentAt.getParent() : null;
        if (this.buyTabContainer.equals(componentAt) || this.sellTabContainer.equals(componentAt)) {
            return Optional.of((BasicContainer) componentAt);
        } else if (this.buyTabContainer.equals(componentAtParent) || this.sellTabContainer.equals(componentAtParent)) {
            return Optional.of((BasicContainer) componentAtParent);
        }

        return Optional.empty();
    }

    /**
     * Updates the properties for the provided controls based on the attached data for {@link SideType}
     *
     * @param tabContainer The container
     * @param tabLabel The label
     */
    private void updateTabs(final BasicContainer<?> tabContainer, final UILabel tabLabel) {
        if (tabContainer.getData() instanceof SideType && tabLabel.getData() instanceof SideType) {
            tabContainer.setColor(this.currentSide == tabContainer.getData() ? selectedTabColor : defaultTabColor);
            tabLabel.setFontOptions(this.currentSide == tabLabel.getData() ? FontColors.WHITE_FO : FontColors.GRAY_FO);

            // Update the following if is the current side
            if (this.currentSide == tabContainer.getData()) {

                // Update something that doesn't exist
                final int offsetX = this.currentSide == SideType.BUY ? 1 : -1;
                this.thisDoesNotExistLine.setColor(selectedTabColor);
                this.thisDoesNotExistLine.setPosition(tabContainer.getX() + offsetX, BasicScreen.getPaddedY(tabContainer, 0),
                        tabContainer.getAnchor());

                this.updateStoreControls();
            }
        }
    }

    private void updateStoreControls() {
        final String sideName = this.currentSide.name().toLowerCase();
        final StoreItem selectedItem = this.itemList.getSelectedItem();

        // Common
        this.buttonTransactOne.setText(I18n.format("almura.feature.common.button." + sideName + ".one"));
        this.buttonTransactQuantity.setText(I18n.format("almura.feature.common.button." + sideName + ".quantity"));
        this.buttonTransactAll.setText(I18n.format("almura.feature.common.button." + sideName + ".all"));

        // Nothing selected
        if (selectedItem == null) {
            this.buttonTransactOne.setEnabled(false);
            this.buttonTransactStack.setEnabled(false);
            this.buttonTransactQuantity.setEnabled(false);
            this.buttonTransactAll.setEnabled(false);

            // Generic info
            this.buttonTransactStack.setText(I18n.format("almura.feature.common.button." + sideName + ".stack", 64));

            return;
        }

        final boolean isAvailable = selectedItem.getQuantity() != 0; // Do we have stock?
        final int maxStackSize = selectedItem.asRealStack().getMaxStackSize();

        if (currentSide == SideType.SELL) {
            // Determine how much we have in our main inventory
            final int heldQuantity =
              Minecraft.getMinecraft().player.inventory.mainInventory
                .stream()
                .filter(i -> ItemStack.areItemsEqual(selectedItem.asRealStack(), i))
                .mapToInt(ItemStack::getCount)
                .sum();

            // Update all enable states
            this.buttonTransactOne.setEnabled(isAvailable && heldQuantity >= 1); // We want at least one
            this.buttonTransactStack.setEnabled(isAvailable && heldQuantity >= maxStackSize);
            this.buttonTransactQuantity.setEnabled(isAvailable && heldQuantity > 1);
            this.buttonTransactAll.setEnabled(isAvailable && heldQuantity >= 1); // We want at least one

            // Update stack button
            this.buttonTransactStack.setText(I18n.format("almura.feature.common.button." + sideName + ".stack", maxStackSize));
        } else {
            final boolean isInfinite = selectedItem.getQuantity() == -1;
            this.buttonTransactStack.setText(I18n.format("almura.feature.common.button." + sideName + ".stack", maxStackSize));

            this.buttonTransactOne.setEnabled(isInfinite || isAvailable && selectedItem.getQuantity() > 0);
            this.buttonTransactStack.setEnabled(isInfinite || isAvailable && selectedItem.getQuantity() >= maxStackSize);
            this.buttonTransactQuantity.setEnabled(isInfinite || isAvailable && selectedItem.getQuantity() > 0);
            this.buttonTransactAll.setEnabled(isAvailable && selectedItem.getQuantity() >= 1);
        }
    }

    @SuppressWarnings("unchecked")
    private void createAdminControls(final ItemFinder finder) {
        // Clear the base list
        this.adminBaseList.clear();

        // Set the base list to items from item finder sorted by display name
        this.adminBaseList.addAll((List<ItemStack>) finder.getItems(itemFinderRelationshipMap.get(finder))
          .stream()
          .sorted(Comparator.comparing(ItemStack::getDisplayName))
          .collect(Collectors.toList()));

        // Set the visible item list to the base list filtered by search text
        this.adminItemList.setItems(this.adminBaseList
          .stream()
          .filter(i -> i.getDisplayName().toLowerCase().contains(this.adminSearchTextBox.getText().toLowerCase()))
          .collect(Collectors.toList()));

        // Attempt to select the first item
        this.adminItemList.setSelectedItem(this.adminItemList.getItems().stream().findFirst().orElse(null));

        this.updateAdminControls();
    }

    private void updateAdminControls() {
        if (!this.isAdmin) {
            return;
        }
        final ItemStack selectedItem = this.adminItemList.getSelectedItem();

        this.buttonAdminList.setEnabled(selectedItem != null);

        final Optional<BuyingItem> buyingItem = this.store.getBuyingItems()
                .stream()
                .filter(i -> StoreScreen.isStackEqualIgnoreSize(i.asRealStack(), selectedItem))
                .findAny();
        final Optional<SellingItem> sellingItem = this.store.getSellingItems()
                .stream()
                .filter(i -> StoreScreen.isStackEqualIgnoreSize(i.asRealStack(), selectedItem))
                .findAny();

        this.buttonAdminUnlist.setEnabled(buyingItem.isPresent() || sellingItem.isPresent());

        final String status = (buyingItem.isPresent() || sellingItem.isPresent()) ? "modify" : "list";
        this.buttonAdminList.setText(I18n.format("almura.feature.common.button." + status));

        this.adminItemList.getComponents()
          .stream()
          .filter(i -> i instanceof AdminItemComponent)
          .forEach(i -> ((AdminItemComponent) i).update());

        this.adminListTotalLabel.setText(TextFormatting.WHITE + I18n.format("almura.feature.common.text.total") + ": " + this.adminItemList.getItems().size());
    }

    public static boolean isStackEqualIgnoreSize(@Nullable final ItemStack a, @Nullable final ItemStack b) {
        if (a == null || b == null) {
            return false;
        }
        return net.minecraft.item.ItemStack.areItemsEqual(a, b) && net.minecraft.item.ItemStack.areItemStackTagsEqual(a, b);
    }

    public static class StoreItemComponent<T extends StoreItem> extends BasicList.ItemComponent<T> {

        protected UIImage image;
        protected UIExpandingLabel itemLabel, priceLabel;
        protected int itemNameSpaceAvailable;
        private UILabel soldOutLabel;
        private BasicContainer<?> soldOutContainer;
        private boolean hasMoveArrows;

        public StoreItemComponent(final MalisisGui gui, final BasicList<T> parent, final T item) {
            super(gui, parent, item);

            this.setOnDoubleClickConsumer(i -> ((StoreScreen) getGui()).transact(1));
        }

        @Override
        protected void construct(final MalisisGui gui) {
            this.setSize(0, 24);

            // Sold out container
            this.soldOutContainer = new BasicContainer<>(UIComponent.INHERITED, UIComponent.INHERITED);
            this.soldOutContainer.setColor(FontColors.DARK_GRAY);
            this.soldOutContainer.setBackgroundAlpha(180);
            this.soldOutContainer.setVisible(false);

            // Sold out label
            this.soldOutLabel = new UILabel(I18n.format("almura.feature.store.text.soldout"));
            this.soldOutLabel.setFontOptions(FontColors.RED_FO.toBuilder().scale(2f).build());
            this.soldOutLabel.setPosition(0, 1, Anchor.MIDDLE | Anchor.CENTER);
            this.soldOutContainer.add(this.soldOutLabel);

            // Default available space
            this.itemNameSpaceAvailable = this.getWidth();

            // Add components
            final ItemStack fakeStack = this.item.asRealStack().copy();
            fakeStack.setCount(1);
            final EntityPlayer player = Minecraft.getMinecraft().player;
            final boolean useAdvancedTooltips = Minecraft.getMinecraft().gameSettings.advancedItemTooltips;

            this.image = new UIImage(fakeStack);
            this.image.setPosition(0, 0, Anchor.LEFT | Anchor.MIDDLE);
            this.image.setTooltip(new UISaneTooltip(String.join("\n", fakeStack.getTooltip(player, useAdvancedTooltips
                    ? ITooltipFlag.TooltipFlags.ADVANCED
                    : ITooltipFlag.TooltipFlags.NORMAL))));

            this.itemLabel = new UIExpandingLabel("");
            this.itemLabel.setPosition(getPaddedX(this.image, 4), 0, Anchor.LEFT | Anchor.MIDDLE);

            // Exact value
            if (this.item.getQuantity() >= (int) FeatureConstants.ONE_MILLION) {
                this.itemLabel.setTooltip(new UISaneTooltip(FeatureConstants.CURRENCY_DECIMAL_FORMAT.format(this.item.getQuantity())));
            }

            // Store admins in edit mode get move arrows right of the price
            final StoreScreen screen = (StoreScreen) gui;
            this.hasMoveArrows = screen.isAdmin && screen.editMode;

            this.priceLabel = new UIExpandingLabel("");
            this.priceLabel.setFontOptions(this.priceLabel.getFontOptions().toBuilder().scale(0.8f).build());
            this.priceLabel.setPosition(-(this.hasMoveArrows ? ARROW_BUTTON_WIDTH + ARROW_GAP : 6), 0, Anchor.RIGHT | Anchor.MIDDLE);

            this.add(this.image, this.itemLabel, this.priceLabel, this.soldOutContainer);

            // After the sold out cover, so they stay clickable on sold out items
            if (this.hasMoveArrows) {
                this.addMoveArrows(gui, screen);
            }

            this.update();
        }

        /**
         * Up and down arrows that move this item in the shop order; only clickable while the list shows the whole side in shop order.
         */
        private void addMoveArrows(final MalisisGui gui, final StoreScreen screen) {
            final List<StoreItem> order = screen.itemList.getItems();
            final boolean first = order.isEmpty() || order.get(0).getRecord() == this.item.getRecord();
            final boolean last = order.isEmpty() || order.get(order.size() - 1).getRecord() == this.item.getRecord();
            final String disabledTooltip = screen.listInShopOrder ? null : I18n.format("almura.feature.store.button.move.shop_order_only");

            // Stacked on the row's middle, sharing the outline row between them
            final int half = ARROW_BUTTON_HEIGHT / 2;
            this.add(this.arrowButton(gui, screen, ARROW_UP, -1, -half, screen.listInShopOrder && !first, disabledTooltip),
                this.arrowButton(gui, screen, ARROW_DOWN, 1, ARROW_BUTTON_HEIGHT - 1 - half, screen.listInShopOrder && !last, disabledTooltip));
        }

        private UIButton arrowButton(final MalisisGui gui, final StoreScreen screen, final ResourceLocation texture, final int direction,
            final int y, final boolean enabled, @Nullable final String disabledTooltip) {
            final UIImage arrow = new UIImage(new GuiTexture(texture), null);
            arrow.setSize(9, 5);
            final UIButton button = new UIButtonBuilder()
                .image(arrow)
                .anchor(Anchor.MIDDLE | Anchor.RIGHT)
                .position(0, y)
                .enabled(enabled)
                .tooltip(disabledTooltip != null ? disabledTooltip
                    : I18n.format(direction < 0 ? "almura.feature.store.button.move_up" : "almura.feature.store.button.move_down"))
                .onClick(() -> screen.moveItem(this.item, direction))
                .build("button.move." + (direction < 0 ? "up" : "down"));
            // The button graphic has a 1 pixel black outline: at 15 x 11 the 9 x 5 arrow sits in the middle of the grey inside it
            button.setAutoSize(false);
            button.setSize(ARROW_BUTTON_WIDTH, ARROW_BUTTON_HEIGHT);
            return button;
        }

        protected void refreshDisplayName() {
            // Limit item name to prevent over drawing
            final ItemStack fakeStack = this.item.asRealStack().copy();
            final FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
            final String suffix = this.item.getQuantity() > 0
                    ? TextFormatting.GRAY + " x " + FeatureConstants.withSuffix(this.item.getQuantity())
                    : "";

            StringBuilder displayName = new StringBuilder();
            for (final char c : fakeStack.getDisplayName().toCharArray()) {
                if (fontRenderer.getStringWidth(displayName.toString() + c + suffix) > this.itemNameSpaceAvailable) {
                    displayName = new StringBuilder(displayName.toString().substring(0, Math.max(displayName.length() - 3, 0)) + "...");
                    break;
                }
                displayName.append(c);
            }

            this.itemLabel.setText(TextFormatting.WHITE + displayName.toString() + suffix);
        }


        public void update() {
            this.priceLabel.setText(TextFormatting.GOLD + FeatureConstants.withSuffix(this.item.getPrice().doubleValue())
                    + TextFormatting.GRAY + "/ea");

            if (this.item.getQuantity() > 0) {
                // Exact value
                this.priceLabel.setTooltip(I18n.format("almura.feature.exchange.tooltip.total") + ": " + FeatureConstants.CURRENCY_DECIMAL_FORMAT
                  .format(this.item.getQuantity() * this.item.getPrice().doubleValue()));
            }

            this.itemNameSpaceAvailable = this.getWidth()
                    - (this.priceLabel.isVisible() ? this.priceLabel.getWidth() : 0)
                    - (this.image.isVisible() ? this.image.getWidth() : 0)
                    - (this.hasMoveArrows ? ARROW_BUTTON_WIDTH + ARROW_GAP : 0)
                    - 16; // Static amount for spacing

            this.refreshDisplayName();

            final boolean isSoldOut = this.item.getQuantity() == 0;
            this.soldOutContainer.setVisible(isSoldOut);
        }
    }

    public static class AdminItemComponent extends BasicList.ItemComponent<ItemStack> {

        protected UIImage image;
        protected UIExpandingLabel itemLabel, buyPriceLabel, sellPriceLabel;
        protected int itemNameSpaceAvailable;
        private BasicContainer<?> listedIndicatorContainer;

        AdminItemComponent(final MalisisGui gui, final BasicList<ItemStack> parent, final ItemStack item) {
            super(gui, parent, item);

            this.setOnDoubleClickConsumer(itemStack -> {
                final StoreScreen parentScreen = (StoreScreen) this.getGui();
                parentScreen.listOrModify();
            });
        }

        @Override
        protected void construct(final MalisisGui gui) {
            this.setHeight(24);

            // Default available space
            this.itemNameSpaceAvailable = this.getWidth();

            this.listedIndicatorContainer = new BasicContainer<>(5, this.height - (this.getLeftBorderSize() + this.getRightBorderSize()));
            this.listedIndicatorContainer.setVisible(false);
            this.listedIndicatorContainer.setPosition(2, -2, Anchor.TOP | Anchor.RIGHT);

            // Add components
            final ItemStack fakeStack = this.item.copy();
            fakeStack.setCount(1);
            final EntityPlayer player = Minecraft.getMinecraft().player;
            final boolean useAdvancedTooltips = Minecraft.getMinecraft().gameSettings.advancedItemTooltips;

            this.image = new UIImage(fakeStack);
            this.image.setPosition(0, 0, Anchor.LEFT | Anchor.MIDDLE);
            this.image.setTooltip(new UISaneTooltip(String.join("\n", fakeStack.getTooltip(player, useAdvancedTooltips
                    ? ITooltipFlag.TooltipFlags.ADVANCED
                    : ITooltipFlag.TooltipFlags.NORMAL))));

            this.itemLabel = new UIExpandingLabel("");
            this.itemLabel.setPosition(getPaddedX(this.image, 4), 0, Anchor.LEFT | Anchor.MIDDLE);

            // Prices
            this.buyPriceLabel = new UIExpandingLabel("");
            this.buyPriceLabel.setFontOptions(this.buyPriceLabel.getFontOptions().toBuilder().scale(0.8f).build());
            this.buyPriceLabel.setPosition(-(this.listedIndicatorContainer.getWidth() + 6), 2, Anchor.TOP | Anchor.RIGHT);

            this.sellPriceLabel = new UIExpandingLabel("");
            this.sellPriceLabel.setFontOptions(this.sellPriceLabel.getFontOptions().toBuilder().scale(0.8f).build());
            this.sellPriceLabel.setPosition(-(this.listedIndicatorContainer.getWidth() + 6), 0, Anchor.BOTTOM | Anchor.RIGHT);

            // Exact value
            if (this.item.getCount() >= (int) FeatureConstants.ONE_MILLION) {
                this.itemLabel.setTooltip(new UISaneTooltip(FeatureConstants.CURRENCY_DECIMAL_FORMAT.format(this.item.getCount())));
            }

            this.add(this.image, this.itemLabel, this.buyPriceLabel, this.sellPriceLabel, this.listedIndicatorContainer);

            this.update();
        }

        private void refreshDisplayName() {
            // Limit item name to prevent over drawing
            final ItemStack fakeStack = this.item.copy();
            final FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;

            StringBuilder displayName = new StringBuilder();
            for (final char c : fakeStack.getDisplayName().toCharArray()) {
                if (fontRenderer.getStringWidth(displayName.toString() + c) > this.itemNameSpaceAvailable) {
                    displayName = new StringBuilder(displayName.toString().substring(0, Math.max(displayName.length() - 3, 0)) + "...");
                    break;
                }
                displayName.append(c);
            }

            this.itemLabel.setText(TextFormatting.WHITE + displayName.toString());
        }


        public void update() {
            final StoreScreen parentScreen = (StoreScreen) this.getGui();
            final Optional<BuyingItem> buyingItem = parentScreen.store.getBuyingItems()
                    .stream()
                    .filter(i -> StoreScreen.isStackEqualIgnoreSize(i.asRealStack(), this.item))
                    .findAny();
            final Optional<SellingItem> sellingItem = parentScreen.store.getSellingItems()
                    .stream()
                    .filter(i -> StoreScreen.isStackEqualIgnoreSize(i.asRealStack(), this.item))
                    .findAny();

            this.buyPriceLabel.setVisible(buyingItem.isPresent());
            this.buyPriceLabel.setText(TextFormatting.GRAY + "B: " + TextFormatting.GOLD +
                    FeatureConstants.withSuffix(buyingItem.map(StoreItem::getPrice).orElse(BigDecimal.ZERO).doubleValue()) + TextFormatting.GRAY +
                    "/ea");

            this.sellPriceLabel.setVisible(sellingItem.isPresent());
            this.sellPriceLabel.setText(TextFormatting.GRAY + "S: " + TextFormatting.GOLD +
                    FeatureConstants.withSuffix(sellingItem.map(StoreItem::getPrice).orElse(BigDecimal.ZERO).doubleValue()) + TextFormatting.GRAY +
                    "/ea");

            this.listedIndicatorContainer.setVisible(buyingItem.isPresent() || sellingItem.isPresent());
            if (this.listedIndicatorContainer.isVisible()) {
                this.listedIndicatorContainer.setTopColor(buyingItem.isPresent() ? FontColors.DARK_GREEN : FontColors.DARK_RED);
                this.listedIndicatorContainer.setBottomColor(sellingItem.isPresent() ? FontColors.DARK_GREEN : FontColors.DARK_RED);
            }

            this.itemNameSpaceAvailable = this.getWidth()
                    - Math.max(
                            (this.buyPriceLabel.isVisible() ? this.buyPriceLabel.getWidth() : 0),
                            (this.sellPriceLabel.isVisible() ? this.sellPriceLabel.getWidth() : 0))
                    - (this.image.isVisible() ? this.image.getWidth() : 0)
                    - (this.listedIndicatorContainer.isVisible() ? this.listedIndicatorContainer.getWidth() : 0)
                    - 24;

            this.refreshDisplayName();
        }
    }
}
