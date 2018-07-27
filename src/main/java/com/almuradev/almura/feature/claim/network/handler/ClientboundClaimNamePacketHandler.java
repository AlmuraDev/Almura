/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.claim.network.handler;

import com.almuradev.almura.feature.claim.network.ClientboundClaimNamePacket;
import com.almuradev.almura.feature.hud.HeadUpDisplay;
import com.almuradev.almura.feature.hud.network.ClientboundWorldNamePacket;
import com.almuradev.almura.shared.util.PacketUtil;
import net.minecraft.client.Minecraft;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;

import javax.inject.Inject;

public final class ClientboundClaimNamePacketHandler implements MessageHandler<ClientboundClaimNamePacket> {

    private final HeadUpDisplay hudData;

    @Inject
    private ClientboundClaimNamePacketHandler(final HeadUpDisplay hudData) {
        this.hudData = hudData;
    }

    @Override
    public void handleMessage(ClientboundClaimNamePacket message, RemoteConnection connection, Platform.Type side) {
        if (side.isClient()) {
            if (PacketUtil.checkThreadAndEnqueue(Minecraft.getMinecraft(), message, this, connection, side)) {
                this.hudData.isClaim = message.isClaim;
                this.hudData.claimName = message.name;
                this.hudData.isWilderness = message.isWilderness;
                this.hudData.isTownClaim = message.isTownClaim;
                this.hudData.isAdminClaim = message.isAdminClaim;
                this.hudData.isBasicClaim = message.isBasicClaim;
                this.hudData.isSubdivision = message.isSubdivision;
            }
        }
    }
}
