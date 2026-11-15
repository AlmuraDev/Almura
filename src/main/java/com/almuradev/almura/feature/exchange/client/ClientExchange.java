/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.client;

import com.almuradev.almura.feature.exchange.network.ExchangeMessages.ClientboundForSale;
import com.almuradev.almura.feature.exchange.network.ExchangeMessages.ClientboundListItems;
import com.almuradev.almura.feature.exchange.network.ExchangeMessages.ClientboundOpen;
import com.almuradev.almura.feature.exchange.network.ExchangeMessages.ClientboundRefresh;
import com.almuradev.almura.feature.exchange.network.ExchangeMessages.ClientboundRegistry;
import com.almuradev.almura.feature.exchange.network.ExchangeMessages.ExchangeInfo;
import com.almuradev.almura.feature.exchange.network.ExchangeMessages.ServerboundOpen;
import com.almuradev.almura.feature.menu.InGameScreens;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * The exchanges on the client, as 3.5's {@code ClientExchangeManager}: the exchanges the player may see, the exchange or management screen
 * opened or refreshed with what the server sends, and the player's requests sent to the server.
 */
public final class ClientExchange {

    private static List<ExchangeInfo> exchanges = List.of();
    private static @Nullable ExchangeScreen open;
    private static @Nullable ExchangeManagementScreen management;

    private ClientExchange() {
    }

    static List<ExchangeInfo> exchanges() {
        return exchanges;
    }

    static Optional<ExchangeInfo> exchange(final String id) {
        return exchanges.stream().filter(exchange -> exchange.id().equalsIgnoreCase(id)).findFirst();
    }

    /**
     * Asks the server for the management screen (the Features menu's Manage Exchanges).
     */
    public static void requestManage() {
        send(new ServerboundOpen(""));
    }

    static void requestOpen(final String id) {
        send(new ServerboundOpen(id));
    }

    static void send(final CustomPacketPayload payload) {
        ClientPacketDistributor.sendToServer(payload);
    }

    public static void onRegistry(final ClientboundRegistry registry) {
        exchanges = List.copyOf(registry.exchanges());
        final ExchangeManagementScreen screen = shownManagement();
        if (screen != null) {
            screen.refresh();
        }
    }

    public static void onOpen(final ClientboundOpen message) {
        final Minecraft minecraft = Minecraft.getInstance();
        switch (message.screen()) {
            case MANAGE -> {
                final ExchangeManagementScreen shown = shownManagement();
                if (shown != null) {
                    shown.refresh();
                    return;
                }
                management = new ExchangeManagementScreen();
                InGameScreens.open(management, minecraft.gui.screen());
            }
            case SPECIFIC -> {
                final ExchangeInfo exchange = exchange(message.id()).orElse(null);
                if (exchange == null) {
                    return;
                }
                open = new ExchangeScreen(exchange, message.limit(), message.admin());
                InGameScreens.open(open, minecraft.gui.screen());
            }
            case OFFER -> {
                final ExchangeScreen screen = shownScreen();
                if (screen != null && screen.id().equals(message.id())) {
                    screen.openOffer(message.limit());
                }
            }
        }
    }

    public static void onListItems(final ClientboundListItems message) {
        final ExchangeScreen screen = shownScreen();
        if (screen != null && screen.id().equals(message.id())) {
            screen.showListItems(message.items());
        }
    }

    public static void onForSale(final ClientboundForSale message) {
        final ExchangeScreen screen = shownScreen();
        if (screen != null && screen.id().equals(message.id())) {
            screen.showForSale(message.items(), message.total());
        }
    }

    public static void onRefresh(final ClientboundRefresh message) {
        final ExchangeScreen screen = shownScreen();
        if (screen != null && screen.id().equals(message.id())) {
            screen.search();
        }
    }

    /**
     * A purchase went through: the level up sound, as 3.5.
     */
    public static void onTransactionComplete() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            minecraft.player.playSound(SoundEvents.PLAYER_LEVELUP, 1f, 1f);
        }
    }

    private static @Nullable ExchangeScreen shownScreen() {
        if (open != null && !InGameScreens.isShowing(open)) {
            open = null;
        }
        return open;
    }

    private static @Nullable ExchangeManagementScreen shownManagement() {
        if (management != null && !InGameScreens.isShowing(management)) {
            management = null;
        }
        return management;
    }
}
