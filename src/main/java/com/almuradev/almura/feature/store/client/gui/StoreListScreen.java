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
import com.almuradev.almura.shared.client.ui.FontColors;
import com.almuradev.almura.shared.client.ui.component.UIForm;
import com.almuradev.almura.shared.client.ui.component.UILine;
import com.almuradev.almura.shared.client.ui.component.UITextBox;
import com.almuradev.almura.shared.client.ui.component.button.UIButtonBuilder;
import com.almuradev.almura.shared.client.ui.component.container.UIContainer;
import com.almuradev.almura.shared.client.ui.screen.SimpleScreen;
import com.almuradev.almura.shared.feature.FeatureConstants;
import com.almuradev.almura.shared.item.BasicVanillaStack;
import com.almuradev.almura.shared.item.VanillaStack;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UICheckBox;
import net.malisis.core.client.gui.component.interaction.UITextField;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.malisis.core.renderer.font.FontOptions;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

import java.math.BigDecimal;

public class StoreListScreen extends SimpleScreen {

    private static final double TEN_MILLION = 10000000.0;

    @Inject private static ClientStoreManager storeManager;

    private final Store store;
    private final ItemStack toList;

    private UIButton buttonList, buttonCancel;
    private UICheckBox buyInfiniteCheckBox, sellInfiniteCheckBox;
    private UIContainer<?> buyContainer, sellContainer;
    private UILabel buyQtyLabel, buyTitleLabel, buyPerLabel, buyTotalPriceLabel, sellQtyLabel, sellTitleLabel, sellPerLabel, sellTotalPriceLabel;
    private UITextBox buyQtyTextBox, buyPricePerTextBox, sellQtyTextBox, sellPricePerTextBox;
    private UIForm form;

    public StoreListScreen(final StoreScreen parent, final Store store, final ItemStack toList) {
        super(parent, true);

        this.store = store;
        this.toList = toList;
    }

