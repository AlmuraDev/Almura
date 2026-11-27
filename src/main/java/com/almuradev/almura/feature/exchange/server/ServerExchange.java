/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.server;

import com.almuradev.almura.Almura;
import com.almuradev.almura.core.database.AlmuraDatabase;
import com.almuradev.almura.feature.economy.Economy;
import com.almuradev.almura.feature.economy.EconomyAccounts;
import com.almuradev.almura.feature.economy.EconomyAccounts.Account;
import com.almuradev.almura.feature.exchange.ExchangeMoney;
import com.almuradev.almura.feature.exchange.ExchangeSort;
import com.almuradev.almura.feature.exchange.network.ExchangeMessages.ClientboundForSale;
import com.almuradev.almura.feature.exchange.network.ExchangeMessages.ClientboundListItems;
import com.almuradev.almura.feature.exchange.network.ExchangeMessages.ClientboundOpen;
import com.almuradev.almura.feature.exchange.network.ExchangeMessages.ClientboundRefresh;
import com.almuradev.almura.feature.exchange.network.ExchangeMessages.ClientboundRegistry;
import com.almuradev.almura.feature.exchange.network.ExchangeMessages.ClientboundTransactionComplete;
import com.almuradev.almura.feature.exchange.network.ExchangeMessages.Direction;
import com.almuradev.almura.feature.exchange.network.ExchangeMessages.ExchangeInfo;
import com.almuradev.almura.feature.exchange.network.ExchangeMessages.ForSaleView;
import com.almuradev.almura.feature.exchange.network.ExchangeMessages.ListItemView;
import com.almuradev.almura.feature.exchange.network.ExchangeMessages.Move;
import com.almuradev.almura.feature.exchange.network.ExchangeMessages.Screen;
import com.almuradev.almura.feature.exchange.network.ExchangeMessages.ServerboundBuy;
import com.almuradev.almura.feature.exchange.network.ExchangeMessages.ServerboundListStatus;
import com.almuradev.almura.feature.exchange.network.ExchangeMessages.ServerboundModify;
import com.almuradev.almura.feature.exchange.network.ExchangeMessages.ServerboundMoveItems;
import com.almuradev.almura.feature.exchange.network.ExchangeMessages.ServerboundQuery;
import com.almuradev.almura.feature.notification.server.ServerNotifications;
import com.almuradev.almura.core.permission.RuntimePermissions;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletionException;
import java.util.regex.Pattern;

/**
 * The exchanges on the server, as 3.5's {@code ServerExchangeManager}: player to player markets where a player moves items from their
 * inventory onto an exchange (the offer screen), lists them at a price each, and others buy them, the money going from buyer to seller
 * through the server's economy. Exchanges, items, listings and purchases are in Almura's database ({@link AlmuraDatabase}); an exchange's
 * items are read when someone first opens it.
 * <p>
 * Needs the server's economy (SpongeNeo with an economy plugin such as EconomyLite), as banking does; without it there are no exchanges.
 * <p>
 * Permissions, as 3.5's (a permission plugin such as LuckPerms grants them; without one only operators have them):
 * <ul>
 *     <li>{@code almura.exchange.manage}, {@code almura.exchange.open}: the {@code /almura exchange} commands and their requests;</li>
 *     <li>{@code almura.exchange.admin}: the management screen, hidden exchanges, and administering every exchange;</li>
 *     <li>{@code almura.exchange.add}, {@code .modify}, {@code .delete}: change the exchanges;</li>
 *     <li>for each exchange, its permission (such as {@code almura.exchange.global}) followed by {@code .open} (open it), {@code .admin}
 *     (administer it: change any listing's price or delist it, and list up to a million items) and {@code .slots.<n>} for n of 10, 25, 50,
 *     100, 500 and 5000 (how many items the player may list there; none without one). These are registered for the exchanges there when
 *     the server starts; one added later is for operators until the next start.</li>
 * </ul>
 */
public final class ServerExchange {

    private static final String TITLE = "Exchange";
    private static final String GLOBAL_ID = "almura.exchange.global";
    private static final UUID UNKNOWN_OWNER = new UUID(0L, 0L);
    private static final Pattern ID = Pattern.compile("[a-z0-9_.]+");
    private static final int[] SLOTS = {5000, 500, 100, 50, 25, 10};
    private static final int ADMIN_SLOTS = 1_000_000;
    private static final int MAX_PAGE = 50;

    public static final PermissionNode<Boolean> MANAGE = node("exchange.manage");
    public static final PermissionNode<Boolean> OPEN = node("exchange.open");
    public static final PermissionNode<Boolean> ADMIN = node("exchange.admin");
    public static final PermissionNode<Boolean> ADD = node("exchange.add");
    public static final PermissionNode<Boolean> MODIFY = node("exchange.modify");
    public static final PermissionNode<Boolean> DELETE = node("exchange.delete");

    private static final Map<String, Exchange> exchanges = new LinkedHashMap<>();
    // Each exchange's own permissions (open, admin, slots), by their full name, registered at the server's start
    private static boolean ready;

    // ---- What the server keeps

    /**
     * An exchange, and (once someone opens it) the items players have on it, by player.
     */
    private static final class Exchange {

        final String id;
        final Instant created;
        final UUID creator;
        String name;
        String permission;
        boolean hidden;
        boolean loaded, loading;
        final Set<UUID> waiting = new HashSet<>();
        final Map<UUID, List<ListItem>> listItems = new HashMap<>();

        Exchange(final String id, final Instant created, final UUID creator, final String name, final String permission, final boolean hidden) {
            this.id = id;
            this.created = created;
            this.creator = creator;
            this.name = name;
            this.permission = permission;
            this.hidden = hidden;
        }

