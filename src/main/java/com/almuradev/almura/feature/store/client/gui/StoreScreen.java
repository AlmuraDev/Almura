/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.store.client.gui;

import com.almuradev.almura.feature.store.Store;
import com.almuradev.almura.feature.store.client.ClientStoreManager;
import com.almuradev.almura.feature.store.listing.BuyingItem;
import com.almuradev.almura.feature.store.listing.SellingItem;
import com.almuradev.almura.feature.store.listing.StoreItem;
import com.almuradev.almura.shared.client.ui.FontColors;
import com.almuradev.almura.shared.client.ui.component.UIDynamicList;
import com.almuradev.almura.shared.client.ui.component.UIExpandingLabel;
import com.almuradev.almura.shared.client.ui.component.UIForm;
import com.almuradev.almura.shared.client.ui.component.UILine;
import com.almuradev.almura.shared.client.ui.component.UISaneTooltip;
import com.almuradev.almura.shared.client.ui.component.UITextBox;
import com.almuradev.almura.shared.client.ui.component.button.UIButtonBuilder;
import com.almuradev.almura.shared.client.ui.component.container.UIContainer;
import com.almuradev.almura.shared.client.ui.screen.SimpleScreen;
import com.almuradev.almura.shared.feature.FeatureConstants;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.decoration.UITooltip;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UISelect;
import net.malisis.core.client.gui.component.interaction.UITextField;
import net.malisis.core.client.gui.event.ComponentEvent;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.inject.Inject;

@SideOnly(Side.CLIENT)
public class StoreScreen extends SimpleScreen {

    @Inject private static ClientStoreManager storeManager;
    private static final int defaultTabColor = 0x1E1E1E;
    private static final int hoveredTabColor = 0x3C3C3C;
    private static final NonNullList<ItemStack> registryItems = NonNullList.create();

    private final Store store;
    private final boolean isAdmin;
    private final int selectedTabColor = 0;
    private final int tabWidth = 115;
    private final int tabHeight = 16;
    private final List<ItemStack> adminBaseList = new ArrayList<>();

    private SideType currentSide = SideType.BUY;
    private UIButton buttonTransactStack, buttonTransactOne, buttonTransactAll, buttonTransactQuantity, buttonAdminList, buttonAdminUnlist;
    private UIContainer<?> adminTitleContainer, buyTabContainer, sellTabContainer;
    private UIDynamicList<StoreItem> itemList;
    private UIDynamicList<ItemStack> adminItemList;
    private UILabel adminListTotalLabel, buyTabLabel, sellTabLabel;
    private UILine thisDoesNotExistLine;
    private UISelect<ItemLocation> locationSelect;
    private UITextBox adminSearchTextBox;

    static {
        // Get registry items
        ForgeRegistries.ITEMS.getValuesCollection()
          .stream()
          .map(ItemStack::new)
          .forEach(i -> i.getItem().getSubItems(CreativeTabs.SEARCH, registryItems));
    }

    public StoreScreen(final Store store, final boolean isAdmin) {
        this.store = store;
        this.isAdmin = isAdmin;
    }

