/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.store.server;

import com.almuradev.almura.Almura;
import com.almuradev.almura.core.database.AlmuraDatabase;
import com.almuradev.almura.feature.economy.Economy;
import com.almuradev.almura.feature.economy.EconomyAccounts;
import com.almuradev.almura.feature.economy.EconomyAccounts.Account;
import com.almuradev.almura.feature.notification.server.ServerNotifications;
import com.almuradev.almura.feature.store.StoreNumbers;
import com.almuradev.almura.feature.store.StoreSide;
import com.almuradev.almura.feature.store.network.StoreMessages.ClientboundItems;
import com.almuradev.almura.feature.store.network.StoreMessages.ClientboundOpen;
import com.almuradev.almura.feature.store.network.StoreMessages.ClientboundRegistry;
import com.almuradev.almura.feature.store.network.StoreMessages.ItemView;
import com.almuradev.almura.feature.store.network.StoreMessages.ModifyCandidate;
import com.almuradev.almura.feature.store.network.StoreMessages.ServerboundDelistItems;
import com.almuradev.almura.feature.store.network.StoreMessages.ServerboundListItem;
import com.almuradev.almura.feature.store.network.StoreMessages.ServerboundModifyItems;
import com.almuradev.almura.feature.store.network.StoreMessages.ServerboundModifyStore;
import com.almuradev.almura.feature.store.network.StoreMessages.ServerboundOpen;
import com.almuradev.almura.feature.store.network.StoreMessages.ServerboundTransaction;
import com.almuradev.almura.feature.store.network.StoreMessages.StoreInfo;
import com.google.gson.JsonParser;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.JsonOps;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.server.players.NameAndId;
import net.minecraft.util.Prediction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import net.neoforged.neoforge.server.permission.nodes.PermissionTypes;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionException;
import java.util.regex.Pattern;

/**
 * Stores on the server, as 3.5's {@code ServerStoreManager}: stores made by admins in the management screen, each with items players buy
 * from it and items they sell to it, at the prices and quantities (or without end) the store's admins list, in the order they arrange.
 * Money goes through the server's economy (SpongeNeo with an economy plugin such as EconomyLite); without one there are no stores. Stores,
 * listings and transactions are in Almura's database ({@link AlmuraDatabase}); stores and listings are kept in memory.
 * <p>
 * Permissions, as 3.5's (a permission plugin such as LuckPerms grants them; without one only operators have them):
 * <ul>
 *     <li>{@code almura.store.admin}: manage stores (the Features menu's Manage Stores), see hidden ones, and list, change and unlist items
 *     in a store's edit mode;</li>
 *     <li>{@code almura.store.add}, {@code almura.store.modify}, {@code almura.store.delete}: add, change and delete stores;</li>
 *     <li>{@code almura.store.manage}, {@code almura.store.open}: the {@code /almura store manage [player]} and
 *     {@code /almura store open <player> <id>} commands (for NPCs and command blocks);</li>
 *     <li>each store's own permission, typed in the management screen: open and use that store. As the exchanges' permissions, each is
 *     registered when the server starts (so the permission plugin answers for it); a store added, or given another permission, since then is
 *     for operators until the next start.</li>
 * </ul>
 */
public final class ServerStore {

    private static final String TITLE = "Store";
    private static final Pattern ID = Pattern.compile("[a-z0-9_.]+");
    private static final BigDecimal MAX_PRICE = BigDecimal.valueOf(StoreNumbers.TEN_MILLION);

    public static final PermissionNode<Boolean> ADMIN = node("store.admin");
    public static final PermissionNode<Boolean> ADD = node("store.add");
    public static final PermissionNode<Boolean> MODIFY = node("store.modify");
    public static final PermissionNode<Boolean> DELETE = node("store.delete");
    public static final PermissionNode<Boolean> MANAGE_COMMAND = node("store.manage");
    public static final PermissionNode<Boolean> OPEN_COMMAND = node("store.open");

