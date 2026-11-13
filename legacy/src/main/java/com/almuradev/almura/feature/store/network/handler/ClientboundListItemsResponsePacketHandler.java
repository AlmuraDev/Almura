/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.store.network.handler;

import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.almura.feature.store.client.ClientStoreManager;
import com.almuradev.almura.feature.store.listing.BuyingItem;
import com.almuradev.almura.feature.store.listing.SellingItem;
import com.almuradev.almura.feature.store.network.ClientboundListItemsResponsePacket;
import com.almuradev.almura.shared.util.PacketUtil;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class ClientboundListItemsResponsePacketHandler implements MessageHandler<ClientboundListItemsResponsePacket> {

    private final ClientStoreManager storeManager;

    @Inject
    public ClientboundListItemsResponsePacketHandler(final ClientStoreManager storeManager) {
        this.storeManager = storeManager;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void handleMessage(final ClientboundListItemsResponsePacket message, final RemoteConnection connection, final Platform.Type side) {
        if (side.isClient() && PacketUtil.checkThreadAndEnqueue(Minecraft.getMinecraft(), message, this, connection, side)) {

            checkNotNull(message.type);

            switch (message.type) {
                case SELLING:
                    this.storeManager.handleSellingItems(message.id, (List<SellingItem>) message.storeItems);
                    break;
                case BUYING:
                    this.storeManager.handleBuyingItems(message.id, (List<BuyingItem>) message.storeItems);
                    break;
                default:
                    throw new UnsupportedOperationException(message.type + " is not supported yet!");
            }
        }
    }
}
