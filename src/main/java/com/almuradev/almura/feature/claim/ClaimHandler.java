/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.claim;

import com.almuradev.almura.feature.notification.ServerNotificationManager;
import com.almuradev.almura.shared.network.NetworkConfig;
import com.almuradev.core.event.Witness;
import me.ryanhamshire.griefprevention.api.event.*;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;

import javax.inject.Inject;

public final class ClaimHandler implements Witness {

    private final ServerNotificationManager serverNotificationManager;
    private final ChannelBinding.IndexedMessageChannel network;

    @Inject
    public ClaimHandler(final ServerNotificationManager serverNotificationManager, final @ChannelId(NetworkConfig.CHANNEL) ChannelBinding.IndexedMessageChannel network) {
        this.serverNotificationManager = serverNotificationManager;
        this.network = network;
    }

    @Listener(order = Order.LAST)
    public void onPlayerJoin(final ClientConnectionEvent.Join event, @Getter("getTargetEntity") Player player) {

    }

    @Listener()
    public void onChangeClaim(final ChangeClaimEvent event, @Getter("getTargetEntity") Player player) {

    }

    @Listener()
    public void onCreateClaim(final CreateClaimEvent event, @Getter("getTargetEntity") Player player) {

    }

    @Listener()
    public void onDeleteClaim(final DeleteClaimEvent event, @Getter("getTargetEntity") Player player) {

    }

    @Listener()
    public void onTaxClaim(final TaxClaimEvent event, @Getter("getTargetEntity") Player player) {

    }

    @Listener()
    public void onClaimFlagChange(final FlagClaimEvent event, @Getter("getTargetEntity") Player player) {

    }
}
