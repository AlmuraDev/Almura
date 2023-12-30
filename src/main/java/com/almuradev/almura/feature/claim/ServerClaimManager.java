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
import com.griefdefender.api.claim.TrustTypes;
import com.griefdefender.api.data.PlayerData;
import com.griefdefender.api.event.*;
import com.griefdefender.lib.kyori.adventure.text.Component;
import com.griefdefender.lib.kyori.adventure.text.TextComponent;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.event.group.GroupDataRecalculateEvent;
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
import org.spongepowered.common.SpongeImplHooks;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.UUID;

@Singleton
public final class ServerClaimManager implements Witness {

    private final ChannelBinding.IndexedMessageChannel network;
    private final ServerNotificationManager serverNotificationManager;
    private final PluginContainer container;
    private boolean debugClaimManager = false;
    public static Text notificationTitle = Text.of("Claim Manager");
    public static String adminPermission = "griefdefender.admin.*";
    private boolean debug = false;

    @Inject
    public ServerClaimManager(@ChannelId(NetworkConfig.CHANNEL) final ChannelBinding.IndexedMessageChannel network, final
    ServerNotificationManager notificationManager, final PluginContainer container) {
        this.network = network;
        this.serverNotificationManager = notificationManager;
        this.container = container;
    }

    @Listener
    public void serverStarted(final GameAboutToStartServerEvent event) {
        if (Sponge.getPluginManager().isLoaded("griefdefender")) {
            // GD won't load properly when inDev
            if (!SpongeImplHooks.isDeobfuscatedEnvironment()) {
                GriefDefender.getEventManager().getBus().subscribe(BorderClaimEvent.class, this::onEnterExitClaim);
                GriefDefender.getEventManager().getBus().subscribe(ChangeClaimEvent.class, this::onChangeClaim);
                GriefDefender.getEventManager().getBus().subscribe(CreateClaimEvent.class, this::onCreateClaim);
                GriefDefender.getEventManager().getBus().subscribe(RemoveClaimEvent.class, this::onDeleteClaim);
                GriefDefender.getEventManager().getBus().subscribe(TaxClaimEvent.class, this::onTaxClaim);
                GriefDefender.getEventManager().getBus().subscribe(SaveClaimEvent.class, this::onSaveClaim);
            }

            LuckPermsProvider.get().getEventBus().subscribe(GroupDataRecalculateEvent.class, this::onLpPermChange);
        }
    }

    @Listener(order = Order.LAST)
    public void onPlayerJoin(final ClientConnectionEvent.Join event, @Getter("getTargetEntity") Player player) {
        this.sendUpdateTo(player, null, null,false, "playerJoin");
    }

    // Note: due to the fact that GriefDefender does an internal re-locate for these Kyori libraries; we have to shade in the relocated libraries
    // to make these listeners function.  This is insanity....

    public void onEnterExitClaim(BorderClaimEvent event) {
        final Player player = (Player)FMLServerHandler.instance().getServer().getPlayerList().getPlayerByUUID(event.getEntityUniqueId());
        this.sendUpdateTo(player, event.getClaim(), null,false, "exitClaim");
    }

    public void onSaveClaim(final SaveClaimEvent event) {
        if (event.getClaim() != null && !event.getClaim().isWilderness()) {
            this.sendUpdateTo(null, event.getClaim(), event.getClaim().getPlayers(), false, "saveClaim");
        }
    }

    public void onChangeClaim(final ChangeClaimEvent event) {
        if (event.getClaim() != null && !event.getClaim().isWilderness()) {
            this.sendUpdateTo(null, event.getClaim(), event.getClaim().getPlayers(), false, "changeClaim");
        }
    }

    public void onCreateClaim(final CreateClaimEvent event) {
        this.sendUpdateTo(null, event.getClaim(), event.getClaim().getPlayers(), false, "createClaim");
    }

    public void onDeleteClaim(final RemoveClaimEvent event) {
        // This method gets called on the claim that is being deleted.
        this.sendUpdateTo(null, null, event.getClaim().getPlayers(), false, "deleteClaim");
    }

    public void onTaxClaim(final TaxClaimEvent event) {
        this.sendUpdateTo(null, event.getClaim(), event.getClaim().getPlayers(), false, "taxClaim");
    }

    //LP Event system uses inline Registration.
    private void onLpPermChange(final GroupDataRecalculateEvent event) {
        if (Sponge.getServer().getOnlinePlayers().size() > 0) {
            if (event.getGroup().getName().equalsIgnoreCase("griefdefender_default")) {
                // This will send an update to everyone on the server if a flag change is detected within GD.
                sendUpdateTo(null, null, null, true, "lpPermsChange");
            }
        }
    }

