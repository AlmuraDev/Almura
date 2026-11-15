/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.membership.server;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.economy.Economy;
import com.almuradev.almura.feature.economy.EconomyAccounts;
import com.almuradev.almura.feature.economy.EconomyAccounts.Account;
import com.almuradev.almura.feature.membership.Membership;
import com.almuradev.almura.feature.membership.network.MembershipMessages.ClientboundApplied;
import com.almuradev.almura.feature.membership.network.MembershipMessages.ClientboundOpen;
import com.almuradev.almura.feature.notification.server.ServerNotifications;
import com.almuradev.almura.feature.skills.server.ServerSkills;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import net.neoforged.neoforge.server.permission.nodes.PermissionTypes;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;

/**
 * Memberships on the server, as 3.5's {@code MembershipHandler}: a player on LuckPerms' {@code members} track (survivor, citizen, explorer,
 * pioneer) takes a higher membership by paying its in-game price, or by reaching its skills total (with the Skills plugin). Taking one
 * puts them in its group on that track, through LuckPerms' console command as 3.5 did.
 * <p>
 * Needs LuckPerms (the player's group) and, to buy, the server's economy; without them the window says the service is offline.
 * <p>
 * Permissions, as 3.5's (a permission plugin such as LuckPerms grants them; without one only operators have them):
 * <ul>
 *     <li>{@code almura.membership.gui.open}: open the memberships window;</li>
 *     <li>{@code almura.membership.purchase}: take a membership;</li>
 *     <li>{@code almura.membership.admin}: sent to the window, as 3.5 did (it shows nothing more yet).</li>
 * </ul>
 */
public final class ServerMemberships {

    /** The LuckPerms track the memberships climb, as 3.5's. */
    public static final String TRACK = "members";

    public static final PermissionNode<Boolean> OPEN = node("membership.gui.open");
    public static final PermissionNode<Boolean> PURCHASE = node("membership.purchase");
    public static final PermissionNode<Boolean> ADMIN = node("membership.admin");

    private static final boolean LUCKPERMS = present("net.luckperms.api.LuckPermsProvider");
    private static final String TITLE = "Memberships";

    private static PermissionNode<Boolean> node(final String path) {
        return new PermissionNode<>(Almura.ID, path, PermissionTypes.BOOLEAN,
            (player, uuid, context) -> player != null && player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER));
    }

    @SubscribeEvent
    public void onPermissionNodes(final PermissionGatherEvent.Nodes event) {
        event.addNodes(OPEN, PURCHASE, ADMIN);
    }

    /**
     * Opens the memberships window (the Features menu, the Membership Exchange).
     */
    public static void open(final ServerPlayer player) {
        if (!PermissionAPI.getPermission(player, OPEN)) {
            ServerNotifications.sendPopup(player, "Insufficient Permissions", "Missing [almura.membership.gui.open]", 5);
            return;
        }
        final EconomyAccounts economy = Economy.accounts();
        final String group = group(player);
        if (economy == null || group == null) {
            ServerNotifications.sendPopup(player, "§c" + TITLE, "Economy or Permission Service offline!", 5);
            return;
        }
        final BigDecimal balance = economy.balance(Account.player(player.getUUID()));
        PacketDistributor.sendToPlayer(player, new ClientboundOpen(PermissionAPI.getPermission(player, ADMIN), skillsTotal(player),
            (balance == null ? BigDecimal.ZERO : balance).toPlainString(), Membership.levelOf(group)));
    }

    /**
     * The player takes a membership: bought with in-game money, or earned with their skills total. Everything the window checked is
     * checked again, as 3.5 did against a modified client.
     */
    public static void upgrade(final ServerPlayer player, final int level, final boolean bySkills) {
        if (!PermissionAPI.getPermission(player, PURCHASE)) {
            ServerNotifications.sendPopup(player, "§cPermission Denied", "Missing: almura.membership.purchase", 5);
            return;
        }
        final Membership membership = Membership.byLevel(level);
        final String group = group(player);
        if (membership == null || group == null) {
            ServerNotifications.sendPopup(player, "§c" + TITLE, "Economy or Permission Service offline!", 5);
            return;
        }
        if (!membership.availableFrom(Membership.levelOf(group))) {
            ServerNotifications.sendPopup(player, "§c" + TITLE, "You can't take the " + membership.displayName() + " membership from "
                + group + ".", 5);
            return;
        }
        if (bySkills) {
            final int total = skillsTotal(player);
            if (total < membership.skillsTotal()) {
                ServerNotifications.sendPopup(player, "§c" + TITLE, "Your skills total (" + Math.max(0, total) + ") is below "
                    + membership.skillsTotal() + ".", 5);
                return;
            }
            setGroup(player, membership);
            ServerNotifications.sendPopup(player, "Membership Upgrade", "You've been upgraded to: " + membership.displayName() + "!", 5);
            return;
        }
        final EconomyAccounts economy = Economy.accounts();
        if (economy == null) {
            ServerNotifications.sendPopup(player, "§c" + TITLE, "Economy or Permission Service offline!", 5);
            return;
        }
        final Account wallet = Account.player(player.getUUID());
        final BigDecimal fee = BigDecimal.valueOf(membership.fee());
        final BigDecimal balance = economy.balance(wallet);
        if (balance == null || balance.compareTo(fee) < 0) {
            ServerNotifications.sendPopup(player, "§cInsufficient Funds", "Insufficient Funds to purchase " + membership.displayName()
                + " Membership.", 5);
            return;
        }
        // The money first: the membership only follows once it is paid (3.5 promoted first and withdrew after)
        if (!economy.withdraw(wallet, fee)) {
            ServerNotifications.sendPopup(player, "§c" + TITLE, "The payment didn't go through; nothing was taken.", 5);
            return;
        }
        setGroup(player, membership);
        PacketDistributor.sendToPlayer(player, new ClientboundApplied(membership.displayName()));
    }

    /**
     * Puts the player in the membership's group on the members track: LuckPerms' {@code parent settrack}, from the console, where 3.5
     * promoted along the track once per step.
     */
    private static void setGroup(final ServerPlayer player, final Membership membership) {
        final MinecraftServer server = player.level().getServer();
        final String command = "lp user " + player.getUUID() + " parent settrack " + TRACK + " " + membership.group();
        Almura.LOGGER.info("{} took the {} membership: {}", player.getGameProfile().name(), membership.displayName(), command);
        server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), command);
    }

    /**
     * @return the player's skills total, or -1 without Skills (the window then hides the skills buttons, as 3.5's)
     */
    private static int skillsTotal(final ServerPlayer player) {
        return ServerSkills.totalLevel(player);
    }

    private static @Nullable String group(final ServerPlayer player) {
        return LUCKPERMS ? LuckPermsGroups.primaryGroup(player.getUUID()) : null;
    }

    private static boolean present(final String className) {
        try {
            Class.forName(className, false, ServerMemberships.class.getClassLoader());
            return true;
        } catch (final ClassNotFoundException e) {
            return false;
        }
    }
}