    @Override
    public void construct() {
        this.guiscreenBackground = false;

        this.form = new UIForm(this, 250, 130, "List"); // TODO: Translation
        this.form.setZIndex(10); // Fixes issue overlapping draws from parent
        this.form.setBackgroundAlpha(255);

        final int containerWidth = getPaddedWidth(this.form) / 2 - 1;

        // Buy container
        this.buyContainer = new UIContainer<>(this, containerWidth, -18);
        this.buyContainer.setColor(FontColors.BLACK);
        this.buyContainer.setBorder(FontColors.WHITE, 1, 185);
        this.buyContainer.setPadding(2);

        this.buyTitleLabel = new UILabel(this, TextFormatting.WHITE + "Buy"); // TODO: Translation
        this.buyTitleLabel.setPosition(0, 2, Anchor.TOP | Anchor.CENTER);

        final UILine buyLine = new UILine(this, getPaddedWidth(this.buyContainer) + 2);
        buyLine.setPosition(-1, getPaddedY(this.buyTitleLabel, 2));

        this.buyPricePerTextBox = new UITextBox(this, "");
        this.buyPricePerTextBox.setPosition(-2, getPaddedY(buyLine, 2), Anchor.TOP | Anchor.RIGHT);
        this.buyPricePerTextBox.setSize(50, 0);
        this.buyPricePerTextBox.setFilter(s -> this.filterAndLimit(s, 2, true));
        this.buyPricePerTextBox.register(this);

        this.buyPerLabel = new UILabel(this, TextFormatting.WHITE + "Price (per):"); // TODO: Translation
        this.buyPerLabel.setPosition(
          getPaddedX(this.buyPricePerTextBox, 2, Anchor.RIGHT), this.buyPricePerTextBox.getY() + 2, Anchor.TOP | Anchor.RIGHT);

        this.buyQtyTextBox = new UITextBox(this, "");
        this.buyQtyTextBox.setPosition(-2, getPaddedY(this.buyPricePerTextBox, 2), Anchor.TOP | Anchor.RIGHT);
        this.buyQtyTextBox.setSize(50, 0);
        this.buyQtyTextBox.setFilter(s -> this.filterAndLimit(s, 0, false));
        this.buyQtyTextBox.register(this);

        this.buyQtyLabel = new UILabel(this, TextFormatting.WHITE + "Quantity:"); // TODO: Translation
        this.buyQtyLabel.setPosition(
          getPaddedX(this.buyQtyTextBox, 2, Anchor.RIGHT), this.buyQtyTextBox.getY() + 2, Anchor.TOP | Anchor.RIGHT);

        this.buyInfiniteCheckBox = new UICheckBox(this, TextFormatting.WHITE + "Infinite"); // TODO: Translation
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
        final UILabel buyTotalLabel = new UILabel(this, TextFormatting.WHITE + "Total:"); // TODO: Translation
        buyTotalLabel.setPosition(this.buyTotalPriceLabel.getX() - buyTotalLabel.getWidth() - 3, 0, Anchor.BOTTOM | Anchor.LEFT);

        this.buyContainer.add(this.buyTitleLabel, buyLine,
                              this.buyPerLabel, this.buyPricePerTextBox,
                              this.buyQtyLabel, this.buyQtyTextBox,
                              this.buyInfiniteCheckBox,
                              buyTotalLabel, this.buyTotalPriceLabel);


        // Sell container
        this.sellContainer = new UIContainer<>(this, containerWidth, -18);
        this.sellContainer.setColor(FontColors.BLACK);
        this.sellContainer.setBorder(FontColors.WHITE, 1, 185);
        this.sellContainer.setPadding(2);
        this.sellContainer.setPosition(0, 0, Anchor.TOP | Anchor.RIGHT);

        this.sellTitleLabel = new UILabel(this, TextFormatting.WHITE + "Sell"); // TODO: Translation
        this.sellTitleLabel.setPosition(0, 2, Anchor.TOP | Anchor.CENTER);

        final UILine sellLine = new UILine(this, getPaddedWidth(this.sellContainer) + 2);
        sellLine.setPosition(-1, getPaddedY(this.sellTitleLabel, 2));

        this.sellPricePerTextBox = new UITextBox(this, "");
        this.sellPricePerTextBox.setPosition(-2, getPaddedY(sellLine, 2), Anchor.TOP | Anchor.RIGHT);
        this.sellPricePerTextBox.setSize(50, 0);
        this.sellPricePerTextBox.setFilter(s -> this.filterAndLimit(s, 2, false));
        this.sellPricePerTextBox.register(this);

        this.sellPerLabel = new UILabel(this, TextFormatting.WHITE + "Price (per):"); // TODO: Translation
        this.sellPerLabel.setPosition(
          getPaddedX(this.sellPricePerTextBox, 2, Anchor.RIGHT), this.sellPricePerTextBox.getY() + 2, Anchor.TOP | Anchor.RIGHT);

        this.sellQtyTextBox = new UITextBox(this, "");
        this.sellQtyTextBox.setPosition(-2, getPaddedY(this.sellPricePerTextBox, 2), Anchor.TOP | Anchor.RIGHT);
        this.sellQtyTextBox.setSize(50, 0);
        this.sellQtyTextBox.setFilter(s -> this.filterAndLimit(s, 0, false));
        this.sellQtyTextBox.register(this);

        this.sellQtyLabel = new UILabel(this, TextFormatting.WHITE + "Quantity:"); // TODO: Translation
        this.sellQtyLabel.setPosition(
          getPaddedX(this.sellQtyTextBox, 2, Anchor.RIGHT), this.sellQtyTextBox .getY() + 2, Anchor.TOP | Anchor.RIGHT);

        this.sellInfiniteCheckBox = new UICheckBox(this, TextFormatting.WHITE + "Infinite"); // TODO: Translation
        this.sellInfiniteCheckBox.setPosition(column2X, getPaddedY(this.sellQtyTextBox, 2));
        this.sellInfiniteCheckBox.register(this);

        this.sellTotalPriceLabel = new UILabel(this, "");
        this.sellTotalPriceLabel.setFontOptions(FontOptions.builder().shadow(false).color(0x999999).build());
        this.sellTotalPriceLabel.setPosition(column2X + 2, 0, Anchor.BOTTOM | Anchor.LEFT);
        final UILabel sellTotalLabel = new UILabel(this, TextFormatting.WHITE + "Total:"); // TODO: Translation
        sellTotalLabel.setPosition(this.sellTotalPriceLabel.getX() - sellTotalLabel.getWidth() - 3, 0, Anchor.BOTTOM | Anchor.LEFT);

        this.sellContainer.add(this.sellTitleLabel, sellLine,
                               this.sellPerLabel, this.sellPricePerTextBox,
                               this.sellQtyLabel, this.sellQtyTextBox,
                               this.sellInfiniteCheckBox,
                               sellTotalLabel, this.sellTotalPriceLabel);

        this.buttonList = new UIButtonBuilder(this)
          .text("List") // TODO: Translation
          .anchor(Anchor.BOTTOM | Anchor.RIGHT)
          .onClick(this::list)
          .width(50)
          .enabled(false)
          .build("button.list");

        this.buttonCancel = new UIButtonBuilder(this)
          .text("Cancel") // TODO: Translation
          .anchor(Anchor.BOTTOM | Anchor.RIGHT)
          .x(getPaddedX(this.buttonList, 2, Anchor.RIGHT))
          .onClick(this::close)
          .width(50)
          .build("button.cancel");

        this.form.add(this.buyContainer, this.sellContainer, this.buttonCancel, this.buttonList);
        this.addToScreen(this.form);
    }

