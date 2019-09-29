/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.client.gui;

import com.almuradev.almura.feature.exchange.Exchange;
import com.almuradev.almura.feature.exchange.ListStatusType;
import com.almuradev.almura.feature.exchange.client.ClientExchangeManager;
import com.almuradev.almura.feature.exchange.listing.ListItem;
import com.almuradev.almura.shared.feature.FeatureConstants;
import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.BasicScreen;
import net.malisis.core.client.gui.component.container.BasicForm;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.BasicTextBox;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UITextField;
import net.malisis.core.client.gui.component.interaction.button.builder.UIButtonBuilder;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.malisis.core.util.FontColors;
import net.minecraft.client.resources.I18n;

import java.math.BigDecimal;
import java.text.DecimalFormatSymbols;

public class ExchangeListPriceScreen extends BasicScreen {

    private static final int maxTrailingDigits = 2;

    @Inject private static ClientExchangeManager exchangeManager;

    private final Exchange axs;
    private final ListItem toList;
    private final boolean isModifying;

    private UIButton buttonList, buttonCancel;
    private UILabel eaLabel;
    private BasicTextBox pricePerTextBox;
    private BasicForm form;

    public ExchangeListPriceScreen(ExchangeScreen parent, Exchange axs, ListItem toList, boolean isModifying) {
        super(parent, true);

        this.axs = axs;
        this.toList = toList;
        this.isModifying = isModifying;
    }

    @Override
    public void construct() {
        this.guiscreenBackground = false;

        this.form = new BasicForm(this, 120, 65, I18n.format("almura.feature.exchange.title.enter_a_price"));
        this.form.setZIndex(10); // Fixes issue overlapping draws from parent
        this.form.setBackgroundAlpha(255);

        final BigDecimal initialPrice = this.isModifying
          ? this.toList.getForSaleItem().get().getPrice()
          : this.toList.getLastKnownPrice().orElse(BigDecimal.ZERO);
        this.pricePerTextBox = new BasicTextBox(this, initialPrice.toString());
        this.pricePerTextBox.setSize(65, 0);
        this.pricePerTextBox.setFilter(s -> {
            // TODO: Maybe make a fancy regex for some of this

            // Get the current decimal separator
            final String decimalSeparator = String.valueOf(DecimalFormatSymbols.getInstance().getDecimalSeparator());

            // Filter out all non-digit and decimal separator characters
            final String filteredValue = s.replaceAll("[^\\d" + decimalSeparator + "]", "");

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
        });
        this.pricePerTextBox.setPosition(2, 0, Anchor.MIDDLE | Anchor.LEFT);
        this.pricePerTextBox.setAcceptsReturn(false);
        this.pricePerTextBox.setOnEnter(tb -> this.list());
        this.pricePerTextBox.register(this);

        this.eaLabel = new UILabel(this, "/ea");
        this.eaLabel.setFontOptions(FontColors.WHITE_FO);
        this.eaLabel.setPosition(BasicScreen.getPaddedX(this.pricePerTextBox, 2), 1, Anchor.MIDDLE | Anchor.LEFT);

        this.buttonList = new UIButtonBuilder(this)
                .text(I18n.format("almura.feature.common.button.list"))
                .width(40)
                .position(-2, -2)
                .anchor(Anchor.RIGHT | Anchor.BOTTOM)
                .enabled(this.toList.getLastKnownPrice().isPresent())
                .onClick(this::list)
                .build("button.list");

        this.buttonCancel = new UIButtonBuilder(this)
                .text(I18n.format("almura.button.cancel"))
                .width(40)
                .position(BasicScreen.getPaddedX(this.buttonList, 2, Anchor.RIGHT), -2)
                .anchor(Anchor.RIGHT | Anchor.BOTTOM)
                .onClick(this::close)
                .build("button.cancel");

        this.form.add(this.pricePerTextBox, this.eaLabel, this.buttonCancel, this.buttonList);
        this.addToScreen(this.form);

        this.pricePerTextBox.focus();
        this.pricePerTextBox.selectAll();
    }

    @Subscribe
    private void onTextChange(ComponentEvent.ValueChange<UITextField, String> event) {
        this.buttonList.setEnabled(this.validate(event.getNewValue()));
    }

    private boolean validate(String value) {
        if (value.isEmpty()) {
            return false;
        }

        try {
            final BigDecimal newValue = new BigDecimal(value);
            return newValue.doubleValue() > 0 && newValue.doubleValue() <= FeatureConstants.ONE_TRILLION;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void list() {
        if (!this.buttonList.isEnabled()) {
            return;
        }

        final ListStatusType type = this.isModifying ? ListStatusType.ADJUST_PRICE : ListStatusType.LIST;
        exchangeManager.modifyListStatus(type, this.axs.getId(), toList.getRecord(), new BigDecimal(this.pricePerTextBox.getText()));
        ((ExchangeScreen) this.parent).refreshListItems();
        this.close();
    }
}