        List<ListItem> forSale() {
            final List<ListItem> forSale = new ArrayList<>();
            this.listItems.values().forEach(items -> items.stream().filter(item -> item.forSale != null).forEach(forSale::add));
            return forSale;
        }

        Optional<ListItem> find(final int recNo) {
            return this.listItems.values().stream().flatMap(List::stream).filter(item -> item.recNo == recNo).findFirst();
        }
    }

    /**
     * Items a player has on an exchange: one of the item and how many, and its listing when it is for sale.
     */
    private static final class ListItem {

        final int recNo;
        final UUID seller;
        final ItemStack item;
        int quantity;
        final int index;
        @Nullable BigDecimal lastKnownPrice;
        @Nullable ForSale forSale;

        ListItem(final int recNo, final UUID seller, final ItemStack item, final int quantity, final int index,
            final @Nullable BigDecimal lastKnownPrice) {
            this.recNo = recNo;
            this.seller = seller;
            this.item = item;
            this.quantity = quantity;
            this.index = index;
            this.lastKnownPrice = lastKnownPrice;
        }
    }

    private static final class ForSale {

        final int recNo;
        final Instant created;
        BigDecimal price;

        ForSale(final int recNo, final Instant created, final BigDecimal price) {
            this.recNo = recNo;
            this.created = created;
            this.price = price;
        }
    }

    private record ListRow(int recNo, UUID seller, String item, int quantity, int index, @Nullable BigDecimal lastKnownPrice) {
    }

    private record SaleRow(int recNo, Instant created, int listItem, BigDecimal price) {
    }

    private record Rows(List<ListRow> items, List<SaleRow> sales) {
    }

    // ---- Permissions and start

    private static PermissionNode<Boolean> node(final String path) {
        return new PermissionNode<>(Almura.ID, path, PermissionTypes.BOOLEAN,
            (player, uuid, context) -> player != null && isOperator(player));
    }

    private static boolean isOperator(final ServerPlayer player) {
        return player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
    }

    /**
     * Reads the exchanges here, at the server's start (making the global one if there are none, as 3.5 did).
     */
    @SubscribeEvent
    public void onPermissionNodes(final PermissionGatherEvent.Nodes event) {
        event.addNodes(MANAGE, OPEN, ADMIN, ADD, MODIFY, DELETE);
        exchanges.clear();
        ready = false;
        if (!Economy.available()) {
            return;
        }
        try {
            AlmuraDatabase.install("exchange").join();
            final List<Exchange> loaded = AlmuraDatabase.submit(ServerExchange::readExchanges).join();
            if (loaded.isEmpty()) {
                final Exchange global = new Exchange(GLOBAL_ID, Instant.now(), UNKNOWN_OWNER, "Global Exchange", GLOBAL_ID, false);
                AlmuraDatabase.submit(connection -> {
                    insertExchange(connection, global);
                    return null;
                }).join();
                loaded.add(global);
            }
            loaded.forEach(exchange -> {
                exchanges.put(exchange.id, exchange);
                Almura.LOGGER.info("Loaded exchange '{}' ({})", exchange.name, exchange.id);
            });
            ready = true;
            Almura.LOGGER.info("Loaded {} exchange(s)", exchanges.size());
        } catch (final CompletionException e) {
            Almura.LOGGER.error("Couldn't read the exchanges; they are unavailable", e.getCause() == null ? e : e.getCause());
            return;
        }
    }

    /**
     * Whether the player has one of an exchange's permissions, asked of LuckPerms by name as 3.5 asked Sponge, so an exchange added while the
     * server runs works straight away (without LuckPerms, only operators).
     */
    private static boolean has(final ServerPlayer player, final String name) {
        return RuntimePermissions.has(player, name);
    }

    private static boolean isAdmin(final ServerPlayer player, final Exchange exchange) {
        return has(player, exchange.permission + ".admin") || PermissionAPI.getPermission(player, ADMIN);
    }

    /**
     * @return how many items the player may list on the exchange, as 3.5: a million for its administrators, else the largest of its slot
     *     permissions, else none
     */
    private static int listingsLimit(final ServerPlayer player, final Exchange exchange) {
        if (has(player, exchange.permission + ".admin")) {
            return ADMIN_SLOTS;
        }
        for (final int slots : SLOTS) {
            if (has(player, exchange.permission + ".slots." + slots)) {
                return slots;
            }
        }
        return 0;
    }

    @SubscribeEvent
    public void onLoggedIn(final PlayerEvent.PlayerLoggedInEvent event) {
        if (ready && event.getEntity() instanceof final ServerPlayer player) {
            syncRegistry(player);
        }
    }

    @SubscribeEvent
    public void onServerStopped(final ServerStoppedEvent event) {
        ready = false;
        exchanges.clear();
    }

    // ---- Commands

    /**
     * {@code /almura exchange} (or {@code axs}) {@code manage [player]} and {@code open [player] <id>}, as 3.5's (whose NPCs and command
     * blocks name the player first, as the store's command does).
     */
    @SubscribeEvent
    public void onRegisterCommands(final RegisterCommandsEvent event) {
        for (final String name : new String[] {"exchange", "axs"}) {
            event.getDispatcher().register(Commands.literal("almura").then(command(name)));
        }
    }

