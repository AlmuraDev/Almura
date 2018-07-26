/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange;

import com.almuradev.almura.feature.exchange.network.ServerboundExchangeOpenRequestPacket;
import com.almuradev.almura.shared.network.NetworkConfig;
import com.almuradev.core.event.Witness;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@SideOnly(Side.CLIENT)
public final class ClientExchangeManager implements Witness {

    private final ChannelBinding.IndexedMessageChannel network;

    @Inject
    public ClientExchangeManager(final @ChannelId(NetworkConfig.CHANNEL) ChannelBinding.IndexedMessageChannel network) {
        this.network = network;
    }

    public void requestExchangeGUI() {
        this.network.sendToServer(new ServerboundExchangeOpenRequestPacket());
    }
}
