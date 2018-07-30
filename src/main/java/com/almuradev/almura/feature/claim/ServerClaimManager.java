/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.claim;

import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.almura.feature.claim.network.ClientboundClaimDataPacket;
import com.almuradev.almura.feature.claim.network.ClientboundClaimGuiResponsePacket;
import com.almuradev.almura.feature.notification.ServerNotificationManager;
import com.almuradev.almura.shared.network.NetworkConfig;
import com.almuradev.core.event.Witness;
import me.ryanhamshire.griefprevention.GPPlayerData;
import me.ryanhamshire.griefprevention.GriefPrevention;
import me.ryanhamshire.griefprevention.GriefPreventionPlugin;
import me.ryanhamshire.griefprevention.api.claim.Claim;
import me.ryanhamshire.griefprevention.api.event.BorderClaimEvent;
import me.ryanhamshire.griefprevention.api.event.ChangeClaimEvent;
import me.ryanhamshire.griefprevention.api.event.CreateClaimEvent;
import me.ryanhamshire.griefprevention.api.event.DeleteClaimEvent;
import me.ryanhamshire.griefprevention.api.event.FlagClaimEvent;
import me.ryanhamshire.griefprevention.api.event.TaxClaimEvent;
import me.ryanhamshire.griefprevention.configuration.GriefPreventionConfig;
import me.ryanhamshire.griefprevention.permission.GPOptionHandler;
import me.ryanhamshire.griefprevention.permission.GPOptions;
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
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import javax.annotation.Nullable;
import javax.inject.Inject;

public final class ServerClaimManager implements Witness {

    private final ChannelBinding.IndexedMessageChannel network;
    private final ServerNotificationManager notificationManager;

    @Inject
    public ServerClaimManager(@ChannelId(NetworkConfig.CHANNEL) final ChannelBinding.IndexedMessageChannel network, final
    ServerNotificationManager notificationManager) {
        this.network = network;
        this.notificationManager = notificationManager;
    }

    @Listener(order = Order.LAST)
    public void onPlayerJoin(final ClientConnectionEvent.Join event, @Getter("getTargetEntity") final Player player) {
        this.sendUpdatePacket(null, GriefPrevention.getApi().getClaimManager(player.getWorld()).getClaimAt(player.getLocation()));
        //sendUpdatePacket(null, GriefPreventionPlugin.instance.dataStore.getClaimAt(player.getLocation()));
    }

    @Listener()
    public void onEnterExitClaim(final BorderClaimEvent event, @Getter("getTargetEntity") final Player player) {
        // Notes:  this event does NOT fire when a player logs into the server.
        this.sendUpdatePacket(player, event.getEnterClaim());
    }

    @Listener()
    public void onChangeClaim(final ChangeClaimEvent event) {
        this.sendUpdatePacket(null, event.getClaim());
    }

    @Listener()
    public void onCreateClaim(final CreateClaimEvent event) {
        this.sendUpdatePacket(null, event.getClaim());
    }

    @Listener()
    public void onDeleteClaim(final DeleteClaimEvent event) {
        this.sendUpdatePacket(null, event.getClaim());
    }

    @Listener()
    public void onTaxClaim(final TaxClaimEvent event) {
        this.sendUpdatePacket(null, event.getClaim());
    }

    @Listener()
    public void onClaimFlagChange(final FlagClaimEvent event) {
        this.sendUpdatePacket(null, event.getClaim());
    }

