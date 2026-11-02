/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.claim.network.handler;

import com.almuradev.almura.feature.claim.ClientClaimVisuals;
import com.almuradev.almura.feature.claim.network.ClientboundClaimVisualPacket;
import com.almuradev.almura.shared.util.PacketUtil;
import net.minecraft.client.Minecraft;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;

import javax.inject.Inject;

public final class ClientboundClaimVisualPacketHandler implements MessageHandler<ClientboundClaimVisualPacket> {

    private final ClientClaimVisuals visuals;

    @Inject
    private ClientboundClaimVisualPacketHandler(final ClientClaimVisuals visuals) {
        this.visuals = visuals;
    }

    @Override
    public void handleMessage(ClientboundClaimVisualPacket message, RemoteConnection connection, Platform.Type side) {
        if (side.isClient() && PacketUtil.checkThreadAndEnqueue(Minecraft.getMinecraft(), message, this, connection, side)) {
            if (message.boxes.isEmpty()) {
                this.visuals.clear();
            } else {
                this.visuals.show(message.boxes);
            }
        }
    }
}