    @Override
    public void construct() {
        guiscreenBackground = false;

        final UIForm form = new UIForm(this, 257, 300, this.store.getName());
        form.setBackgroundAlpha(255);

        final UIContainer<?> storeContainer = new UIContainer<>(this, 251, UIComponent.INHERITED);
        storeContainer.setBorder(FontColors.WHITE, 1, 185);
        storeContainer.setPadding(3);
        storeContainer.setColor(0);

        // Buy tab
        final int leftOffset = storeContainer.getLeftPadding() - storeContainer.getLeftBorderSize();
        this.buyTabContainer = new UIContainer<>(this, this.tabWidth, this.tabHeight);
        this.buyTabContainer.setBorders(FontColors.WHITE, 185, 1, 1, 1, 0);
        this.buyTabContainer.setPosition(4, 2);
        this.buyTabContainer.setColor(defaultTabColor);
        this.buyTabContainer.attachData(SideType.BUY);

        // Buy tab label
        this.buyTabLabel = new UILabel(this, I18n.format("almura.feature.common.button.buy"));
        this.buyTabLabel.setPosition(0, 1, Anchor.MIDDLE | Anchor.CENTER);
        this.buyTabLabel.attachData(this.buyTabContainer.getData()); // Always use the same data as their parent
        this.buyTabContainer.add(this.buyTabLabel);

        // Sell tab
        this.sellTabContainer = new UIContainer<>(this, this.tabWidth, this.tabHeight);
        this.sellTabContainer.setBorders(FontColors.WHITE, 185, 1, 1, 1, 0);
        this.sellTabContainer.setColor(defaultTabColor);
        this.sellTabContainer.setPosition(-4, 2, Anchor.TOP | Anchor.RIGHT);
        this.sellTabContainer.attachData(SideType.SELL);

        // Sell tab label
        this.sellTabLabel = new UILabel(this, I18n.format("almura.feature.common.button.sell"));
        this.sellTabLabel.setPosition(0, 1, Anchor.MIDDLE | Anchor.CENTER);
        this.sellTabLabel.attachData(this.sellTabContainer.getData()); // Always use the same data as their parent
        this.sellTabContainer.add(this.sellTabLabel);

        // Doesn't exist.
        this.thisDoesNotExistLine = new UILine(this, this.tabWidth - 2);
        this.thisDoesNotExistLine.setBackgroundAlpha(255);
        this.thisDoesNotExistLine.setColor(selectedTabColor);

        // Bottom tab line
        final UILine tabContainerLineBottom = new UILine(this,
                storeContainer.getWidth() - (storeContainer.getLeftBorderSize() + storeContainer.getRightBorderSize()), 1);
        tabContainerLineBottom.setPosition(-leftOffset, SimpleScreen.getPaddedY(this.buyTabContainer, 0));

        // List
        this.itemList = new UIDynamicList<>(this, UIComponent.INHERITED,
                SimpleScreen.getPaddedHeight(form) - this.buyTabContainer.getHeight() - tabContainerLineBottom.getHeight() - 24);
        this.itemList.setPosition(0, SimpleScreen.getPaddedY(tabContainerLineBottom, 2));
        this.itemList.setSelectConsumer(i -> this.updateStoreControls());
        this.itemList.setItemComponentSpacing(1);
        this.itemList.setItemComponentFactory(StoreItemComponent::new);

        this.buttonTransactOne = new UIButtonBuilder(this)
                .width(60)
                .anchor(Anchor.BOTTOM | Anchor.LEFT)
                .enabled(false)
                .onClick(() -> this.transact(1))
                .build("button.transact.single");

        this.buttonTransactStack = new UIButtonBuilder(this)
                .width(60)
                .x(SimpleScreen.getPaddedX(this.buttonTransactOne, 2))
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

        this.buttonTransactAll = new UIButtonBuilder(this)
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

        this.buttonTransactQuantity = new UIButtonBuilder(this)
                .width(60)
                .x(SimpleScreen.getPaddedX(this.buttonTransactAll, 2, Anchor.RIGHT))
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

        storeContainer.add(this.buyTabContainer, this.sellTabContainer, tabContainerLineBottom, this.thisDoesNotExistLine, this.itemList,
                this.buttonTransactOne, this.buttonTransactStack, this.buttonTransactQuantity, this.buttonTransactAll);

        form.add(storeContainer);

        // Logic for admin pane
        if (this.isAdmin) {
            // Adjust form width
            form.setWidth(form.getWidth() + 224);

            // Create admin pane
            final UIContainer<?> adminContainer = new UIContainer(this, 220, UIComponent.INHERITED);
            adminContainer.setPosition(SimpleScreen.getPaddedX(storeContainer, 4), 0);
            adminContainer.setBorder(FontColors.WHITE, 1, 185);
            adminContainer.setPadding(3);
            adminContainer.setColor(0);

            // Item location selection
            this.locationSelect = new UISelect<>(this, UIComponent.INHERITED);
            this.locationSelect.setOptions(Arrays.stream(ItemLocation.values()).collect(Collectors.toList()));
            this.locationSelect.setLabelFunction(o -> o != null ? o.name : "");
            this.locationSelect.setPosition(0, 0);
            this.locationSelect.register(this);

            // Search text box
            this.adminSearchTextBox = new UITextBox(this, "");
            this.adminSearchTextBox.setSize(UIComponent.INHERITED, this.locationSelect.getHeight());
            this.adminSearchTextBox.setPosition(0, SimpleScreen.getPaddedY(this.locationSelect, 2));
            this.adminSearchTextBox.register(this);

            // Item list
            this.adminItemList = new UIDynamicList<>(this, UIComponent.INHERITED,
                    SimpleScreen.getPaddedHeight(form) - this.locationSelect.getHeight() - this.adminSearchTextBox.getHeight() - 28);
            this.adminItemList.setItemComponentFactory(AdminItemComponent::new);
            this.adminItemList.setItemComponentSpacing(1);
            this.adminItemList.setPosition(0, SimpleScreen.getPaddedY(this.adminSearchTextBox, 2));
            this.adminItemList.setPadding(2);
            this.adminItemList.setBorder(FontColors.WHITE, 1, 185);
            this.adminItemList.setSelectConsumer(i -> this.updateAdminControls());

            this.buttonAdminList = new UIButtonBuilder(this)
                    .width(50)
                    .anchor(Anchor.BOTTOM | Anchor.LEFT)
                    .enabled(false)
                    .onClick(this::listOrModify)
                    .text(I18n.format("almura.feature.common.button.list"))
                    .build("button.list");
            this.buttonAdminUnlist = new UIButtonBuilder(this)
                    .width(50)
                    .anchor(Anchor.BOTTOM | Anchor.LEFT)
                    .position(SimpleScreen.getPaddedX(this.buttonAdminList, 2), 0)
                    .enabled(false)
                    .onClick(this::unlist)
                    .text(I18n.format("almura.feature.common.button.unlist"))
                    .build("button.unlist");

            this.adminListTotalLabel = new UILabel(this, TextFormatting.WHITE + "Total: "); // TODO: Translation
            this.adminListTotalLabel.setPosition(0, -2, Anchor.BOTTOM | Anchor.RIGHT);

            adminContainer.add(this.adminTitleContainer, this.locationSelect, this.adminSearchTextBox, this.adminItemList, this.buttonAdminList,
                               this.buttonAdminUnlist, this.adminListTotalLabel);

            form.add(adminContainer);

            // Select the first item
            this.locationSelect.selectFirst();
        }

        addToScreen(form);
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

            this.adminListTotalLabel.setText(TextFormatting.WHITE + "Total: " + this.adminItemList.getItems().size()); // TODO: Translation
        }
    }

    @Subscribe
    private void onSelect(final UISelect.SelectEvent<ItemLocation> event) {
        this.createAdminControls(event.getNewValue());
    }

    public void refresh(final boolean createControls) {
        if (this.currentSide == SideType.BUY && this.store.getBuyingItems().size() == 0) {
            // Determine if we stay on buy or move to sell
            this.currentSide = this.store.getSellingItems().size() > 0 ? SideType.SELL : SideType.BUY;
        } else if (this.currentSide == SideType.SELL && this.store.getSellingItems().size() == 0) {
            this.currentSide = SideType.BUY;
        }

        // TODO: Translation
        this.buyTabContainer.setTooltip(this.store.getBuyingItems().size() == 0 ? new UITooltip(this, "There are no items for sale.") : null);
        // TODO: Translation
        this.sellTabContainer.setTooltip(this.store.getSellingItems().size() == 0 ? new UITooltip(this, "There are no items available for purchase.") : null);

        // Collections.unmodifiableList because you know... Java.
        this.itemList.setItems(Collections.unmodifiableList(this.currentSide == SideType.BUY
                ? this.store.getBuyingItems()
                : this.store.getSellingItems()));

        this.itemList.setSelectedItem(this.itemList.getItems().stream().findFirst().orElse(null));

        if (createControls && this.isAdmin) {
            this.createAdminControls(this.locationSelect.getSelectedValue());
        }

        this.updateStoreControls();
        this.updateAdminControls();
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
          .ifPresent(i -> storeManager.requestDelistSellingItem(this.store.getId(), i.getRecord()));
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
    private Optional<UIContainer> getTabContainer(final int x, final int y) {
        final UIComponent<?> componentAt = this.getComponentAt(x, y);
        final UIComponent<?> componentAtParent = componentAt != null ? componentAt.getParent() : null;
        if (this.buyTabContainer.equals(componentAt) || this.sellTabContainer.equals(componentAt)) {
            return Optional.of((UIContainer) componentAt);
        } else if (this.buyTabContainer.equals(componentAtParent) || this.sellTabContainer.equals(componentAtParent)) {
            return Optional.of((UIContainer) componentAtParent);
        }

        return Optional.empty();
    }

    /**
     * Updates the properties for the provided controls based on the attached data for {@link SideType}
     *
     * @param tabContainer The container
     * @param tabLabel The label
     */
    private void updateTabs(final UIContainer<?> tabContainer, final UILabel tabLabel) {
        if (tabContainer.getData() instanceof SideType && tabLabel.getData() instanceof SideType) {
            tabContainer.setColor(this.currentSide == tabContainer.getData() ? selectedTabColor : defaultTabColor);
            tabLabel.setFontOptions(this.currentSide == tabLabel.getData() ? FontColors.WHITE_FO : FontColors.GRAY_FO);

            // Update the following if is the current side
            if (this.currentSide == tabContainer.getData()) {

                // Update something that doesn't exist
                final int offsetX = this.currentSide == SideType.BUY ? 1 : -1;
                this.thisDoesNotExistLine.setColor(selectedTabColor);
                this.thisDoesNotExistLine.setPosition(tabContainer.getX() + offsetX, SimpleScreen.getPaddedY(tabContainer, 0),
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

    private void createAdminControls(final ItemLocation location) {
        // Clear search field
        this.adminSearchTextBox.setText("");

        final NonNullList<ItemStack> items = NonNullList.create();
        switch (location) {
            case STORE:
                items.addAll(this.store.getBuyingItems()
                  .stream()
                  .map(i -> {
                      final ItemStack copyStack = i.asRealStack().copy();
                      copyStack.setCount(1);
                      return copyStack;
                  })
                  .collect(Collectors.toList()));
                items.addAll(this.store.getSellingItems()
                  .stream()
                  .map(i -> {
                      final ItemStack copyStack = i.asRealStack().copy();
                      copyStack.setCount(1);
                      return copyStack;
                  })
                  .filter(i1 -> items.stream().noneMatch(i2 -> ItemStack.areItemStacksEqual(i1, i2)))
                  .collect(Collectors.toList()));
                break;
            case REGISTRY:
                items.addAll(registryItems);
                break;
            case INVENTORY:
                items.addAll(Minecraft.getMinecraft().player.inventory.mainInventory
                  .stream()
                  .filter(i -> !i.isEmpty())
                  .collect(Collectors.toList()));
                break;
        }

        this.adminBaseList.clear();
        this.adminBaseList.addAll(items
          .stream()
          .sorted(Comparator.comparing(ItemStack::getDisplayName))
          .collect(Collectors.toList()));
        this.adminItemList.setItems(this.adminBaseList);

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

        this.adminListTotalLabel.setText(TextFormatting.WHITE + "Total: " + this.adminItemList.getItems().size()); // TODO: Translation
    }

    public static boolean isStackEqualIgnoreSize(@Nullable final ItemStack a, @Nullable final ItemStack b) {
        if (a == null || b == null) {
            return false;
        }
        return net.minecraft.item.ItemStack.areItemsEqual(a, b) && net.minecraft.item.ItemStack.areItemStackTagsEqual(a, b);
    }

    public static class StoreItemComponent<T extends StoreItem> extends UIDynamicList.ItemComponent<T> {

        protected UIImage image;
        protected UIExpandingLabel itemLabel, priceLabel;
        protected int itemNameSpaceAvailable;
        private UILabel soldOutLabel;
        private UIContainer<?> soldOutContainer;

        public StoreItemComponent(final MalisisGui gui, final UIDynamicList<T> parent, final T item) {
            super(gui, parent, item);

            this.setOnDoubleClickConsumer(i -> ((StoreScreen) getGui()).transact(1));
        }

        @Override
        protected void construct(final MalisisGui gui) {
            this.setSize(0, 24);

            // Sold out container
            this.soldOutContainer = new UIContainer<>(this.getGui(), UIComponent.INHERITED, UIComponent.INHERITED);
            this.soldOutContainer.setColor(FontColors.DARK_GRAY);
            this.soldOutContainer.setBackgroundAlpha(180);
            this.soldOutContainer.setVisible(false);

            // Sold out label
            this.soldOutLabel = new UILabel(this.getGui(), "Unavailable"); // TODO: Translation
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

            this.image = new UIImage(gui, fakeStack);
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

            this.priceLabel = new UIExpandingLabel(gui, "");
            this.priceLabel.setFontOptions(this.priceLabel.getFontOptions().toBuilder().scale(0.8f).build());
            this.priceLabel.setPosition(-6, 0, Anchor.RIGHT | Anchor.MIDDLE);

            this.add(this.image, this.itemLabel, this.priceLabel, this.soldOutContainer);

            this.update();
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

            // Exact value
            this.priceLabel.setTooltip(I18n.format("almura.feature.exchange.tooltip.total")
                    + ": " + FeatureConstants.CURRENCY_DECIMAL_FORMAT.format(this.item.getQuantity() * this.item.getPrice().doubleValue()));

            this.itemNameSpaceAvailable = this.getWidth()
                    - (this.priceLabel.isVisible() ? this.priceLabel.getWidth() : 0)
                    - (this.image.isVisible() ? this.image.getWidth() : 0)
                    - 16; // Static amount for spacing

            this.refreshDisplayName();

            final boolean isSoldOut = this.item.getQuantity() == 0;
            this.soldOutContainer.setVisible(isSoldOut);
        }
    }

    public static class AdminItemComponent extends UIDynamicList.ItemComponent<ItemStack> {

        protected UIImage image;
        protected UIExpandingLabel itemLabel, buyPriceLabel, sellPriceLabel;
        protected int itemNameSpaceAvailable;
        private UIContainer<?> listedIndicatorContainer;

        AdminItemComponent(final MalisisGui gui, final UIDynamicList<ItemStack> parent, final ItemStack item) {
            super(gui, parent, item);
        }

        @Override
        protected void construct(final MalisisGui gui) {
            this.setHeight(24);

            // Default available space
            this.itemNameSpaceAvailable = this.getWidth();

            this.listedIndicatorContainer = new UIContainer<>(gui, 5, this.height - (this.getLeftBorderSize() + this.getRightBorderSize()));
            this.listedIndicatorContainer.setVisible(false);
            this.listedIndicatorContainer.setPosition(2, -2, Anchor.TOP | Anchor.RIGHT);

            // Add components
            final ItemStack fakeStack = this.item.copy();
            fakeStack.setCount(1);
            final EntityPlayer player = Minecraft.getMinecraft().player;
            final boolean useAdvancedTooltips = Minecraft.getMinecraft().gameSettings.advancedItemTooltips;

            this.image = new UIImage(gui, fakeStack);
            this.image.setPosition(0, 0, Anchor.LEFT | Anchor.MIDDLE);
            this.image.setTooltip(new UISaneTooltip(gui, String.join("\n", fakeStack.getTooltip(player, useAdvancedTooltips
                    ? ITooltipFlag.TooltipFlags.ADVANCED
                    : ITooltipFlag.TooltipFlags.NORMAL))));

            this.itemLabel = new UIExpandingLabel(gui, "");
            this.itemLabel.setPosition(getPaddedX(this.image, 4), 0, Anchor.LEFT | Anchor.MIDDLE);

            // Prices
            this.buyPriceLabel = new UIExpandingLabel(gui, "");
            this.buyPriceLabel.setFontOptions(this.buyPriceLabel.getFontOptions().toBuilder().scale(0.8f).build());
            this.buyPriceLabel.setPosition(-(this.listedIndicatorContainer.getWidth() + 6), 2, Anchor.TOP | Anchor.RIGHT);

            this.sellPriceLabel = new UIExpandingLabel(gui, "");
            this.sellPriceLabel.setFontOptions(this.sellPriceLabel.getFontOptions().toBuilder().scale(0.8f).build());
            this.sellPriceLabel.setPosition(-(this.listedIndicatorContainer.getWidth() + 6), 0, Anchor.BOTTOM | Anchor.RIGHT);

            // Exact value
            if (this.item.getCount() >= (int) FeatureConstants.ONE_MILLION) {
                this.itemLabel.setTooltip(new UISaneTooltip(gui, FeatureConstants.CURRENCY_DECIMAL_FORMAT.format(this.item.getCount())));
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

    protected enum SideType {
        BUY,
        SELL
    }

    private enum ItemLocation {
        STORE("Store Items"),
        REGISTRY("Registry Items"),
        INVENTORY("Inventory Items");

        protected final String name;
        ItemLocation(final String name) {
            this.name = name;
        }
    }
}
