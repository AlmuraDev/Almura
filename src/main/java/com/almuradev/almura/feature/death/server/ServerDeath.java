/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.death.server;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.bank.BankCoins;
import com.almuradev.almura.feature.bank.BankMoney;
import com.almuradev.almura.feature.death.network.DeathMessages.ClientboundDied;
import com.almuradev.almura.feature.economy.Economy;
import com.almuradev.almura.feature.economy.EconomyAccounts;
import com.almuradev.almura.feature.economy.EconomyAccounts.Account;
import com.almuradev.almura.feature.notification.server.ServerNotifications;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import net.neoforged.neoforge.server.permission.nodes.PermissionTypes;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The death penalty, as 3.5's {@code DeathHandler}: a player who dies loses part of their wallet. A random 50 to 75% of it is the "death tax"
 * rate; what is left after that rate (25 to 50% of the wallet) is taken out and dropped where they died as Almura's coins (platinum
 * $1,000,000, gold $100,000, silver $1,000, copper $100, in stacks), and the change too small for a coin is lost to death taxes. The player is
 * shown Almura's death screen with both amounts, and everyone else a popup.
 * <p>
 * Revive, on the death screen, brings the player back where they died rather than at their spawn: the server's own record of the place, not
 * the client's (3.5 trusted the position the client sent).
 * <p>
 * Needs the server's economy and the coin items; without them deaths cost nothing and the screen shows no money, as 3.5's.
 * <p>
 * Permissions (a permission plugin such as LuckPerms grants them; without one only operators have them, as 3.5's Sponge defaults did):
 * <ul>
 *     <li>{@code almura.death.exempt}: keeps all their money on death;</li>
 *     <li>{@code almura.death.revive}: Revive on the death screen.</li>
 * </ul>
 */
public final class ServerDeath {

    public static final PermissionNode<Boolean> EXEMPT = node("death.exempt");
    public static final PermissionNode<Boolean> REVIVE = node("death.revive");

    // 3.5's coins, largest first (BankCoins has what each is worth)
    private static final String[] COIN_IDS = {
        "almura:normal/currency/platinumcoin", "almura:normal/currency/goldcoin", "almura:normal/currency/silvercoin",
        "almura:normal/currency/coppercoin"
    };
    // 3.5's death tax rate, in percent
    private static final double TAX_MIN = 50, TAX_MAX = 75;
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    // Where each player last died, until they respawn; the main thread only
    private static final Map<UUID, Death> DEATHS = new HashMap<>();
    // Players who asked to be revived, until their respawn
    private static final Set<UUID> REVIVING = new HashSet<>();
    // Players respawned to be revived, moved back where they died on a later tick, with the tick they respawned on
    private static final Map<UUID, Revival> REVIVALS = new HashMap<>();

    private static Item @Nullable [] coins;
    private static boolean coinsChecked;

    private static PermissionNode<Boolean> node(final String path) {
        return new PermissionNode<>(Almura.ID, path, PermissionTypes.BOOLEAN,
            (player, uuid, context) -> player != null && player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER));
    }

    @SubscribeEvent
    public void onPermissionNodes(final PermissionGatherEvent.Nodes event) {
        event.addNodes(EXEMPT, REVIVE);
    }

    /**
     * Last, after anything that might keep the player alive.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onDeath(final LivingDeathEvent event) {
        if (!(event.getEntity() instanceof final ServerPlayer player)) {
            return;
        }
        DEATHS.put(player.getUUID(), new Death(player.level().dimension(), player.position(), player.getYRot(), player.getXRot()));
        REVIVING.remove(player.getUUID());

        // Vanilla's death message, as its death screen shows it
        final Component cause = player.level().getGameRules().get(GameRules.SHOW_DEATH_MESSAGES)
            ? player.getCombatTracker().getDeathMessage() : Component.empty();
        final boolean canRevive = PermissionAPI.getPermission(player, REVIVE);

        final EconomyAccounts economy = Economy.accounts();
        final Item[] coins = coins();
        if (economy == null || coins == null) {
            // No economy or no coins: nothing taken, as 3.5's fallback
            PacketDistributor.sendToPlayer(player, new ClientboundDied("0", "0", false, canRevive, cause));
            return;
        }

        BigDecimal dropped = BigDecimal.ZERO, lost = BigDecimal.ZERO;
        boolean showMoney = false;
        final Account wallet = Account.player(player.getUUID());
        final BigDecimal balance = economy.balance(wallet);
        if (balance != null && balance.signum() > 0 && !PermissionAPI.getPermission(player, EXEMPT)) {
            final double tax = ThreadLocalRandom.current().nextDouble(TAX_MIN, TAX_MAX);
            // 3.5's: the balance less the tax rate's share of it, never more than the balance; to the cent
            final BigDecimal taken = balance.subtract(balance.multiply(BigDecimal.valueOf(tax)).divide(HUNDRED))
                .min(balance).setScale(2, RoundingMode.DOWN);
            // The money first: coins only drop once it is out of the wallet (3.5 dropped them either way)
            if (taken.signum() > 0 && economy.withdraw(wallet, taken)) {
                lost = drop(player, coins, taken);
                dropped = taken.subtract(lost);
                showMoney = true;
            } else if (taken.signum() > 0) {
                Almura.LOGGER.warn("Couldn't take {}'s death penalty of {} from their wallet; nothing was dropped",
                    player.getGameProfile().name(), taken.toPlainString());
            }
        }

        PacketDistributor.sendToPlayer(player, new ClientboundDied(dropped.toPlainString(), lost.toPlainString(), showMoney, canRevive, cause));
        final String name = player.getGameProfile().name();
        final String amounts = "Dropped: §6$" + BankMoney.format(dropped) + "§r and lost: §c$" + BankMoney.format(lost) + "§r to death taxes.";
        for (final ServerPlayer other : player.level().getServer().getPlayerList().getPlayers()) {
            if (other != player) {
                ServerNotifications.sendPopup(other, name + " has died!", amounts, 5);
            }
        }
    }

    /**
     * A player who joins while dead gets the death screen again, as 3.5's; what they lost was shown when they died.
     */
    @SubscribeEvent
    public void onLoggedIn(final PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof final ServerPlayer player && player.isDeadOrDying()) {
            PacketDistributor.sendToPlayer(player, new ClientboundDied("0", "0", false, PermissionAPI.getPermission(player, REVIVE),
                Component.empty()));
        }
    }

    @SubscribeEvent
    public void onLoggedOut(final PlayerEvent.PlayerLoggedOutEvent event) {
        // Where they died is kept, for Revive when they come back dead
        REVIVING.remove(event.getEntity().getUUID());
        REVIVALS.remove(event.getEntity().getUUID());
    }

    /**
     * Revive: the dead player will come back where they died when they respawn, which their client asks for right after this.
     */
    public static void revive(final ServerPlayer player) {
        if (!PermissionAPI.getPermission(player, REVIVE)) {
            player.sendSystemMessage(Component.literal("Access denied, missing permission: §balmura.death.revive§r."));
            return;
        }
        if (!player.isDeadOrDying()) {
            return;
        }
        final Death death = deathOf(player);
        if (death == null) {
            player.sendSystemMessage(Component.literal("There's nowhere to revive you; you'll respawn instead."));
            return;
        }
        final ServerLevel level = player.level().getServer().getLevel(death.dimension());
        if (level == null) {
            player.sendSystemMessage(Component.literal("The world you died in isn't loaded; you'll respawn instead."));
            return;
        }
        if (death.position().y < level.getMinY()) {
            player.sendSystemMessage(Component.literal("You died in the void; you'll respawn instead."));
            return;
        }
        REVIVING.add(player.getUUID());
    }

    @SubscribeEvent
    public void onRespawn(final PlayerEvent.PlayerRespawnEvent event) {
        if (event.isEndConquered() || !(event.getEntity() instanceof final ServerPlayer player)) {
            return;
        }
        final UUID id = player.getUUID();
        final Death death = deathOf(player);
        DEATHS.remove(id);
        if (REVIVING.remove(id) && death != null) {
            REVIVALS.put(id, new Revival(death, player.level().getServer().getTickCount()));
        }
    }

    /**
     * Moves the revived players back where they died, a tick after their respawn has finished.
     */
    @SubscribeEvent
    public void onServerTick(final ServerTickEvent.Post event) {
        if (REVIVALS.isEmpty()) {
            return;
        }
        final MinecraftServer server = event.getServer();
        for (final Iterator<Map.Entry<UUID, Revival>> it = REVIVALS.entrySet().iterator(); it.hasNext(); ) {
            final Map.Entry<UUID, Revival> entry = it.next();
            final Revival revival = entry.getValue();
            if (server.getTickCount() <= revival.respawnTick()) {
                continue;
            }
            it.remove();
            final ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            final Death death = revival.death();
            final ServerLevel level = server.getLevel(death.dimension());
            if (player == null || player.isDeadOrDying() || level == null) {
                continue;
            }
            final Vec3 at = death.position();
            player.teleportTo(level, at.x, at.y, at.z, Set.of(), death.yRot(), death.xRot(), true);
        }
    }

    /**
     * Where the player died: this server's record, or else vanilla's (the block, kept with the player, across restarts).
     */
    private static @Nullable Death deathOf(final ServerPlayer player) {
        final Death death = DEATHS.get(player.getUUID());
        if (death != null) {
            return death;
        }
        final GlobalPos last = player.getLastDeathLocation().orElse(null);
        if (last == null) {
            return null;
        }
        final BlockPos pos = last.pos();
        return new Death(last.dimension(), Vec3.atBottomCenterOf(pos), player.getYRot(), player.getXRot());
    }

    /**
     * Drops the amount as coins where the player is, as 3.5's {@code dropAmountReturnChange}.
     *
     * @return the change too small for a coin, which isn't dropped
     */
    private static BigDecimal drop(final ServerPlayer player, final Item[] coins, final BigDecimal amount) {
        BigDecimal remaining = amount;
        for (int i = 0; i < coins.length; i++) {
            final BigDecimal value = BigDecimal.valueOf(BankCoins.valueOf(COIN_IDS[i]));
            long count = remaining.divideToIntegralValue(value).longValue();
            remaining = remaining.subtract(value.multiply(BigDecimal.valueOf(count)));
            final int stackSize = new ItemStack(coins[i]).getMaxStackSize();
            while (count > 0) {
                final int size = (int) Math.min(count, stackSize);
                dropStack(player, new ItemStack(coins[i], size));
                count -= size;
            }
        }
        return remaining;
    }

    private static void dropStack(final ServerPlayer player, final ItemStack stack) {
        final Level level = player.level();
        // Where the player stands, without vanilla's toss, as 3.5's
        final ItemEntity item = new ItemEntity(level, player.getX(), player.getY(), player.getZ(), stack, 0, 0, 0);
        item.setDefaultPickUpDelay();
        level.addFreshEntity(item);
    }

    /**
     * @return the coin items, largest first, or null if the content doesn't have them all
     */
    private static Item @Nullable [] coins() {
        if (!coinsChecked) {
            coinsChecked = true;
            final Item[] found = new Item[COIN_IDS.length];
            for (int i = 0; i < COIN_IDS.length; i++) {
                found[i] = BuiltInRegistries.ITEM.getOptional(Identifier.parse(COIN_IDS[i])).orElse(null);
                if (found[i] == null) {
                    Almura.LOGGER.warn("Death penalty off: coin {} isn't registered", COIN_IDS[i]);
                    return null;
                }
            }
            coins = found;
        }
        return coins;
    }

    private record Death(ResourceKey<Level> dimension, Vec3 position, float yRot, float xRot) {
    }

    private record Revival(Death death, int respawnTick) {
    }
}
