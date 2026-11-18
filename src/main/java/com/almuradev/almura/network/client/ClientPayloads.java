/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.network.client;

import com.almuradev.almura.feature.accessory.client.ClientAccessories;
import com.almuradev.almura.feature.accessory.network.AccessoryMessages;
import com.almuradev.almura.feature.almanac.client.AlmanacScreens;
import com.almuradev.almura.feature.almanac.network.ClientboundAlmanacCrops;
import com.almuradev.almura.feature.almanac.network.ClientboundAlmanacField;
import com.almuradev.almura.feature.bank.client.ClientBank;
import com.almuradev.almura.feature.bank.network.BankMessages;
import com.almuradev.almura.feature.death.client.ClientDeath;
import com.almuradev.almura.feature.death.network.DeathMessages;
import com.almuradev.almura.feature.exchange.client.ClientExchange;
import com.almuradev.almura.feature.exchange.network.ExchangeMessages;
import com.almuradev.almura.feature.guide.client.ClientGuide;
import com.almuradev.almura.feature.guide.network.GuideMessages;
import com.almuradev.almura.feature.handbook.client.ClientHandbook;
import com.almuradev.almura.feature.handbook.network.ClientboundHandbook;
import com.almuradev.almura.feature.hud.network.ClientboundBiomeNames;
import com.almuradev.almura.feature.hud.network.ClientboundPlayerMoney;
import com.almuradev.almura.feature.hud.network.ClientboundPlayerWorlds;
import com.almuradev.almura.feature.hud.network.ClientboundServerTps;
import com.almuradev.almura.feature.hud.network.ClientboundWorldName;
import com.almuradev.almura.feature.hud.ServerHudData;
import com.almuradev.almura.feature.membership.client.ClientMemberships;
import com.almuradev.almura.feature.membership.network.MembershipMessages;
import com.almuradev.almura.feature.menu.ingame.FeaturesMenu;
import com.almuradev.almura.feature.menu.ingame.network.FeaturesMessages;
import com.almuradev.almura.feature.nick.client.ClientNicks;
import com.almuradev.almura.feature.nick.network.NickMessages;
import com.almuradev.almura.feature.notification.client.ClientNotifications;
import com.almuradev.almura.feature.notification.network.ClientboundPopupNotification;
import com.almuradev.almura.feature.notification.PopupNotification;
import com.almuradev.almura.feature.skills.client.ClientSkills;
import com.almuradev.almura.feature.skills.network.ClientboundSkills;
import com.almuradev.almura.feature.store.client.ClientStore;
import com.almuradev.almura.feature.store.network.StoreMessages;
import com.almuradev.almura.feature.title.client.ClientTitles;
import com.almuradev.almura.feature.title.network.TitleMessages;
import com.almuradev.almura.feature.worldload.client.ClientWorldLoad;
import com.almuradev.almura.feature.worldload.network.ClientboundWorldLoad;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;

/**
 * Handles Almura's messages to the client, on the client's main thread. Registered by {@link com.almuradev.almura.AlmuraClient} only, so
 * nothing here is loaded on a dedicated server.
 */
public final class ClientPayloads {

    private ClientPayloads() {
    }

    public static void register(final IEventBus modBus) {
        modBus.addListener(ClientPayloads::onRegisterHandlers);
    }

