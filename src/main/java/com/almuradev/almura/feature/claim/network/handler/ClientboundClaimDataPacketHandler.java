/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.claim.network.handler;

import com.almuradev.almura.feature.claim.ClientClaimManager;
import com.almuradev.almura.feature.claim.gui.ClaimManageScreen;
import com.almuradev.almura.feature.claim.network.ClientboundClaimDataPacket;
import com.almuradev.almura.shared.util.PacketUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;

import javax.inject.Inject;

public final class ClientboundClaimDataPacketHandler implements MessageHandler<ClientboundClaimDataPacket> {

    private final ClientClaimManager clientClaimManager;

    @Inject
    private ClientboundClaimDataPacketHandler(final ClientClaimManager claimManager) {
        this.clientClaimManager = claimManager;
    }

    @Override
    public void handleMessage(ClientboundClaimDataPacket message, RemoteConnection connection, Platform.Type side) {
        if (side.isClient()) {
            if (PacketUtil.checkThreadAndEnqueue(Minecraft.getMinecraft(), message, this, connection, side)) {
                clientClaimManager.isClaim = message.isClaim;
                clientClaimManager.claimName = message.claimName;
                clientClaimManager.claimOwner = message.claimOwner;
                clientClaimManager.claimGreeting = message.claimGreeting;
                clientClaimManager.claimFarewell = message.claimFarewell;
                clientClaimManager.claimEconBalance = message.claimEconBalance;
                clientClaimManager.isClaim = message.isClaim;
                clientClaimManager.isWilderness = message.isWilderness;
                clientClaimManager.isTownClaim = message.isTownClaim;
                clientClaimManager.isAdminClaim = message.isAdminClaim;
                clientClaimManager.isBasicClaim = message.isBasicClaim;
                clientClaimManager.isSubdivision = message.isSubdivision;
                clientClaimManager.claimSize = message.claimSize;
                clientClaimManager.showWarnings = message.showWarnings;
                clientClaimManager.claimTaxes = message.claimTaxes;
                clientClaimManager.claimBlockCost = message.claimBlockCost;
                clientClaimManager.claimBlockSell = message.claimBlockSell;
                clientClaimManager.hasWECUI = message.hasWECUI;
                final GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;

                if (currentScreen instanceof ClaimManageScreen) {
                    ((ClaimManageScreen) currentScreen).updateValues();
                }
            }
        }
    }
}
