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
import com.almuradev.almura.shared.network.NetworkConfig;
import com.almuradev.core.event.Witness;
import org.spongepowered.api.GameState;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;
import org.spongepowered.api.text.Text;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class ServerNotificationManager extends Witness.Impl implements Witness.Lifecycle {

    private final ChannelBinding.IndexedMessageChannel network;

    @Inject
    public ServerNotificationManager(@ChannelId(NetworkConfig.CHANNEL) final ChannelBinding.IndexedMessageChannel network) {
        this.network = network;
    }

    @Override
    public boolean lifecycleSubscribable(final GameState state) {
        return state == GameState.SERVER_STARTING;
    }

    public void sendPopupNotification(final Player player, final Text title, final Text message, final int secondsToLive) {
        checkNotNull(player);
        checkNotNull(title);
        checkNotNull(message);
        checkState(secondsToLive > 0);

        this.network.sendTo(player, new ClientboundPlayerNotificationPacket(title, message, secondsToLive));
    }

    public void sendWindowMessage(final Player player, final Text title, final Text message) {
        checkNotNull(player);
        checkNotNull(title);
        checkNotNull(message);

        this.network.sendTo(player, new ClientboundPlayerNotificationPacket(title, message));
    }
}