    public void sendUpdateTo(final Player player, Claim claim, List<UUID> players, final boolean everyone, final String reason) {

        if (!Sponge.getPluginManager().isLoaded("griefdefender")) {
            // Check here to see if its loaded because in the deobfuscated environment I force load the ClaimManager classes for testing purposes.
            return;
        }
        if ((player == null && players == null && !everyone) || (player == null && (players != null && players.isEmpty()) && !everyone)) {
            if (debug) {
                System.out.println("Claim sendUpdateTo canceled because no players listed and everyone was false, reason: " + reason);
            }
            return;
        }

        if (claim != null && debug)
            System.out.println("Claim sendUpdateTo: [" + reason + "] everyone: [" + everyone + "] name: [" + claim.getDisplayName()  + "] owner: [" + claim.getOwnerName() + "] wilderness: [" + claim.isWilderness() + "]");

        // Lookup claim where player or players is standing
        if (claim == null) {
            if (player != null) {
                claim = GriefDefender.getCore().getClaimManager(player.getWorld().getUniqueId()).getClaimAt(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ());
            }
            if (players != null) {
                for (UUID playerUUID : players) {
                    final Player finalPlayer = (Player)FMLServerHandler.instance().getServer().getPlayerList().getPlayerByUUID(playerUUID);
                    if (finalPlayer != null) {
                        Task.builder().delayTicks(10).execute(t -> this.sendUpdate(finalPlayer, null)).submit(this.container);
                    }
                }
            }
        }

        // Send to specific player
        if (!everyone && player != null && claim != null) {
            final Claim finalClaim = claim;
            Task.builder().delayTicks(10).execute(t -> this.sendUpdate(player, finalClaim)).submit(this.container);
            return;
        }
        // Send to everyone regardless of implied list from events.
        if (everyone && player == null && claim != null) {
            final Claim finalClaim = claim;
            for (Player onlinePlayer : Sponge.getServer().getOnlinePlayers()) {
                Task.builder().delayTicks(10).execute(t -> this.sendUpdate(onlinePlayer, finalClaim)).submit(this.container);
            }
            return;
        }
        // Send to the implied list of players when claim is handed into the method.
        if (players != null && claim != null) {
            final Claim finalClaim = claim;
            for (UUID playerUUID : players) {
                final Player finalPlayer = (Player)FMLServerHandler.instance().getServer().getPlayerList().getPlayerByUUID(playerUUID);
                if (finalPlayer != null) {
                    Task.builder().delayTicks(10).execute(t -> this.sendUpdate(finalPlayer, finalClaim)).submit(this.container);
                }
            }
            return;
        }
       // Send update to everyone on server regardless of location or claim
        if (player == null && players == null && claim == null && everyone) {
            for (final Player onlinePlayer : Sponge.getServer().getOnlinePlayers()) {
                if (onlinePlayer != null && onlinePlayer.getWorld() != null) {
                    claim = GriefDefender.getCore().getClaimManager(onlinePlayer.getWorld().getUniqueId()).getClaimAt(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ());
                    if (claim != null) {
                        final Claim finalClaim = claim;
                        Task.builder().delayTicks(10).execute(t -> this.sendUpdate(onlinePlayer, finalClaim)).submit(this.container);
                    }
                }
            }
            return;
        }
    }

