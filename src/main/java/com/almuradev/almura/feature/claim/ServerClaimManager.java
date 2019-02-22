/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.claim;

import com.almuradev.almura.Almura;
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
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import javax.inject.Inject;

public final class ServerClaimManager implements Witness {

    private final ChannelBinding.IndexedMessageChannel network;
    private final ServerNotificationManager serverNotificationManager;
    private final PluginContainer container;
    private boolean debugClaimManager = false;

    @Inject
    public ServerClaimManager(@ChannelId(NetworkConfig.CHANNEL) final ChannelBinding.IndexedMessageChannel network, final
    ServerNotificationManager notificationManager, final PluginContainer container) {
        this.network = network;
        this.serverNotificationManager = notificationManager;
        this.container = container;
    }

    public void sendUpdate(final Player player, final Claim claim, boolean everyone) {
        if (GriefPreventionPlugin.instance != null) {
            if (GriefPreventionPlugin.instance.permissionService != null) {
                if (claim != null) {
                    boolean isClaim = true;
                    boolean hasWECUI = false;
                    boolean isForSale = false;
                    boolean showWarnings = true;

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

                    if (claim.isWilderness()) {
                        isForSale = claim.getEconomyData().isForSale();
                    }

                    if (claim.getData() != null) {
                        showWarnings = claim.getData().allowDenyMessages();
                    }

                    if (player != null && GriefPreventionPlugin.instance.worldEditProvider != null) {
                        hasWECUI = GriefPreventionPlugin.instance.worldEditProvider.hasCUISupport(player);
                    }

                    if (claim.getOwnerName() != null) {
                        claimOwner = claim.getOwnerName().toPlain();
                    }

                    if (claim.getData() != null && claim.getData().getGreeting().isPresent()) {
                        claimGreeting = claim.getData().getGreeting().get().toPlain();
                    }

                    if (claim.getData() != null &&claim.getData().getFarewell().isPresent()) {
                        claimFarewell = claim.getData().getFarewell().get().toPlain();
                    }

                    if (claim.getName().isPresent()) {
                        claimName = claim.getName().get().toPlain();

                        final EconomyService service = Sponge.getServiceManager().provide(EconomyService.class).orElse(null);
                        if (service != null && player != null) {
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

                    // Set custom Claim Name for Protected Area's
                    if (claim.isWilderness() && (claim.getWorld().getName().equalsIgnoreCase("orilla") || claim.getWorld().getName().equalsIgnoreCase("asgard"))) {
                        claimName = "Server Protected Area";
                    }


                    if (player != null) {
                        this.network.sendTo(player, new ClientboundClaimDataPacket(isClaim, claimName, claimOwner, isWilderness, isTownClaim, isAdminClaim, isBasicClaim, isSubdivision, claimEconBalance, claimGreeting, claimFarewell, claimSize,
                                isForSale, showWarnings, claimTaxes, claimBlockCost, claimBlockSell, hasWECUI));
                    }
                    if (everyone && !claim.isWilderness()) {
                        for (final Player players : claim.getPlayers()) {  //Apparently claims.getPlayers() doesn't include the one that just entered or exited it.
                            this.network.sendTo(players,
                                new ClientboundClaimDataPacket(isClaim, claimName, claimOwner, isWilderness, isTownClaim, isAdminClaim, isBasicClaim, isSubdivision, claimEconBalance, claimGreeting, claimFarewell, claimSize, isForSale, showWarnings, claimTaxes, claimBlockCost, claimBlockSell,
                                    hasWECUI));
                        }
                    }
                }
            }
        }
    }

    @Listener(order = Order.LAST)
    public void onPlayerJoin(final ClientConnectionEvent.Join event, @Getter("getTargetEntity") Player player) {
        if (player != null && this.isGPEnabled(player)) {
            Task.builder()
                .delayTicks(20) // Give GP time to register the user as its not at tick zero.
                .execute(t -> this.sendUpdate(player, GriefPrevention.getApi().getClaimManager(player.getWorld()).getClaimAt(player.getLocation()), false))
                .submit(this.container);
        }
    }

    @Listener()
    public void onEnterExitClaim(final BorderClaimEvent event, @Getter("getTargetEntity") Player player) {
        // Notes:  this event does NOT fire when a player logs into the server.
        Task.builder()
            .delayTicks(10) // Give GP time to finish event
            .execute(t -> this.sendUpdate(player, GriefPrevention.getApi().getClaimManager(player.getWorld()).getClaimAt(player.getLocation()), false))
            .submit(this.container);
    }

    @Listener()
    public void onChangeClaim(final ChangeClaimEvent event) {
        for (final Player players : event.getClaim().getPlayers()) {
            Task.builder()
                .delayTicks(10) // Give GP time to finish event
                .execute(t -> this.sendUpdate(players, GriefPrevention.getApi().getClaimManager(players.getWorld()).getClaimAt(players.getLocation()), false))
                .submit(this.container);
        }
    }

    @Listener()
    public void onCreateClaim(final CreateClaimEvent event) {
        for (final Player players : event.getClaim().getPlayers()) {
            Task.builder()
                .delayTicks(10) // Give GP time to finish event
                .execute(t -> this.sendUpdate(players, GriefPrevention.getApi().getClaimManager(players.getWorld()).getClaimAt(players.getLocation()), false))
                .submit(this.container);
        }
    }

    @Listener()
    public void onDeleteClaim(final DeleteClaimEvent event) {
        for (final Player players : event.getClaim().getPlayers()) {
            Task.builder()
                .delayTicks(10) // Give GP time to finish event
                .execute(t -> this.sendUpdate(players, GriefPrevention.getApi().getClaimManager(players.getWorld()).getClaimAt(players.getLocation()), false))
                .submit(this.container);
        }
    }

    @Listener()
    public void onTaxClaim(final TaxClaimEvent event) {
        /*
        for (final Player players : event.getClaim().getPlayers()) {
            Task.builder()
                .delayTicks(10) // Give GP time to finish event
                .execute(t -> this.sendUpdate(players, GriefPrevention.getApi().getClaimManager(players.getWorld()).getClaimAt(players.getLocation()), false))
                .submit(this.container);
        } */
    }

    @Listener()
    public void onClaimFlagChange(final FlagClaimEvent event) {
        for (final Player players : event.getClaim().getPlayers()) {
            Task.builder()
                .delayTicks(10) // Give GP time to finish event
                .execute(t -> this.sendUpdate(players, GriefPrevention.getApi().getClaimManager(players.getWorld()).getClaimAt(players.getLocation()), false))
                .submit(this.container);
        }
    }

    public final boolean isGPEnabled(final Player player) {
        if (GriefPreventionPlugin.instance == null) {
            if (debugClaimManager)
                this.serverNotificationManager.sendPopupNotification(player, Text.of("Claim Manager"), Text.of("GriefPrevention is not initialized!"), 2);
            return false;
        } else {
            if (GriefPreventionPlugin.instance.permissionService == null) {
                if (debugClaimManager)
                    this.serverNotificationManager.sendPopupNotification(player, Text.of("Claim Manager"), Text.of("GriefPrevention is not enabled!"), 2);
                return false;
            }
        }
        return true;
    }

    public void toggleVisuals(final Player player, final Claim claim, final boolean value) {
        if (!isGPEnabled(player)) {
            return;
        }

        if (!GriefPreventionPlugin.instance.worldEditProvider.hasCUISupport(player)) {
            this.serverNotificationManager.sendPopupNotification(player, Text.of("Claim Manager"), Text.of("WECUI Mod not loaded!"), 2);
        }

        GPPlayerData playerData = GriefPreventionPlugin.instance.dataStore.getOrCreatePlayerData(player.getWorld(), player.getUniqueId());
        // The below is tested and verified working!
        if (value && !claim.isWilderness()) {
            GriefPreventionPlugin.instance.worldEditProvider.visualizeClaim(claim, player, playerData, false);
        } else {
            GriefPreventionPlugin.instance.worldEditProvider.revertVisuals(player, playerData, null);
        }
    }

    public void saveChanges(final Player player, final String claimName, final String claimGreeting, final String claimFarewell, final double x, final double y, final double z, final String worldName) {
        if (!isGPEnabled(player))
            return;

        final Claim claim = this.claimLookup(player, x, y, z, worldName);
        if (claim != null) {
            final boolean isOwner = (claim.getOwnerUniqueId().equals(player.getUniqueId()));
            final boolean isAdmin = player.hasPermission("griefprevention.admin");

            if (isOwner || isAdmin) {
                claim.getData().setName(Text.of(claimName));
                claim.getData().setGreeting(Text.of(claimGreeting));
                claim.getData().setFarewell(Text.of(claimFarewell));
                this.sendUpdate(player, claim, true);
                this.serverNotificationManager.sendPopupNotification(player, Text.of("Claim Manager"), Text.of("Changed Saved!"), 5);
            } else {
                this.serverNotificationManager.sendPopupNotification(player, Text.of("Claim Manager"), Text.of("Insufficient Permissions!"), 5);
            }
        } else {
            this.serverNotificationManager
                .sendPopupNotification(player, Text.of("Claim Manager"), Text.of("Unable to lookup Claim, changed not saved!"), 5);
        }
    }

    public final Claim claimLookup(final Player player, final double x, final double y, final double z, final String worldName) {
        final World world = Sponge.getServer().getWorld(worldName).orElse(null);
        if (world == null) {
            this.serverNotificationManager
                .sendPopupNotification(player, Text.of("Claim Manager"), Text.of("Unable to find world, changes not saved!"), 5);
            return null;
        }
        final Location<World> location = new Location<>(world, x, y, z);
        if (location == null) {
            this.serverNotificationManager
                .sendPopupNotification(player, Text.of("Claim Manager"), Text.of("Invalid location sent to server.  Changes not saved!"), 5);
        } else {
            final Claim claim = GriefPrevention.getApi().getClaimManager(player.getWorld()).getClaimAt(location);
            if (claim != null) {
                return claim;
            }
        }
        return null;
    }

    public final double claimTaxes(final Player player, final Claim claim) {
        if (claim != null && player != null) {
            GPPlayerData playerData = GriefPreventionPlugin.instance.dataStore.getOrCreatePlayerData(player.getWorld(), player.getUniqueId());
            final Subject subject = playerData.getPlayerSubject();
            final Account claimAccount = claim.getEconomyAccount().orElse(null);
            final double taxRate = GPOptionHandler.getClaimOptionDouble(subject, claim, GPOptions.Type.TAX_RATE, playerData);
            final double taxOwed = (claim.getClaimBlocks() / 256) * taxRate;
            return taxOwed;
        } else {
            return 0;
        }
    }

    public final double claimBlockCost(final Player player, final Claim claim) {
        if (claim != null && player != null) {
            GriefPreventionPlugin.instance.economyService.get().getOrCreateAccount(player.getUniqueId());
            GriefPreventionConfig<?> activeConfig = GriefPreventionPlugin.getActiveConfig(player.getWorld().getProperties());
            return activeConfig.getConfig().economy.economyClaimBlockCost;
        } else {
            return 0.0;
        }
    }

    public final double claimBlockSell(final Player player, final Claim claim) {
        if (claim != null && player != null) {
            GriefPreventionPlugin.instance.economyService.get().getOrCreateAccount(player.getUniqueId());
            GriefPreventionConfig<?> activeConfig = GriefPreventionPlugin.getActiveConfig(player.getWorld().getProperties());
            return activeConfig.getConfig().economy.economyClaimBlockSell;
        } else {
            return 0.0;
        }
    }

    public void abandonClaim(final Player player, final Claim claim) {
        if (claim != null && player != null) {
            claim.getClaimManager().deleteClaim(claim);
        }
    }

    public void toggleClaimDenyMessages(final Player player, final Claim claim, final boolean value) {
        if (claim != null && player != null) {
            claim.getData().setDenyMessages(value);
        }
    }

    public void openClientGUI (final Player player){
        if (!isGPEnabled(player)) {
            return;
        }

        if (!player.hasPermission(Almura.ID + ".claim.base")) {
            this.serverNotificationManager.sendPopupNotification(player, Text.of("Claim Manager"), Text.of("Insufficient Permissions to Manage Claim!"), 2);
            return;
        }

        final Claim claim = GriefPrevention.getApi().getClaimManager(player.getWorld()).getClaimAt(player.getLocation());
        if (claim != null) { // if GP is loaded, claim should never be null.
            final boolean isOwner = (claim.getOwnerUniqueId().equals(player.getUniqueId()));
            final boolean isTrusted = claim.isTrusted(player.getUniqueId());
            final boolean isAdmin = player.hasPermission("griefprevention.admin");
            if (!isAdmin && claim.isWilderness()) {
                this.serverNotificationManager
                    .sendPopupNotification(player, Text.of("Claim Manager"), Text.of("Insufficient permissions to open Claim Manager in Wilderness"),
                        5);
            } else {
                this.network.sendTo(player, new ClientboundClaimGuiResponsePacket(isOwner, isTrusted, isAdmin));
            }
        }
    }
}

