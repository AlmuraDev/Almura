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
import com.almuradev.almura.feature.store.listing.StoreItem;
import com.almuradev.almura.shared.feature.FeatureConstants;
import com.almuradev.almura.shared.item.BasicVanillaStack;
import com.almuradev.almura.shared.item.VanillaStack;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.BasicScreen;
import net.malisis.core.client.gui.component.container.BasicContainer;
import net.malisis.core.client.gui.component.container.BasicForm;
import net.malisis.core.client.gui.component.decoration.BasicLine;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.BasicTextBox;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UICheckBox;
import net.malisis.core.client.gui.component.interaction.UITextField;
import net.malisis.core.client.gui.component.interaction.button.builder.UIButtonBuilder;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.util.FontColors;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

import java.math.BigDecimal;

import javax.annotation.Nullable;

public class StoreListScreen extends BasicScreen {

    @Inject private static ClientStoreManager storeManager;

    private final Store store;

    private ItemStack toList;
    @Nullable private StoreItem toModifyBuy, toModifySell;
    private UIButton buttonList, buttonCancel;
    private UICheckBox buyInfiniteCheckBox, sellInfiniteCheckBox;
    private BasicContainer<?> buyContainer, sellContainer;
    private UILabel buyQtyLabel, buyTitleLabel, buyPerLabel, buyTotalPriceLabel, buyTotalLabel, sellQtyLabel, sellTitleLabel, sellPerLabel, sellTotalPriceLabel, sellTotalLabel;
    private BasicTextBox buyQtyTextBox, buyPricePerTextBox, sellQtyTextBox, sellPricePerTextBox;
    private BasicForm form;

    public StoreListScreen(final StoreScreen parent, final Store store, final ItemStack toList) {
        super(parent, true);

        this.store = store;
        this.toList = toList;
    }

    public StoreListScreen(final StoreScreen parent, final Store store, @Nullable final StoreItem toModifyBuy, @Nullable final StoreItem toModifySell,
      final ItemStack toList) {
        super(parent, true);

        this.store = store;
        this.toModifyBuy = toModifyBuy;
        this.toModifySell = toModifySell;
        this.toList = toList;
    }