    @Subscribe
    private void onCheckChange(final ComponentEvent.ValueChange<UICheckBox, Boolean> event) {
        if (this.buyInfiniteCheckBox.equals(event.getComponent())) {
            this.buyQtyTextBox.setEditable(!event.getNewValue());
            this.buyQtyTextBox.setFilter(s -> this.filterAndLimit(s, 0, event.getNewValue()));
            this.buyQtyTextBox.setText(event.getNewValue() ? "-" : "" + "1");
        }

        if (this.sellInfiniteCheckBox.equals(event.getComponent())) {
            this.sellQtyTextBox.setEditable(!event.getNewValue());
            this.sellQtyTextBox.setFilter(s -> this.filterAndLimit(s, 0, event.getNewValue()));
            this.sellQtyTextBox.setText(event.getNewValue() ? "-" : "" + "1");
        }

        this.updateListButton();
    }

    @Subscribe
    private void onTextChange(final ComponentEvent.ValueChange<UITextField, String> event) {
        this.updateListButton();
    }

    private void updateListButton() {
        // Check if there are any valid combinations
        final boolean isBuyValid = !this.buyQtyTextBox.getText().isEmpty() && !this.buyPricePerTextBox.getText().isEmpty();
        final boolean isSellValid = !this.sellQtyTextBox.getText().isEmpty() && !this.sellPricePerTextBox.getText().isEmpty();

        // Update the list button based on the above
        this.buttonList.setEnabled(isBuyValid || isSellValid);

        if (isBuyValid) {
            final int buyQty = Integer.valueOf(this.buyQtyTextBox.getText());
            final BigDecimal buyPrice = new BigDecimal(this.buyPricePerTextBox.getText());
            this.buyTotalPriceLabel.setText(FeatureConstants.withSuffix(buyPrice.doubleValue() * buyQty));
        } else {
            this.buyTotalPriceLabel.setText("");
        }

        if (isSellValid) {
            final int sellQty = Integer.valueOf(this.sellQtyTextBox.getText());
            final BigDecimal sellPrice = new BigDecimal(this.sellPricePerTextBox.getText());
            this.sellTotalPriceLabel.setText(FeatureConstants.withSuffix(sellPrice.doubleValue() * sellQty));
        } else {
            this.sellTotalPriceLabel.setText("");
        }
    }

    private void list() {
        if (!this.buttonList.isEnabled()) {
            return;
        }

        // Check if there are any valid combinations
        final boolean isBuyValid = !this.buyQtyTextBox.getText().isEmpty() && !this.buyPricePerTextBox.getText().isEmpty();
        final boolean isSellValid = !this.sellQtyTextBox.getText().isEmpty() && !this.sellPricePerTextBox.getText().isEmpty();

        if (isBuyValid) {
            final VanillaStack toListBuy = new BasicVanillaStack(this.toList);
            toListBuy.setQuantity(Integer.valueOf(this.buyQtyTextBox.getText()));

            storeManager.requestListBuyingItem(this.store.getId(), toListBuy, 0, new BigDecimal(this.buyPricePerTextBox.getText()));
        }

        if (isSellValid) {
            final VanillaStack toListSell = new BasicVanillaStack(this.toList);
            toListSell.setQuantity(Integer.valueOf(this.sellQtyTextBox.getText()));

            storeManager.requestListSellingItem(this.store.getId(), toListSell, 0, new BigDecimal(this.sellPricePerTextBox.getText()));
        }

        ((StoreScreen) this.parent.get()).refresh();
        this.close();
    }

    private String filterAndLimit(final String value, final int maxTrailingDigits, final boolean allowNegatives) {
        final String filteredValue = FeatureConstants.filterToNumber(value, maxTrailingDigits, allowNegatives);
        try {
            final BigDecimal parsedValue = new BigDecimal(filteredValue);
            if (parsedValue.longValue() > TEN_MILLION) {
                return filteredValue.substring(0, filteredValue.length() - 1);
            }
        } catch (final NumberFormatException e) {
            return "";
        }
        return filteredValue;
    }
}
