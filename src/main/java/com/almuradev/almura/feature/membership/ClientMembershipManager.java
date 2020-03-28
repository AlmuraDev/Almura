/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.membership;

import com.almuradev.almura.feature.membership.network.ServerboundMembershipGuiOpenRequestPacket;
import com.almuradev.almura.feature.membership.network.ServerboundMembershipPurchaseRequestPacket;
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
public final class ClientMembershipManager implements Witness {

    public final ChannelBinding.IndexedMessageChannel network;
    private final ClientNotificationManager clientNotificationManager;

    @Inject
    public ClientMembershipManager(final @ChannelId(NetworkConfig.CHANNEL) ChannelBinding.IndexedMessageChannel network, final ClientNotificationManager manager) {
        this.network = network;
        this.clientNotificationManager = manager;
    }

    public void requestMembershipGUI() {
        this.network.sendToServer(new ServerboundMembershipGuiOpenRequestPacket());
    }

    public void requestMembershipPurchase(int membershipLevel) {
        this.network.sendToServer(new ServerboundMembershipPurchaseRequestPacket(membershipLevel));
    }
}
