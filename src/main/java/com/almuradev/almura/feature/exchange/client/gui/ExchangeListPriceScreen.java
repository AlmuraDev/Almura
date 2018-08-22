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
import com.almuradev.almura.shared.client.ui.FontColors;
import com.almuradev.almura.shared.client.ui.component.UIForm;
import com.almuradev.almura.shared.client.ui.component.button.UIButtonBuilder;
import com.almuradev.almura.shared.client.ui.screen.SimpleScreen;
import com.almuradev.almura.shared.feature.store.listing.ListItem;
import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UITextField;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.minecraft.client.resources.I18n;

import java.math.BigDecimal;
import java.text.DecimalFormatSymbols;

public class ExchangeListPriceScreen extends SimpleScreen {

    private static final int maxTrailingDigits = 2;

    @Inject private static ClientExchangeManager exchangeManager;

    private final Exchange axs;
    private final ListItem toList;

    private UIButton buttonList, buttonCancel;
    private UILabel eaLabel;
    private UITextField pricePerField;
    private UIForm form;

    public ExchangeListPriceScreen(ExchangeScreen parent, Exchange axs, ListItem toList) {
        super(parent, true);

        this.axs = axs;
        this.toList = toList;
    }

    @Override
    public void construct() {
        this.guiscreenBackground = false;

        this.form = new UIForm(this, 120, 65, "");
        this.form.setTitle(I18n.format("almura.title.exchange.enter_a_price"));
        this.form.setAnchor(Anchor.CENTER | Anchor.MIDDLE);
        this.form.setMovable(true);
        this.form.setClosable(true);
        this.form.setBorder(FontColors.WHITE, 1, 185);
        this.form.setBackgroundAlpha(215);
        this.form.setPadding(3, 3);
        this.form.setTopPadding(20);

        this.pricePerField = new UITextField(this, "0.00");
        this.pricePerField.setSize(65, 0);
        this.pricePerField.setFilter(s -> {
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
        this.pricePerField.setPosition(2, 0, Anchor.MIDDLE | Anchor.LEFT);
        this.pricePerField.register(this);

        this.eaLabel = new UILabel(this, "/ea");
        this.eaLabel.setFontOptions(FontColors.WHITE_FO);
        this.eaLabel.setPosition(SimpleScreen.getPaddedX(this.pricePerField, 2), 1, Anchor.MIDDLE | Anchor.LEFT);

        this.buttonList = new UIButtonBuilder(this)
                .text(I18n.format("almura.button.exchange.list"))
                .width(40)
                .position(-2, -2)
                .anchor(Anchor.RIGHT | Anchor.BOTTOM)
                .enabled(false)
                .onClick(() -> {
                    exchangeManager.modifyListStatus(ListStatusType.LIST, this.axs.getId(), toList.getRecord(),
                            new BigDecimal(this.pricePerField.getText()));
                    ((ExchangeScreen) this.parent.get()).refreshListItems();
                    this.close();
                })
                .build("button.list");

        this.buttonCancel = new UIButtonBuilder(this)
                .text(I18n.format("almura.button.cancel"))
                .width(40)
                .position(SimpleScreen.getPaddedX(this.buttonList, 2, Anchor.RIGHT), -2)
                .anchor(Anchor.RIGHT | Anchor.BOTTOM)
                .onClick(this::close)
                .build("button.cancel");

        this.form.add(this.pricePerField, this.eaLabel, this.buttonCancel, this.buttonList);

        this.addToScreen(this.form);
    }

    @Subscribe
    private void onTextChange(ComponentEvent.ValueChange<UITextField, String> event) {
        if (event.getNewValue().isEmpty()) {
            this.buttonList.setEnabled(false);
            return;
        }

        try {
            this.buttonList.setEnabled(new BigDecimal(event.getNewValue()).doubleValue() > 0);
        } catch (NumberFormatException e) {
            this.buttonList.setEnabled(false);
        }
    }
}