    private static LiteralArgumentBuilder<CommandSourceStack> command(final String name) {
        return Commands.literal(name)
            .then(Commands.literal("manage").requires(source -> allowed(source, MANAGE))
                .executes(context -> manage(context, context.getSource().getPlayerOrException()))
                .then(Commands.argument("player", EntityArgument.player())
                    .executes(context -> manage(context, EntityArgument.getPlayer(context, "player")))))
            // The id alone is registered first: given one word, both branches read it and the first that can run is taken
            .then(Commands.literal("open").requires(source -> allowed(source, OPEN))
                .then(Commands.argument("id", StringArgumentType.string())
                    .executes(context -> open(context, context.getSource().getPlayerOrException())))
                .then(Commands.argument("player", EntityArgument.player())
                    .then(Commands.argument("id", StringArgumentType.string())
                        .executes(context -> open(context, EntityArgument.getPlayer(context, "player"))))));
    }

    private static boolean allowed(final CommandSourceStack source, final PermissionNode<Boolean> node) {
        final ServerPlayer player = source.getPlayer();
        return player == null || PermissionAPI.getPermission(player, node);
    }

    private static int manage(final CommandContext<CommandSourceStack> context, final ServerPlayer player) {
        openManage(player);
        return 1;
    }

    private static int open(final CommandContext<CommandSourceStack> context, final ServerPlayer player) throws CommandSyntaxException {
        open(player, StringArgumentType.getString(context, "id"));
        return 1;
    }

    // ---- Opening

    /**
     * The management screen, for administrators (the Features menu's Manage Exchanges).
     */
    public static void openManage(final ServerPlayer player) {
        if (unavailable(player)) {
            return;
        }
        if (!PermissionAPI.getPermission(player, ADMIN)) {
            error(player, "You do not have permission to manage exchanges!");
            return;
        }
        syncRegistry(player);
        send(player, new ClientboundOpen(Screen.MANAGE, "", 0, true));
    }

    /**
     * Opens an exchange; an empty id is the management screen. Its items are read first if nobody has opened it since the server started.
     */
    public static void open(final ServerPlayer player, final String id) {
        if (id.isEmpty()) {
            openManage(player);
            return;
        }
        if (unavailable(player)) {
            return;
        }
        final Exchange exchange = exchanges.get(id);
        if (exchange == null) {
            error(player, id + " does not exist!");
            return;
        }
        if (!has(player, exchange.permission + ".open") || exchange.hidden && !PermissionAPI.getPermission(player, ADMIN)) {
            error(player, "You do not have permission to open this exchange!");
            return;
        }
        syncRegistry(player);
        send(player, new ClientboundOpen(Screen.SPECIFIC, exchange.id, listingsLimit(player, exchange), isAdmin(player, exchange)));
        if (exchange.loaded) {
            sendListItems(player, exchange);
            send(player, new ClientboundRefresh(exchange.id));
            return;
        }
        exchange.waiting.add(player.getUUID());
        if (exchange.loading) {
            return;
        }
        exchange.loading = true;
        Almura.LOGGER.info("Reading the items of exchange '{}' ({})", exchange.name, exchange.id);
        AlmuraDatabase.run("read the items of exchange " + exchange.id, connection -> readRows(connection, exchange.id, null), rows -> {
            exchange.loading = false;
            apply(exchange, null, rows);
            exchange.loaded = true;
            Almura.LOGGER.info("Loaded {} list item(s) and {} for sale item(s) for exchange '{}' ({})", rows.items().size(), rows.sales().size(),
                exchange.name, exchange.id);
            final MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            for (final UUID waiting : exchange.waiting) {
                final ServerPlayer online = server == null ? null : server.getPlayerList().getPlayer(waiting);
                if (online != null) {
                    sendListItems(online, exchange);
                    send(online, new ClientboundRefresh(exchange.id));
                }
            }
            exchange.waiting.clear();
        });
    }

    /**
     * The offer screen (the +/- button), for a player who may list something here.
     */
    public static void openOffer(final ServerPlayer player, final String id) {
        final Exchange exchange = loadedExchange(player, id);
        if (exchange == null) {
            return;
        }
        if (listingsLimit(player, exchange) <= 0) {
            error(player, "You lack sufficient permissions *slots* to list anything!");
            return;
        }
        send(player, new ClientboundOpen(Screen.OFFER, exchange.id, listingsLimit(player, exchange), isAdmin(player, exchange)));
    }

    // ---- Management

