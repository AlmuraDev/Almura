/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.title.server;

import com.almuradev.almura.Almura;
import com.almuradev.almura.core.database.AlmuraDatabase;
import com.almuradev.almura.core.permission.RuntimePermissions;
import com.almuradev.almura.feature.notification.server.ServerNotifications;
import com.almuradev.almura.feature.title.Title;
import com.almuradev.almura.feature.title.network.TitleMessages.ClientboundAvailable;
import com.almuradev.almura.feature.title.network.TitleMessages.ClientboundOpen;
import com.almuradev.almura.feature.title.network.TitleMessages.ClientboundRegistry;
import com.almuradev.almura.feature.title.network.TitleMessages.ClientboundSelected;
import com.almuradev.almura.feature.title.network.TitleMessages.ServerboundModify;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.loading.FMLPaths;
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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Titles on the server, as 3.5's {@code ServerTitleManager}: the titles in Almura's database (the first start fills it from
 * {@code config/almura/title.conf}, else 3.5's titles), each player's pick (kept, and on joining without one the highest of 3.5's rank titles
 * they may have), sent to everyone so the title shows above the player's name.
 * <p>
 * Permissions, as 3.5's (a permission plugin such as LuckPerms grants them; without one only operators have them):
 * <ul>
 *     <li>{@code almura.title.base}: open the Titles window (the Features menu's Titles);</li>
 *     <li>{@code almura.title.change}: pick a title;</li>
 *     <li>{@code almura.title.admin}: see hidden titles and manage them; {@code almura.title.create} and {@code almura.title.modify}: add and
 *     change titles (deleting one needs {@code almura.title.admin});</li>
 *     <li>each title's own permission ({@code almura.title.ancient} and the like): pick it, asked of LuckPerms by name.</li>
 * </ul>
 */
public final class ServerTitles {

    private static final String TITLE = "Title Manager";
    private static final UUID NOBODY = new UUID(0L, 0L);
    private static final Pattern ID = Pattern.compile("[a-z0-9_]+");
    // title.conf's "permission": "content" pairs
    private static final Pattern CONF_ENTRY = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\"([^\"]*)\"");
    // 3.5's rank titles, lowest first: a player joining without a pick gets the last they may have, else Lost Soul
    private static final String[] RANK_TITLES = {"soldier", "survivor", "citizen", "explorer", "pioneer", "architect", "ancient"};
    private static final String DEFAULT_TITLE = "lostsoul";
    // 3.5's titles (its title.conf), for a first start without one of its own
    private static final Map<String, String> DEFAULT_TITLES = new LinkedHashMap<>();

    static {
        DEFAULT_TITLES.put("almura.title.ancient", "§7[§cAncient§7]§f");
        DEFAULT_TITLES.put("almura.title.architect", "§7[§9Architect§7]§f");
        DEFAULT_TITLES.put("almura.title.commander", "§7[§1Commander§7]§f");
        DEFAULT_TITLES.put("almura.title.veteran", "§7[§6Veteran§7]§f");
        DEFAULT_TITLES.put("almura.title.soldier", "§7[§3Soldier§7]§f");
        DEFAULT_TITLES.put("almura.title.citizen", "§7[§eCitizen§7]§f");
        DEFAULT_TITLES.put("almura.title.survivor", "§7[§7Survivor§7]§f");
        DEFAULT_TITLES.put("almura.title.lostsoul", "§7[§8Lost Soul§7]§f");
    }

    public static final PermissionNode<Boolean> BASE = node("title.base");
    public static final PermissionNode<Boolean> CHANGE = node("title.change");
    public static final PermissionNode<Boolean> ADMIN = node("title.admin");
    public static final PermissionNode<Boolean> CREATE = node("title.create");
    public static final PermissionNode<Boolean> MODIFY = node("title.modify");

    private static final Map<String, Title> titles = new LinkedHashMap<>();
    private static final Map<UUID, Title> selected = new HashMap<>();
    private static boolean ready;

    private static PermissionNode<Boolean> node(final String path) {
        return new PermissionNode<>(Almura.ID, path, PermissionTypes.BOOLEAN, (player, uuid, context) -> player != null && isOperator(player));
    }

    private static boolean isOperator(final ServerPlayer player) {
        return player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
    }

    @SubscribeEvent
    public void onPermissionNodes(final PermissionGatherEvent.Nodes event) {
        event.addNodes(BASE, CHANGE, ADMIN, CREATE, MODIFY);
    }

    @SubscribeEvent
    public void onServerStarted(final ServerStartedEvent event) {
        AlmuraDatabase.install("title").whenComplete((result, error) -> {
            if (error != null) {
                Almura.LOGGER.error("Couldn't make the titles' tables; titles are unavailable", error.getCause() == null ? error : error.getCause());
                return;
            }
            AlmuraDatabase.run("read the titles", ServerTitles::readOrSeed, loaded -> {
                titles.clear();
                titles.putAll(loaded);
                ready = true;
                Almura.LOGGER.info("Loaded {} title(s)", titles.size());
            });
        });
    }

    @SubscribeEvent
    public void onServerStopped(final ServerStoppedEvent event) {
        ready = false;
        titles.clear();
        selected.clear();
    }

    @SubscribeEvent
    public void onLoggedIn(final PlayerEvent.PlayerLoggedInEvent event) {
        if (!ready || !(event.getEntity() instanceof final ServerPlayer player)) {
            return;
        }
        selected.remove(player.getUUID());
        sendRegistry(player);
        PacketDistributor.sendToPlayer(player, new ClientboundSelected(true, selectedIds()));
        sendAvailable(player);
        // Their pick, else the highest rank title they may have, as 3.5
        final UUID holder = player.getUUID();
        AlmuraDatabase.run("read a player's title", connection -> {
            try (PreparedStatement select = connection.prepareStatement("select title from title_select where holder = ?")) {
                select.setObject(1, holder);
                try (ResultSet result = select.executeQuery()) {
                    return result.next() ? result.getString("title") : null;
                }
            }
        }, picked -> {
            final ServerPlayer online = online(holder);
            if (online == null) {
                return;
            }
            String id = picked;
            if (id == null || id.isEmpty()) {
                id = DEFAULT_TITLE;
                for (final String rank : RANK_TITLES) {
                    final Title title = titles.get(rank);
                    if (title != null && RuntimePermissions.has(online, title.permission())) {
                        id = rank;
                    }
                }
            }
            final Title title = titles.get(id);
            if (title != null && mayUse(online, title)) {
                selected.put(holder, title);
                broadcastSelected(holder, title.id());
            } else if (picked != null) {
                // Gone, hidden, or no longer theirs
                deleteSelection(holder);
            }
        });
    }

    @SubscribeEvent
    public void onLoggedOut(final PlayerEvent.PlayerLoggedOutEvent event) {
        if (selected.remove(event.getEntity().getUUID()) != null) {
            broadcastSelected(event.getEntity().getUUID(), "");
        }
    }

    // ---- From the client

    /**
     * Opens the Titles window, as 3.5's: for players with {@code almura.title.base}.
     */
    public static void open(final ServerPlayer player) {
        if (!PermissionAPI.getPermission(player, BASE)) {
            ServerNotifications.sendPopup(player, TITLE, "Insufficient Permissions to view Title Manager!", 2);
            return;
        }
        if (!ready) {
            ServerNotifications.sendPopup(player, TITLE, "Titles are unavailable right now, try again later.", 3);
            return;
        }
        sendAvailable(player);
        PacketDistributor.sendToPlayer(player, new ClientboundOpen(PermissionAPI.getPermission(player, CHANGE),
            PermissionAPI.getPermission(player, ADMIN)));
    }

    /**
     * The player picks a title, or none (an empty id).
     */
    public static void select(final ServerPlayer player, final String id) {
        if (!ready) {
            return;
        }
        if (!PermissionAPI.getPermission(player, CHANGE)) {
            ServerNotifications.sendPopup(player, TITLE, "Insufficient Permission! You can't change your title.", 5);
            return;
        }
        final UUID holder = player.getUUID();
        if (id.isEmpty()) {
            if (selected.remove(holder) != null) {
                broadcastSelected(holder, "");
            }
            deleteSelection(holder);
            return;
        }
        final Title title = titles.get(id);
        if (title == null || !mayUse(player, title)) {
            // Gone, or not theirs: what they may have now
            sendRegistry(player);
            sendAvailable(player);
            return;
        }
        final Title previous = selected.put(holder, title);
        broadcastSelected(holder, title.id());
        AlmuraDatabase.change("save a player's title", connection -> {
            if (previous != null) {
                try (PreparedStatement history = connection.prepareStatement("insert into title_select_history (created, holder, old_title) values (?, ?, ?)")) {
                    history.setTimestamp(1, Timestamp.from(Instant.now()));
                    history.setObject(2, holder);
                    history.setString(3, previous.id());
                    history.executeUpdate();
                }
            }
            try (PreparedStatement delete = connection.prepareStatement("delete from title_select where holder = ?")) {
                delete.setObject(1, holder);
                delete.executeUpdate();
            }
            try (PreparedStatement insert = connection.prepareStatement("insert into title_select (holder, created, title) values (?, ?, ?)")) {
                insert.setObject(1, holder);
                insert.setTimestamp(2, Timestamp.from(Instant.now()));
                insert.setString(3, title.id());
                insert.executeUpdate();
            }
            return null;
        });
    }

    /**
     * An administrator adds, modifies or deletes a title.
     */
    public static void modify(final ServerPlayer player, final ServerboundModify message) {
        if (!ready) {
            return;
        }
        final String id = message.id().toLowerCase(Locale.ROOT).trim();
        final String permission = message.permission().toLowerCase(Locale.ROOT).trim();
        final String content = message.content().trim();
        final Title existing = titles.get(id);
        switch (message.modify()) {
            case ADD -> {
                if (!PermissionAPI.getPermission(player, CREATE)) {
                    ServerNotifications.sendPopup(player, TITLE, "Insufficient Permission!, Title addition failed.", 5);
                    return;
                }
                if (existing != null) {
                    ServerNotifications.sendPopup(player, TITLE, "This Title already exists!", 5);
                    sendRegistry(player);
                    return;
                }
                if (!valid(player, id, permission, content)) {
                    return;
                }
                final UUID creator = player.getUUID();
                change(player, "add a title", connection -> {
                    try (PreparedStatement insert = connection.prepareStatement(
                        "insert into title (id, created, creator, permission, content, is_hidden) values (?, ?, ?, ?, ?, ?)")) {
                        insert.setString(1, id);
                        insert.setTimestamp(2, Timestamp.from(Instant.now()));
                        insert.setObject(3, creator);
                        insert.setString(4, permission);
                        insert.setString(5, content);
                        insert.setBoolean(6, message.hidden());
                        insert.executeUpdate();
                    }
                });
            }
            case MODIFY -> {
                if (!PermissionAPI.getPermission(player, MODIFY)) {
                    ServerNotifications.sendPopup(player, TITLE, "Insufficient Permission!, Title change failed.", 5);
                    return;
                }
                if (existing == null) {
                    sendRegistry(player);
                    return;
                }
                if (!valid(player, id, permission, content)) {
                    return;
                }
                change(player, "change a title", connection -> {
                    try (PreparedStatement update = connection.prepareStatement("update title set permission = ?, content = ?, is_hidden = ? where id = ?")) {
                        update.setString(1, permission);
                        update.setString(2, content);
                        update.setBoolean(3, message.hidden());
                        update.setString(4, id);
                        update.executeUpdate();
                    }
                });
            }
            case DELETE -> {
                if (!PermissionAPI.getPermission(player, ADMIN)) {
                    ServerNotifications.sendPopup(player, TITLE, "Insufficient Permission!, Title removal failed.", 5);
                    return;
                }
                if (existing == null) {
                    sendRegistry(player);
                    return;
                }
                change(player, "delete a title", connection -> {
                    // The players who picked it lose it (on delete cascade)
                    try (PreparedStatement delete = connection.prepareStatement("delete from title where id = ?")) {
                        delete.setString(1, id);
                        delete.executeUpdate();
                    }
                });
            }
            default -> { }
        }
    }

    // ---- Helpers

    @FunctionalInterface
    private interface Change {

        void run(Connection connection) throws SQLException;
    }

    /**
     * Runs a change, then reads the titles again and sends everyone what they may see and pick now, as 3.5's {@code loadTitles}.
     */
    private static void change(final ServerPlayer player, final String what, final Change change) {
        AlmuraDatabase.run(what, connection -> {
            change.run(connection);
            return readTitles(connection);
        }, loaded -> {
            titles.clear();
            titles.putAll(loaded);
            final MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server == null) {
                return;
            }
            for (final ServerPlayer online : server.getPlayerList().getPlayers()) {
                sendRegistry(online);
                final Title picked = selected.get(online.getUUID());
                if (picked != null) {
                    final Title now = titles.get(picked.id());
                    if (now == null || !mayUse(online, now)) {
                        selected.remove(online.getUUID());
                    } else {
                        selected.put(online.getUUID(), now);
                    }
                }
                sendAvailable(online);
            }
            PacketDistributor.sendToAllPlayers(new ClientboundSelected(true, selectedIds()));
            ServerNotifications.sendPopup(player, TITLE, "Titles saved.", 2);
        });
    }

    private static boolean valid(final ServerPlayer player, final String id, final String permission, final String content) {
        if (!ID.matcher(id).matches() || id.length() > 64) {
            ServerNotifications.sendPopup(player, TITLE, "A title's ID is a-z, 0-9 and _.", 5);
            return false;
        }
        if (permission.isEmpty() || content.replaceAll("§.", "").isBlank()) {
            ServerNotifications.sendPopup(player, TITLE, "A title needs a permission and content.", 5);
            return false;
        }
        return true;
    }

    /**
     * Whether the player may pick the title: its permission (a hidden one is for title administrators).
     */
    private static boolean mayUse(final ServerPlayer player, final Title title) {
        return title.hidden() ? PermissionAPI.getPermission(player, ADMIN) : RuntimePermissions.has(player, title.permission());
    }

    private static void sendRegistry(final ServerPlayer player) {
        final boolean admin = PermissionAPI.getPermission(player, ADMIN);
        PacketDistributor.sendToPlayer(player, new ClientboundRegistry(titles.values().stream().filter(title -> !title.hidden() || admin).toList()));
    }

    private static void sendAvailable(final ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new ClientboundAvailable(titles.values().stream().filter(title -> mayUse(player, title))
            .map(Title::id).toList()));
    }

    private static void broadcastSelected(final UUID holder, final String id) {
        final Map<UUID, String> change = new HashMap<>();
        change.put(holder, id);
        PacketDistributor.sendToAllPlayers(new ClientboundSelected(false, change));
    }

    private static Map<UUID, String> selectedIds() {
        final Map<UUID, String> ids = new HashMap<>();
        selected.forEach((holder, title) -> ids.put(holder, title.id()));
        return ids;
    }

    private static void deleteSelection(final UUID holder) {
        AlmuraDatabase.change("clear a player's title", connection -> {
            try (PreparedStatement delete = connection.prepareStatement("delete from title_select where holder = ?")) {
                delete.setObject(1, holder);
                delete.executeUpdate();
            }
            return null;
        });
    }

    private static @Nullable ServerPlayer online(final UUID player) {
        final MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        return server == null ? null : server.getPlayerList().getPlayer(player);
    }

    // ---- Reading

    private static Map<String, Title> readOrSeed(final Connection connection) throws SQLException {
        final Map<String, Title> loaded = readTitles(connection);
        if (!loaded.isEmpty()) {
            return loaded;
        }
        // The first start: 3.5's titles, from config/almura/title.conf if there is one
        final Map<String, String> seed = seedTitles();
        try (PreparedStatement insert = connection.prepareStatement(
            "insert into title (id, created, creator, permission, content, is_hidden) values (?, ?, ?, ?, ?, ?)")) {
            for (final Map.Entry<String, String> entry : seed.entrySet()) {
                final String permission = entry.getKey().toLowerCase(Locale.ROOT);
                final String id = permission.substring(permission.lastIndexOf('.') + 1);
                if (!ID.matcher(id).matches()) {
                    continue;
                }
                insert.setString(1, id);
                insert.setTimestamp(2, Timestamp.from(Instant.now()));
                insert.setObject(3, NOBODY);
                insert.setString(4, permission);
                insert.setString(5, entry.getValue());
                insert.setBoolean(6, false);
                insert.addBatch();
            }
            insert.executeBatch();
        }
        Almura.LOGGER.info("Added {} title(s) to an empty title table", seed.size());
        return readTitles(connection);
    }

    private static Map<String, String> seedTitles() {
        final Path file = FMLPaths.CONFIGDIR.get().resolve(Almura.ID).resolve("title.conf");
        if (Files.isRegularFile(file)) {
            try {
                final Map<String, String> read = new LinkedHashMap<>();
                final Matcher entry = CONF_ENTRY.matcher(Files.readString(file, StandardCharsets.UTF_8));
                while (entry.find()) {
                    read.put(entry.group(1), entry.group(2));
                }
                if (!read.isEmpty()) {
                    return read;
                }
            } catch (final IOException e) {
                Almura.LOGGER.warn("Couldn't read {}; using 3.5's titles", file, e);
            }
        }
        return DEFAULT_TITLES;
    }

    private static Map<String, Title> readTitles(final Connection connection) throws SQLException {
        final List<Title> read = new ArrayList<>();
        try (PreparedStatement select = connection.prepareStatement("select id, created, creator, permission, content, is_hidden from title order by id");
             ResultSet result = select.executeQuery()) {
            while (result.next()) {
                read.add(new Title(result.getString("id"), result.getTimestamp("created").getTime(), result.getObject("creator", UUID.class),
                    result.getString("permission"), result.getString("content"), result.getBoolean("is_hidden")));
            }
        }
        final Map<String, Title> loaded = new LinkedHashMap<>();
        read.forEach(title -> loaded.put(title.id(), title));
        return loaded;
    }
}
