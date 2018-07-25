/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.title.network.handler;

import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.almura.feature.title.ServerTitleManager;
import com.almuradev.almura.feature.title.network.ServerboundModifyTitlePacket;
import com.almuradev.almura.shared.util.PacketUtil;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.PlayerConnection;
import org.spongepowered.api.network.RemoteConnection;

import javax.inject.Inject;

public final class ServerboundModifyTitlePacketHandler implements MessageHandler<ServerboundModifyTitlePacket> {

    private final ServerTitleManager manager;

    @Inject
    public ServerboundModifyTitlePacketHandler(final ServerTitleManager manager) {
        this.manager = manager;
    }

    @Override
    public void handleMessage(final ServerboundModifyTitlePacket message, final RemoteConnection connection, final Platform.Type side) {
        if (side.isServer() && connection instanceof PlayerConnection && PacketUtil
            .checkThreadAndEnqueue((MinecraftServer) Sponge.getServer(), message, this, connection, side)) {

            checkNotNull(message.type);

            final Player player = ((PlayerConnection) connection).getPlayer();

            switch (message.type) {
                case ADD:
                    this.manager.addTitle(player, message.id, message.permission, message.content, message.isHidden);
                    break;
                case MODIFY:
                    this.manager.modifyTitle(player, message.id, message.permission, message.content, message.isHidden);
                    break;
                case DELETE:
                    this.manager.deleteTitle(player, message.id);
            }
        }
    }
}