    public static void modify(final ServerPlayer player, final ServerboundModify message) {
        if (unavailable(player)) {
            return;
        }
        final String id = message.id().trim().toLowerCase(Locale.ROOT);
        final String permission = message.permission().trim().toLowerCase(Locale.ROOT);
        final String name = message.name().trim();
        switch (message.change()) {
            case ADD -> {
                if (!PermissionAPI.getPermission(player, ADD)) {
                    error(player, "You do not have permission to add exchanges!");
                    return;
                }
                if (exchanges.containsKey(id)) {
                    syncRegistry(player);
                    return;
                }
                if (!ID.matcher(id).matches() || !ID.matcher(permission).matches() || name.isEmpty()) {
                    error(player, "An exchange needs an ID and permission (a-z, 0-9, _ and .) and a title.");
                    return;
                }
                final Exchange exchange = new Exchange(id, Instant.now(), player.getUUID(), name, permission, message.hidden());
                AlmuraDatabase.run("add exchange " + id, connection -> {
                    insertExchange(connection, exchange);
                    return null;
                }, result -> {
                    exchanges.put(id, exchange);
                    syncRegistryToAll();
                });
            }
            case MODIFY -> {
                if (!PermissionAPI.getPermission(player, MODIFY)) {
                    error(player, "You do not have permission to modify exchanges!");
                    return;
                }
                final Exchange exchange = exchanges.get(id);
                if (exchange == null) {
                    syncRegistry(player);
                    return;
                }
                if (!ID.matcher(permission).matches() || name.isEmpty()) {
                    error(player, "An exchange needs a permission (a-z, 0-9, _ and .) and a title.");
                    return;
                }
                AlmuraDatabase.run("modify exchange " + id, connection -> {
                    try (PreparedStatement update = connection.prepareStatement("update axs set name = ?, permission = ?, is_hidden = ? where id = ?")) {
                        update.setString(1, name);
                        update.setString(2, permission);
                        update.setBoolean(3, message.hidden());
                        update.setString(4, id);
                        update.executeUpdate();
                    }
                    return null;
                }, result -> {
                    exchange.name = name;
                    exchange.permission = permission;
                    exchange.hidden = message.hidden();
                    syncRegistryToAll();
                });
            }
            case DELETE -> {
                if (!PermissionAPI.getPermission(player, DELETE)) {
                    error(player, "You do not have permission to delete exchanges!");
                    return;
                }
                if (!exchanges.containsKey(id)) {
                    syncRegistry(player);
                    return;
                }
                // Its items, listings and purchases go with it (on delete cascade), as on 3.5
                AlmuraDatabase.run("delete exchange " + id, connection -> {
                    try (PreparedStatement delete = connection.prepareStatement("delete from axs where id = ?")) {
                        delete.setString(1, id);
                        delete.executeUpdate();
                    }
                    return null;
                }, result -> {
                    exchanges.remove(id);
                    syncRegistryToAll();
                });
            }
        }
    }

    // ---- Searching

    /**
     * A page of the items for sale matching the player's search, as 3.5's filters and sorts.
     */
    public static void query(final ServerPlayer player, final ServerboundQuery query) {
        final Exchange exchange = exchanges.get(query.id());
        if (exchange == null || !exchange.loaded) {
            return;
        }
        final String itemName = query.itemName().trim().toLowerCase(Locale.ROOT);
        final String sellerName = query.sellerName().trim().toLowerCase(Locale.ROOT);
        final Map<UUID, String> names = new HashMap<>();
        final List<ListItem> matches = new ArrayList<>();
        for (final ListItem item : exchange.forSale()) {
            if (!itemName.isEmpty() && !ExchangeItems.name(item.item).toLowerCase(Locale.ROOT).contains(itemName)) {
                continue;
            }
            if (!sellerName.isEmpty() && !names.computeIfAbsent(item.seller, ServerExchange::name).toLowerCase(Locale.ROOT).contains(sellerName)) {
                continue;
            }
            if (query.hideOwn() && item.seller.equals(player.getUUID())) {
                continue;
            }
            matches.add(item);
        }
        matches.sort(comparator(query.sort(), names));
        final int skip = Math.max(0, query.skip());
        final int limit = Math.max(1, Math.min(MAX_PAGE, query.limit()));
        final List<ForSaleView> page = matches.stream().skip(skip).limit(limit)
            .map(item -> {
                final ForSale forSale = item.forSale;
                return new ForSaleView(item.recNo, item.item.copy(), item.quantity, forSale == null ? "0" : forSale.price.toPlainString(),
                    item.seller, names.computeIfAbsent(item.seller, ServerExchange::name),
                    forSale == null ? 0L : forSale.created.toEpochMilli());
            })
            .toList();
        send(player, new ClientboundForSale(exchange.id, page, matches.size()));
    }

    private static Comparator<ListItem> comparator(final ExchangeSort sort, final Map<UUID, String> names) {
        final Comparator<ListItem> comparator = switch (sort) {
            case CREATED_ASC, CREATED_DESC -> Comparator.comparing(item -> item.forSale == null ? Instant.EPOCH : item.forSale.created);
            case PRICE_ASC, PRICE_DESC -> Comparator.comparing(item -> item.forSale == null ? BigDecimal.ZERO : item.forSale.price);
            case ITEM_NAME_ASC, ITEM_NAME_DESC -> Comparator.comparing(item -> ExchangeItems.name(item.item).toLowerCase(Locale.ROOT));
            case SELLER_ASC, SELLER_DESC -> Comparator.comparing(item -> names.computeIfAbsent(item.seller, ServerExchange::name)
                .toLowerCase(Locale.ROOT));
        };
        return sort.ascending() ? comparator : comparator.reversed();
    }

    // ---- Listing

    /**
     * Lists, unlists or reprices an item: the player's own, or (the admin changes) anyone's for the exchange's administrators.
     */
    public static void listStatus(final ServerPlayer player, final ServerboundListStatus message) {
        final Exchange exchange = loadedExchange(player, message.id());
        if (exchange == null) {
            return;
        }
        final boolean admin = switch (message.change()) {
            case ADMIN_DE_LIST, ADMIN_ADJUST_PRICE -> true;
            default -> false;
        };
        if (admin && !isAdmin(player, exchange)) {
            return;
        }
        final ListItem item = exchange.find(message.listItemRecNo())
            .filter(found -> admin || found.seller.equals(player.getUUID()))
            .orElse(null);
        if (item == null) {
            // Sold or taken back meanwhile
            sendListItems(player, exchange);
            send(player, new ClientboundRefresh(exchange.id));
            return;
        }
        switch (message.change()) {
            case LIST -> list(player, exchange, item, message.price());
            case DE_LIST, ADMIN_DE_LIST -> delist(player, exchange, item);
            case ADJUST_PRICE, ADMIN_ADJUST_PRICE -> reprice(player, exchange, item, message.price());
        }
    }