    @Override
    public void construct() {
        this.guiscreenBackground = false;

        this.form = new BasicForm(this, 250, 130, I18n.format("almura.feature.common.text.list"));
        this.form.setZIndex(10); // Fixes issue overlapping draws from parent
        this.form.setBackgroundAlpha(255);

        final int containerWidth = getPaddedWidth(this.form) / 2 - 1;

        // Buy container
        this.buyContainer = new BasicContainer<>(this, containerWidth, -18);
        this.buyContainer.setColor(FontColors.BLACK);
        this.buyContainer.setBorder(FontColors.WHITE, 1, 185);
        this.buyContainer.setPadding(2);

        this.buyTitleLabel = new UILabel(this, TextFormatting.WHITE + I18n.format("almura.feature.common.button.buy"));
        this.buyTitleLabel.setPosition(0, 2, Anchor.TOP | Anchor.CENTER);

        final BasicLine buyLine = new BasicLine(this, getPaddedWidth(this.buyContainer) + 2);
        buyLine.setPosition(-1, getPaddedY(this.buyTitleLabel, 2));

        this.buyPricePerTextBox = new BasicTextBox(this, this.toModifyBuy != null ? this.toModifyBuy.getPrice().toString() : "");
        this.buyPricePerTextBox.setPosition(-2, getPaddedY(buyLine, 2), Anchor.TOP | Anchor.RIGHT);
        this.buyPricePerTextBox.setSize(50, 0);
        this.buyPricePerTextBox.setAcceptsTab(false);
        this.buyPricePerTextBox.setTabIndex(1);
        this.buyPricePerTextBox.setFilter(s -> this.filterAndLimit(s, 2, true));
        this.buyPricePerTextBox.register(this);

        this.buyPerLabel = new UILabel(this, TextFormatting.WHITE + I18n.format("almura.feature.common.text.price_per") + ":");
        this.buyPerLabel.setPosition(
          getPaddedX(this.buyPricePerTextBox, 2, Anchor.RIGHT), this.buyPricePerTextBox.getY() + 2, Anchor.TOP | Anchor.RIGHT);

        this.buyQtyTextBox = new BasicTextBox(this, "");
        this.buyQtyTextBox.setPosition(-2, getPaddedY(this.buyPricePerTextBox, 2), Anchor.TOP | Anchor.RIGHT);
        this.buyQtyTextBox.setSize(50, 0);
        this.buyQtyTextBox.setAcceptsTab(false);
        this.buyQtyTextBox.setTabIndex(2);
        this.buyQtyTextBox.setFilter(s -> this.filterAndLimit(s, 0, false));
        this.buyQtyTextBox.register(this);

        this.buyQtyLabel = new UILabel(this, TextFormatting.WHITE + I18n.format("almura.feature.common.text.quantity") + ":");
        this.buyQtyLabel.setPosition(
          getPaddedX(this.buyQtyTextBox, 2, Anchor.RIGHT), this.buyQtyTextBox.getY() + 2, Anchor.TOP | Anchor.RIGHT);

        this.buyInfiniteCheckBox = new UICheckBox(this, TextFormatting.WHITE + I18n.format("almura.feature.common.text.infinite"));
        this.buyInfiniteCheckBox.setPosition(
          this.buyContainer.componentX(this.buyQtyTextBox) - this.buyContainer.getRightPadding() - this.buyContainer.getRightBorderSize(),
          getPaddedY(this.buyQtyTextBox, 2));
        this.buyInfiniteCheckBox.register(this);

        final int column2X = this.buyContainer.componentX(this.buyQtyTextBox)
          - this.buyContainer.getRightPadding()
          - this.buyContainer.getRightBorderSize();

        this.buyTotalPriceLabel = new UILabel(this);
        this.buyTotalPriceLabel.setFontOptions(FontOptions.builder().shadow(false).color(0x999999).build());
        this.buyTotalPriceLabel.setPosition(column2X + 2, 0, Anchor.BOTTOM | Anchor.LEFT);

        buyTotalLabel = new UILabel(this, TextFormatting.WHITE + I18n.format("almura.feature.common.text.total") + ":");
        buyTotalLabel.setPosition(this.buyTotalPriceLabel.getX() - buyTotalLabel.getWidth() - 3, 0, Anchor.BOTTOM | Anchor.LEFT);

        this.buyContainer.add(this.buyTitleLabel, buyLine,
                              this.buyPerLabel, this.buyPricePerTextBox,
                              this.buyQtyLabel, this.buyQtyTextBox,
                              this.buyInfiniteCheckBox,
                              buyTotalLabel, this.buyTotalPriceLabel);


        // Sell container
        this.sellContainer = new BasicContainer<>(this, containerWidth, -18);
        this.sellContainer.setColor(FontColors.BLACK);
        this.sellContainer.setBorder(FontColors.WHITE, 1, 185);
        this.sellContainer.setPadding(2);
        this.sellContainer.setPosition(0, 0, Anchor.TOP | Anchor.RIGHT);

        this.sellTitleLabel = new UILabel(this, TextFormatting.WHITE + I18n.format("almura.feature.common.button.sell"));
        this.sellTitleLabel.setPosition(0, 2, Anchor.TOP | Anchor.CENTER);

        final BasicLine sellLine = new BasicLine(this, getPaddedWidth(this.sellContainer) + 2);
        sellLine.setPosition(-1, getPaddedY(this.sellTitleLabel, 2));

        this.sellPricePerTextBox = new BasicTextBox(this, this.toModifySell != null ? this.toModifySell.getPrice().toString() : "");
        this.sellPricePerTextBox.setPosition(-2, getPaddedY(sellLine, 2), Anchor.TOP | Anchor.RIGHT);
        this.sellPricePerTextBox.setSize(50, 0);
        this.sellPricePerTextBox.setAcceptsTab(false);
        this.sellPricePerTextBox.setTabIndex(3);
        this.sellPricePerTextBox.setFilter(s -> this.filterAndLimit(s, 2, false));
        this.sellPricePerTextBox.register(this);

        this.sellPerLabel = new UILabel(this, TextFormatting.WHITE + I18n.format("almura.feature.common.text.price_per") + ":");
        this.sellPerLabel.setPosition(
          getPaddedX(this.sellPricePerTextBox, 2, Anchor.RIGHT), this.sellPricePerTextBox.getY() + 2, Anchor.TOP | Anchor.RIGHT);

        this.sellQtyTextBox = new BasicTextBox(this, "");
        this.sellQtyTextBox.setPosition(-2, getPaddedY(this.sellPricePerTextBox, 2), Anchor.TOP | Anchor.RIGHT);
        this.sellQtyTextBox.setSize(50, 0);
        this.sellQtyTextBox.setAcceptsTab(false);
        this.sellQtyTextBox.setTabIndex(4);
        this.sellQtyTextBox.setFilter(s -> this.filterAndLimit(s, 0, false));
        this.sellQtyTextBox.register(this);

        this.sellQtyLabel = new UILabel(this, TextFormatting.WHITE + I18n.format("almura.feature.common.text.quantity") + ":");
        this.sellQtyLabel.setPosition(
          getPaddedX(this.sellQtyTextBox, 2, Anchor.RIGHT), this.sellQtyTextBox .getY() + 2, Anchor.TOP | Anchor.RIGHT);

        this.sellInfiniteCheckBox = new UICheckBox(this, TextFormatting.WHITE + I18n.format("almura.feature.common.text.infinite"));
        this.sellInfiniteCheckBox.setPosition(column2X, getPaddedY(this.sellQtyTextBox, 2));
        this.sellInfiniteCheckBox.register(this);

        this.sellTotalPriceLabel = new UILabel(this, "");
        this.sellTotalPriceLabel.setFontOptions(FontOptions.builder().shadow(false).color(0x999999).build());
        this.sellTotalPriceLabel.setPosition(column2X + 2, 0, Anchor.BOTTOM | Anchor.LEFT);

        sellTotalLabel = new UILabel(this, TextFormatting.WHITE + I18n.format("almura.feature.common.text.price_per") + ":");
        sellTotalLabel.setPosition(this.sellTotalPriceLabel.getX() - sellTotalLabel.getWidth() - 3, 0, Anchor.BOTTOM | Anchor.LEFT);

        this.sellContainer.add(this.sellTitleLabel, sellLine,
                               this.sellPerLabel, this.sellPricePerTextBox,
                               this.sellQtyLabel, this.sellQtyTextBox,
                               this.sellInfiniteCheckBox,
                               sellTotalLabel, this.sellTotalPriceLabel);


        this.buttonList = new UIButtonBuilder(this)
          .text(I18n.format(this.toModifyBuy != null || this.toModifySell != null
            ? "almura.feature.common.button.modify"
            : "almura.feature.common.text.list"))
          .anchor(Anchor.BOTTOM | Anchor.RIGHT)
          .onClick(this::listOrModify)
          .width(50)
          .enabled(false)
          .build("button.list");

        this.buttonCancel = new UIButtonBuilder(this)
          .text(I18n.format("almura.button.cancel"))
          .anchor(Anchor.BOTTOM | Anchor.RIGHT)
          .x(getPaddedX(this.buttonList, 2, Anchor.RIGHT))
          .onClick(this::close)
          .width(50)
          .build("button.cancel");

        this.form.add(this.buyContainer, this.sellContainer, this.buttonCancel, this.buttonList);
        this.addToScreen(this.form);

        // Update quantity
        if (this.toModifyBuy != null) {
            if (this.toModifyBuy.getQuantity() == -1) {
                this.buyInfiniteCheckBox.setChecked(true);
                this.buyInfiniteCheckBox.fireEvent(new ComponentEvent.ValueChange<>(this.buyInfiniteCheckBox, false, true));
            } else {
                this.buyQtyTextBox.setText(String.valueOf(this.toModifyBuy.getQuantity()));
            }
        }

        if (this.toModifySell != null) {
            if (this.toModifySell.getQuantity() == -1) {
                this.sellInfiniteCheckBox.setChecked(true);
                this.sellInfiniteCheckBox.fireEvent(new ComponentEvent.ValueChange<>(this.sellInfiniteCheckBox, false, true));
            } else {
                this.sellQtyTextBox.setText(String.valueOf(this.toModifySell.getQuantity()));
            }
        }
    }

