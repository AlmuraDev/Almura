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
import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.claim.Claim;
import com.griefdefender.api.data.PlayerData;
import com.griefdefender.api.event.BorderClaimEvent;
import com.griefdefender.api.event.ChangeClaimEvent;
import com.griefdefender.api.event.CreateClaimEvent;
import com.griefdefender.api.event.FlagPermissionEvent;
import com.griefdefender.api.event.RemoveClaimEvent;
import com.griefdefender.api.event.TaxClaimEvent;
import com.griefdefender.api.permission.Context;
import net.kyori.event.method.annotation.Subscribe;
import net.kyori.text.TextComponent;
import net.kyori.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraftforge.fml.server.FMLServerHandler;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.game.state.GameAboutToStartServerEvent;
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
import javax.inject.Singleton;

@Singleton
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

    @Listener
    public void serverStarted(final GameAboutToStartServerEvent event) {
        //System.out.println("Hello Murray2");
        //GriefDefender.getEventManager().register(this);
    }

    public void sendUpdate(final Player player, final Claim claim, boolean everyone) {
        if (GriefDefender.getCore() != null) {
            boolean isWorldOrilla = false;
            boolean isWorldAsgard = false;
            if (Sponge.getServer().getWorld("orilla").isPresent()) {
                isWorldOrilla = Sponge.getServer().getWorld("orilla").get().getUniqueId() == claim.getWorldUniqueId();
            }
            if (Sponge.getServer().getWorld("asgard").isPresent()) {
                isWorldOrilla = Sponge.getServer().getWorld("asgard").get().getUniqueId() == claim.getWorldUniqueId();
            }
            if (GriefDefender.getPermissionManager() != null) {
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

                    if (player != null && GriefDefender.getCore().getWorldEditProvider() != null) {
                        hasWECUI = GriefDefender.getCore().getWorldEditProvider().hasCUISupport(player.getUniqueId());
                    }

                    if (claim.getOwnerName() != null) {
                        claimOwner = claim.getOwnerName();
                    }

                    if (claim.getData() != null && claim.getData().getGreeting().isPresent()) {
                        claimGreeting = LegacyComponentSerializer.legacy().serialize(claim.getData().getGreeting().get());
                    }

                    if (claim.getData() != null &&claim.getData().getFarewell().isPresent()) {
                        claimFarewell = LegacyComponentSerializer.legacy().serialize(claim.getData().getFarewell().get());
                    }

                    if (claim.getName().isPresent()) {
                        claimName = LegacyComponentSerializer.legacy().serialize(claim.getName().get());

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
                                    final UniqueAccount claimAccount = service.getOrCreateAccount(accountID).orElse(null);
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
                            this.network.sendTo((Player) FMLServerHandler.instance().getServer().getPlayerList().getPlayerByUUID(playersUUID),
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
                .execute(t -> this.sendUpdate(player, GriefDefender.getCore().getClaimManager(player.getWorld().getUniqueId()).getClaimAt(player.getLocation().getPosition().toInt()), false))
                .submit(this.container);
        }
    }

    @Subscribe
    public void onEnterExitClaim(BorderClaimEvent event) {
        // Notes:  this event does NOT fire when a player logs into the server.
        System.out.println("Enter Claim Event");
        Task.builder()
            .delayTicks(10) // Give GP time to finish event
            .execute(t -> this.sendUpdate((Player)FMLServerHandler.instance().getServer().getPlayerList().getPlayerByUUID(event.getEntityUniqueId()), event.getClaim(), false))
            .submit(this.container);
    }

    @Subscribe
    public void onChangeClaim(final ChangeClaimEvent event) {
        for (final UUID playersUUID : event.getClaim().getPlayers()) {
            Task.builder()
                .delayTicks(10) // Give GP time to finish event
                .execute(t -> this.sendUpdate((Player)FMLServerHandler.instance().getServer().getPlayerList().getPlayerByUUID(playersUUID), event.getClaim(), false))
                .submit(this.container);
        }
    }

    @Subscribe
    public void onCreateClaim(final CreateClaimEvent event) {
        for (final UUID playersUUID : event.getClaim().getPlayers()) {
            Task.builder()
                .delayTicks(10) // Give GP time to finish event
                .execute(t -> this.sendUpdate((Player)FMLServerHandler.instance().getServer().getPlayerList().getPlayerByUUID(playersUUID), event.getClaim(), false))
                .submit(this.container);
        }
    }

    @Subscribe
    public void onDeleteClaim(final RemoveClaimEvent event) {
        for (final UUID playersUUID : event.getClaim().getPlayers()) {
            Task.builder()
                .delayTicks(10) // Give GP time to finish event
                .execute(t -> this.sendUpdate((Player)FMLServerHandler.instance().getServer().getPlayerList().getPlayerByUUID(playersUUID), event.getClaim(), false))
                .submit(this.container);
        }
    }

    @Subscribe
    public void onTaxClaim(final TaxClaimEvent event) {
        /*
        for (final Player players : event.getClaim().getPlayers()) {
            Task.builder()
                .delayTicks(10) // Give GP time to finish event
                .execute(t -> this.sendUpdate(players, GriefPrevention.getApi().getClaimManager(players.getWorld()).getClaimAt(players.getLocation()), false))
                .submit(this.container);
        } */
    }

    @Subscribe
    public void onClaimFlagChange(final FlagPermissionEvent event) {
        Claim claim = null;
        for (Context context : event.getContexts()) {
            if (context.getKey().contains("gd_claim") || context.getKey().equals("server")) {
                claim = GriefDefender.getCore().getClaim(UUID.fromString(context.getValue()));

            }
        }
        System.out.println("FlagPermissionEvent detected");

        if (claim != null) {
            System.out.println("Claim: " + claim.getOwnerName());
            final Claim finalClaim = claim;
            for (UUID playersUUID : claim.getPlayers()) {
                Task.builder().delayTicks(10) // Give GP time to finish event
                    .execute(t -> this.sendUpdate((Player)FMLServerHandler.instance().getServer().getPlayerList().getPlayerByUUID(playersUUID), finalClaim, false))
                    .submit(this.container);
            }
        } else {
            System.out.println("Claim is null");
        }
    }

    public final boolean isGPEnabled(final Player player) {
        // Todo: this needs to be rebuilt.
        return true;
    }

    public void toggleVisuals(final Player player, final Claim claim, final boolean value) {
        if (!isGPEnabled(player) || GriefDefender.getCore().getWorldEditProvider() == null) {
            return;
        }

        if (!GriefDefender.getCore().getWorldEditProvider().hasCUISupport(player.getUniqueId())) {
            this.serverNotificationManager.sendPopupNotification(player, Text.of("Claim Manager"), Text.of("WECUI Mod not loaded!"), 2);
        }

        // The below is tested and verified working!
        if (value && !claim.isWilderness()) {
            GriefDefender.getCore().getWorldEditProvider().displayClaimCUIVisual(claim, player.getUniqueId());
        } else {
            GriefDefender.getCore().getWorldEditProvider().revertAllVisuals(player.getUniqueId());
        }
    }

    public void saveChanges(final Player player, final String claimName, final String claimGreeting, final String claimFarewell, final double x, final double y, final double z, final String worldName) {
        if (!isGPEnabled(player))
            return;

        final Claim claim = this.claimLookup(player, x, y, z, worldName);
        if (claim != null) {
            final boolean isOwner = (claim.getOwnerUniqueId().equals(player.getUniqueId()));
            final boolean isAdmin = player.hasPermission("griefdefender.admin");

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
            PlayerData playerData = GriefDefender.getCore().getPlayerData(player.getWorld().getUniqueId(), player.getUniqueId()).get();
            final double taxRate = playerData.getTaxRate(claim.getType());
            final double taxOwed = (claim.getClaimBlocks() / 256) * taxRate;
            return taxOwed;
        } else {
            return 0;
        }
    }

    public final double claimBlockCost(final Player player, final Claim claim) {
        if (claim != null && player != null) {
            PlayerData playerData = GriefDefender.getCore().getPlayerData(player.getWorld().getUniqueId(), player.getUniqueId()).get();
            return playerData.getEconomyClaimBlockCost();
        } else {
            return 0.0;
        }
    }

    public final double claimBlockSell(final Player player, final Claim claim) {
        if (claim != null && player != null) {
            PlayerData playerData = GriefDefender.getCore().getPlayerData(player.getWorld().getUniqueId(), player.getUniqueId()).get();
            return playerData.getEconomyClaimBlockReturn();
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

        final Claim claim = GriefDefender.getCore().getClaimManager(player.getWorld().getUniqueId()).getClaimAt(player.getLocation().getBlockPosition());
        if (claim != null) { // if GP is loaded, claim should never be null.
            final boolean isOwner = (claim.getOwnerUniqueId().equals(player.getUniqueId()));
            final boolean isTrusted = claim.isTrusted(player.getUniqueId());
            final boolean isAdmin = player.hasPermission("griefdefender.admin");
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