    private static void list(final ServerPlayer player, final Exchange exchange, final ListItem item, final String rawPrice) {
        if (item.forSale != null) {
            sendListItems(player, exchange);
            return;
        }
        final BigDecimal price = validatePrice(player, rawPrice);
        if (price == null) {
            return;
        }
        final long listed = exchange.listItems.getOrDefault(player.getUUID(), List.of()).stream().filter(other -> other.forSale != null).count();
        if (listed + 1 > listingsLimit(player, exchange)) {
            error(player, "You have reached your listing limit.");
            return;
        }
        final Instant created = Instant.now();
        AlmuraDatabase.run("list item " + item.recNo + " on exchange " + exchange.id, connection -> {
            try (PreparedStatement insert = connection.prepareStatement("insert into axs_for_sale_item (created, list_item, price) values (?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
                insert.setTimestamp(1, Timestamp.from(created));
                insert.setInt(2, item.recNo);
                insert.setBigDecimal(3, price);
                insert.executeUpdate();
                try (ResultSet keys = insert.getGeneratedKeys()) {
                    return keys.next() ? keys.getInt(1) : -1;
                }
            }
        }, recNo -> {
            if (recNo < 0) {
                error(player, "Critical error encountered, check the server console for more details!");
                return;
            }
            item.forSale = new ForSale(recNo, created, price);
            sendListItems(player, exchange);
            refreshAll(exchange);
        });
    }

    private static void delist(final ServerPlayer player, final Exchange exchange, final ListItem item) {
        final ForSale forSale = item.forSale;
        if (forSale == null) {
            sendListItems(player, exchange);
            return;
        }
        AlmuraDatabase.run("delist item " + item.recNo + " on exchange " + exchange.id, connection -> {
            try (PreparedStatement hide = connection.prepareStatement("update axs_for_sale_item set is_hidden = true where rec_no = ?");
                 PreparedStatement last = connection.prepareStatement("update axs_list_item set last_known_price = ? where rec_no = ?")) {
                hide.setInt(1, forSale.recNo);
                hide.executeUpdate();
                last.setBigDecimal(1, forSale.price);
                last.setInt(2, item.recNo);
                last.executeUpdate();
            }
            return null;
        }, result -> {
            item.lastKnownPrice = forSale.price;
            item.forSale = null;
            if (!item.seller.equals(player.getUUID())) {
                Almura.LOGGER.info("Admin '{}' delisted item '{}' for '{}' on exchange '{}'", player.getGameProfile().name(), item.recNo,
                    name(item.seller), exchange.id);
            }
            sendListItemsTo(item.seller, exchange);
            refreshAll(exchange);
        });
    }

    private static void reprice(final ServerPlayer player, final Exchange exchange, final ListItem item, final String rawPrice) {
        final ForSale forSale = item.forSale;
        if (forSale == null) {
            sendListItems(player, exchange);
            return;
        }
        final BigDecimal price = validatePrice(player, rawPrice);
        if (price == null) {
            return;
        }
        AlmuraDatabase.run("reprice item " + item.recNo + " on exchange " + exchange.id, connection -> {
            try (PreparedStatement update = connection.prepareStatement("update axs_for_sale_item set price = ? where rec_no = ?");
                 PreparedStatement last = connection.prepareStatement("update axs_list_item set last_known_price = ? where rec_no = ?")) {
                update.setBigDecimal(1, price);
                update.setInt(2, forSale.recNo);
                update.executeUpdate();
                last.setBigDecimal(1, forSale.price);
                last.setInt(2, item.recNo);
                last.executeUpdate();
            }
            return null;
        }, result -> {
            item.lastKnownPrice = forSale.price;
            forSale.price = price;
            sendListItemsTo(item.seller, exchange);
            refreshAll(exchange);
        });
    }

    // ---- Moving items

    /**
     * The offer screen's moves: items from the player's inventory onto the exchange (added to the same item there, if they have it), and
     * unlisted items back. The inventory is changed first; what the database refuses is given back.
     */
    public static void moveItems(final ServerPlayer player, final ServerboundMoveItems message) {
        // Carried out on the next tick, not while the exchange's message is being handled: Sponge sees inventory changes made then as
        // part of the player's tick, warns of them and fires no inventory event (as the store and the cache)
        final MinecraftServer server = player.level().getServer();
        server.schedule(new TickTask(server.getTickCount(), () -> {
            if (!player.isRemoved()) {
                moveItemsNow(player, message);
            }
        }));
    }

    private static void moveItemsNow(final ServerPlayer player, final ServerboundMoveItems message) {
        final Exchange exchange = loadedExchange(player, message.id());
        if (exchange == null || message.moves().isEmpty()) {
            return;
        }
        final UUID seller = player.getUUID();
        final List<ListItem> current = new ArrayList<>(exchange.listItems.getOrDefault(seller, List.of()));
        final MinecraftServer server = player.level().getServer();
        // To the exchange: taken from the inventory now
        record ToListing(ItemStack item, int quantity, @Nullable ListItem existing) {
        }
        final List<ToListing> toListing = new ArrayList<>();
        // Back to the inventory: given once the database has them
        record ToInventory(ListItem item, int quantity) {
        }
        final List<ToInventory> toInventory = new ArrayList<>();
        for (final Move move : message.moves()) {
            if (move.quantity() <= 0 || move.item().isEmpty()) {
                continue;
            }
            final ListItem existing = current.stream().filter(item -> ExchangeItems.same(item.item, move.item())).findFirst().orElse(null);
            if (move.direction() == Direction.TO_LISTING) {
                final int taken = ExchangeItems.extract(player, move.item(), move.quantity());
                if (taken > 0) {
                    toListing.add(new ToListing(move.item().copyWithCount(1), taken, existing));
                }
            } else if (existing != null && existing.forSale == null) {
                final int quantity = Math.min(Math.min(move.quantity(), existing.quantity), ExchangeItems.space(player, existing.item));
                if (quantity > 0) {
                    toInventory.add(new ToInventory(existing, quantity));
                }
            }
        }
        if (toListing.isEmpty() && toInventory.isEmpty()) {
            return;
        }
        final int firstIndex = current.stream().mapToInt(item -> item.index + 1).max().orElse(0);
        final List<String> encoded = toListing.stream().map(move -> ExchangeItems.encode(server.registryAccess(), move.item())).toList();
        AlmuraDatabase.submit(connection -> {
            int index = firstIndex;
            for (int i = 0; i < toListing.size(); i++) {
                final ToListing move = toListing.get(i);
                if (move.existing() != null) {
                    try (PreparedStatement update = connection.prepareStatement("update axs_list_item set quantity = quantity + ? where rec_no = ?")) {
                        update.setInt(1, move.quantity());
                        update.setInt(2, move.existing().recNo);
                        update.executeUpdate();
                    }
                } else {
                    try (PreparedStatement insert = connection.prepareStatement("insert into axs_list_item (created, axs, seller, item, quantity, "
                        + "list_index) values (?, ?, ?, ?, ?, ?)")) {
                        insert.setTimestamp(1, Timestamp.from(Instant.now()));
                        insert.setString(2, exchange.id);
                        insert.setObject(3, seller);
                        insert.setString(4, encoded.get(i));
                        insert.setInt(5, move.quantity());
                        insert.setInt(6, index++);
                        insert.executeUpdate();
                    }
                }
            }
            for (final ToInventory move : toInventory) {
                final boolean all = move.quantity() >= move.item().quantity;
                try (PreparedStatement update = connection.prepareStatement(all
                    ? "update axs_list_item set is_hidden = true where rec_no = ?"
                    : "update axs_list_item set quantity = quantity - ? where rec_no = ?")) {
                    if (all) {
                        update.setInt(1, move.item().recNo);
                    } else {
                        update.setInt(1, move.quantity());
                        update.setInt(2, move.item().recNo);
                    }
                    update.executeUpdate();
                }
            }
            return readRows(connection, exchange.id, seller);
        }).whenComplete((rows, error) -> server.execute(() -> {
            if (error != null) {
                Almura.LOGGER.error("Couldn't move items for '{}' on exchange '{}'; giving back what was taken", player.getGameProfile().name(),
                    exchange.id, error.getCause() == null ? error : error.getCause());
                toListing.forEach(move -> ExchangeItems.give(player, move.item(), move.quantity()));
                error(player, "Critical error encountered, check the server console for more details!");
                return;
            }
            apply(exchange, seller, rows);
            final ServerPlayer online = server.getPlayerList().getPlayer(seller);
            if (online != null) {
                toInventory.forEach(move -> ExchangeItems.give(online, move.item().item, move.quantity()));
                sendListItems(online, exchange);
            } else {
                Almura.LOGGER.warn("'{}' left before getting back items from exchange '{}': {}", seller, exchange.id, toInventory.stream()
                    .map(move -> move.quantity() + " x " + ExchangeItems.name(move.item().item)).toList());
            }
            refreshAll(exchange);
        }));
    }

    // ---- Buying

    /**
     * Buys some of an item for sale: the buyer needs the money and the room for it, the money goes to the seller (3.5 let a player buy their
     * own), and the items come once the database has the purchase.
     */
    public static void buy(final ServerPlayer player, final ServerboundBuy message) {
        // Carried out on the next tick, not while the exchange's message is being handled: Sponge sees inventory changes made then as
        // part of the player's tick, warns of them and fires no inventory event (as the store and the cache)
        final MinecraftServer server = player.level().getServer();
        server.schedule(new TickTask(server.getTickCount(), () -> {
            if (!player.isRemoved()) {
                buyNow(player, message);
            }
        }));
    }

    private static void buyNow(final ServerPlayer player, final ServerboundBuy message) {
        final Exchange exchange = loadedExchange(player, message.id());
        if (exchange == null) {
            return;
        }
        final EconomyAccounts economy = Economy.accounts();
        if (economy == null) {
            error(player, "There is no economy on this server.");
            return;
        }
        final ListItem item = exchange.find(message.listItemRecNo()).orElse(null);
        final ForSale forSale = item == null ? null : item.forSale;
        if (item == null || forSale == null) {
            error(player, "This item is no longer for sale!");
            send(player, new ClientboundRefresh(exchange.id));
            return;
        }
        final int quantity = message.quantity();
        if (quantity <= 0 || item.quantity < quantity) {
            error(player, "There is not enough quantity left to purchase this item!");
            send(player, new ClientboundRefresh(exchange.id));
            return;
        }
        final BigDecimal total = forSale.price.multiply(BigDecimal.valueOf(quantity));
        final BigDecimal balance = economy.balance(Account.player(player.getUUID()));
        if (balance == null) {
            error(player, "Critical error encountered, check the server console for more details!");
            Almura.LOGGER.error("The economy has no account for '{}'", player.getGameProfile().name());
            return;
        }
        if (total.compareTo(balance) > 0) {
            popup(player, "You attempted to purchase items totaling to §c" + ExchangeMoney.format(total) + "§r while you only have §a"
                + ExchangeMoney.format(balance) + "§r. You need §d" + ExchangeMoney.format(total.subtract(balance)) + "§r more!");
            return;
        }
        if (ExchangeItems.space(player, item.item) < quantity) {
            error(player, "You lack sufficient inventory space to purchase these item(s)!");
            return;
        }
        final UUID seller = item.seller;
        final UUID buyer = player.getUUID();
        if (!seller.equals(buyer) && !economy.transfer(Account.player(buyer), Account.player(seller), total)) {
            error(player, "The purchase could not be paid, nothing was bought.");
            return;
        }
        // Taken off now, so nobody else buys the same items while the database catches up
        final int remaining = item.quantity - quantity;
        item.quantity = remaining;
        if (remaining == 0) {
            item.forSale = null;
        }
        final ItemStack bought = item.item.copy();
        final MinecraftServer server = player.level().getServer();
        AlmuraDatabase.submit(connection -> {
            try (PreparedStatement update = connection.prepareStatement(remaining == 0
                ? "update axs_list_item set quantity = 0, is_hidden = true where rec_no = ?"
                : "update axs_list_item set quantity = ? where rec_no = ?")) {
                if (remaining == 0) {
                    update.setInt(1, item.recNo);
                } else {
                    update.setInt(1, remaining);
                    update.setInt(2, item.recNo);
                }
                update.executeUpdate();
            }
            if (remaining == 0) {
                try (PreparedStatement hide = connection.prepareStatement("update axs_for_sale_item set is_hidden = true where rec_no = ?")) {
                    hide.setInt(1, forSale.recNo);
                    hide.executeUpdate();
                }
            }
            try (PreparedStatement insert = connection.prepareStatement("insert into axs_transaction (created, for_sale_item, buyer, price, quantity) "
                + "values (?, ?, ?, ?, ?)")) {
                insert.setTimestamp(1, Timestamp.from(Instant.now()));
                insert.setInt(2, forSale.recNo);
                insert.setObject(3, buyer);
                insert.setBigDecimal(4, forSale.price);
                insert.setInt(5, quantity);
                insert.executeUpdate();
            }
            return readRows(connection, exchange.id, seller);
        }).whenComplete((rows, error) -> server.execute(() -> {
            final ServerPlayer online = server.getPlayerList().getPlayer(buyer);
            if (error != null) {
                // Paid but not recorded: the buyer still gets the items, and the console says so
                Almura.LOGGER.error("Couldn't record '{}' buying {} of list item {} on exchange '{}'", buyer, quantity, item.recNo, exchange.id,
                    error.getCause() == null ? error : error.getCause());
            } else {
                apply(exchange, seller, rows);
            }
            if (online != null) {
                ExchangeItems.give(online, bought, quantity);
                send(online, new ClientboundTransactionComplete());
            } else {
                Almura.LOGGER.warn("'{}' left before getting {} x {} bought on exchange '{}'", buyer, quantity, ExchangeItems.name(bought),
                    exchange.id);
            }
            sendListItemsTo(seller, exchange);
            refreshAll(exchange);
        }));
    }

    // ---- Database

    private static List<Exchange> readExchanges(final Connection connection) throws SQLException {
        final List<Exchange> loaded = new ArrayList<>();
        try (PreparedStatement select = connection.prepareStatement("select id, created, creator, name, permission, is_hidden from axs order by created");
             ResultSet result = select.executeQuery()) {
            while (result.next()) {
                loaded.add(new Exchange(result.getString("id"), result.getTimestamp("created").toInstant(), result.getObject("creator", UUID.class),
                    result.getString("name"), result.getString("permission"), result.getBoolean("is_hidden")));
            }
        }
        return loaded;
    }

    private static void insertExchange(final Connection connection, final Exchange exchange) throws SQLException {
        try (PreparedStatement insert = connection.prepareStatement("insert into axs (id, created, creator, name, permission, is_hidden) "
            + "values (?, ?, ?, ?, ?, ?)")) {
            insert.setString(1, exchange.id);
            insert.setTimestamp(2, Timestamp.from(exchange.created));
            insert.setObject(3, exchange.creator);
            insert.setString(4, exchange.name);
            insert.setString(5, exchange.permission);
            insert.setBoolean(6, exchange.hidden);
            insert.executeUpdate();
        }
    }

    /**
     * Reads an exchange's items still on it and their listings still up: everyone's, or one seller's.
     */
    private static Rows readRows(final Connection connection, final String id, final @Nullable UUID seller) throws SQLException {
        final List<ListRow> items = new ArrayList<>();
        try (PreparedStatement select = connection.prepareStatement("select rec_no, seller, item, quantity, list_index, last_known_price "
            + "from axs_list_item where axs = ? and is_hidden = false" + (seller == null ? "" : " and seller = ?"))) {
            select.setString(1, id);
            if (seller != null) {
                select.setObject(2, seller);
            }
            try (ResultSet result = select.executeQuery()) {
                while (result.next()) {
                    items.add(new ListRow(result.getInt("rec_no"), result.getObject("seller", UUID.class), result.getString("item"),
                        result.getInt("quantity"), result.getInt("list_index"), result.getBigDecimal("last_known_price")));
                }
            }
        }
        final List<SaleRow> sales = new ArrayList<>();
        try (PreparedStatement select = connection.prepareStatement("select f.rec_no, f.created, f.list_item, f.price from axs_for_sale_item f "
            + "join axs_list_item l on l.rec_no = f.list_item where l.axs = ? and l.is_hidden = false and f.is_hidden = false"
            + (seller == null ? "" : " and l.seller = ?"))) {
            select.setString(1, id);
            if (seller != null) {
                select.setObject(2, seller);
            }
            try (ResultSet result = select.executeQuery()) {
                while (result.next()) {
                    sales.add(new SaleRow(result.getInt("rec_no"), result.getTimestamp("created").toInstant(), result.getInt("list_item"),
                        result.getBigDecimal("price")));
                }
            }
        }
        return new Rows(items, sales);
    }

    /**
     * Puts read items in place of what the exchange had: everyone's, or one seller's. Done on the server's thread, where the items are
     * decoded.
     */
    private static void apply(final Exchange exchange, final @Nullable UUID seller, final Rows rows) {
        final MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }
        final Map<Integer, ListItem> byRecNo = new HashMap<>();
        final Map<UUID, List<ListItem>> bySeller = new HashMap<>();
        for (final ListRow row : rows.items()) {
            final ItemStack item = ExchangeItems.decode(server.registryAccess(), row.item()).orElse(null);
            if (item == null || row.quantity() <= 0) {
                if (item == null) {
                    Almura.LOGGER.error("Unknown item at exchange list item {} ({}); skipped (was a mod removed?)", row.recNo(), row.item());
                }
                continue;
            }
            final ListItem listItem = new ListItem(row.recNo(), row.seller(), item, row.quantity(), row.index(), row.lastKnownPrice());
            byRecNo.put(row.recNo(), listItem);
            bySeller.computeIfAbsent(row.seller(), key -> new ArrayList<>()).add(listItem);
        }
        for (final SaleRow sale : rows.sales()) {
            final ListItem listItem = byRecNo.get(sale.listItem());
            if (listItem != null) {
                listItem.forSale = new ForSale(sale.recNo(), sale.created(), sale.price());
            }
        }
        bySeller.values().forEach(items -> items.sort(Comparator.comparingInt(item -> item.index)));
        if (seller == null) {
            exchange.listItems.clear();
            exchange.listItems.putAll(bySeller);
        } else {
            exchange.listItems.put(seller, bySeller.getOrDefault(seller, new ArrayList<>()));
        }
    }

