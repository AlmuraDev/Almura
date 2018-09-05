/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.client.gui;

import com.almuradev.almura.feature.exchange.Exchange;
import com.almuradev.almura.feature.exchange.client.ClientExchangeManager;
import com.almuradev.almura.shared.client.ui.FontColors;
import com.almuradev.almura.shared.client.ui.component.UIForm;
import com.almuradev.almura.shared.client.ui.component.UITextBox;
import com.almuradev.almura.shared.client.ui.component.button.UIButtonBuilder;
import com.almuradev.almura.shared.client.ui.screen.SimpleScreen;
import com.almuradev.almura.shared.feature.FeatureConstants;
import com.almuradev.almura.feature.exchange.listing.ForSaleItem;
import com.almuradev.almura.shared.util.MathUtil;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UITextField;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.minecraft.client.resources.I18n;

public class ExchangeBuyQuantityScreen extends SimpleScreen {

    @Inject private static ClientExchangeManager exchangeManager;

    private final Exchange axs;
    private final ForSaleItem toBuyItem;

    private UIButton buttonBuy, buttonCancel;
    private UILabel quantityLabel, perItemLabel, perItemValueLabel, totalLabel, totalValueLabel;
    private UITextBox quantityTextBox;
    private UIForm form;

    public ExchangeBuyQuantityScreen(final ExchangeScreen parent, final Exchange axs, final ForSaleItem toBuyItem) {
        super(parent, true);

        this.axs = axs;
        this.toBuyItem = toBuyItem;
    }

    @Override
    public void construct() {
        this.guiscreenBackground = false;

        this.form = new UIForm(this, 130, 90, I18n.format("almura.feature.exchange.title.enter_a_quantity"));
        this.form.setZIndex(10); // Fixes issue overlapping draws from parent
        this.form.setBackgroundAlpha(255);

        this.quantityLabel = new UILabel(this, I18n.format("almura.feature.common.text.quantity") + ":");
        this.quantityLabel.setFontOptions(FontColors.WHITE_FO);
        this.quantityLabel.setPosition(0, 2);

        this.quantityTextBox = new UITextBox(this, "1");
        this.quantityTextBox.setAcceptsReturn(false);
        this.quantityTextBox.setOnEnter(tb -> this.buy());
        this.quantityTextBox.setSize(45, 0);
        this.quantityTextBox.setPosition(0, 0, Anchor.TOP | Anchor.RIGHT);
        this.quantityTextBox.setFilter(s -> s = String.valueOf(
                MathUtil.squashi(Integer.valueOf(s.replaceAll("[^\\d]", "")), 1, toBuyItem.getQuantity())));
        this.quantityTextBox.setPosition(0, 0);
        this.quantityTextBox.register(this);

        this.perItemLabel = new UILabel(this, "Per:");
        this.perItemLabel.setPosition(0, SimpleScreen.getPaddedY(this.quantityTextBox, 8));
        this.perItemLabel.setFontOptions(FontColors.WHITE_FO);

        this.perItemValueLabel = new UILabel(this, "");
        this.perItemValueLabel.setPosition(0, this.perItemLabel.getY(), Anchor.TOP | Anchor.RIGHT);
        this.perItemValueLabel.setFontOptions(FontColors.WHITE_FO);

        this.totalLabel = new UILabel(this, I18n.format("almura.feature.common.text.total") + ":");
        this.totalLabel.setPosition(0, SimpleScreen.getPaddedY(this.perItemLabel, 4));
        this.totalLabel.setFontOptions(FontColors.WHITE_FO);

        this.totalValueLabel = new UILabel(this, "");
        this.totalValueLabel.setPosition(0, this.totalLabel.getY(), Anchor.TOP | Anchor.RIGHT);
        this.totalValueLabel.setFontOptions(FontColors.WHITE_FO);

        this.buttonBuy = new UIButtonBuilder(this)
                .text(I18n.format("almura.feature.common.button.buy"))
                .width(40)
                .position(-2, -2)
                .anchor(Anchor.RIGHT | Anchor.BOTTOM)
                .onClick(this::buy)
                .build("button.buy");

        this.buttonCancel = new UIButtonBuilder(this)
                .text(I18n.format("almura.button.cancel"))
                .width(40)
                .position(SimpleScreen.getPaddedX(this.buttonBuy, 2, Anchor.RIGHT), -2)
                .anchor(Anchor.RIGHT | Anchor.BOTTOM)
                .onClick(this::close)
                .build("button.cancel");

        this.form.add(this.quantityLabel, this.quantityTextBox,
                      this.perItemLabel, this.perItemValueLabel,
                      this.totalLabel, this.totalValueLabel,
                      this.buttonCancel, this.buttonBuy);

        this.updateControls(this.quantityTextBox.getText());

        this.addToScreen(this.form);

        this.quantityTextBox.focus();
        this.quantityTextBox.selectAll();
    }

    @Subscribe
    private void onTextChange(final ComponentEvent.ValueChange<UITextField, String> event) {
        this.updateControls(event.getNewValue());
    }

    private boolean validate(final String rawValue) {
        try {
            final int value = Integer.parseInt(rawValue);

            this.perItemValueLabel.setText(FeatureConstants.CURRENCY_DECIMAL_FORMAT.format(this.toBuyItem.getPrice()));
            this.totalValueLabel.setText(FeatureConstants.CURRENCY_DECIMAL_FORMAT.format(
                    this.toBuyItem.getPrice().doubleValue() * value));

            return value > 0 && value <= this.toBuyItem.getQuantity();
        } catch (final NumberFormatException e) {
            return false;
        }
    }

    private void updateControls(final String rawValue) {
        this.buttonBuy.setEnabled(this.validate(rawValue));
    }

    private void buy() {
        exchangeManager.purchase(this.axs.getId(), this.toBuyItem.getListItem().getRecord(),
                Integer.valueOf(this.quantityTextBox.getText()));
        this.close();
    }
}