    public void sendUpdatePacket(@Nullable final Player player, final Claim claim) {
        checkNotNull(claim);

        if (GriefPreventionPlugin.instance != null && GriefPreventionPlugin.instance.permissionService != null) {
            boolean isClaim = true;

            String claimName = "";
            String claimGreeting = "";
            String claimFarewell = "";
            String claimOwner = "";

            double claimEconBalance = 0.0;
            double claimTaxes = 0.0;
            double claimBlockCost = 0.0;
            double claimBlockSell = 0.0;
            int claimSize = 0;

            final boolean isWilderness = claim.isWilderness();
            final boolean isTownClaim = claim.isTown();
            final boolean isAdminClaim = claim.isAdminClaim();
            final boolean isBasicClaim = claim.isBasicClaim();
            final boolean isSubdivision = claim.isSubdivision();
            final boolean isForSale = claim.getEconomyData().isForSale();
            final boolean showWarnings = claim.getData().allowDenyMessages();

            if (claim.getOwnerName() != null) {
                claimOwner = claim.getOwnerName().toPlain();
            }
            if (claim.getData().getGreeting().isPresent()) {
                claimGreeting = claim.getData().getGreeting().get().toPlain();
            }
            if (claim.getData().getFarewell().isPresent()) {
                claimFarewell = claim.getData().getFarewell().get().toPlain();
            }
            if (claim.getName().isPresent()) {
                claimName = claim.getName().get().toPlain();

                final EconomyService service = Sponge.getServiceManager().provide(EconomyService.class).orElse(null);
                if (service != null) {
                    // Todo: implement the rest of the econ stuffz.
                    claimTaxes = this.claimTaxes(player, claim);
                    claimBlockCost = this.claimBlockCost(player, claim);
                    claimBlockSell = this.claimBlockSell(player, claim);
                    final Currency currency = service.getDefaultCurrency();
                    if (claim.getEconomyAccount().isPresent()) {
                        claimEconBalance = claim.getEconomyAccount().get().getBalance(currency).doubleValue();
                    }
                }
            }

            if (!claim.isWilderness()) {
                claimSize = claim.getArea();
            }

            if (player != null) {
                this.network.sendTo(player,
                    new ClientboundClaimDataPacket(isClaim, claimName, claimOwner, isWilderness, isTownClaim, isAdminClaim, isBasicClaim,
                        isSubdivision, claimEconBalance, claimGreeting, claimFarewell, claimSize,
                        isForSale, showWarnings, claimTaxes, claimBlockCost, claimBlockSell));
            }

            for (final Player players : claim
                .getPlayers()) {  //Apparently claims.getPlayers() doesn't include the one that just entered or exited it.
                this.network.sendTo(players,
                    new ClientboundClaimDataPacket(isClaim, claimName, claimOwner, isWilderness, isTownClaim, isAdminClaim, isBasicClaim,
                        isSubdivision, claimEconBalance, claimGreeting, claimFarewell, claimSize,
                        isForSale, showWarnings, claimTaxes, claimBlockCost, claimBlockSell));
            }
        }
    }

    private boolean isGPEnabled(final Player player) {
        if (GriefPreventionPlugin.instance == null) {
            this.notificationManager.sendPopupNotification(player, Text.of("Claim Manager"), Text.of("GriefPrevention is not initialized!"), 2);
            return false;
        } else {
            if (GriefPreventionPlugin.instance.permissionService == null) {
                this.notificationManager.sendPopupNotification(player, Text.of("Claim Manager"), Text.of("GriefPrevention is not enabled!"), 2);
                return false;
            }
        }
        return true;
    }

    // This is not ready to be used yet...
    public void toggleVisualization(final Player player, final Claim claim, final boolean show) {
        if (!this.isGPEnabled(player)) {
            return;
        }

        GPPlayerData playerData = GriefPreventionPlugin.instance.dataStore.getOrCreatePlayerData(player.getWorld(), player.getUniqueId());
        if (show) {
            GriefPreventionPlugin.instance.worldEditProvider.visualizeClaim(claim, player, playerData, false);
        } else {
            GriefPreventionPlugin.instance.worldEditProvider.revertVisuals(player, playerData, null);
        }

    }