    // ---- Sending

    private static void sendListItems(final ServerPlayer player, final Exchange exchange) {
        final List<ListItemView> views = exchange.listItems.getOrDefault(player.getUUID(), List.of()).stream()
            .map(item -> new ListItemView(item.recNo, item.item.copy(), item.quantity,
                item.lastKnownPrice == null ? "" : item.lastKnownPrice.toPlainString(),
                item.forSale == null ? -1 : item.forSale.recNo, item.forSale == null ? "" : item.forSale.price.toPlainString()))
            .toList();
        send(player, new ClientboundListItems(exchange.id, views));
    }

    private static void sendListItemsTo(final UUID uniqueId, final Exchange exchange) {
        final MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        final ServerPlayer online = server == null ? null : server.getPlayerList().getPlayer(uniqueId);
        if (online != null) {
            sendListItems(online, exchange);
        }
    }

    /**
     * Everyone looking at the exchange searches again.
     */
    private static void refreshAll(final Exchange exchange) {
        PacketDistributor.sendToAllPlayers(new ClientboundRefresh(exchange.id));
    }

    private static void syncRegistry(final ServerPlayer player) {
        final boolean admin = PermissionAPI.getPermission(player, ADMIN);
        send(player, new ClientboundRegistry(exchanges.values().stream()
            .filter(exchange -> !exchange.hidden || admin)
            .map(exchange -> new ExchangeInfo(exchange.id, exchange.name, exchange.permission, exchange.hidden, exchange.creator,
                UNKNOWN_OWNER.equals(exchange.creator) ? "" : name(exchange.creator), exchange.created.toEpochMilli()))
            .toList()));
    }

