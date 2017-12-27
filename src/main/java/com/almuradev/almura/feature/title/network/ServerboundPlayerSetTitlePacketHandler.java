/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.title.network;

import com.almuradev.almura.feature.title.ServerTitleManager;
import org.spongepowered.api.Platform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.PlayerConnection;
import org.spongepowered.api.network.RemoteConnection;
import org.spongepowered.api.text.serializer.TextSerializers;

import javax.inject.Inject;

public final class ServerboundPlayerSetTitlePacketHandler implements MessageHandler<ServerboundPlayerSetTitlePacket> {

    private final ServerTitleManager manager;

    @Inject
    public ServerboundPlayerSetTitlePacketHandler(final ServerTitleManager manager) {
        this.manager = manager;
    }

    @Override
    public void handleMessage(ServerboundPlayerSetTitlePacket message, RemoteConnection connection, Platform.Type side) {
        if (side.isServer()) {
            if (connection instanceof PlayerConnection) {
                final Player player = ((PlayerConnection) connection).getPlayer();

                // TODO Perms/etc checking
                if (message.add) {
                    manager.putSelectedTitle(player.getUniqueId(), TextSerializers.LEGACY_FORMATTING_CODE.deserialize(message.title));
                } else {
                    manager.removeSelectedTitle(player.getUniqueId());
                }

                manager.refreshSelectedTitleFor(player, message.add);
            }
        }
    }
}
