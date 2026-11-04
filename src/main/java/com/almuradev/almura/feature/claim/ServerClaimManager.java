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
import com.almuradev.almura.feature.claim.network.ClientboundClaimVisualPacket;
import com.almuradev.almura.feature.claim.network.ServerboundClaimGuiFlagsRequestPacket;
import com.almuradev.almura.feature.notification.ServerNotificationManager;
import com.almuradev.almura.shared.network.NetworkConfig;
import com.almuradev.core.event.Witness;
import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.claim.Claim;
import com.griefdefender.api.claim.ClaimManager;
import com.griefdefender.api.claim.TrustTypes;
import com.griefdefender.api.permission.Context;
import com.griefdefender.api.permission.ContextKeys;
import com.griefdefender.api.permission.PermissionResult;
import com.griefdefender.api.permission.flag.Flag;
import com.griefdefender.api.permission.flag.Flags;
import com.griefdefender.api.Tristate;
import com.griefdefender.api.data.PlayerData;
import com.griefdefender.api.event.*;
import com.griefdefender.lib.kyori.adventure.text.Component;
import com.griefdefender.lib.kyori.adventure.text.TextComponent;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.event.group.GroupDataRecalculateEvent;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.server.FMLServerHandler;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
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

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Singleton
public final class ServerClaimManager implements Witness {

    private final ChannelBinding.IndexedMessageChannel network;
    private final ServerNotificationManager serverNotificationManager;
    private final PluginContainer container;
    private boolean debugClaimManager = false;
    public static Text notificationTitle = Text.of("Claim Manager");
    public static String adminPermission = "griefdefender.admin.*";
    private boolean debug = false;
    // How often monsters are cleared from claims with "Kill monsters in claim" on.
    private static final int KILL_MONSTERS_INTERVAL = 40;
    // The claim each player has Toggle Visuals on for, so its outline can be resent when the claim changes.
    private final Map<UUID, Claim> visualClaims = new HashMap<>();
    // Claims with "Kill monsters in claim" on, by world. Saved as "world uuid|claim uuid" lines in config/almura/claim-kill-monsters.txt.
    private final Map<UUID, Set<UUID>> killMonsterClaims = new HashMap<>();
    private final Path killMonstersFile;
    private final Logger logger;

    @Inject
    public ServerClaimManager(@ChannelId(NetworkConfig.CHANNEL) final ChannelBinding.IndexedMessageChannel network, final
    ServerNotificationManager notificationManager, final PluginContainer container, @ConfigDir(sharedRoot = false) final Path configDir, final Logger logger) {
        this.network = network;
        this.serverNotificationManager = notificationManager;
        this.container = container;
        this.killMonstersFile = configDir.resolve("claim-kill-monsters.txt");
        this.logger = logger;
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

                this.loadKillMonsterClaims();
                Task.builder().intervalTicks(KILL_MONSTERS_INTERVAL).execute(this::killMonstersInClaims).submit(this.container);
            }

