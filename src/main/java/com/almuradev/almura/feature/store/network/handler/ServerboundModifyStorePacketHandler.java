/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.store.network.handler;

import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.almura.feature.store.ServerStoreManager;
import com.almuradev.almura.feature.store.network.ServerboundModifyStorePacket;
import com.almuradev.almura.shared.util.PacketUtil;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.PlayerConnection;
import org.spongepowered.api.network.RemoteConnection;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class ServerboundModifyStorePacketHandler implements MessageHandler<ServerboundModifyStorePacket> {

    private final ServerStoreManager storeManager;

    @Inject
    public ServerboundModifyStorePacketHandler(final ServerStoreManager storeManager) {
        this.storeManager = storeManager;
    }

    @Override
    public void handleMessage(final ServerboundModifyStorePacket message, final RemoteConnection connection, final Platform.Type side) {
        if (side.isServer() && connection instanceof PlayerConnection && PacketUtil
            .checkThreadAndEnqueue((MinecraftServer) Sponge.getServer(), message, this, connection, side)) {

            checkNotNull(message.type);

            final Player player = ((PlayerConnection) connection).getPlayer();

            switch (message.type) {
                case ADD:
                    this.storeManager.handleStoreAdd(player, message.id, message.name, message.permission, message.isHidden);
                    break;
                case MODIFY:
                    this.storeManager.handleStoreModify(player, message.id, message.name, message.permission, message.isHidden);
                    break;
                case DELETE:
                    this.storeManager.handleStoreDelete(player, message.id);
                    break;
                default:
                    throw new UnsupportedOperationException(message.type + " is not supported yet!");
            }
        }
    }
}