    public void saveClaimChanges(final Player player, final String claimName, final String claimGreeting, final String claimFarewell, final double x,
        final double y, final double z, final String worldName) {
        if (!this.isGPEnabled(player)) {
            return;
        }

        final Claim claim = this.claimLookup(player, x, y, z, worldName);
        if (claim == null) {
            this.notificationManager
                .sendPopupNotification(player, Text.of("Claim Manager"), Text.of("Unable to lookup Claim, changed not saved!"), 5);
        } else {
            final boolean isOwner = claim.getOwnerUniqueId().equals(player.getUniqueId());
            final boolean isAdmin = player.hasPermission("griefprevention.admin");

            if (!isOwner && !isAdmin) {
                this.notificationManager.sendPopupNotification(player, Text.of("Claim Manager"), Text.of("Insufficient Permissions!"), 5);
            } else {
                claim.getData().setName(Text.of(claimName));
                claim.getData().setGreeting(Text.of(claimGreeting));
                claim.getData().setFarewell(Text.of(claimFarewell));
                this.sendUpdatePacket(player, claim);
                this.notificationManager.sendPopupNotification(player, Text.of("Claim Manager"), Text.of("Changed Saved!"), 5);
            }
        }
    }

    private Claim claimLookup(final Player player, final double x, final double y, final double z, final String worldName) {
        checkNotNull(player);
        checkNotNull(worldName);

        final World world = Sponge.getServer().getWorld(worldName).orElse(null);

        if (world == null) {
            this.notificationManager.sendPopupNotification(player, Text.of("Claim Manager"), Text.of("Unable to find world, changes not saved!"), 5);
            return null;
        }

        final Location<World> location = new Location<>(world, x, y, z);
        final Claim claim = GriefPrevention.getApi().getClaimManager(world).getClaimAt(location);
        return claim;
    }

    private double claimTaxes(final Player player, final Claim claim) {
        checkNotNull(player);
        checkNotNull(claim);

        final GPPlayerData playerData = GriefPreventionPlugin.instance.dataStore.getOrCreatePlayerData(player.getWorld(), player.getUniqueId());
        final Subject subject = playerData.getPlayerSubject();
        final Account claimAccount = claim.getEconomyAccount().orElse(null);
        final double taxRate = GPOptionHandler.getClaimOptionDouble(subject, claim, GPOptions.Type.TAX_RATE, playerData);
        final double taxOwed = (claim.getClaimBlocks() / 256) * taxRate;
        return taxOwed;
    }

    private double claimBlockCost(final Player player, final Claim claim) {
        checkNotNull(player);
        checkNotNull(claim);
        final EconomyService economyService = GriefPreventionPlugin.instance.economyService.orElse(null);
        checkNotNull(economyService);

        // TODO Did you mean to create the account here Dockter? This call isn't stored nor used
        economyService.getOrCreateAccount(player.getUniqueId());
        final GriefPreventionConfig<?> activeConfig = GriefPreventionPlugin.getActiveConfig(player.getWorld().getProperties());
        return activeConfig.getConfig().economy.economyClaimBlockCost;
    }

    private double claimBlockSell(final Player player, final Claim claim) {
        checkNotNull(player);
        checkNotNull(claim);
        final EconomyService economyService = GriefPreventionPlugin.instance.economyService.orElse(null);
        checkNotNull(economyService);

        // TODO Did you mean to create the account here Dockter? This call isn't stored nor used
        economyService.getOrCreateAccount(player.getUniqueId());
        final GriefPreventionConfig<?> activeConfig = GriefPreventionPlugin.getActiveConfig(player.getWorld().getProperties());
        return activeConfig.getConfig().economy.economyClaimBlockSell;
    }

    public void openClientGUI(final Player player) {
        if (!this.isGPEnabled(player)) {
            return;
        }

        final Claim claim = GriefPrevention.getApi().getClaimManager(player.getWorld()).getClaimAt(player.getLocation());
        if (claim != null) { // if GP is loaded, claim should never be null.
            final boolean isOwner = (claim.getOwnerUniqueId().equals(player.getUniqueId()));
            final boolean isTrusted = claim.isTrusted(player.getUniqueId());
            final boolean isAdmin = player.hasPermission("griefprevention.admin");
            this.network.sendTo(player, new ClientboundClaimGuiResponsePacket(isOwner, isTrusted, isAdmin));
        }
    }
}

