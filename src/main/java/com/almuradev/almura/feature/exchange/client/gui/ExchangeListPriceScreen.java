package com.almuradev.almura.feature.exchange.client.gui;

import com.almuradev.almura.feature.exchange.Exchange;
import com.almuradev.almura.feature.exchange.ListStatusType;
import com.almuradev.almura.feature.exchange.client.ClientExchangeManager;
import com.almuradev.almura.shared.client.ui.FontColors;
import com.almuradev.almura.shared.client.ui.component.UIFormContainer;
import com.almuradev.almura.shared.client.ui.component.button.UIButtonBuilder;
import com.almuradev.almura.shared.client.ui.screen.SimpleScreen;
import com.almuradev.almura.shared.feature.store.listing.ListItem;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UITextField;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.minecraft.client.resources.I18n;

import java.math.BigDecimal;

public class ExchangeListPriceScreen extends SimpleScreen {

    @Inject private static ClientExchangeManager exchangeManager;

    private final Exchange axs;
    private final ListItem toList;

    private UIButton buttonList, buttonCancel;
    private UILabel eaLabel;
    private UITextField pricePerField;
    private UIFormContainer form;

    public ExchangeListPriceScreen(ExchangeScreen parent, Exchange axs, ListItem toList) {
        super(parent, true);

        this.axs = axs;
        this.toList = toList;
    }

    @Override
    public void construct() {
        this.guiscreenBackground = false;

        this.form = new UIFormContainer(this, 120, 65, "");
        this.form.setTitle(I18n.format("almura.title.exchange.enter_a_price"));
        this.form.setAnchor(Anchor.CENTER | Anchor.MIDDLE);
        this.form.setMovable(true);
        this.form.setClosable(true);
        this.form.setBorder(FontColors.WHITE, 1, 185);
        this.form.setBackgroundAlpha(215);
        this.form.setPadding(3, 3);
        this.form.setTopPadding(20);

        this.pricePerField = new UITextField(this, "0.00");
        this.pricePerField.setSize(55, 0);
        this.pricePerField.setFilter(s -> s.replaceAll("[^\\d.]", ""));
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
