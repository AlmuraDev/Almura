/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.network;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.hud.network.ClientboundPlayerWorlds;
import com.almuradev.almura.feature.hud.network.ClientboundServerTps;
import com.almuradev.almura.feature.hud.network.ClientboundWorldName;
import com.almuradev.almura.feature.notification.network.ClientboundPopupNotification;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Almura's messages between the server and the client, as NeoForge payloads under the {@code almura} namespace.
 *
 * <p>Registered on both sides. Messages to the client are handled by the client alone (see
 * {@code com.almuradev.almura.network.client.ClientPayloads}), so none of their handling is loaded on a dedicated server. The client and the
 * server must both have Almura at the same {@link #VERSION}, as 1.12 required its client; a player without it can't join.</p>
 */
public final class AlmuraNetwork {

    /** Raised whenever a message changes in a way older clients or servers can't read. */
    public static final String VERSION = "1";

    private AlmuraNetwork() {
    }

    public static void register(final IEventBus modBus) {
        modBus.addListener(AlmuraNetwork::onRegisterPayloads);
    }

    public static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> type(final String path) {
        return new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Almura.ID, path));
    }

    private static void onRegisterPayloads(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(Almura.ID).versioned(VERSION);
        // To the client
        registrar.playToClient(ClientboundPopupNotification.TYPE, ClientboundPopupNotification.CODEC);
        registrar.playToClient(ClientboundWorldName.TYPE, ClientboundWorldName.CODEC);
        registrar.playToClient(ClientboundServerTps.TYPE, ClientboundServerTps.CODEC);
        registrar.playToClient(ClientboundPlayerWorlds.TYPE, ClientboundPlayerWorlds.CODEC);
        registrar.playToClient(com.almuradev.almura.feature.almanac.network.ClientboundAlmanacField.TYPE,
            com.almuradev.almura.feature.almanac.network.ClientboundAlmanacField.CODEC);
        registrar.playToClient(com.almuradev.almura.feature.almanac.network.ClientboundAlmanacCrops.TYPE,
            com.almuradev.almura.feature.almanac.network.ClientboundAlmanacCrops.CODEC);
        registrar.playToClient(com.almuradev.almura.feature.guide.network.GuideMessages.ClientboundListing.TYPE, com.almuradev.almura.feature.guide.network.GuideMessages.ClientboundListing.CODEC);
        registrar.playToClient(com.almuradev.almura.feature.guide.network.GuideMessages.ClientboundPage.TYPE, com.almuradev.almura.feature.guide.network.GuideMessages.ClientboundPage.CODEC);
        // To the server
        registrar.playToServer(com.almuradev.almura.feature.guide.network.GuideMessages.ServerboundOpen.TYPE, com.almuradev.almura.feature.guide.network.GuideMessages.ServerboundOpen.CODEC, (payload, context) ->
            com.almuradev.almura.feature.guide.server.ServerGuide.open((net.minecraft.server.level.ServerPlayer) context.player()));
        registrar.playToServer(com.almuradev.almura.feature.guide.network.GuideMessages.ServerboundPage.TYPE, com.almuradev.almura.feature.guide.network.GuideMessages.ServerboundPage.CODEC, (payload, context) ->
            com.almuradev.almura.feature.guide.server.ServerGuide.openPage((net.minecraft.server.level.ServerPlayer) context.player(),
                payload.id()));
    }
}
