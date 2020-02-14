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
import com.griefdefender.GDPlayerData;
import com.griefdefender.GriefDefenderPlugin;
import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.claim.Claim;
import com.griefdefender.api.event.BorderClaimEvent;
import com.griefdefender.api.event.ChangeClaimEvent;
import com.griefdefender.api.event.CreateClaimEvent;
import com.griefdefender.api.event.FlagPermissionEvent;
import com.griefdefender.api.event.RemoveClaimEvent;
import com.griefdefender.api.event.TaxClaimEvent;
import com.griefdefender.configuration.GriefDefenderConfig;
import com.griefdefender.permission.GDPermissionUser;
import net.kyori.text.TextComponent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.server.FMLServerHandler;
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
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.UUID;

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
        if (GriefDefenderPlugin.getInstance() != null) {

            boolean isWorldOrilla = false;
            boolean isWorldAsgard = false;
            if (Sponge.getServer().getWorld("orilla").isPresent()) {
                isWorldOrilla = Sponge.getServer().getWorld("orilla").get().getUniqueId() == claim.getWorldUniqueId();
            }
            if (Sponge.getServer().getWorld("asgard").isPresent()) {
                isWorldOrilla = Sponge.getServer().getWorld("asgard").get().getUniqueId() == claim.getWorldUniqueId();
            }

            if (GriefDefenderPlugin.getInstance().getPermissionProvider() != null) {
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

                    if (player != null && GriefDefenderPlugin.getInstance().getWorldEditProvider() != null) {
                        hasWECUI = GriefDefenderPlugin.getInstance().getWorldEditProvider().hasCUISupport(player);
                    }

                    if (claim.getOwnerName() != null) {
                        claimOwner = claim.getOwnerName().toString();
                    }

                    if (claim.getData() != null && claim.getData().getGreeting().isPresent()) {
                        claimGreeting = claim.getData().getGreeting().get().toString();
                    }

                    if (claim.getData() != null &&claim.getData().getFarewell().isPresent()) {
                        claimFarewell = claim.getData().getFarewell().get().toString();
                    }

                    if (claim.getName().isPresent()) {
                        claimName = claim.getName().get().toString();

                        final EconomyService service = Sponge.getServiceManager().provide(EconomyService.class).orElse(null);
                        if (service != null && player != null) {
                            // Todo: implement the rest of the econ stuffz.
                            claimTaxes = this.claimTaxes(player, claim);
                            claimBlockCost = this.claimBlockCost(player, claim);
                            claimBlockSell = this.claimBlockSell(player, claim);
                            final Currency currency = service.getDefaultCurrency();

                            if (claim.getEconomyAccountId().isPresent()) {
                                UUID accountID = claim.getEconomyAccountId().orElse(null);
                                if (!(accountID == null)) {
                                    final UniqueAccount claimAccount = GriefDefenderPlugin.getInstance().economyService.get().getOrCreateAccount(accountID).orElse(null);
                                    claimEconBalance = claimAccount.getBalance(currency).doubleValue();
                                }
                            }
                        }
                    }

                    if (!claim.isWilderness()) {
                        claimSize = claim.getArea();
                    }

                    // Set custom Claim Name for Protected Area's
                    if (isWorldAsgard || isWorldOrilla) {
                        claimName = "Server Protected Area";
                    }


                    if (player != null) {
                        this.network.sendTo(player, new ClientboundClaimDataPacket(isClaim, claimName, claimOwner, isWilderness, isTownClaim, isAdminClaim, isBasicClaim, isSubdivision, claimEconBalance, claimGreeting, claimFarewell, claimSize,
                                isForSale, showWarnings, claimTaxes, claimBlockCost, claimBlockSell, hasWECUI));
                    }
                    if (everyone && !claim.isWilderness()) {
                        for (final UUID playersUUID : claim.getPlayers()) {  //Apparently claims.getPlayers() doesn't include the one that just entered or exited it.
                            this.network.sendTo((Player)FMLServerHandler.instance().getServer().getPlayerList().getPlayerByUUID(playersUUID),
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
                .execute(t -> this.sendUpdate(player, GriefDefender.getCore().getClaimManager(player.getWorld().getUniqueId()).getClaimAt(player.getPosition().toInt()), false))
                .submit(this.container);
        }
    }

    public void onEnterExitClaim(BorderClaimEvent event, @Getter("getTargetEntity") Player player) {
        // Notes:  this event does NOT fire when a player logs into the server.
        Task.builder()
            .delayTicks(10) // Give GP time to finish event
            .execute(t -> this.sendUpdate(player, GriefDefender.getCore().getClaimManager(player.getWorld().getUniqueId()).getClaimAt(player.getPosition().toInt()), false))
            .submit(this.container);
    }

    public void onChangeClaim(final ChangeClaimEvent event) {
        for (final UUID playersUUID : event.getClaim().getPlayers()) {
            Task.builder()
                .delayTicks(10) // Give GP time to finish event
                .execute(t -> this.sendUpdate((Player)FMLServerHandler.instance().getServer().getPlayerList().getPlayerByUUID(playersUUID), event.getClaim(), false))
                .submit(this.container);
        }
    }

    public void onCreateClaim(final CreateClaimEvent event) {
        for (final UUID playersUUID : event.getClaim().getPlayers()) {
            Task.builder()
                .delayTicks(10) // Give GP time to finish event
                .execute(t -> this.sendUpdate((Player)FMLServerHandler.instance().getServer().getPlayerList().getPlayerByUUID(playersUUID), event.getClaim(), false))
                .submit(this.container);
        }
    }

    public void onDeleteClaim(final RemoveClaimEvent event) {
        for (final UUID playersUUID : event.getClaim().getPlayers()) {
            Task.builder()
                .delayTicks(10) // Give GP time to finish event
                .execute(t -> this.sendUpdate((Player)FMLServerHandler.instance().getServer().getPlayerList().getPlayerByUUID(playersUUID), event.getClaim(), false))
                .submit(this.container);
        }
    }

    public void onTaxClaim(final TaxClaimEvent event) {
        /*
        for (final Player players : event.getClaim().getPlayers()) {
            Task.builder()
                .delayTicks(10) // Give GP time to finish event
                .execute(t -> this.sendUpdate(players, GriefPrevention.getApi().getClaimManager(players.getWorld()).getClaimAt(players.getLocation()), false))
                .submit(this.container);
        } */
    }

    public void onClaimFlagChange(final FlagPermissionEvent event) {
        for (final Player players : event.getClaim().getPlayers()) {
            Task.builder()
                .delayTicks(10) // Give GP time to finish event
                .execute(t -> this.sendUpdate(players, GriefDefender.getCore().getClaimManager(players.getWorld().getUniqueId()).getClaimAt(players.getPosition().toInt()), false))
                .submit(this.container);
        }
    }

    public final boolean isGPEnabled(final Player player) {
        if (GriefDefenderPlugin.getInstance() == null) {
            if (debugClaimManager)
                this.serverNotificationManager.sendPopupNotification(player, Text.of("Claim Manager"), Text.of("GriefPrevention is not initialized!"), 2);
            return false;
        } else {
            if (GriefDefenderPlugin.getInstance().getPermissionProvider() == null) {
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

        if (!GriefDefenderPlugin.getInstance().getWorldEditProvider().hasCUISupport(player)) {
            this.serverNotificationManager.sendPopupNotification(player, Text.of("Claim Manager"), Text.of("WECUI Mod not loaded!"), 2);
        }

        GDPlayerData playerData = GriefDefenderPlugin.getInstance().dataStore.getOrCreatePlayerData(player.getWorld(), player.getUniqueId());
        // The below is tested and verified working!
        if (value && !claim.isWilderness()) {
            GriefDefenderPlugin.getInstance().getWorldEditProvider().visualizeClaim(claim, player, playerData, false);
        } else {
            GriefDefenderPlugin.getInstance().getWorldEditProvider().revertVisuals(player, playerData, null);
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
                claim.getData().setName(TextComponent.builder(claimName).build());
                claim.getData().setGreeting(TextComponent.builder(claimGreeting).build());
                claim.getData().setFarewell(TextComponent.builder(claimFarewell).build());
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
            final Claim claim = GriefDefender.getCore().getClaimManager(player.getWorld().getUniqueId()).getClaimAt(location.getBlockPosition());
            if (claim != null) {
                return claim;
            }
        }
        return null;
    }

    public final double claimTaxes(final Player player, final Claim claim) {
        if (claim != null && player != null) {
            GDPlayerData playerData = GriefDefenderPlugin.getInstance().dataStore.getOrCreatePlayerData(player.getWorld(), player.getUniqueId());
            final GDPermissionUser subject = playerData.getSubject();
            UUID accountID = claim.getEconomyAccountId().orElse(null);
            if (!(accountID == null)) {
                final UniqueAccount claimAccount = GriefDefenderPlugin.getInstance().economyService.get().getOrCreateAccount(accountID).orElse(null);
                final double taxRate = GDOptionHandler.getClaimOptionDouble(subject, claim, GPOptions.Type.TAX_RATE, playerData);
                final double taxOwed = (claim.getClaimBlocks() / 256) * taxRate;
                return taxOwed;
            }

        } else {
            return 0;
        }
    }

    public final double claimBlockCost(final Player player, final Claim claim) {
        if (claim != null && player != null) {

            if (GriefDefenderPlugin.getInstance().isEconomyModeEnabled()) {
              //  GriefDefenderPlugin.getInstance().economyService.get().getOrCreateAccount(player.getUniqueId());
            }
            GriefDefenderConfig<?> activeConfig = GriefDefenderPlugin.getActiveConfig(player.getWorld().getProperties());
            return activeConfig.getConfig().economy.economyClaimBlockCost;
        } else {
            return 0.0;
        }
    }

    public final double claimBlockSell(final Player player, final Claim claim) {
        if (claim != null && player != null) {
            GriefDefenderPlugin.getInstance().economyService.get().getOrCreateAccount(player.getUniqueId());
            GriefDefenderConfig<?> activeConfig = GriefDefenderPlugin.getActiveConfig(player.getWorld().getProperties());
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

        final Claim claim = GriefDefender.getCore().getClaimManager(player.getWorld().getUniqueId()).getClaimAt(player.getPosition().toInt());
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