    private static final Map<String, Store> stores = new LinkedHashMap<>();
    // Each store's own permission, registered at the server's start
    private static final Map<String, PermissionNode<Boolean>> storeNodes = new HashMap<>();
    private static boolean ready;

    /**
     * A store and its listings on both sides, in shop order.
     */
    private static final class Store {

        final String id;
        final Instant created;
        final UUID creator;
        String name, permission;
        boolean hidden;
        final List<Listing> buy = new ArrayList<>(), sell = new ArrayList<>();

        Store(final String id, final Instant created, final UUID creator, final String name, final String permission, final boolean hidden) {
            this.id = id;
            this.created = created;
            this.creator = creator;
            this.name = name;
            this.permission = permission;
            this.hidden = hidden;
        }

        List<Listing> side(final StoreSide side) {
            return side == StoreSide.BUY ? this.buy : this.sell;
        }

        void sort() {
            final Comparator<Listing> order = Comparator.<Listing>comparingInt(listing -> listing.index).thenComparingInt(listing -> listing.recNo);
            this.buy.sort(order);
            this.sell.sort(order);
        }
    }

    /**
     * A listing: its item (one of it), how many are left ({@code -1} for no end), the price of one and its place in the shop order.
     */
    private static final class Listing {

        final int recNo;
        final Instant created;
        final ItemStack stack;
        int quantity, index;
        BigDecimal price;

        Listing(final int recNo, final Instant created, final ItemStack stack, final int quantity, final BigDecimal price, final int index) {
            this.recNo = recNo;
            this.created = created;
            this.stack = stack;
            this.quantity = quantity;
            this.price = price;
            this.index = index;
        }

        boolean infinite() {
            return this.quantity == StoreNumbers.UNLIMITED;
        }

        ItemView view() {
            return new ItemView(this.recNo, this.created.toEpochMilli(), this.stack, this.quantity, this.price.toPlainString(), this.index);
        }
    }

    private static PermissionNode<Boolean> node(final String path) {
        return new PermissionNode<>(Almura.ID, path, PermissionTypes.BOOLEAN,
            (player, uuid, context) -> player != null && isOperator(player));
    }

    private static boolean isOperator(final ServerPlayer player) {
        return player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
    }

    /**
     * Reads the stores here, at the server's start, so each store's own permission is registered (as the exchanges' are).
     */
    @SubscribeEvent
    public void onPermissionNodes(final PermissionGatherEvent.Nodes event) {
        event.addNodes(ADMIN, ADD, MODIFY, DELETE, MANAGE_COMMAND, OPEN_COMMAND);
        stores.clear();
        storeNodes.clear();
        ready = false;
        // Only where there can be an economy: a server without Sponge has no stores
        if (!Economy.available()) {
            return;
        }
        try {
            AlmuraDatabase.install("store").join();
            stores.putAll(build(AlmuraDatabase.submit(ServerStore::readStores).join()));
        } catch (final CompletionException e) {
            Almura.LOGGER.error("Couldn't read the stores; stores are unavailable", e.getCause() == null ? e : e.getCause());
            return;
        }
        ready = true;
        Almura.LOGGER.info("Loaded {} store(s)", stores.size());
        for (final Store store : stores.values()) {
            final PermissionNode<Boolean> node = storeNode(store.permission);
            if (node != null && !storeNodes.containsKey(store.permission)) {
                storeNodes.put(store.permission, node);
                event.addNodes(node);
            }
        }
    }

    /**
     * @return the node for a full permission name ({@code namespace.path}), or null if it can't be one
     */
    private static @Nullable PermissionNode<Boolean> storeNode(final String name) {
        final int dot = name.indexOf('.');
        if (dot <= 0 || dot == name.length() - 1) {
            return null;
        }
        try {
            return new PermissionNode<>(name.substring(0, dot), name.substring(dot + 1), PermissionTypes.BOOLEAN,
                (player, uuid, context) -> player != null && isOperator(player));
        } catch (final RuntimeException e) {
            Almura.LOGGER.warn("'{}' can't be a permission; only operators have it", name);
            return null;
        }
    }

