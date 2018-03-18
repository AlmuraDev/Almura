/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.title.network.handler;

import com.almuradev.almura.feature.title.ServerTitleManager;
import com.almuradev.almura.feature.title.network.ServerboundPlayerSetTitlePacket;
import org.spongepowered.api.Platform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.PlayerConnection;
import org.spongepowered.api.network.RemoteConnection;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.text.serializer.TextSerializers;

import javax.inject.Inject;

@SuppressWarnings("deprecation")
public final class ServerboundPlayerSetTitlePacketHandler implements MessageHandler<ServerboundPlayerSetTitlePacket> {

    private final Scheduler scheduler;
    private final PluginContainer container;
    private final ServerTitleManager manager;

    @Inject
    public ServerboundPlayerSetTitlePacketHandler(final Scheduler scheduler, final PluginContainer container, final ServerTitleManager manager) {
        this.scheduler = scheduler;
        this.container = container;
        this.manager = manager;
    }

    @Override
    public void handleMessage(ServerboundPlayerSetTitlePacket message, RemoteConnection connection, Platform.Type side) {
        if (side.isServer() && connection instanceof PlayerConnection) {

            this.scheduler.createTaskBuilder().delayTicks(0).execute(() -> {
                final Player player = ((PlayerConnection) connection).getPlayer();

                // TODO Perms/etc checking
                if (message.add) {
                    manager.putSelectedTitle(player.getUniqueId(), TextSerializers.LEGACY_FORMATTING_CODE.deserialize(message.title));
                } else {
                    manager.removeSelectedTitle(player.getUniqueId());
                }

                manager.refreshSelectedTitleFor(player, message.add);
            }).submit(this.container);
        }
    }
}
