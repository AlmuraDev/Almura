/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.claim;

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

    @Inject
    public ClientClaimManager(@ChannelId(NetworkConfig.CHANNEL) final ChannelBinding.IndexedMessageChannel network) {
        this.network = network;
    }

    public void requestClaimGUI() {
        //this.network.sendToServer(new ServerboundClaimGuiRequestPacket());
    }
}
