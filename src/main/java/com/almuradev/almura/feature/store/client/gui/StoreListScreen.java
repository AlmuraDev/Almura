/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.store.client.gui;

import com.almuradev.almura.feature.store.Store;
import com.almuradev.almura.feature.store.basic.listing.BasicBuyingItem;
import com.almuradev.almura.feature.store.basic.listing.BasicSellingItem;
import com.almuradev.almura.feature.store.client.ClientStoreManager;
import com.almuradev.almura.feature.store.listing.BuyingItem;
import com.almuradev.almura.feature.store.listing.SellingItem;
import com.almuradev.almura.shared.client.ui.FontColors;
import com.almuradev.almura.shared.client.ui.component.UIForm;
import com.almuradev.almura.shared.client.ui.component.UITextBox;
import com.almuradev.almura.shared.client.ui.component.button.UIButtonBuilder;
import com.almuradev.almura.shared.client.ui.screen.SimpleScreen;
import com.almuradev.almura.shared.feature.FeatureConstants;
import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UICheckBox;
import net.malisis.core.client.gui.component.interaction.UITextField;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

import java.math.BigDecimal;
import java.text.DecimalFormatSymbols;
import java.time.Instant;

public class StoreListScreen extends SimpleScreen {

    private static final int maxTrailingDigits = 2;

    @Inject private static ClientStoreManager storeManager;

    private final Store store;
    private final ItemStack toList;

    private UIButton buttonList, buttonCancel;
    private UICheckBox infiniteCheckBox;
    private UILabel quantityLabel, buyLabel, buyEaLabel, sellLabel, sellEaLabel;
    private UITextBox quantityTextBox, buyPricePerTextBox, sellPricePerTextBox;
    private UIForm form;

    public StoreListScreen(final StoreScreen parent, final Store store, final ItemStack toList) {
        super(parent, true);

        this.store = store;
        this.toList = toList;
    }

    @Override
    public void construct() {
        this.guiscreenBackground = false;

        this.form = new UIForm(this, 130, 130, I18n.format("almura.feature.exchange.title.enter_a_price"));
        this.form.setZIndex(10); // Fixes issue overlapping draws from parent
        this.form.setBackgroundAlpha(255);

        this.quantityLabel = new UILabel(this, I18n.format("almura.feature.common.text.quantity"));
        this.quantityLabel.setPosition(2, 2);
        this.quantityLabel.setFontOptions(FontColors.WHITE_FO);

        this.quantityTextBox = new UITextBox(this, "0");
        this.quantityTextBox.setSize(65, 0);
        this.quantityTextBox.setFilter(s -> s.replaceAll("[^\\d-]", ""));
        this.quantityTextBox.setPosition(2, SimpleScreen.getPaddedY(this.quantityLabel, 2));
        this.quantityTextBox.setAcceptsReturn(false);
        this.quantityTextBox.setOnEnter(tb -> this.list());
        this.quantityTextBox.setAcceptsTab(false);
        this.quantityTextBox.setTabIndex(0);
        this.quantityTextBox.register(this);

        this.infiniteCheckBox = new UICheckBox(this, TextFormatting.WHITE + "Infinite");
        this.infiniteCheckBox.setPosition(SimpleScreen.getPaddedX(this.quantityTextBox, 2), this.quantityTextBox.getY() + 1);
        this.infiniteCheckBox.register(this);

        this.buyLabel = new UILabel(this, I18n.format("almura.feature.common.button.buy"));
        this.buyLabel.setPosition(2, SimpleScreen.getPaddedY(this.quantityTextBox, 5));
        this.buyLabel.setFontOptions(FontColors.WHITE_FO);

        this.buyPricePerTextBox = new UITextBox(this, BigDecimal.ZERO.toString());
        this.buyPricePerTextBox.setSize(65, 0);
        this.buyPricePerTextBox.setFilter(this::filter);
        this.buyPricePerTextBox.setPosition(2, SimpleScreen.getPaddedY(this.buyLabel, 2));
        this.buyPricePerTextBox.setAcceptsReturn(false);
        this.buyPricePerTextBox.setOnEnter(tb -> this.list());
        this.buyPricePerTextBox.setAcceptsTab(false);
        this.buyPricePerTextBox.setTabIndex(1);
        this.buyPricePerTextBox.register(this);

        this.buyEaLabel = new UILabel(this, "/ea");
        this.buyEaLabel.setFontOptions(FontColors.WHITE_FO);
        this.buyEaLabel.setPosition(SimpleScreen.getPaddedX(this.buyPricePerTextBox, 2), this.buyPricePerTextBox.getY());

        this.sellLabel = new UILabel(this, I18n.format("almura.feature.common.button.sell"));
        this.sellLabel.setPosition(2, SimpleScreen.getPaddedY(this.buyPricePerTextBox, 5));
        this.sellLabel.setFontOptions(FontColors.WHITE_FO);

        this.sellPricePerTextBox = new UITextBox(this, BigDecimal.ZERO.toString());
        this.sellPricePerTextBox.setSize(65, 0);
        this.sellPricePerTextBox.setFilter(this::filter);
        this.sellPricePerTextBox.setPosition(2, SimpleScreen.getPaddedY(this.sellLabel, 2));
        this.sellPricePerTextBox.setAcceptsReturn(false);
        this.sellPricePerTextBox.setOnEnter(tb -> this.list());
        this.sellPricePerTextBox.setAcceptsTab(false);
        this.sellPricePerTextBox.setTabIndex(2);
        this.sellPricePerTextBox.register(this);

        this.sellEaLabel = new UILabel(this, "/ea");
        this.sellEaLabel.setFontOptions(FontColors.WHITE_FO);
        this.sellEaLabel.setPosition(SimpleScreen.getPaddedX(this.sellPricePerTextBox, 2), this.sellPricePerTextBox.getY());

        this.buttonList = new UIButtonBuilder(this)
                .text(I18n.format("almura.feature.common.button.list"))
                .width(40)
                .position(-2, -2)
                .anchor(Anchor.RIGHT | Anchor.BOTTOM)
                .enabled(false)
                .onClick(this::list)
                .build("button.list");

        this.buttonCancel = new UIButtonBuilder(this)
                .text(I18n.format("almura.button.cancel"))
                .width(40)
                .position(SimpleScreen.getPaddedX(this.buttonList, 2, Anchor.RIGHT), -2)
                .anchor(Anchor.RIGHT | Anchor.BOTTOM)
                .onClick(this::close)
                .build("button.cancel");

        this.form.add(this.quantityLabel, this.quantityTextBox, this.infiniteCheckBox, this.buyLabel, this.buyPricePerTextBox, this.buyEaLabel,
                this.sellLabel, this.sellPricePerTextBox, this.sellEaLabel, this.buttonCancel, this.buttonList);
        this.addToScreen(this.form);

        this.quantityTextBox.focus();
        this.quantityTextBox.selectAll();
    }

