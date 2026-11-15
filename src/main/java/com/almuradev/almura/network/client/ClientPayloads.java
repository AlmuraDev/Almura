/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.network.client;

import com.almuradev.almura.feature.almanac.client.AlmanacScreens;
import com.almuradev.almura.feature.almanac.network.ClientboundAlmanacCrops;
import com.almuradev.almura.feature.almanac.network.ClientboundAlmanacField;
import com.almuradev.almura.feature.guide.client.ClientGuide;
import com.almuradev.almura.feature.guide.network.GuideMessages;
import com.almuradev.almura.feature.hud.ServerHudData;
import com.almuradev.almura.feature.hud.network.ClientboundPlayerWorlds;
import com.almuradev.almura.feature.hud.network.ClientboundServerTps;
import com.almuradev.almura.feature.hud.network.ClientboundWorldName;
import com.almuradev.almura.feature.notification.PopupNotification;
import com.almuradev.almura.feature.notification.client.ClientNotifications;
import com.almuradev.almura.feature.notification.network.ClientboundPopupNotification;
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
        event.register(ClientboundAlmanacField.TYPE, (payload, context) -> AlmanacScreens.openField(payload));
        event.register(ClientboundAlmanacCrops.TYPE, (payload, context) -> AlmanacScreens.openCrops(payload));
        event.register(GuideMessages.ClientboundListing.TYPE, (payload, context) -> ClientGuide.onListing(payload));
        event.register(GuideMessages.ClientboundPage.TYPE, (payload, context) -> ClientGuide.onPage(payload));
        event.register(GuideMessages.ClientboundChangeResult.TYPE, (payload, context) -> ClientGuide.onChangeResult(payload));
    }
}