            LuckPermsProvider.get().getEventBus().subscribe(GroupDataRecalculateEvent.class, this::onLpPermChange);
        }
    }

    @Listener(order = Order.LAST)
    public void onPlayerJoin(final ClientConnectionEvent.Join event, @Getter("getTargetEntity") Player player) {
        this.sendUpdateTo(player, null, null,false, "playerJoin");
    }

    @Listener
    public void onPlayerQuit(final ClientConnectionEvent.Disconnect event, @Getter("getTargetEntity") Player player) {
        this.visualClaims.remove(player.getUniqueId());
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
            this.refreshVisuals(event.getClaim());
        }
    }

    public void onCreateClaim(final CreateClaimEvent event) {
        this.sendUpdateTo(null, event.getClaim(), event.getClaim().getPlayers(), false, "createClaim");
        // A new subdivision shows up in the outline of the claim it was made in.
        this.refreshVisuals(event.getClaim());
    }

    public void onDeleteClaim(final RemoveClaimEvent event) {
        // This method gets called on the claim that is being deleted.
        this.sendUpdateTo(null, null, event.getClaim().getPlayers(), false, "deleteClaim");
        this.refreshVisuals(event.getClaim());
        // The event comes before the removal (and may be cancelled), so check once it has happened.
        final UUID worldId = event.getClaim().getWorldUniqueId();
        final UUID claimId = event.getClaim().getUniqueId();
        Task.builder().delayTicks(10).execute(t -> {
            if (GriefDefender.getCore().getClaimManager(worldId).getClaimByUUID(claimId) == null) {
                this.setKillMonsters(worldId, claimId, false);
            }
        }).submit(this.container);
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
                                    claimEconBalance = claimAccount != null ? claimAccount.getBalance(currency).doubleValue() : 0;
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
                                isForSale, showWarnings, claimTaxRate, claimTaxes, claimBlockCost, claimBlockSell, hasWECUI, claimTaxBalance, claimSalePrice,
                                isWilderness ? 0 : this.getFlags(claim), !isWilderness && this.isKillMonsters(claim)));
                    }
                }
            }
        }
    }

    public void toggleVisuals(final Player player, final Claim claim, final boolean value) {
        if (value && !claim.isWilderness()) {
            // Show the whole claim the player is in, with its subdivisions, even when standing in a subdivision.
            final Claim top = topClaim(claim);
            this.visualClaims.put(player.getUniqueId(), top);
            this.sendVisuals(player, top);
        } else {
            this.visualClaims.remove(player.getUniqueId());
            this.sendVisuals(player, null);
        }
    }

    // Almura draws the outline itself; an empty packet clears it.
    private void sendVisuals(final Player player, @Nullable final Claim top) {
        final ClientboundClaimVisualPacket packet = new ClientboundClaimVisualPacket();
        if (top != null) {
            packet.boxes.add(this.visualBox(top));
            for (final Claim child : top.getChildren(true)) {
                packet.boxes.add(this.visualBox(child));
            }
        }
        this.network.sendTo(player, packet);
    }

    /**
     * Resends the outline to everyone viewing the claim that {@code changed} belongs to. GriefDefender fires its claim events before applying
     * the change, so this waits like {@link #sendUpdateTo} does; a claim that is gone by then has its outline cleared.
     */
    private void refreshVisuals(final Claim changed) {
        final Claim top = topClaim(changed);
        Task.builder().delayTicks(10).execute(t -> {
            final Claim current = GriefDefender.getCore().getClaimManager(top.getWorldUniqueId()).getClaimByUUID(top.getUniqueId());
            for (final Map.Entry<UUID, Claim> entry : new ArrayList<>(this.visualClaims.entrySet())) {
                if (!entry.getValue().getUniqueId().equals(top.getUniqueId())) {
                    continue;
                }
                final Player player = Sponge.getServer().getPlayer(entry.getKey()).orElse(null);
                if (player == null || current == null) {
                    this.visualClaims.remove(entry.getKey());
                    if (player != null) {
                        this.sendVisuals(player, null);
                    }
                } else {
                    entry.setValue(current);
                    this.sendVisuals(player, current);
                }
            }
        }).submit(this.container);
    }

    /**
     * The GriefDefender flag permissions behind each Flags checkbox, matching GriefDefender's own presets (presets/minecraft.yml), so "Prevent
     * Monsters" is the same as /cf entity-spawn #monster false. A checkbox is on when all of its permissions are denied.
     */
    // Built on first use, not when this class loads: until GriefDefender has registered its flags, the Flags constants are placeholders that
    // throw when used, and a table built at class load would keep those placeholders.
    @Nullable private static Map<ClaimFlag, List<FlagRule>> flagRules;

    private static Map<ClaimFlag, List<FlagRule>> flagRules() {
        if (flagRules == null) {
            final Map<ClaimFlag, List<FlagRule>> rules = new EnumMap<>(ClaimFlag.class);
            // Spawning, plus GriefDefender's monster-player-damage preset so monsters outside the claim can't hurt players in it either (arrows included).
            rules.put(ClaimFlag.PREVENT_MONSTERS, Arrays.asList(
                new FlagRule(Flags.ENTITY_SPAWN, ContextKeys.TARGET, "#monster"),
                new FlagRule(Flags.ENTITY_DAMAGE, ContextKeys.SOURCE, "#monster", ContextKeys.TARGET, "minecraft:player"),
                new FlagRule(Flags.PROJECTILE_IMPACT_ENTITY, ContextKeys.SOURCE, "#monster", ContextKeys.TARGET, "minecraft:player")));
            rules.put(ClaimFlag.PREVENT_PVP, Arrays.asList(
                new FlagRule(Flags.ENTITY_DAMAGE, ContextKeys.SOURCE, "minecraft:player", ContextKeys.TARGET, "minecraft:player"),
                new FlagRule(Flags.PROJECTILE_IMPACT_ENTITY, ContextKeys.SOURCE, "minecraft:player", ContextKeys.TARGET, "minecraft:player")));
            // Every explosion source, not only the creepers and TNT that GriefDefender's presets cover.
            rules.put(ClaimFlag.PREVENT_EXPLOSIONS, Collections.singletonList(new FlagRule(Flags.EXPLOSION_BLOCK)));
            rules.put(ClaimFlag.PREVENT_FIRE_SPREAD, Arrays.asList(
                new FlagRule(Flags.BLOCK_SPREAD, ContextKeys.SOURCE, "minecraft:fire"),
                new FlagRule(Flags.BLOCK_SPREAD, ContextKeys.SOURCE, "minecraft:flowing_lava")));
            rules.put(ClaimFlag.PREVENT_ENDERMAN_GRIEF, Arrays.asList(
                new FlagRule(Flags.BLOCK_BREAK, ContextKeys.SOURCE, "minecraft:enderman"),
                new FlagRule(Flags.BLOCK_PLACE, ContextKeys.SOURCE, "minecraft:enderman")));
            flagRules = rules;
        }
        return flagRules;
    }

    private static final class FlagRule {

        final Flag flag;
        private final Set<Context> contexts = new HashSet<>();

        // Context key and value pairs, such as ContextKeys.SOURCE, "minecraft:enderman".
        FlagRule(final Flag flag, final String... contexts) {
            this.flag = flag;
            for (int i = 0; i < contexts.length; i += 2) {
                this.contexts.add(new Context(contexts[i], contexts[i + 1]));
            }
        }

        // A fresh copy for every GriefDefender call: GriefDefender adds the claim's (and, for active lookups, the game's) contexts to the set
        // it is given, and a shared set would carry those into later saves, storing flags with contexts such as target=#any.
        Set<Context> contexts() {
            return new HashSet<>(this.contexts);
        }

        // The claim's own value when it has one, otherwise what GriefDefender's defaults resolve to.
        boolean isDenied(final Claim claim) {
            Tristate value = claim.getFlagPermissionValue(this.flag, this.contexts());
            if (value == Tristate.UNDEFINED) {
                value = claim.getActiveFlagPermissionValue(this.flag, GriefDefender.getCore().getDefaultSubject(), this.contexts());
            }
            return value == Tristate.FALSE;
        }
    }

    public int getFlags(final Claim claim) {
        int flags = 0;
        for (final Map.Entry<ClaimFlag, List<FlagRule>> entry : flagRules().entrySet()) {
            if (entry.getValue().stream().allMatch(rule -> rule.isDenied(claim))) {
                flags |= entry.getKey().bit();
            }
        }
        return flags;
    }

    public boolean isKillMonsters(final Claim claim) {
        final Set<UUID> claims = this.killMonsterClaims.get(claim.getWorldUniqueId());
        return claims != null && claims.contains(claim.getUniqueId());
    }

    /**
     * Applies one Flags checkbox. {@code option} is a {@link ClaimFlag} name or {@link ServerboundClaimGuiFlagsRequestPacket#KILL_MONSTERS}.
     */
    public void setFlag(final Player player, final Claim claim, final String option, final boolean value) {
        final List<CompletableFuture<PermissionResult>> saves = new ArrayList<>();
        if (ServerboundClaimGuiFlagsRequestPacket.KILL_MONSTERS.equals(option)) {
            // Killing monsters is a sub-option of preventing them.
            this.setKillMonsters(claim.getWorldUniqueId(), claim.getUniqueId(), value && ClaimFlag.PREVENT_MONSTERS.in(this.getFlags(claim)));
        } else {
            final ClaimFlag flag;
            try {
                flag = ClaimFlag.valueOf(option);
            } catch (final IllegalArgumentException e) {
                return;
            }
            for (final FlagRule rule : flagRules().get(flag)) {
                saves.add(claim.setFlagPermission(rule.flag, value ? Tristate.FALSE : Tristate.TRUE, rule.contexts()));
            }
            if (flag == ClaimFlag.PREVENT_MONSTERS && !value) {
                this.setKillMonsters(claim.getWorldUniqueId(), claim.getUniqueId(), false);
            }
        }
        // GriefDefender saves through LuckPerms in the background; send the new state once the saves are done, or the screen would be sent
        // the old one and flip the box back.
        CompletableFuture.allOf(saves.toArray(new CompletableFuture<?>[0])).whenComplete((result, error) -> Task.builder().execute(t -> {
            this.sendUpdateTo(null, claim, claim.getPlayers(), false, "claimFlags");
            this.sendUpdateTo(player, claim, null, false, "claimFlags");
        }).submit(this.container));
    }

    private void setKillMonsters(final UUID worldId, final UUID claimId, final boolean value) {
        final boolean changed;
        if (value) {
            changed = this.killMonsterClaims.computeIfAbsent(worldId, k -> new HashSet<>()).add(claimId);
        } else {
            final Set<UUID> claims = this.killMonsterClaims.get(worldId);
            changed = claims != null && claims.remove(claimId);
            if (claims != null && claims.isEmpty()) {
                this.killMonsterClaims.remove(worldId);
            }
        }
        if (changed) {
            this.saveKillMonsterClaims();
        }
    }

    /**
     * Stops monsters hurting players in claims with Prevent Monsters on. GriefDefender's damage flags only see the entity that dealt the damage,
     * so a witch's splash potion, a blaze's fireball or other indirect attacks get through; this checks who is really behind the damage. It only
     * looks up the claim when a monster hurts a player.
     */
    @SubscribeEvent
    public void onLivingAttack(final LivingAttackEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer) || event.getEntityLiving().world.isRemote
            || !(event.getSource().getTrueSource() instanceof IMob) || SpongeImplHooks.isDeobfuscatedEnvironment()) {
            return;
        }
        final EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        final ClaimManager claimManager = GriefDefender.getCore().getClaimManager(((World) player.world).getUniqueId());
        Claim claim = claimManager.getClaimAt((int) Math.floor(player.posX), (int) Math.floor(player.posY), (int) Math.floor(player.posZ));
        while (claim != null && !claim.isWilderness()) {
            final Claim current = claim;
            if (flagRules().get(ClaimFlag.PREVENT_MONSTERS).stream().allMatch(rule -> rule.isDenied(current))) {
                event.setCanceled(true);
                return;
            }
            claim = claim.getParent();
        }
    }

    /**
     * Removes hostile mobs standing in claims with "Kill monsters in claim" on. To stay light it never scans claim areas: it walks the loaded
     * entities of worlds that have such a claim, skips everything that isn't a monster, and only then asks GriefDefender which claim the monster
     * is in. Monsters are removed without drops or experience, so a claim can't be turned into a mob farm.
     */
    private void killMonstersInClaims(final Task task) {
        for (final Map.Entry<UUID, Set<UUID>> entry : this.killMonsterClaims.entrySet()) {
            final World world = Sponge.getServer().getWorld(entry.getKey()).orElse(null);
            if (world == null) {
                continue;
            }
            final ClaimManager claimManager = GriefDefender.getCore().getClaimManager(entry.getKey());
            for (final net.minecraft.entity.Entity entity : new ArrayList<>(((net.minecraft.world.World) world).loadedEntityList)) {
                // Named monsters (name tags, quest mobs) are left alone.
                if (!(entity instanceof IMob) || entity.isDead || entity.hasCustomName()) {
                    continue;
                }
                Claim claim = claimManager.getClaimAt((int) Math.floor(entity.posX), (int) Math.floor(entity.posY), (int) Math.floor(entity.posZ));
                while (claim != null && !claim.isWilderness()) {
                    if (entry.getValue().contains(claim.getUniqueId())) {
                        entity.setDead();
                        break;
                    }
                    claim = claim.getParent();
                }
            }
        }
    }

    private void loadKillMonsterClaims() {
        if (Files.notExists(this.killMonstersFile)) {
            return;
        }
        try {
            for (final String line : Files.readAllLines(this.killMonstersFile, StandardCharsets.UTF_8)) {
                final String[] ids = line.trim().split("\\|");
                if (ids.length == 2) {
                    this.killMonsterClaims.computeIfAbsent(UUID.fromString(ids[0]), k -> new HashSet<>()).add(UUID.fromString(ids[1]));
                }
            }
        } catch (final IOException | IllegalArgumentException e) {
            this.logger.error("Unable to read " + this.killMonstersFile, e);
        }
    }

    private void saveKillMonsterClaims() {
        final List<String> lines = new ArrayList<>();
        this.killMonsterClaims.forEach((world, claims) -> claims.forEach(claim -> lines.add(world + "|" + claim)));
        try {
            Files.createDirectories(this.killMonstersFile.getParent());
            Files.write(this.killMonstersFile, lines, StandardCharsets.UTF_8);
        } catch (final IOException e) {
            this.logger.error("Unable to write " + this.killMonstersFile, e);
        }
    }

    private static Claim topClaim(final Claim claim) {
        Claim top = claim;
        while (top.isSubdivision() && top.getParent() != null) {
            top = top.getParent();
        }
        return top;
    }

    private ClientboundClaimVisualPacket.Box visualBox(final Claim claim) {
        final byte type;
        if (claim.isAdminClaim()) {
            type = ClientboundClaimVisualPacket.ADMIN;
        } else if (claim.isTown()) {
            type = ClientboundClaimVisualPacket.TOWN;
        } else if (claim.isSubdivision()) {
            type = ClientboundClaimVisualPacket.SUBDIVISION;
        } else {
            type = ClientboundClaimVisualPacket.BASIC;
        }
        final int[] lesser = corner(claim, "getLesserBoundaryCorner");
        final int[] greater = corner(claim, "getGreaterBoundaryCorner");
        return new ClientboundClaimVisualPacket.Box(type, claim.isCuboid(), lesser[0], lesser[1], lesser[2], greater[0], greater[1], greater[2]);
    }

    // The corners are read by reflection: the API jar Almura compiles against returns GriefDefender's relocated Vector3i
    // (com.griefdefender.lib.flowpowered), while the GriefDefender on the server returns Sponge's com.flowpowered one, so a direct call fails
    // with NoSuchMethodError.
    private static int[] corner(final Claim claim, final String method) {
        try {
            final Object vector = Claim.class.getMethod(method).invoke(claim);
            final Class<?> type = vector.getClass();
            return new int[] {(int) type.getMethod("getX").invoke(vector), (int) type.getMethod("getY").invoke(vector),
                (int) type.getMethod("getZ").invoke(vector)};
        } catch (final ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to read claim corner " + method, e);
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
            return playerData != null ? playerData.getTaxRate(claim.getType()) : 0;
        }
        return 0;
    }

    public double claimTaxes(final Claim claim) {
        if (claim != null) {
            PlayerData playerData = GriefDefender.getCore().getPlayerData(claim.getWorldUniqueId(), claim.getOwnerUniqueId());
            final double taxRate = playerData != null ? playerData.getTaxRate(claim.getType()) : 0;
            return claim.getClaimBlocks() * taxRate;
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