    private static void onRegisterHandlers(final RegisterClientPayloadHandlersEvent event) {
        event.register(ClientboundPopupNotification.TYPE, (payload, context) ->
            ClientNotifications.show(new PopupNotification(payload.title(), payload.message(), payload.secondsToLive())));
        event.register(ClientboundWorldName.TYPE, (payload, context) -> ServerHudData.setWorldName(payload.name()));
        event.register(ClientboundServerTps.TYPE, (payload, context) -> ServerHudData.setTps(payload.tps()));
        event.register(ClientboundPlayerWorlds.TYPE, (payload, context) -> ServerHudData.updatePlayerWorlds(payload.replace(), payload.worlds()));
        event.register(ClientboundBiomeNames.TYPE, (payload, context) -> ServerHudData.addBiomeNames(payload.names()));
        event.register(ClientboundWorldLoad.TYPE, (payload, context) -> ClientWorldLoad.handle(payload));
        event.register(ClientboundPlayerMoney.TYPE, (payload, context) -> ServerHudData.setMoney(payload.symbol(), payload.balance()));
        event.register(ClientboundAlmanacField.TYPE, (payload, context) -> AlmanacScreens.openField(payload));
        event.register(ClientboundAlmanacCrops.TYPE, (payload, context) -> AlmanacScreens.openCrops(payload));
        event.register(GuideMessages.ClientboundListing.TYPE, (payload, context) -> ClientGuide.onListing(payload));
        event.register(GuideMessages.ClientboundPage.TYPE, (payload, context) -> ClientGuide.onPage(payload));
        event.register(GuideMessages.ClientboundChangeResult.TYPE, (payload, context) -> ClientGuide.onChangeResult(payload));
        event.register(ClientboundHandbook.TYPE, (payload, context) -> ClientHandbook.open(payload));
        event.register(TitleMessages.ClientboundRegistry.TYPE, (payload, context) -> ClientTitles.onRegistry(payload));
        event.register(TitleMessages.ClientboundAvailable.TYPE, (payload, context) -> ClientTitles.onAvailable(payload));
        event.register(TitleMessages.ClientboundSelected.TYPE, (payload, context) -> ClientTitles.onSelected(payload));
        event.register(TitleMessages.ClientboundOpen.TYPE, (payload, context) -> ClientTitles.onOpen(payload));
        event.register(NickMessages.ClientboundNames.TYPE, (payload, context) -> ClientNicks.onNames(payload));
        event.register(NickMessages.ClientboundOpen.TYPE, (payload, context) -> ClientNicks.onOpen(payload));
        event.register(MembershipMessages.ClientboundOpen.TYPE, (payload, context) -> ClientMemberships.onOpen(payload));
        event.register(MembershipMessages.ClientboundApplied.TYPE, (payload, context) -> ClientMemberships.onApplied(payload));
        event.register(DeathMessages.ClientboundDied.TYPE, (payload, context) -> ClientDeath.onDied(payload));
        event.register(AccessoryMessages.ClientboundOpen.TYPE, (payload, context) -> ClientAccessories.onOpen(payload));
        event.register(AccessoryMessages.ClientboundWorn.TYPE, (payload, context) -> ClientAccessories.onWorn(payload));
        event.register(ClientboundSkills.TYPE, (payload, context) -> ClientSkills.setSkills(payload.skills()));
        event.register(BankMessages.ClientboundState.TYPE, (payload, context) -> ClientBank.onState(payload));
        event.register(FeaturesMessages.ClientboundOpen.TYPE, (payload, context) -> FeaturesMenu.onOpen(payload));
        event.register(BankMessages.ClientboundHistory.TYPE, (payload, context) -> ClientBank.onHistory(payload));
        event.register(ExchangeMessages.ClientboundRegistry.TYPE, (payload, context) -> ClientExchange.onRegistry(payload));
        event.register(ExchangeMessages.ClientboundOpen.TYPE, (payload, context) -> ClientExchange.onOpen(payload));
        event.register(ExchangeMessages.ClientboundListItems.TYPE, (payload, context) -> ClientExchange.onListItems(payload));
        event.register(ExchangeMessages.ClientboundForSale.TYPE, (payload, context) -> ClientExchange.onForSale(payload));
        event.register(ExchangeMessages.ClientboundRefresh.TYPE, (payload, context) -> ClientExchange.onRefresh(payload));
        event.register(ExchangeMessages.ClientboundTransactionComplete.TYPE, (payload, context) -> ClientExchange.onTransactionComplete());
        event.register(StoreMessages.ClientboundRegistry.TYPE, (payload, context) -> ClientStore.onRegistry(payload));
        event.register(StoreMessages.ClientboundOpen.TYPE, (payload, context) -> ClientStore.onOpen(payload));
        event.register(StoreMessages.ClientboundItems.TYPE, (payload, context) -> ClientStore.onItems(payload));
    }
}
