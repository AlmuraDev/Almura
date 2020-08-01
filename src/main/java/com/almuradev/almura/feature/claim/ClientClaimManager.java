/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.claim;

import com.almuradev.almura.feature.claim.network.ServerboundClaimGuiRequestPacket;
import com.almuradev.almura.feature.notification.ClientNotificationManager;
import com.almuradev.almura.shared.network.NetworkConfig;
import com.almuradev.core.event.Witness;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;

import javax.inject.Inject;
import javax.inject.Singleton;

@SideOnly(Side.CLIENT)
@Singleton
public final class ClientClaimManager implements Witness {

    private final ChannelBinding.IndexedMessageChannel network;
    private final ClientNotificationManager clientNotificationManager;
    public double claimEconBalance = 0.0;
    public double claimTaxes = 0.0;
    public double claimBlockSell = 0.0;
    public double claimBlockCost = 0.0;
    public int claimSize = 0;
    public boolean isClaim = false;
    public boolean isWilderness = false;
    public boolean isTownClaim = false;
    public boolean isAdminClaim = false;
    public boolean isBasicClaim = false;
    public boolean isSubdivision = false;
    public boolean isForSale = false;
    public boolean showWarnings = true;
    public boolean hasWECUI = false;
    public String claimName = "";
    public String claimOwner = "";
    public String claimGreeting = "";
    public String claimFarewell = "";
    public double claimTaxBalance;
    public double claimSalePrice;

    @Inject
    public ClientClaimManager(@ChannelId(NetworkConfig.CHANNEL) final ChannelBinding.IndexedMessageChannel network, final
    ClientNotificationManager clientNotificationManager) {
        this.network = network;
        this.clientNotificationManager = clientNotificationManager;
    }

    public void requestClaimGUI() {
        this.network.sendToServer(new ServerboundClaimGuiRequestPacket());
    }
}
