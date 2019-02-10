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
        super(I18n.format("almura.feature.common.title.management", I18n.format("almura.feature.exchange.name")),
                I18n.format("almura.feature.exchange.name"),
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
                    if (feature.isPresent()) { // It's an existing listing if true
                      exchangeManager.modifyExchange(screen.getPendingId(), screen.getPendingTitle(), screen.getPendingPermission(),
                        screen.getPendingHiddenState());
                    } else {
                      exchangeManager.addExchange(screen.getPendingId(), screen.getPendingTitle(), screen.getPendingPermission(),
                        screen.getPendingHiddenState());
                    }
                });
    }
}
