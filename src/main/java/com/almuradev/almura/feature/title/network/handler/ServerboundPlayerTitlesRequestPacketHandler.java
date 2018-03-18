/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.title.network.handler;

import com.almuradev.almura.feature.title.ServerTitleManager;
import com.almuradev.almura.feature.title.network.ClientboundPlayerTitlesResponsePacket;
import com.almuradev.almura.feature.title.network.ServerboundPlayerTitlesRequestPacket;
import com.almuradev.almura.shared.network.NetworkConfig;
import org.spongepowered.api.Platform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.PlayerConnection;
import org.spongepowered.api.network.RemoteConnection;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.text.Text;

import java.util.Set;

import javax.inject.Inject;

@SuppressWarnings("deprecation")
public final class ServerboundPlayerTitlesRequestPacketHandler implements MessageHandler<ServerboundPlayerTitlesRequestPacket> {

    private final Scheduler scheduler;
    private final PluginContainer container;
    private final ServerTitleManager manager;
    private final ChannelBinding.IndexedMessageChannel network;

    @Inject
    public ServerboundPlayerTitlesRequestPacketHandler(final Scheduler scheduler, final PluginContainer container, final ServerTitleManager manager,
            @ChannelId(NetworkConfig.CHANNEL) ChannelBinding.IndexedMessageChannel network) {
        this.scheduler = scheduler;
        this.container = container;
        this.manager = manager;
        this.network = network;
    }

    @Override
    public void handleMessage(ServerboundPlayerTitlesRequestPacket message, RemoteConnection connection, Platform.Type side) {
        if (side.isServer() && connection instanceof PlayerConnection) {

            this.scheduler.createTaskBuilder().delayTicks(0).execute(() -> {
                final Player player = ((PlayerConnection) connection).getPlayer();
                final Set<Text> titles = this.manager.getTitlesFor(player);

                if (titles.isEmpty()) {
                    // TODO Tell the client they have no titles :'(
                } else {

                    final Text selectedTitle = this.manager.getSelectedTitleFor(player).orElse(null);

                    int selectedIndex = 0;
                    if (selectedTitle != null) {
                        for (Text title : titles) {
                            if (title.equals(selectedTitle)) {
                                break;
                            }

                            selectedIndex++;
                        }
                    } else {
                        selectedIndex = -1;
                    }

                    this.network.sendTo(player, new ClientboundPlayerTitlesResponsePacket(selectedIndex, titles));
                }
            }).submit(this.container);
        }
    }
}
