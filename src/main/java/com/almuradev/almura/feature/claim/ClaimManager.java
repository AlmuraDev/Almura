/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.claim;

import com.almuradev.almura.feature.claim.network.ClientboundClaimNamePacket;
import com.almuradev.almura.feature.notification.ServerNotificationManager;
import com.almuradev.almura.shared.network.NetworkConfig;
import com.almuradev.core.event.Witness;
import me.ryanhamshire.griefprevention.GriefPrevention;
import me.ryanhamshire.griefprevention.api.claim.Claim;
import me.ryanhamshire.griefprevention.api.event.*;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;

import javax.inject.Inject;

public final class ClaimManager implements Witness {

    private final ChannelBinding.IndexedMessageChannel network;
    private final ServerNotificationManager notificationManager;

    @Inject
    public ClaimManager(@ChannelId(NetworkConfig.CHANNEL) final ChannelBinding.IndexedMessageChannel network, final ServerNotificationManager notificationManager) {
        this.network = network;
        this.notificationManager = notificationManager; // Not used at the moment but will in the future.
    }

    private void handleEvents(final Player player, final Claim claim) {
        if (claim == null) {
            return; // If claim is null then the HUDdata will just show "Wilderness".
        }

        claim.getName().ifPresent(c -> {
            final String claimName = claim.getName().get().toPlain();
            for (final Player players : claim.getPlayers()) {
                this.network.sendTo(players, new ClientboundClaimNamePacket(claimName));
            }
        });
    }

    @Listener(order = Order.LAST)
    public void onPlayerJoin(final ClientConnectionEvent.Join event, @Getter("getTargetEntity") Player player) {
        handleEvents(null, GriefPrevention.getApi().getClaimManager(player.getWorld()).getClaimAt(player.getLocation()));
    }

    @Listener()
    public void onEnterExitClaim(final BorderClaimEvent event, @Getter("getTargetEntity") Player player) {
        // Notes:  this event does NOT fire when a player logs into the server.
        handleEvents(player, event.getEnterClaim());
    }

    @Listener()
    public void onChangeClaim(final ChangeClaimEvent event) {
        handleEvents(null, event.getClaim());
    }

    @Listener()
    public void onCreateClaim(final CreateClaimEvent event) {
        handleEvents(null, event.getClaim());
    }

    @Listener()
    public void onDeleteClaim(final DeleteClaimEvent event) {
        handleEvents(null, event.getClaim());
    }

    @Listener()
    public void onTaxClaim(final TaxClaimEvent event) {
        handleEvents(null, event.getClaim());
    }

    @Listener()
    public void onClaimFlagChange(final FlagClaimEvent event) {
        handleEvents(null, event.getClaim());
    }
}
