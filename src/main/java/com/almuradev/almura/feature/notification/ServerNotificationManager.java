/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.notification;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.almuradev.almura.feature.nick.ServerNickManager;
import com.almuradev.almura.feature.notification.network.ClientboundPlayerNotificationPacket;
import com.almuradev.almura.feature.title.ServerTitleManager;
import com.almuradev.almura.shared.network.NetworkConfig;
import com.almuradev.core.event.Witness;
import io.github.nucleuspowered.nucleus.api.service.NucleusNicknameService;
import org.spongepowered.api.Game;
import org.spongepowered.api.GameState;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;
import org.spongepowered.api.text.Text;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class ServerNotificationManager extends Witness.Impl implements Witness.Lifecycle {

    public final Game game;
    private final ChannelBinding.IndexedMessageChannel network;
    private final ServerTitleManager serverTitleManager;
    private final ServerNickManager serverNickManager;

    @Inject
    public ServerNotificationManager(final Game game, @ChannelId(NetworkConfig.CHANNEL)ChannelBinding.IndexedMessageChannel network,
        ServerTitleManager serverTitleManager, ServerNickManager serverNickManager) {
        this.game = game;
        this.network = network;
        this.serverTitleManager = serverTitleManager;
        this.serverNickManager = serverNickManager;
    }

    @Override
    public boolean lifecycleSubscribable(GameState state) {
        return state == GameState.SERVER_STARTING;
    }

    public void sendPopupNotification(Player player, Text title, Text message, int secondsToLive) {
        checkNotNull(player);
        checkNotNull(title);
        checkNotNull(message);
        checkState(secondsToLive > 0);

        this.network.sendTo(player, new ClientboundPlayerNotificationPacket(title, message, secondsToLive));
    }

    public void sendWindowMessage(Player player, Text title, Text message) {
        checkNotNull(player);
        checkNotNull(title);
        checkNotNull(message);

        this.network.sendTo(player, new ClientboundPlayerNotificationPacket(title, message));
    }

    // when a player attempts to join the server...
    @Listener(order = Order.LAST)
    public void onPlayerJoin(final ClientConnectionEvent.Join event, @Getter("getTargetEntity") Player player) {
        checkNotNull(player);

        String displayName = serverNickManager.getNickname(player);
        //String playerTitle = serverTitleManager.getSelectedTitleFor(player);

        for (final Player players : Sponge.getServer().getOnlinePlayers()) {
            if (players.equals(player)) {
                continue;
            }
            this.sendPopupNotification(player, Text.of("Player Joined"), Text.of(displayName + " has joined the server"), 5);
        }
    }

    // when a player attempts to join the server...
    @Listener(order = Order.PRE)
    public void onPlayerQuit(final ClientConnectionEvent.Disconnect event, @Getter("getTargetEntity") Player player) {
        checkNotNull(player); // I hope that this fire before our nickname or playername disappears.

        String displayName = serverNickManager.getNickname(player);
        //String playerTitle = serverTitleManager.getSelectedTitleFor(player);

        for (final Player players : Sponge.getServer().getOnlinePlayers()) {
            if (players.equals(player)) {
                continue;
            }
            this.sendPopupNotification(player, Text.of("Player Disconnected"), Text.of(displayName + " has left the server"), 5);
        }
    }
}