    private static void syncRegistryToAll() {
        final MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            server.getPlayerList().getPlayers().forEach(ServerExchange::syncRegistry);
        }
    }

    private static void send(final ServerPlayer player, final CustomPacketPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    // ---- Helpers

    /**
     * @return the exchange, read and usable, or null having told the player (or synced their list of exchanges)
     */
    private static @Nullable Exchange loadedExchange(final ServerPlayer player, final String id) {
        if (unavailable(player)) {
            return null;
        }
        final Exchange exchange = exchanges.get(id);
        if (exchange == null) {
            syncRegistry(player);
            return null;
        }
        if (!exchange.loaded) {
            open(player, id);
            return null;
        }
        return exchange;
    }

    private static boolean unavailable(final ServerPlayer player) {
        if (!ready || Economy.accounts() == null) {
            error(player, "Exchanges are unavailable: there is no economy on this server.");
            return true;
        }
        return false;
    }

    private static @Nullable BigDecimal validatePrice(final ServerPlayer player, final String raw) {
        final BigDecimal price = ExchangeMoney.parse(raw).setScale(2, RoundingMode.DOWN);
        if (price.signum() <= 0 || price.compareTo(ExchangeMoney.ONE_TRILLION) > 0) {
            error(player, "Enter a price between 0.01 and " + ExchangeMoney.format(ExchangeMoney.ONE_TRILLION) + ".");
            return null;
        }
        return price;
    }

    private static String name(final UUID uniqueId) {
        final MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return uniqueId.toString().substring(0, 8);
        }
        final ServerPlayer online = server.getPlayerList().getPlayer(uniqueId);
        if (online != null) {
            return online.getGameProfile().name();
        }
        return server.services().nameToIdCache().get(uniqueId).map(NameAndId::name).orElse(uniqueId.toString().substring(0, 8));
    }

    private static void popup(final ServerPlayer player, final String message) {
        ServerNotifications.sendPopup(player, TITLE, message, 5);
    }

    private static void error(final ServerPlayer player, final String message) {
        ServerNotifications.sendPopup(player, "§c" + TITLE, message, 5);
    }
}