    @Subscribe
    private void onCheckChange(final ComponentEvent.ValueChange<UICheckBox, Boolean> event) {
        if (this.buyInfiniteCheckBox.equals(event.getComponent())) {
            this.buyQtyTextBox.setEditable(!event.getNewValue());
            this.buyQtyTextBox.setFilter(s -> this.filterAndLimit(s, 0, event.getNewValue()));
            this.buyQtyTextBox.setText((event.getNewValue() ? "-" : "") + "1");
            this.buyTotalLabel.setVisible(!event.getNewValue());
            this.buyTotalPriceLabel.setVisible(!event.getNewValue());
        }

        if (this.sellInfiniteCheckBox.equals(event.getComponent())) {
            this.sellQtyTextBox.setEditable(!event.getNewValue());
            this.sellQtyTextBox.setFilter(s -> this.filterAndLimit(s, 0, event.getNewValue()));
            this.sellQtyTextBox.setText((event.getNewValue() ? "-" : "") + "1");
            this.sellTotalLabel.setVisible(!event.getNewValue());
            this.sellTotalPriceLabel.setVisible(!event.getNewValue());
        }

        this.updateListButton(this.buyPricePerTextBox.getText(), this.buyQtyTextBox.getText(), this.sellPricePerTextBox.getText(),
          this.sellQtyTextBox.getText());
    }

