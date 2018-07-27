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
import me.ryanhamshire.griefprevention.GriefPrevention;
import me.ryanhamshire.griefprevention.api.claim.Claim;
import me.ryanhamshire.griefprevention.api.event.BorderClaimEvent;
import me.ryanhamshire.griefprevention.api.event.ChangeClaimEvent;
import me.ryanhamshire.griefprevention.api.event.CreateClaimEvent;
import me.ryanhamshire.griefprevention.api.event.DeleteClaimEvent;
import me.ryanhamshire.griefprevention.api.event.FlagClaimEvent;
import me.ryanhamshire.griefprevention.api.event.TaxClaimEvent;
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
        this.notificationManager = notificationManager;
    }

    private void handleEvents(final Player player, final Claim claim) {
        if (claim == null) {
            System.out.println("ClaimManager.class: Null Claim passed into handleEvents");
            return;
        }

        final String claimOwner = claim.getOwnerName().toPlain();
        final boolean isWilderness = claim.isWilderness();
        final boolean isTownClaim = claim.isTown();
        final boolean isAdminClaim = claim.isAdminClaim();
        final boolean isBasicClaim = claim.isBasicClaim();
        final boolean is3dClaim = claim.isCuboid();

        String claimName;

        if (claim.getName().isPresent()) {
            claimName = claim.getName().get().toPlain();
        } else {
            claimName = "Claim Name not Set";
        }

        System.out.println("Player: [" + player.getName() + "] has enter claim: [" + claimName + "]");

        for (Player players : claim.getPlayers()) {
            //this.network.sendTo(player, new ClientboundClaimInfoPacket(claimName, claimOwner, isWilderness, isTown, isAdminClaim, isBasicClaim, is3dClaim));
        }
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
        System.out.println("ClaimManager.class: Claim changed");
    }

    @Listener()
    public void onCreateClaim(final CreateClaimEvent event) {
        handleEvents(null, event.getClaim());
        System.out.println("ClaimManager.class: Claim created");
    }

    @Listener()
    public void onDeleteClaim(final DeleteClaimEvent event) {
        handleEvents(null, event.getClaim());
        System.out.println("ClaimManager.class: Claim deleted");
    }

    @Listener()
    public void onTaxClaim(final TaxClaimEvent event) {
        handleEvents(null, event.getClaim());
    }

    @Listener()
    public void onClaimFlagChange(final FlagClaimEvent event) {
        handleEvents(null, event.getClaim());
        System.out.println("ClaimManager.class: Claim flag changed: " + event.getCause().toString());
    }
}
