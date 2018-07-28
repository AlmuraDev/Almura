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
import me.ryanhamshire.griefprevention.api.event.BorderClaimEvent;
import me.ryanhamshire.griefprevention.api.event.ChangeClaimEvent;
import me.ryanhamshire.griefprevention.api.event.CreateClaimEvent;
import me.ryanhamshire.griefprevention.api.event.DeleteClaimEvent;
import me.ryanhamshire.griefprevention.api.event.FlagClaimEvent;
import me.ryanhamshire.griefprevention.api.event.TaxClaimEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.EconomyService;

import javax.inject.Inject;

public final class ServerClaimManager implements Witness {

    private final ChannelBinding.IndexedMessageChannel network;
    private final ServerNotificationManager notificationManager;

    @Inject
    public ServerClaimManager(@ChannelId(NetworkConfig.CHANNEL) final ChannelBinding.IndexedMessageChannel network, final ServerNotificationManager notificationManager) {
        this.network = network;
        this.notificationManager = notificationManager; // Not used at the moment but will in the future.
    }

    private void handleEvents(final Player player, final Claim claim) {
        double claimEconBalance = 0.0;
        boolean isClaim = false;
        String claimName = "";
        boolean isWilderness = false;
        boolean isTownClaim = false;
        boolean isAdminClaim = false;
        boolean isBasicClaim = false;
        boolean isSubdivision = false;

        boolean isOwner = false;
        boolean isTrusted = false;

        if (claim != null) {
            isClaim = true;
            isWilderness = claim.isWilderness();
            isTownClaim = claim.isTown();
            isAdminClaim = claim.isAdminClaim();
            isBasicClaim = claim.isBasicClaim();
            isSubdivision = claim.isSubdivision();
            isOwner = claim.getOwnerUniqueId().equals(player.getUniqueId());
            isTrusted = claim.isTrusted(player.getUniqueId());

            final EconomyService service = Sponge.getServiceManager().provide(EconomyService.class).orElse(null);
            if (service != null) {
                final Currency currency = service.getDefaultCurrency();
                if (claim.getEconomyAccount().isPresent()) {
                    claimEconBalance = claim.getEconomyAccount().get().getBalance(currency).doubleValue();
                }
            }

            if (claim.getName().isPresent()) {
                claimName = claim.getName().get().toPlain();
            } else {
                claimName = "claim name not set";
            }
            System.out.println("Claim Name: " + claimName);
        }

        System.out.println("Executed Packet Build from Server");

        if (player != null) {
            this.network.sendTo(player, new ClientboundClaimNamePacket(isClaim, claimName, isWilderness, isTownClaim, isAdminClaim, isBasicClaim, isSubdivision));
        }

        for (final Player players : claim.getPlayers()) {  //Apparently claims.getPlayers() doesn't include the one that just entered or exited it.
            this.network.sendTo(players, new ClientboundClaimNamePacket(isClaim, claimName, isWilderness, isTownClaim, isAdminClaim, isBasicClaim, isSubdivision));
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
        System.out.println("Entered: " + event.getEnterClaim().getName());
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