    @Subscribe
    private void onTextChange(final ComponentEvent.ValueChange<UITextField, String> event) {
        // Ensure we're using the latest information
        // TODO: Add POST method for MalisisCore
        final String buyPriceText = this.buyPricePerTextBox.equals(event.getComponent()) ? event.getNewValue() : this.buyPricePerTextBox.getText();
        final String buyQtyText = this.buyQtyTextBox.equals(event.getComponent()) ? event.getNewValue() : this.buyQtyTextBox.getText();
        final String sellPriceText = this.sellPricePerTextBox.equals(event.getComponent()) ? event.getNewValue() : this.sellPricePerTextBox.getText();
        final String sellQtyText = this.sellQtyTextBox.equals(event.getComponent()) ? event.getNewValue() : this.sellQtyTextBox.getText();

        this.updateListButton(buyPriceText, buyQtyText, sellPriceText, sellQtyText);
    }

    private void updateListButton(final String buyPriceText, final String buyQtyText, final String sellPriceText, final String sellQtyText) {
        // Check if there are any valid combinations
        final boolean isBuyValid = !buyQtyText.isEmpty() && !buyPriceText.isEmpty() && !buyPriceText.equals("0");
        final boolean isSellValid = !sellQtyText.isEmpty() && !sellPriceText.isEmpty() && !sellPriceText.equals("0");

        // Update the list button based on the above
        this.buttonList.setEnabled(isBuyValid || isSellValid);

        if (isBuyValid) {
            final int buyQty = Integer.valueOf(buyQtyText);
            final BigDecimal buyPrice = new BigDecimal(buyPriceText);
            this.buyTotalPriceLabel.setText(FeatureConstants.withSuffix(buyPrice.doubleValue() * buyQty));
        } else {
            this.buyTotalPriceLabel.setText("");
        }

        if (isSellValid) {
            final int sellQty = Integer.valueOf(sellQtyText);
            final BigDecimal sellPrice = new BigDecimal(sellPriceText);
            this.sellTotalPriceLabel.setText(FeatureConstants.withSuffix(sellPrice.doubleValue() * sellQty));
        } else {
            this.sellTotalPriceLabel.setText("");
        }
    }

    private void listOrModify() {
        if (!this.buttonList.isEnabled()) {
            return;
        }

        // Check if there are any valid combinations
        final boolean isBuyValid = !this.buyQtyTextBox.getText().isEmpty() && !this.buyPricePerTextBox.getText().isEmpty();
        final boolean isSellValid = !this.sellQtyTextBox.getText().isEmpty() && !this.sellPricePerTextBox.getText().isEmpty();

        if (isBuyValid) {
            // We're modifying an existing listing
            if (this.toModifyBuy != null) {
                final int newBuyQuantity = Integer.valueOf(this.buyQtyTextBox.getText());
                storeManager.requestModifyBuyingItem(this.store.getId(), this.toModifyBuy.getRecord(), newBuyQuantity, 0,
                  new BigDecimal(this.buyPricePerTextBox.getText()));
            } else { // We're adding a new listing
                final VanillaStack toListBuy = new BasicVanillaStack(this.toList);
                toListBuy.setQuantity(Integer.valueOf(this.buyQtyTextBox.getText()));

                storeManager.requestListBuyingItem(this.store.getId(), toListBuy, 0, new BigDecimal(this.buyPricePerTextBox.getText()));
            }
        } else if (this.toModifyBuy != null) { // We're unlisting an existing listing
            storeManager.requestDelistBuyingItem(this.store.getId(), this.toModifyBuy.getRecord());
        }

        if (isSellValid) {
            // We're modifying an existing listing
            if (this.toModifySell != null) {
                final int newQuantity = Integer.valueOf(this.sellQtyTextBox.getText());
                storeManager.requestModifySellingItem(this.store.getId(), this.toModifySell.getRecord(), newQuantity, 0,
                  new BigDecimal(this.sellPricePerTextBox.getText()));
            } else { // We're adding a new listing
                final VanillaStack toListSell = new BasicVanillaStack(this.toList);
                toListSell.setQuantity(Integer.valueOf(this.sellQtyTextBox.getText()));

                storeManager.requestListSellingItem(this.store.getId(), toListSell, 0, new BigDecimal(this.sellPricePerTextBox.getText()));
            }
        } else if (this.toModifySell != null) { // We're unlisting an existing listing
            storeManager.requestDelistSellingItem(this.store.getId(), this.toModifySell.getRecord());
        }

        ((StoreScreen) this.parent).refresh(true);
        this.close();
    }

    private String filterAndLimit(final String value, final int maxTrailingDigits, final boolean allowNegatives) {
        final String filteredValue = FeatureConstants.filterToNumber(value, maxTrailingDigits, allowNegatives);
        try {
            final BigDecimal parsedValue = new BigDecimal(filteredValue);
            if (parsedValue.longValue() > FeatureConstants.TEN_MILLION) {
                return filteredValue.substring(0, filteredValue.length() - 1);
            }
        } catch (final NumberFormatException e) {
            return "";
        }
        return filteredValue;
    }
}
