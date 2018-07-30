/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.claim.network.handler;

import com.almuradev.almura.feature.claim.ClientClaimManager;
import com.almuradev.almura.feature.claim.gui.ManageClaimGUI;
import com.almuradev.almura.feature.claim.network.ClientboundClaimDataPacket;
import com.almuradev.almura.shared.util.PacketUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;

import javax.inject.Inject;

public final class ClientboundClaimDataPacketHandler implements MessageHandler<ClientboundClaimDataPacket> {

    private final ClientClaimManager claimManager;

    @Inject
    private ClientboundClaimDataPacketHandler(final ClientClaimManager claimManager) {
        this.claimManager = claimManager;
    }

    @Override
    public void handleMessage(final ClientboundClaimDataPacket message, final RemoteConnection connection, final Platform.Type side) {
        if (side.isClient() && PacketUtil.checkThreadAndEnqueue(Minecraft.getMinecraft(), message, this, connection, side)) {
            claimManager.isClaim = message.isClaim;
            claimManager.claimName = message.claimName;
            claimManager.claimOwner = message.claimOwner;
            claimManager.claimGreeting = message.claimGreeting;
            claimManager.claimFarewell = message.claimFarewell;
            claimManager.claimEconBalance = message.claimEconBalance;
            claimManager.isWilderness = message.isWilderness;
            claimManager.isTownClaim = message.isTownClaim;
            claimManager.isAdminClaim = message.isAdminClaim;
            claimManager.isBasicClaim = message.isBasicClaim;
            claimManager.isSubdivision = message.isSubdivision;
            claimManager.claimSize = message.claimSize;
            claimManager.showWarnings = message.showWarnings;
            claimManager.claimTaxes = message.claimTaxes;
            claimManager.claimBlockCost = message.claimBlockCost;
            claimManager.claimBlockSell = message.claimBlockSell;

            final GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;

            if (currentScreen instanceof ManageClaimGUI) {
                ((ManageClaimGUI) currentScreen).updateValues();
            }
        }
    }
}
