/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.worldedit.client;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.enginehub.worldeditcui.protocol.CUIPacket;
import org.enginehub.worldeditcui.protocol.CUIPacketHandler;

/**
 * The CUI channel when WorldEdit is installed on this client. WorldEdit brings the WorldEditCUI protocol mod, which registers the
 * {@code worldedit:cui} payload itself (a second registration would fail), so Almura listens through the protocol's handler list instead.
 * <p>
 * Only loaded when the protocol mod is ({@link ClientWorldEditSelection#PROTOCOL_MOD}).
 */
final class WorldEditCuiChannel implements CuiChannel {

    private WorldEditCuiChannel() {
    }

    /**
     * Adds Almura to the messages the client gets from WorldEdit. Called once WorldEdit has set up (client setup comes after its common setup,
     * where it first asks the protocol for its handler).
     */
    static CuiChannel hook(final ClientWorldEditSelection selection) {
        CUIPacketHandler.instance().registerClientboundHandler((packet, context) -> selection.receive(packet.multi(), packet.eventType(), packet.args()));
        return new WorldEditCuiChannel();
    }

    @Override
    public CustomPacketPayload.Type<?> type() {
        return CUIPacket.TYPE;
    }

    @Override
    public CustomPacketPayload handshake() {
        return new CUIPacket(ClientWorldEditSelection.HANDSHAKE, ClientWorldEditSelection.PROTOCOL);
    }
}