    @SubscribeEvent
    public void onServerStarted(final ServerStartedEvent event) {
        if (ready) {
            syncRegistryToAll();
        }
    }

    @SubscribeEvent
    public void onServerStopped(final ServerStoppedEvent event) {
        ready = false;
        stores.clear();
        storeNodes.clear();
    }

    @SubscribeEvent
    public void onLoggedIn(final PlayerEvent.PlayerLoggedInEvent event) {
        if (ready && event.getEntity() instanceof final ServerPlayer player) {
            syncRegistryTo(player);
        }
    }

    /**
     * 3.5's {@code /almura store manage [player]} and {@code /almura store open <player> <id>}.
     */
    @SubscribeEvent
    public void onRegisterCommands(final RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("almura")
            .then(Commands.literal("store")
                .then(Commands.literal("manage")
                    .requires(source -> allowed(source, MANAGE_COMMAND))
                    .executes(context -> manage(context, context.getSource().getPlayerOrException()))
                    .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> manage(context, EntityArgument.getPlayer(context, "player")))))
                .then(Commands.literal("open")
                    .requires(source -> allowed(source, OPEN_COMMAND))
                    .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("id", StringArgumentType.string())
                            .executes(ServerStore::openCommand))))));
    }

    // The console and command blocks may; a player needs the command's permission
    private static boolean allowed(final CommandSourceStack source, final PermissionNode<Boolean> node) {
        final ServerPlayer player = source.getPlayer();
        return player == null || PermissionAPI.getPermission(player, node);
    }

    private static int manage(final CommandContext<CommandSourceStack> context, final ServerPlayer player) {
        openManage(player);
        return 1;
    }

    private static int openCommand(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final ServerPlayer player = EntityArgument.getPlayer(context, "player");
        final String id = StringArgumentType.getString(context, "id");
        final Store store = stores.get(id);
        if (store == null) {
            error(player, "§c" + id + "§r does not exist!");
            context.getSource().sendFailure(Component.literal("There is no store " + id + "."));
            return 0;
        }
        openStore(player, store);
        return 1;
    }

    // ---- Opening

    public static void handleOpen(final ServerPlayer player, final ServerboundOpen message) {
        if (message.manage()) {
            openManage(player);
            return;
        }
        final Store store = stores.get(message.id());
        if (store == null) {
            syncRegistryTo(player);
            return;
        }
        openStore(player, store);
    }

    private static void openManage(final ServerPlayer player) {
        if (unavailable(player)) {
            return;
        }
        if (!PermissionAPI.getPermission(player, ADMIN)) {
            error(player, "You do not have permission to manage stores!");
            return;
        }
        syncRegistryTo(player);
        PacketDistributor.sendToPlayer(player, new ClientboundOpen(true, "", true));
    }

    private static void openStore(final ServerPlayer player, final Store store) {
        if (unavailable(player)) {
            return;
        }
        if (!mayUse(player, store)) {
            error(player, "You do not have permission to open this store!");
            return;
        }
        syncRegistryTo(player);
        PacketDistributor.sendToPlayer(player, new ClientboundOpen(false, store.id, PermissionAPI.getPermission(player, ADMIN)));
        sendItems(player, store, StoreSide.BUY);
        sendItems(player, store, StoreSide.SELL);
    }

    /**
     * Whether the player may open and use the store: its own permission, and a hidden one only for store admins.
     */
    private static boolean mayUse(final ServerPlayer player, final Store store) {
        if (store.hidden && !PermissionAPI.getPermission(player, ADMIN)) {
            return false;
        }
        // Registered at the server's start, else (a store added, or given another permission, since) only operators
        final PermissionNode<Boolean> node = storeNodes.get(store.permission);
        return node == null ? isOperator(player) : PermissionAPI.getPermission(player, node);
    }

    private static boolean unavailable(final ServerPlayer player) {
        if (Economy.accounts() == null) {
            error(player, "Stores are unavailable: there is no economy on this server.");
            return true;
        }
        if (!ready) {
            error(player, "Stores are unavailable right now, try again later.");
            return true;
        }
        return false;
    }

    // ---- Stores (the management screen)

    public static void handleModifyStore(final ServerPlayer player, final ServerboundModifyStore message) {
        if (unavailable(player)) {
            return;
        }
        final String id = message.id().toLowerCase();
        final String name = message.name().trim();
        final String permission = message.permission().toLowerCase().trim();
        final Store existing = stores.get(id);
        switch (message.change()) {
            case ADD -> {
                if (!PermissionAPI.getPermission(player, ADD)) {
                    error(player, "You do not have permission to add stores!");
                    return;
                }
                if (existing != null || !ID.matcher(id).matches() || name.isEmpty() || !ID.matcher(permission).matches()) {
                    syncRegistryTo(player);
                    return;
                }
                final Instant created = Instant.now();
                final UUID creator = player.getUUID();
                AlmuraDatabase.run("add store " + id, connection -> {
                    try (PreparedStatement insert = connection.prepareStatement(
                        "insert into store (id, created, creator, name, permission, is_hidden) values (?, ?, ?, ?, ?, ?)")) {
                        insert.setString(1, id);
                        insert.setTimestamp(2, Timestamp.from(created));
                        insert.setObject(3, creator);
                        insert.setString(4, name);
                        insert.setString(5, permission);
                        insert.setBoolean(6, message.hidden());
                        insert.executeUpdate();
                    }
                    return null;
                }, result -> {
                    stores.put(id, new Store(id, created, creator, name, permission, message.hidden()));
                    syncRegistryToAll();
                });
            }
            case MODIFY -> {
                if (!PermissionAPI.getPermission(player, MODIFY)) {
                    error(player, "You do not have permission to modify stores!");
                    return;
                }
                if (existing == null || name.isEmpty() || !ID.matcher(permission).matches()) {
                    syncRegistryTo(player);
                    return;
                }
                AlmuraDatabase.run("change store " + id, connection -> {
                    try (PreparedStatement update = connection.prepareStatement("update store set name = ?, permission = ?, is_hidden = ? where id = ?")) {
                        update.setString(1, name);
                        update.setString(2, permission);
                        update.setBoolean(3, message.hidden());
                        update.setString(4, id);
                        update.executeUpdate();
                    }
                    return null;
                }, result -> {
                    existing.name = name;
                    existing.permission = permission;
                    existing.hidden = message.hidden();
                    syncRegistryToAll();
                });
            }
            case DELETE -> {
                if (!PermissionAPI.getPermission(player, DELETE)) {
                    error(player, "You do not have permission to delete stores!");
                    return;
                }
                if (existing == null) {
                    syncRegistryTo(player);
                    return;
                }
                AlmuraDatabase.run("delete store " + id, connection -> {
                    // Its listings and their transactions go with it (on delete cascade)
                    try (PreparedStatement delete = connection.prepareStatement("delete from store where id = ?")) {
                        delete.setString(1, id);
                        delete.executeUpdate();
                    }
                    return null;
                }, result -> {
                    stores.remove(id);
                    syncRegistryToAll();
                });
            }
        }
    }

    // ---- Listings (a store's edit mode)

    public static void handleListItem(final ServerPlayer player, final ServerboundListItem message) {
        final Store store = adminStore(player, message.id());
        if (store == null) {
            return;
        }
        final BigDecimal price = validPrice(message.price());
        final int quantity = message.quantity();
        if (message.stack().isEmpty() || price == null || quantity == 0 || quantity < StoreNumbers.UNLIMITED || quantity > StoreNumbers.TEN_MILLION) {
            sendItemsToAll(store, message.side());
            return;
        }
        final ItemStack stack = message.stack().copyWithCount(1);
        final String json;
        try {
            json = encode(player.level().getServer(), stack);
        } catch (final RuntimeException e) {
            Almura.LOGGER.error("Couldn't save {} for store {}", stack, store.id, e);
            error(player, "That item couldn't be saved, see the server's log.");
            return;
        }
        final Instant created = Instant.now();
        final StoreSide side = message.side();
        final int index = Math.max(0, message.index());
        AlmuraDatabase.run("list an item in store " + store.id, connection -> {
            try (PreparedStatement insert = connection.prepareStatement("insert into store_item (created, store, side, item, quantity, price, "
                + "sort_index, is_hidden) values (?, ?, ?, ?, ?, ?, ?, false)", Statement.RETURN_GENERATED_KEYS)) {
                insert.setTimestamp(1, Timestamp.from(created));
                insert.setString(2, store.id);
                insert.setString(3, side.name());
                insert.setString(4, json);
                insert.setInt(5, quantity);
                insert.setBigDecimal(6, price);
                insert.setInt(7, index);
                insert.executeUpdate();
                try (ResultSet keys = insert.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("No record number for the new listing");
                    }
                    return keys.getInt(1);
                }
            }
        }, recNo -> {
            store.side(side).add(new Listing(recNo, created, stack, quantity, price, index));
            store.sort();
            sendItemsToAll(store, side);
        });
    }

    public static void handleModifyItems(final ServerPlayer player, final ServerboundModifyItems message) {
        final Store store = adminStore(player, message.id());
        if (store == null) {
            return;
        }
        final List<Listing> listings = store.side(message.side());
        final List<Listing> changed = new ArrayList<>();
        final List<ModifyCandidate> values = new ArrayList<>();
        for (final ModifyCandidate candidate : message.items()) {
            final Listing found = listings.stream().filter(listing -> listing.recNo == candidate.recNo()).findAny().orElse(null);
            final BigDecimal price = validPrice(candidate.price());
            if (found == null || price == null || candidate.quantity() < StoreNumbers.UNLIMITED || candidate.quantity() > StoreNumbers.TEN_MILLION
                || candidate.index() < 0) {
                continue; // A de-sync, or someone trying something
            }
            changed.add(found);
            values.add(new ModifyCandidate(candidate.recNo(), candidate.quantity(), candidate.index(), price.toPlainString()));
        }
        if (changed.isEmpty()) {
            sendItemsToAll(store, message.side());
            return;
        }
        AlmuraDatabase.run("change items in store " + store.id, connection -> {
            try (PreparedStatement update = connection.prepareStatement("update store_item set quantity = ?, sort_index = ?, price = ? where rec_no = ?")) {
                for (final ModifyCandidate value : values) {
                    update.setInt(1, value.quantity());
                    update.setInt(2, value.index());
                    update.setBigDecimal(3, new BigDecimal(value.price()));
                    update.setInt(4, value.recNo());
                    update.addBatch();
                }
                update.executeBatch();
            }
            return null;
        }, result -> {
            for (int i = 0; i < changed.size(); i++) {
                final Listing listing = changed.get(i);
                final ModifyCandidate value = values.get(i);
                listing.quantity = value.quantity();
                listing.index = value.index();
                listing.price = new BigDecimal(value.price());
            }
            store.sort();
            sendItemsToAll(store, message.side());
        });
    }

    public static void handleDelistItems(final ServerPlayer player, final ServerboundDelistItems message) {
        final Store store = adminStore(player, message.id());
        if (store == null) {
            return;
        }
        final List<Listing> listings = store.side(message.side());
        final List<Integer> recNos = message.recNos().stream()
            .filter(recNo -> listings.stream().anyMatch(listing -> listing.recNo == recNo)).toList();
        if (recNos.isEmpty()) {
            sendItemsToAll(store, message.side());
            return;
        }
        AlmuraDatabase.run("unlist items in store " + store.id, connection -> {
            // Hidden, not deleted, so their transactions stay
            try (PreparedStatement update = connection.prepareStatement("update store_item set is_hidden = true where rec_no = ?")) {
                for (final int recNo : recNos) {
                    update.setInt(1, recNo);
                    update.addBatch();
                }
                update.executeBatch();
            }
            return null;
        }, result -> {
            listings.removeIf(listing -> recNos.contains(listing.recNo));
            sendItemsToAll(store, message.side());
        });
    }

    private static @Nullable Store adminStore(final ServerPlayer player, final String id) {
        if (unavailable(player)) {
            return null;
        }
        if (!PermissionAPI.getPermission(player, ADMIN)) {
            error(player, "You do not have permission to change store items!");
            return null;
        }
        final Store store = stores.get(id);
        if (store == null) {
            syncRegistryTo(player);
        }
        return store;
    }

    // ---- Buying and selling

    public static void handleTransaction(final ServerPlayer player, final ServerboundTransaction message) {
        if (unavailable(player)) {
            return;
        }
        final EconomyAccounts economy = Economy.accounts();
        final Store store = stores.get(message.id());
        if (economy == null || store == null) {
            syncRegistryTo(player);
            return;
        }
        if (!mayUse(player, store)) {
            error(player, "You do not have permission to use this store!");
            return;
        }
        final Listing listing = store.side(message.side()).stream().filter(found -> found.recNo == message.recNo()).findAny().orElse(null);
        final int quantity = message.quantity();
        if (listing == null || quantity <= 0) {
            sendItems(player, store, message.side());
            return;
        }
        if (!listing.infinite() && listing.quantity < quantity) {
            error(player, message.side() == StoreSide.BUY ? "There is not enough quantity left to fulfill your order!"
                : "There is not enough quantity left to sell your product!");
            sendItems(player, store, message.side());
            return;
        }
        final BigDecimal total = listing.price.multiply(BigDecimal.valueOf(quantity));
        final Account wallet = Account.player(player.getUUID());
        final String itemName = listing.stack.getHoverName().getString();
        if (message.side() == StoreSide.BUY) {
            final BigDecimal balance = economy.balance(wallet);
            if (balance == null) {
                error(player, "The store could not reach your wallet, try again later.");
                return;
            }
            if (total.compareTo(balance) > 0) {
                error(player, "You attempted to purchase items totaling §c" + StoreNumbers.currency(total.doubleValue()) + "§r while you only "
                    + "have §a" + StoreNumbers.currency(balance.doubleValue()) + "§r. You need §d"
                    + StoreNumbers.currency(total.subtract(balance).doubleValue()) + "§r more!");
                return;
            }
            if (space(player.getInventory(), listing.stack) < quantity) {
                error(player, "You lack sufficient inventory space to purchase these item(s)!");
                return;
            }
            if (!economy.withdraw(wallet, total)) {
                error(player, "The store could not take the money, nothing was bought.");
                return;
            }
            give(player, listing.stack, quantity);
            popup(player, "Purchased §b" + quantity + "§f x §e" + itemName + "§r for a total of §6$" + StoreNumbers.currency(total.doubleValue()));
        } else {
            final Inventory inventory = player.getInventory();
            if (held(inventory, listing.stack) < quantity) {
                error(player, "You don't have that many to sell.");
                return;
            }
            take(inventory, listing.stack, quantity);
            if (!economy.deposit(wallet, total)) {
                // Give them back: the money didn't go in
                give(player, listing.stack, quantity);
                error(player, "The store could not pay you, you still have your items.");
                return;
            }
            popup(player, "Sold §b" + quantity + "§f x §e" + itemName + "§r for a total of §6$" + StoreNumbers.currency(total.doubleValue()));
        }
        if (!listing.infinite()) {
            listing.quantity -= quantity;
            sendItemsToAll(store, message.side());
        }
        record(listing, message.side(), player.getUUID(), quantity);
    }

    private static void record(final Listing listing, final StoreSide side, final UUID player, final int quantity) {
        final int remaining = listing.quantity;
        final boolean infinite = listing.infinite();
        final BigDecimal price = listing.price;
        AlmuraDatabase.change("record a store transaction on listing " + listing.recNo + " by " + player, connection -> {
            if (!infinite) {
                try (PreparedStatement update = connection.prepareStatement("update store_item set quantity = ? where rec_no = ?")) {
                    update.setInt(1, remaining);
                    update.setInt(2, listing.recNo);
                    update.executeUpdate();
                }
            }
            try (PreparedStatement insert = connection.prepareStatement(
                "insert into store_transaction (created, store_item, side, player, quantity, price) values (?, ?, ?, ?, ?, ?)")) {
                insert.setTimestamp(1, Timestamp.from(Instant.now()));
                insert.setInt(2, listing.recNo);
                insert.setString(3, side.name());
                insert.setObject(4, player);
                insert.setInt(5, quantity);
                insert.setBigDecimal(6, price);
                insert.executeUpdate();
            }
            return null;
        });
    }

    /**
     * @return how many of the item the player's inventory (hotbar and main) has room for
     */
    private static int space(final Inventory inventory, final ItemStack item) {
        final int max = item.getMaxStackSize();
        int space = 0;
        for (final ItemStack slot : inventory.getNonEquipmentItems()) {
            if (slot.isEmpty()) {
                space += max;
            } else if (ItemStack.isSameItemSameComponents(slot, item)) {
                space += Math.max(0, max - slot.getCount());
            }
        }
        return space;
    }

    private static int held(final Inventory inventory, final ItemStack item) {
        int held = 0;
        for (final ItemStack slot : inventory.getNonEquipmentItems()) {
            if (ItemStack.isSameItemSameComponents(slot, item)) {
                held += slot.getCount();
            }
        }
        return held;
    }

    private static void take(final Inventory inventory, final ItemStack item, final int quantity) {
        int left = quantity;
        for (final ItemStack slot : inventory.getNonEquipmentItems()) {
            if (left <= 0) {
                break;
            }
            if (ItemStack.isSameItemSameComponents(slot, item)) {
                final int taken = Math.min(left, slot.getCount());
                slot.shrink(taken);
                left -= taken;
            }
        }
        inventory.setChanged();
    }

    /**
     * Gives the player that many of the item, in full stacks; what doesn't fit is dropped at their feet.
     */
    private static void give(final ServerPlayer player, final ItemStack item, final int quantity) {
        int left = quantity;
        while (left > 0) {
            final ItemStack stack = item.copyWithCount(Math.min(left, item.getMaxStackSize()));
            left -= stack.getCount();
            // What doesn't fit is dropped at their feet
            player.getInventory().placeItemBackInInventory(stack, Prediction.SERVER_ONLY);
        }
    }

    // ---- Sending

    private static void syncRegistryToAll() {
        final MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            server.getPlayerList().getPlayers().forEach(ServerStore::syncRegistryTo);
        }
    }

    private static void syncRegistryTo(final ServerPlayer player) {
        final boolean admin = PermissionAPI.getPermission(player, ADMIN);
        final List<StoreInfo> visible = stores.values().stream()
            .filter(store -> !store.hidden || admin)
            .map(store -> new StoreInfo(store.id, store.name, store.permission, store.hidden, store.creator, name(store.creator),
                store.created.toEpochMilli()))
            .toList();
        PacketDistributor.sendToPlayer(player, new ClientboundRegistry(visible));
    }

    private static void sendItems(final ServerPlayer player, final Store store, final StoreSide side) {
        PacketDistributor.sendToPlayer(player, new ClientboundItems(store.id, side, store.side(side).stream().map(Listing::view).toList()));
    }

    private static void sendItemsToAll(final Store store, final StoreSide side) {
        PacketDistributor.sendToAllPlayers(new ClientboundItems(store.id, side, store.side(side).stream().map(Listing::view).toList()));
    }

    // ---- Reading

    private record StoreRow(String id, Instant created, UUID creator, String name, String permission, boolean hidden) {
    }

    private record ListingRow(int recNo, Instant created, String store, String side, String item, int quantity, BigDecimal price, int index) {
    }

    private record Rows(List<StoreRow> stores, List<ListingRow> listings) {
    }

    private static Rows readStores(final Connection connection) throws SQLException {
        final List<StoreRow> storeRows = new ArrayList<>();
        try (PreparedStatement select = connection.prepareStatement("select id, created, creator, name, permission, is_hidden from store order by id");
             ResultSet result = select.executeQuery()) {
            while (result.next()) {
                storeRows.add(new StoreRow(result.getString("id"), result.getTimestamp("created").toInstant(), result.getObject("creator", UUID.class),
                    result.getString("name"), result.getString("permission"), result.getBoolean("is_hidden")));
            }
        }
        final List<ListingRow> listingRows = new ArrayList<>();
        try (PreparedStatement select = connection.prepareStatement("select rec_no, created, store, side, item, quantity, price, sort_index "
            + "from store_item where is_hidden = false order by sort_index, rec_no");
             ResultSet result = select.executeQuery()) {
            while (result.next()) {
                listingRows.add(new ListingRow(result.getInt("rec_no"), result.getTimestamp("created").toInstant(), result.getString("store"),
                    result.getString("side"), result.getString("item"), result.getInt("quantity"), result.getBigDecimal("price"),
                    result.getInt("sort_index")));
            }
        }
        return new Rows(storeRows, listingRows);
    }

    /**
     * The stores and their listings from the rows read, items decoded on the server's thread (with its registries).
     */
    private static Map<String, Store> build(final Rows rows) {
        final MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        final Map<String, Store> built = new LinkedHashMap<>();
        for (final StoreRow row : rows.stores()) {
            built.put(row.id(), new Store(row.id(), row.created(), row.creator(), row.name(), row.permission(), row.hidden()));
        }
        for (final ListingRow row : rows.listings()) {
            final Store store = built.get(row.store());
            final StoreSide side = parseSide(row.side());
            final Optional<ItemStack> stack = server == null ? Optional.empty() : decode(server, row.item());
            if (store == null || side == null || stack.isEmpty()) {
                Almura.LOGGER.error("Skipped store listing {} of store {}: its item or side can't be read (was a mod removed?)", row.recNo(),
                    row.store());
                continue;
            }
            store.side(side).add(new Listing(row.recNo(), row.created(), stack.get(), row.quantity(), row.price(), row.index()));
        }
        built.values().forEach(Store::sort);
        return built;
    }

    private static String encode(final MinecraftServer server, final ItemStack stack) {
        return ItemStack.CODEC.encodeStart(server.registryAccess().createSerializationContext(JsonOps.INSTANCE), stack).getOrThrow().toString();
    }

    private static Optional<ItemStack> decode(final MinecraftServer server, final String json) {
        try {
            return ItemStack.CODEC.parse(server.registryAccess().createSerializationContext(JsonOps.INSTANCE), JsonParser.parseString(json)).result();
        } catch (final RuntimeException e) {
            return Optional.empty();
        }
    }

    // ---- Helpers

    private static @Nullable StoreSide parseSide(final String raw) {
        try {
            return StoreSide.valueOf(raw);
        } catch (final IllegalArgumentException e) {
            return null;
        }
    }

    private static @Nullable BigDecimal validPrice(final String raw) {
        final BigDecimal price = StoreNumbers.parse(raw).setScale(2, RoundingMode.DOWN);
        return price.signum() <= 0 || price.compareTo(MAX_PRICE) > 0 ? null : price;
    }

    private static String name(final UUID uniqueId) {
        final MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return "";
        }
        final ServerPlayer online = server.getPlayerList().getPlayer(uniqueId);
        if (online != null) {
            return online.getGameProfile().name();
        }
        return server.services().nameToIdCache().get(uniqueId).map(NameAndId::name).orElse("");
    }

    private static void popup(final ServerPlayer player, final String message) {
        ServerNotifications.sendPopup(player, "§aTransaction Complete", message, 3);
    }

    private static void error(final ServerPlayer player, final String message) {
        ServerNotifications.sendPopup(player, "§c" + TITLE, message, 5);
    }
}
