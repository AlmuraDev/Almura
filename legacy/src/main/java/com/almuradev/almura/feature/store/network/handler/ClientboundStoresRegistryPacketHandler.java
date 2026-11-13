/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.store.network.handler;

import com.almuradev.almura.feature.store.client.ClientStoreManager;
import com.almuradev.almura.feature.store.network.ClientboundStoresRegistryPacket;
import com.almuradev.almura.shared.util.PacketUtil;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class ClientboundStoresRegistryPacketHandler implements MessageHandler<ClientboundStoresRegistryPacket> {

    private final ClientStoreManager storeManager;

    @Inject
    public ClientboundStoresRegistryPacketHandler(final ClientStoreManager storeManager) {
        this.storeManager = storeManager;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void handleMessage(final ClientboundStoresRegistryPacket message, final RemoteConnection connection, final Platform.Type side) {
        if (side.isClient() && PacketUtil.checkThreadAndEnqueue(Minecraft.getMinecraft(), message, this, connection, side)) {
            this.storeManager.handleStoreRegistry(message.stores);
        }
    }
}
