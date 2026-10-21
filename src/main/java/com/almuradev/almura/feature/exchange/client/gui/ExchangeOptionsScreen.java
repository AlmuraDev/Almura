/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.client.gui;

import com.almuradev.almura.asm.ClientStaticAccess;
import com.almuradev.almura.core.client.config.category.GeneralCategory;
import com.google.common.collect.ImmutableMap;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.BasicScreen;
import net.malisis.core.client.gui.component.container.BasicForm;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UICheckBox;
import net.malisis.core.client.gui.component.interaction.UISelect;
import net.malisis.core.client.gui.component.interaction.button.builder.UIButtonBuilder;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.util.FontColors;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Map;

/**
 * The Exchange's options, saved in the client configuration.
 */
@SideOnly(Side.CLIENT)
public final class ExchangeOptionsScreen extends BasicScreen {

    // Default sort order option value -> label
    private static final Map<String, String> SORT_ORDERS = ImmutableMap.of(
            "item_name", "Item Name",
            "price", "Price",
            "seller", "Seller");

    private final ExchangeScreen exchange;
    private UICheckBox hideOwnListings;
    private UISelect<String> defaultSort;

    public ExchangeOptionsScreen(final ExchangeScreen exchange) {
        super(exchange, true);
        this.exchange = exchange;
    }

    @Override
    public void construct() {
        final GeneralCategory general = ClientStaticAccess.configAdapter.get().general;

        final BasicForm form = new BasicForm(this, 200, 100, "Exchange Options");
        form.setZIndex(10);
        form.setBackgroundAlpha(255);

        this.hideOwnListings = new UICheckBox(this, "Hide my listed items in search");
        this.hideOwnListings.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).build());
        this.hideOwnListings.setPosition(4, 6, Anchor.LEFT | Anchor.TOP);
        this.hideOwnListings.setChecked(general.exchangeHideOwnListings);

        final UILabel sortLabel = new UILabel(this, "Default sort order:");
        sortLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).build());
        sortLabel.setPosition(4, 26, Anchor.LEFT | Anchor.TOP);

        this.defaultSort = new UISelect<>(this, 90, SORT_ORDERS.keySet());
        this.defaultSort.setLabelFunction(SORT_ORDERS::get);
        this.defaultSort.setPosition(4, 38, Anchor.LEFT | Anchor.TOP);
        this.defaultSort.setSelectedOption(SORT_ORDERS.containsKey(general.exchangeDefaultSort) ? general.exchangeDefaultSort : "item_name");

        final UIButton closeButton = new UIButtonBuilder(this)
                .text("Close")
                .width(50)
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .listener(this)
                .build("button.close");

        form.add(this.hideOwnListings, sortLabel, closeButton, this.defaultSort);
        this.addToScreen(form);
    }

    @Subscribe
    public void onUIButtonClickEvent(final UIButton.ClickEvent event) {
        if ("button.close".equalsIgnoreCase(event.getComponent().getName())) {
            this.close();
        }
    }

    @Override
    protected void onClose() {
        // Save and apply when leaving (close button or escape)
        final GeneralCategory general = ClientStaticAccess.configAdapter.get().general;
        general.exchangeHideOwnListings = this.hideOwnListings.isChecked();
        general.exchangeDefaultSort = this.defaultSort.getSelectedValue();
        ClientStaticAccess.configAdapter.save();
        this.exchange.applyOptions();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
