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
import com.almuradev.almura.shared.client.ui.screen.IngameFeatureManagementScreen;
import com.google.inject.Inject;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collections;

@SideOnly(Side.CLIENT)
public final class ExchangeManagementScreen extends IngameFeatureManagementScreen<Exchange> {

    @Inject private static ClientExchangeManager exchangeManager;

    public ExchangeManagementScreen() {
        super(I18n.format("almura.title.exchange.management"), "Exchange",
                // onRefresh
                screen -> {
                    screen.setItems(Collections.unmodifiableList(exchangeManager.getExchanges()));
                },
                // onDelete
                (screen, feature) -> {
                    feature.ifPresent(f -> exchangeManager.deleteExchange(f.getId()));
                },
                // onOpen
                (screen, feature) -> {
                    feature.ifPresent(f -> exchangeManager.requestExchangeSpecificGui(f.getId()));
                },
                // onSave
                (screen, feature) -> {
                    if (feature.isPresent()) { // It's a new listing if true
                        exchangeManager.addExchange(screen.getPendingId(), screen.getPendingTitle(), screen.getPendingPermission(),
                                screen.getPendingHiddenState());
                    } else {
                        exchangeManager.modifyExchange(screen.getPendingId(), screen.getPendingTitle(), screen.getPendingPermission(),
                                screen.getPendingHiddenState());
                    }
                });
    }
}
