/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.client.gui;

import com.almuradev.almura.feature.exchange.Exchange;
import com.almuradev.almura.feature.exchange.ExchangeConstants;
import com.almuradev.almura.feature.exchange.client.ClientExchangeManager;
import com.almuradev.almura.shared.client.ui.FontColors;
import com.almuradev.almura.shared.client.ui.component.UIForm;
import com.almuradev.almura.shared.client.ui.component.button.UIButtonBuilder;
import com.almuradev.almura.shared.client.ui.screen.SimpleScreen;
import com.almuradev.almura.shared.feature.store.listing.ForSaleItem;
import com.almuradev.almura.shared.feature.store.listing.ListItem;
import com.almuradev.almura.shared.util.MathUtil;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UITextField;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.minecraft.client.resources.I18n;

import java.math.BigDecimal;

public class ExchangeBuyQuantityScreen extends SimpleScreen {

    @Inject private static ClientExchangeManager exchangeManager;

    private final Exchange axs;
    private final ForSaleItem toBuyItem;

    private UIButton buttonBuy, buttonCancel;
    private UILabel quantityLabel, perItemLabel, perItemValueLabel, totalLabel, totalValueLabel;
    private UITextField quantityField;
    private UIForm form;

    public ExchangeBuyQuantityScreen(ExchangeScreen parent, Exchange axs, ForSaleItem toBuyItem) {
        super(parent, true);

        this.axs = axs;
        this.toBuyItem = toBuyItem;
    }

    @Override
    public void construct() {
        this.guiscreenBackground = false;

        this.form = new UIForm(this, 130, 90, I18n.format("almura.title.exchange.enter_a_quantity"));

        this.quantityLabel = new UILabel(this, "Quantity:");
        this.quantityLabel.setFontOptions(FontColors.WHITE_FO);
        this.quantityLabel.setPosition(0, 2);

        this.quantityField = new UITextField(this, "1");
        this.quantityField.setSize(45, 0);
        this.quantityField.setPosition(0, 0, Anchor.TOP | Anchor.RIGHT);
        this.quantityField.setFilter(s -> s = String.valueOf(
                MathUtil.squashi(Integer.valueOf(s.replaceAll("[^\\d]", "")), 1, toBuyItem.getQuantity())));
        this.quantityField.setPosition(0, 0);
        this.quantityField.register(this);

        this.perItemLabel = new UILabel(this, "Per:");
        this.perItemLabel.setPosition(0, SimpleScreen.getPaddedY(this.quantityField, 8));
        this.perItemLabel.setFontOptions(FontColors.WHITE_FO);

        this.perItemValueLabel = new UILabel(this, "");
        this.perItemValueLabel.setPosition(0, this.perItemLabel.getY(), Anchor.TOP | Anchor.RIGHT);
        this.perItemValueLabel.setFontOptions(FontColors.WHITE_FO);

        this.totalLabel = new UILabel(this, "Total:");
        this.totalLabel.setPosition(0, SimpleScreen.getPaddedY(this.perItemLabel, 4));
        this.totalLabel.setFontOptions(FontColors.WHITE_FO);

        this.totalValueLabel = new UILabel(this, "");
        this.totalValueLabel.setPosition(0, this.totalLabel.getY(), Anchor.TOP | Anchor.RIGHT);
        this.totalValueLabel.setFontOptions(FontColors.WHITE_FO);

        this.buttonBuy = new UIButtonBuilder(this)
                .text(I18n.format("almura.button.exchange.buy"))
                .width(40)
                .position(-2, -2)
                .anchor(Anchor.RIGHT | Anchor.BOTTOM)
                .onClick(() -> {
                    exchangeManager.purchase(this.axs.getId(), this.toBuyItem.getListItem().getRecord(),
                            Integer.valueOf(this.quantityField.getText()));
                    this.close();
                })
                .build("button.buy");

        this.buttonCancel = new UIButtonBuilder(this)
                .text(I18n.format("almura.button.cancel"))
                .width(40)
                .position(SimpleScreen.getPaddedX(this.buttonBuy, 2, Anchor.RIGHT), -2)
                .anchor(Anchor.RIGHT | Anchor.BOTTOM)
                .onClick(this::close)
                .build("button.cancel");

        this.form.add(this.quantityLabel, this.quantityField,
                      this.perItemLabel, this.perItemValueLabel,
                      this.totalLabel, this.totalValueLabel,
                      this.buttonCancel, this.buttonBuy);

        this.updateControls(this.quantityField.getText());

        this.addToScreen(this.form);
    }

    private void updateControls(String rawValue) {
        try {
            final int value = Integer.parseInt(rawValue);

            // Update controls
            this.perItemValueLabel.setText(ExchangeConstants.CURRENCY_DECIMAL_FORMAT.format(this.toBuyItem.getPrice()));
            this.totalValueLabel.setText(ExchangeConstants.CURRENCY_DECIMAL_FORMAT.format(
                    this.toBuyItem.getPrice().doubleValue() * value));

            this.buttonBuy.setEnabled(value > 0 && value <= this.toBuyItem.getQuantity());
        } catch (NumberFormatException e) {
            this.buttonBuy.setEnabled(false);
        }
    }

    @Subscribe
    private void onTextChange(ComponentEvent.ValueChange<UITextField, String> event) {
        if (event.getNewValue().isEmpty()) {
            this.buttonBuy.setEnabled(false);
            return;
        }

        this.updateControls(event.getNewValue());
    }
}