    protected void sendUpdate(final Player player, Claim claim) {
        if (GriefDefender.getCore() != null) {
            boolean isWorldOrilla = false;
            boolean isWorldAsgard = false;

            if (Sponge.getServer().getWorld("Orilla").isPresent()) {
                isWorldOrilla = Sponge.getServer().getWorld("Orilla").get().getUniqueId() == claim.getWorldUniqueId();
            }
            if (Sponge.getServer().getWorld("Asgard").isPresent()) {
                isWorldAsgard = Sponge.getServer().getWorld("Asgard").get().getUniqueId() == claim.getWorldUniqueId();
            }
            if (GriefDefender.getPermissionManager() != null) {
                if (claim == null) {
                    // This is intended to catch when a delayed task is sent to this method and claim is purposely left null.
                    claim = GriefDefender.getCore().getClaimManager(player.getWorld().getUniqueId()).getClaimAt(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ());
                }
                if (claim != null) {
                    boolean isClaim = true;
                    boolean hasWECUI = false;
                    boolean isForSale = false;
                    boolean showWarnings = true;

                    String claimName = "";
                    String claimGreeting = "";
                    String claimFarewell = "";
                    String dateCreated = "";
                    String claimOwner = "";
                    String claimType = "";

                    double claimEconBalance = 0.0;
                    double claimTaxRate = 0.0;
                    double claimTaxes = 0.0;
                    double claimBlockCost = 0.0;
                    double claimBlockSell = 0.0;
                    int claimSize = 0;
                    double claimTaxBalance = 0.0;
                    double claimSalePrice = 0.0;

                    final boolean isWilderness = claim.isWilderness();
                    final boolean isTownClaim = claim.isTown();
                    final boolean isAdminClaim = claim.isAdminClaim();
                    final boolean isBasicClaim = claim.isBasicClaim();
                    final boolean isSubdivision = claim.isSubdivision();

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
                        claimGreeting = ((TextComponent) claim.getData().getGreeting().get()).content();
                    }

                    if (claim.getData() != null && claim.getData().getFarewell().isPresent()) {
                        claimFarewell = ((TextComponent) claim.getData().getFarewell().get()).content();
                    }

                    if (claim.getDisplayName() == null) {
                        claimName = "Name Not Set";
                    } else {
                        claimName = claim.getDisplayName();
                    }

                    claimType = claim.getType().getName();
                    dateCreated = claim.getData().getDateCreated().toString();

                    if (claim.getData() != null){
                        final EconomyService service = Sponge.getServiceManager().provide(EconomyService.class).orElse(null);
                        if (service != null && player != null) {
                            if (claim.getEconomyData().isForSale()) {
                                isForSale = true;
                                claimSalePrice = this.claimSalePrice(claim);
                            }

                            // Todo: implement the rest of the econ stuffz.
                            claimTaxRate = this.claimTaxRate(claim);
                            claimTaxes = this.claimTaxes(claim);
                            claimBlockCost = this.claimBlockCost(claim);
                            claimBlockSell = this.claimBlockSell(claim);

                            final Currency currency = service.getDefaultCurrency();

                            if (claim.getEconomyData() != null) {
                                claimTaxBalance = this.claimTaxBalance(claim);
                                UUID accountID = claim.getEconomyAccountId();
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
                        if (debug)
                            System.out.println("Sending Claim packet update to: [" + player.getName() + "] for Claim: [" + claim.getDisplayName() +"] Wilderness: [" + isWilderness + "]");
                        this.network.sendTo(player, new ClientboundClaimDataPacket(isClaim, claimName, claimOwner, isWilderness, isTownClaim, isAdminClaim, isBasicClaim, isSubdivision, claimEconBalance, claimGreeting, claimFarewell, dateCreated, claimType, claimSize,
                                isForSale, showWarnings, claimTaxRate, claimTaxes, claimBlockCost, claimBlockSell, hasWECUI, claimTaxBalance, claimSalePrice));
                    }
                }
            }
        }
    }

    public void toggleVisuals(final Player player, final Claim claim, final boolean value) {
        if (GriefDefender.getCore().getWorldEditProvider() == null) {
            return;
        }

        if (!GriefDefender.getCore().getWorldEditProvider().hasCUISupport(player.getUniqueId())) {
            this.serverNotificationManager.sendPopupNotification(player, notificationTitle, Text.of("WECUI Mod not loaded!"), 2);
            return;
        }

        // The below is tested and verified working!
        if (value && !claim.isWilderness()) {
            GriefDefender.getCore().getWorldEditProvider().displayClaimCUIVisual(claim, player.getUniqueId());
        } else {
            GriefDefender.getCore().getWorldEditProvider().revertAllVisuals(player.getUniqueId());
        }
    }

    public void saveChanges(final Player player, final String claimName, final String claimGreeting, final String claimFarewell, final double x, final double y, final double z, final String worldName) {
        final Claim claim = this.claimLookup(player, x, y, z, worldName);
        if (claim != null) {
            final boolean isOwner = (claim.getOwnerUniqueId().equals(player.getUniqueId()));
            final boolean isAdmin = player.hasPermission(adminPermission);

            if (isOwner || isAdmin) {
                claim.getData().setDisplayName(Component.text(claimName));
                claim.getData().setGreeting(Component.text(claimGreeting));
                claim.getData().setFarewell(Component.text(claimFarewell));
                claim.getData().save();

                this.sendUpdateTo(player, claim, claim.getPlayers(), false, "saveChanges");
                this.serverNotificationManager.sendPopupNotification(player, notificationTitle, Text.of("Changed Saved!"), 5);
            } else {
                this.serverNotificationManager.sendPopupNotification(player, notificationTitle, Text.of("Insufficient Permissions!"), 5);
            }
        } else {
            this.serverNotificationManager.sendPopupNotification(player, notificationTitle, Text.of("Unable to lookup Claim, changed not saved!"), 5);
        }
    }

    public Claim claimLookup(final Player player, final double x, final double y, final double z, final String worldName) {
        final World world = Sponge.getServer().getWorld(worldName).orElse(null);
        if (world == null) {
            this.serverNotificationManager
                .sendPopupNotification(player, notificationTitle, Text.of("Unable to find world, changes not saved!"), 5);
            return null;
        }
        final Location<World> location = new Location<>(world, x, y, z);
        if (location == null) {
            this.serverNotificationManager
                .sendPopupNotification(player, notificationTitle, Text.of("Invalid location sent to server.  Changes not saved!"), 5);
        } else {
            final Claim claim = GriefDefender.getCore().getClaimManager(player.getWorld().getUniqueId()).getClaimAt(location.getBlockX(), location.getBlockY(), location.getBlockZ());
            if (claim != null) {
                return claim;
            }
        }
        return null;
    }

    public double claimTaxRate(final Claim claim) {
        if (claim != null) {
            PlayerData playerData = GriefDefender.getCore().getPlayerData(claim.getWorldUniqueId(), claim.getOwnerUniqueId());
            final double taxRate = playerData.getTaxRate(claim.getType());
            return taxRate;
        }
        return 0;
    }

    public double claimTaxes(final Claim claim) {
        if (claim != null) {
            PlayerData playerData = GriefDefender.getCore().getPlayerData(claim.getWorldUniqueId(), claim.getOwnerUniqueId());
            final double taxRate = playerData.getTaxRate(claim.getType());
            final double taxOwed = claim.getClaimBlocks() * taxRate;
            return taxOwed;
        }
        return 0;
    }

    public double claimBlockCost(final Claim claim) {
        if (claim != null) {
            PlayerData playerData = GriefDefender.getCore().getPlayerData(claim.getWorldUniqueId(), claim.getOwnerUniqueId());
            if (playerData != null)
                return playerData.getEconomyClaimBlockCost();
        }
        return 0.0;
    }

    public double claimBlockSell(final Claim claim) {
        if (claim != null) {
            PlayerData playerData = GriefDefender.getCore().getPlayerData(claim.getWorldUniqueId(), claim.getOwnerUniqueId());
            if (playerData != null)
                return playerData.getEconomyClaimBlockReturn();
        }
        return 0.0;
    }

    public double claimTaxBalance(final Claim claim) {
        if (claim != null) {
            if (claim.getEconomyData() != null) {
                return claim.getEconomyData().getTaxBalance();
            }
        }
        return 0.0;
    }

    public double claimSalePrice(final Claim claim) {
        if (claim != null) {
            if (claim.getEconomyData() != null) {
                return claim.getEconomyData().getSalePrice();
            }
        }
        return 0.0;
    }

    // Functions
    public void abandonClaim(final Claim claim) {
        if (claim != null) {
            claim.getClaimManager().deleteClaim(claim);
        }
    }

    public void toggleClaimDenyMessages(final Claim claim, final boolean value) {
        if (claim != null) {
            claim.getData().setDenyMessages(value);
            claim.getData().save();
        }
    }

    public void setForSale(final Claim claim, boolean forSale, double salePrice) {
        if (claim != null) {
            claim.getEconomyData().setForSale(forSale);
            claim.getEconomyData().setSalePrice(salePrice);
            claim.getData().save();
        }
    }

    public void setSpawnLocation(final Player player, final Claim claim) {
        if (claim != null && player != null) {
            claim.getData().setSpawnPos((int)player.getPosition().getX(), (int)player.getPosition().getY(), (int)player.getPosition().getZ());
            claim.getData().save();
        }
    }

    public void openClientGUI (final Player player){
        // GD won't start completely inDev, so I've added this to allow me to see the gui.
        if (SpongeImplHooks.isDeobfuscatedEnvironment()) {
            this.network.sendTo(player, new ClientboundClaimGuiResponsePacket(true, true, true));
        } else {
            final Claim claim = GriefDefender.getCore().getClaimManager(player.getWorld().getUniqueId()).getClaimAt(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ());
            if (claim != null) {
                sendUpdate(player, claim);
            }

            if (!Sponge.getPluginManager().isLoaded("griefdefender")) {
                this.serverNotificationManager.sendPopupNotification(player, notificationTitle, Text.of("GriefDefender not detected!"), 2);
                return;
            }

            if (!player.hasPermission(Almura.ID + ".claim.base")) {
                this.serverNotificationManager.sendPopupNotification(player, notificationTitle, Text.of("Insufficient Permissions to Manage Claim!"), 2);
                return;
            }

            if (claim != null) {
                final boolean isOwner = (claim.getOwnerUniqueId().equals(player.getUniqueId()));
                final boolean isTrusted = claim.isUserTrusted(player.getUniqueId(), TrustTypes.MANAGER);
                final boolean isAdmin = player.hasPermission(adminPermission);

                if (!isAdmin && claim.isWilderness()) {
                    this.serverNotificationManager.sendPopupNotification(player, notificationTitle, Text.of("Insufficient permissions to open Claim Manager in Wilderness"), 5);
                } else {
                    this.network.sendTo(player, new ClientboundClaimGuiResponsePacket(isOwner, isTrusted, isAdmin));
                }
            }
        }
    }
}

