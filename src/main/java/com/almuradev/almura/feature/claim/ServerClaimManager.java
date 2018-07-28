/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.claim;

import com.almuradev.almura.feature.claim.network.ClientboundClaimDataPacket;
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

    @Inject
    public ServerClaimManager(@ChannelId(NetworkConfig.CHANNEL) final ChannelBinding.IndexedMessageChannel network) {
        this.network = network;
    }

    public void buildUpdatePacket(final Player player, final Claim claim) {
        if (claim != null) {
            boolean isClaim = true;

            String claimName = "";
            String claimGreeting = "";
            String claimFarewell = "";
            String claimOwner = "";

            double claimEconBalance = 0.0;

            boolean isWilderness = claim.isWilderness();
            boolean isTownClaim = claim.isTown();
            boolean isAdminClaim = claim.isAdminClaim();
            boolean isBasicClaim = claim.isBasicClaim();
            boolean isSubdivision = claim.isSubdivision();

            if (claim.getOwnerName() != null)
                claimOwner = claim.getOwnerName().toPlain();
            if (claim.getData().getGreeting().isPresent())
                claimGreeting = claim.getData().getGreeting().get().toPlain();
            if (claim.getData().getFarewell().isPresent())
                claimFarewell = claim.getData().getFarewell().get().toPlain();
            if (claim.getName().isPresent()) {
                claimName = claim.getName().get().toPlain();

                final EconomyService service = Sponge.getServiceManager().provide(EconomyService.class).orElse(null);
                if (service != null) {
                    final Currency currency = service.getDefaultCurrency();
                    if (claim.getEconomyAccount().isPresent()) {
                        claimEconBalance = claim.getEconomyAccount().get().getBalance(currency).doubleValue();
                    }
                }
            }

            if (player != null) {
                this.network.sendTo(player, new ClientboundClaimDataPacket(isClaim, claimName, claimOwner, isWilderness, isTownClaim, isAdminClaim, isBasicClaim, isSubdivision, claimEconBalance, claimGreeting, claimFarewell));
            }

            for (final Player players : claim.getPlayers()) {  //Apparently claims.getPlayers() doesn't include the one that just entered or exited it.
                this.network.sendTo(players, new ClientboundClaimDataPacket(isClaim, claimName, claimOwner, isWilderness, isTownClaim, isAdminClaim, isBasicClaim, isSubdivision, claimEconBalance, claimGreeting, claimFarewell));
            }
        }
    }

    @Listener(order = Order.LAST)
    public void onPlayerJoin(final ClientConnectionEvent.Join event, @Getter("getTargetEntity") Player player) {
        buildUpdatePacket(null, GriefPrevention.getApi().getClaimManager(player.getWorld()).getClaimAt(player.getLocation()));
    }

    @Listener()
    public void onEnterExitClaim(final BorderClaimEvent event, @Getter("getTargetEntity") Player player) {
        // Notes:  this event does NOT fire when a player logs into the server.
        buildUpdatePacket(player, event.getEnterClaim());
    }

    @Listener()
    public void onChangeClaim(final ChangeClaimEvent event) {
        buildUpdatePacket(null, event.getClaim());
    }

    @Listener()
    public void onCreateClaim(final CreateClaimEvent event) {
        buildUpdatePacket(null, event.getClaim());
    }

    @Listener()
    public void onDeleteClaim(final DeleteClaimEvent event) {
        buildUpdatePacket(null, event.getClaim());
    }

    @Listener()
    public void onTaxClaim(final TaxClaimEvent event) {
        buildUpdatePacket(null, event.getClaim());
    }

    @Listener()
    public void onClaimFlagChange(final FlagClaimEvent event) {
        buildUpdatePacket(null, event.getClaim());
    }
}
