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

import com.almuradev.almura.feature.notification.network.ClientboundPlayerNotificationPacket;
import com.almuradev.almura.shared.event.Witness;
import com.almuradev.almura.shared.network.NetworkConfig;
import net.kyori.membrane.facet.Activatable;
import org.spongepowered.api.Game;
import org.spongepowered.api.GameState;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;
import org.spongepowered.api.text.Text;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class ServerNotificationManager extends Witness.Impl implements Activatable, Witness.Lifecycle {

    private final Game game;
    private final ChannelBinding.IndexedMessageChannel network;

    @Inject
    public ServerNotificationManager(final Game game, @ChannelId(NetworkConfig.CHANNEL)ChannelBinding.IndexedMessageChannel network) {
        this.game = game;
        this.network = network;
    }

    @Override
    public boolean lifecycleSubscribable(GameState state) {
        return state == GameState.SERVER_STARTING;
    }

    @Override
    public boolean active() {
        return this.game.isServerAvailable();
    }

    public void sendPopupNotification(Player player, Text message, int secondsToLive) {
        checkNotNull(player);
        checkNotNull(message);
        checkState(secondsToLive > 0);

        this.network.sendTo(player, new ClientboundPlayerNotificationPacket(message, secondsToLive));
    }

    public void sendWindowMessage(Player player, Text message) {
        checkNotNull(player);
        checkNotNull(message);

        this.network.sendTo(player, new ClientboundPlayerNotificationPacket(message));
    }
}