    @Subscribe
    private void onCheckChange(final ComponentEvent.ValueChange<UICheckBox, Boolean> event) {
        if (this.infiniteCheckBox.equals(event.getComponent())) {
            this.quantityTextBox.setEditable(!event.getNewValue());

            if (event.getNewValue()) {
                this.quantityTextBox.setText("-1");
            }

            this.updateListButton(this.quantityTextBox, this.quantityTextBox.getText());
        }
    }

    @Subscribe
    private void onTextChange(final ComponentEvent.ValueChange<UITextField, String> event) {
        this.updateListButton(event.getComponent(), event.getNewValue());
    }

    private void updateListButton(final UIComponent<?> component, final String newValue) {
        final boolean isQuantityValid = !this.quantityTextBox.getText().isEmpty()
                && (!this.quantityTextBox.isEditable()
                || (Integer.valueOf(this.quantityTextBox.equals(component) ? newValue : this.quantityTextBox.getText()) > 0));
        final boolean isBuyPriceValid = !this.buyPricePerTextBox.getText().isEmpty()
                && this.validate(this.buyPricePerTextBox.equals(component) ? newValue : this.buyPricePerTextBox.getText());
        final boolean isSellPriceValid = !this.sellPricePerTextBox.getText().isEmpty()
                && this.validate(this.sellPricePerTextBox.equals(component) ? newValue : this.sellPricePerTextBox.getText());

        this.buttonList.setEnabled(isQuantityValid && (isBuyPriceValid || isSellPriceValid));
    }

    private String filter(final String input) {
        // TODO: Maybe make a fancy regex for some of this

        // Get the current decimal separator
        final String decimalSeparator = String.valueOf(DecimalFormatSymbols.getInstance().getDecimalSeparator());

        // Filter out all non-digit and decimal separator characters
        final String filteredValue = input.replaceAll("[^\\d" + decimalSeparator + "]", "");

        // Return if empty
        if (filteredValue.isEmpty()) {
            return filteredValue;
        }

        // Don't allow multiple decimal separators if one is present
        if (filteredValue.indexOf(decimalSeparator) != filteredValue.lastIndexOf(decimalSeparator)) {
            return filteredValue.substring(0, filteredValue.length() - 1);
        }

        // Split against the decimal separator
        final String[] values = Iterables.toArray(Splitter.on(CharMatcher.anyOf(decimalSeparator)).split(filteredValue), String.class);

        // If we have more than 2 sets of values after the split or the length of the 2nd block is bigger than 2
        // then return a value we expect to see.
        if (values.length > 2 || values.length > 1 && values[1].length() > maxTrailingDigits) {
            return values[0] + decimalSeparator + values[1].substring(0, maxTrailingDigits);
        }

        return filteredValue;
    }

    private boolean validate(final String value) {
        if (value.isEmpty()) {
            return false;
        }

        try {
            final BigDecimal newValue = new BigDecimal(value);
            return newValue.doubleValue() > 0 && newValue.doubleValue() <= FeatureConstants.TRILLION;
        } catch (final NumberFormatException e) {
            return false;
        }
    }

    private void list() {
        if (!this.buttonList.isEnabled()) {
            return;
        }

        // TODO TEST CODE
        if (!this.buyPricePerTextBox.getText().isEmpty() && !this.quantityTextBox.getText().isEmpty()) {
            // Add buying item
            final int quantity = Integer.valueOf(this.quantityTextBox.getText());
            final BigDecimal buyingPrice = new BigDecimal(this.buyPricePerTextBox.getText());
            if (buyingPrice.doubleValue() > 0) {
                final BuyingItem buyingItem = new BasicBuyingItem(0, Instant.now(), toList.getItem(), toList.getMetadata(), quantity, buyingPrice,
                    0, null);
                buyingItem.setCompound(toList.getTagCompound() == null ? null : toList.getTagCompound().copy());
                this.store.getBuyingItems().add(buyingItem);
            }
        }

        if (!this.sellPricePerTextBox.getText().isEmpty()) {
            // Add selling item
            final BigDecimal sellingPrice = new BigDecimal(this.sellPricePerTextBox.getText());
            if (sellingPrice.doubleValue() > 0) {
                final SellingItem sellingItem = new BasicSellingItem(0, Instant.now(), toList.getItem(), toList.getMetadata(), -1, sellingPrice, 0,
                    null);
                sellingItem.setCompound(toList.getTagCompound() == null ? null : toList.getTagCompound().copy());
                this.store.getSellingItems().add(sellingItem);
            }
        }

        ((StoreScreen) this.parent.get()).refresh();
        this.close();
    }
}
