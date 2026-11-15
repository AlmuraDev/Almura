/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.guide.server;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.guide.network.GuideMessages;
import com.almuradev.almura.feature.guide.network.GuideMessages.Abilities;
import com.almuradev.almura.feature.guide.network.GuideMessages.ChangeType;
import com.almuradev.almura.feature.guide.network.GuideMessages.ClientboundChangeResult;
import com.almuradev.almura.feature.guide.network.GuideMessages.ClientboundListing;
import com.almuradev.almura.feature.guide.network.GuideMessages.ClientboundPage;
import com.almuradev.almura.feature.guide.network.GuideMessages.Entry;
import com.almuradev.almura.feature.guide.network.GuideMessages.OpenType;
import com.almuradev.almura.feature.guide.network.GuideMessages.ServerboundChange;
import com.almuradev.almura.feature.notification.server.ServerNotifications;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import net.neoforged.neoforge.server.permission.nodes.PermissionTypes;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * The Guide's pages on the server, as 3.5's {@code ServerPageManager}: one file a page, the server's under
 * {@code config/almura/guide/pages} and players' own guides under {@code config/almura/guide/player_pages/<their UUID>} (3.5's {@code .conf}
 * files, which are JSON, can be copied in as they are), sent to the players who may see them, and added, changed, removed and moved by the
 * players who may.
 * <p>
 * Permissions, as 3.5's (a permission plugin such as LuckPerms grants them; without one only operators have them, as Sponge gave a player
 * nothing it wasn't granted):
 * <ul>
 *     <li>{@code almura.guide.open}: open the guide (the Features menu's Guide button);</li>
 *     <li>{@code almura.guide.open_at_login}: the guide opens a few seconds after joining (if the player's Show Guide on Login is on);</li>
 *     <li>{@code almura.guide.page.<id>}, {@code almura.guide.modify.<id>}, {@code almura.guide.remove.<id>}: see, change and remove a
 *     server page (for the pages there when the server started; a page added later is for operators until the next start);</li>
 *     <li>{@code almura.guide.add}: add server pages; {@code almura.guide.remove}: shows the remove button;</li>
 *     <li>{@code almura.guide.modify}: the guide's administrators, who see, change and remove everyone's guides and move the server's pages;</li>
 *     <li>{@code almura.guide.player.add}: add guides of one's own, up to {@code almura.guide.player.limit} (5 unless the permission plugin
 *     gives a number). A player's guide is seen only by them and the administrators, and only they move it.</li>
 * </ul>
 * Commands, operators only until Almura's command permissions are ported: {@code /almura guide refresh} reloads the pages, and
 * {@code /almura guide force <players> [page]} opens the guide on players.
 */
public final class ServerGuide {

    private static final Path ROOT = FMLPaths.CONFIGDIR.get().resolve(Almura.ID).resolve("guide");
    private static final Path PAGES = ROOT.resolve("pages");
    private static final Path PLAYER_PAGES = ROOT.resolve("player_pages");
    private static final String EXTENSION = ".conf";
    private static final String PLAYER_PREFIX = "player/";
    private static final UUID NOBODY = new UUID(0L, 0L);
    private static final Pattern SERVER_ID = Pattern.compile("[a-z0-9_-]+");
    private static final int PLAYER_ID_LENGTH = 32;
    private static final int DEFAULT_PLAYER_LIMIT = 5;
    // As 3.5: the guide opens 100 ticks after joining
    private static final int LOGIN_DELAY_TICKS = 100;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static final PermissionNode<Boolean> OPEN = node("guide.open");
    public static final PermissionNode<Boolean> OPEN_AT_LOGIN = node("guide.open_at_login");
    public static final PermissionNode<Boolean> ADD = node("guide.add");
    public static final PermissionNode<Boolean> REMOVE = node("guide.remove");
    public static final PermissionNode<Boolean> ADMIN = node("guide.modify");
    public static final PermissionNode<Boolean> PLAYER_ADD = node("guide.player.add");
    public static final PermissionNode<Integer> PLAYER_LIMIT = new PermissionNode<>(Almura.ID, "guide.player.limit", PermissionTypes.INTEGER,
        (player, uuid, context) -> DEFAULT_PLAYER_LIMIT);

    private static final Map<String, Page> pages = new LinkedHashMap<>();
    private static final Map<String, Path> files = new HashMap<>();
    // Each server page's see, change and remove permissions, registered at the server's start
    private static final Map<String, PermissionNode<Boolean>[]> pageNodes = new HashMap<>();
    private static final Map<UUID, Integer> loginOpens = new HashMap<>();

    /**
     * A page: a server page's id is its file's name; a player's guide's is {@code player/<owner>/<file name>}.
     */
    public record Page(String id, int index, String name, String content, UUID creator, Instant created, UUID modifier, Instant modified,
        @Nullable UUID owner) {

        Page(final String id, final int index, final String name, final String content, final UUID creator, final Instant created,
            final UUID modifier, final Instant modified) {
            this(id, index, name, content, creator, created, modifier, modified, null);
        }

        boolean playerGuide() {
            return this.owner != null;
        }
    }

    private static PermissionNode<Boolean> node(final String path) {
        return new PermissionNode<>(Almura.ID, path, PermissionTypes.BOOLEAN, (player, uuid, context) -> player != null && isOperator(player));
    }

    private static boolean isOperator(final ServerPlayer player) {
        return player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
    }

    @SubscribeEvent
    @SuppressWarnings("unchecked")
    public void onPermissionNodes(final PermissionGatherEvent.Nodes event) {
        // The pages are read here, at the server's start, so each server page gets its permissions
        load();
        event.addNodes(OPEN, OPEN_AT_LOGIN, ADD, REMOVE, ADMIN, PLAYER_ADD, PLAYER_LIMIT);
        pageNodes.clear();
        for (final Page page : pages.values()) {
            if (page.playerGuide()) {
                continue;
            }
            final PermissionNode<Boolean>[] nodes = new PermissionNode[] {node("guide.page." + page.id()), node("guide.modify." + page.id()),
                node("guide.remove." + page.id())};
            pageNodes.put(page.id(), nodes);
            event.addNodes(nodes);
        }
    }

    // ---- Reading and writing

    /**
     * Reads every page.
     *
     * @return how many there are
     */
    public static int load() {
        pages.clear();
        files.clear();
        int players = 0;
        try {
            Files.createDirectories(PAGES);
            Files.createDirectories(PLAYER_PAGES);
            loadFolder(PAGES, null);
            try (Stream<Path> owners = Files.list(PLAYER_PAGES)) {
                for (final Path folder : (Iterable<Path>) owners.filter(Files::isDirectory)::iterator) {
                    final UUID owner;
                    try {
                        owner = UUID.fromString(folder.getFileName().toString());
                    } catch (final IllegalArgumentException e) {
                        Almura.LOGGER.warn("Skipped {}: a player's guides are in a folder named for their UUID", folder);
                        continue;
                    }
                    players += loadFolder(folder, owner);
                }
            }
        } catch (final IOException e) {
            Almura.LOGGER.error("Couldn't read the guide's pages in {}", ROOT, e);
        }
        Almura.LOGGER.info("Loaded {} guide page(s) and {} player page(s)", pages.size() - players, players);
        return pages.size();
    }

    private static int loadFolder(final Path folder, final @Nullable UUID owner) throws IOException {
        int count = 0;
        try (Stream<Path> found = Files.walk(folder)) {
            for (final Path file : (Iterable<Path>) found.filter(path -> path.toString().endsWith(EXTENSION))::iterator) {
                final String name = file.getFileName().toString();
                final String fileId = name.substring(0, name.length() - EXTENSION.length());
                final String id = owner == null ? fileId : PLAYER_PREFIX + owner + "/" + fileId;
                try {
                    final Page read = read(id, Files.readString(file, StandardCharsets.UTF_8));
                    pages.put(id, owner == null ? read : new Page(read.id(), read.index(), read.name(), read.content(), read.creator(),
                        read.created(), read.modifier(), read.modified(), owner));
                    files.put(id, file);
                    count++;
                } catch (final IOException | RuntimeException e) {
                    Almura.LOGGER.error("Couldn't read the guide page {}", file, e);
                }
            }
        }
        return count;
    }

    /**
     * A page from 3.5's file: {@code index}, {@code name}, {@code content}, {@code created} and {@code last_modified} (each a UUID and an
     * ISO time). Its text keeps § colour codes; 3.5's &amp; ones are turned into them.
     */
    static Page read(final String id, final String text) {
        final JsonObject json = JsonParser.parseString(text).getAsJsonObject();
        final JsonObject created = json.get("created") instanceof final JsonObject object ? object : new JsonObject();
        final JsonObject modified = json.get("last_modified") instanceof final JsonObject object ? object : new JsonObject();
        return new Page(id, json.has("index") ? json.get("index").getAsInt() : 0, sections(string(json, "name", id)),
            sections(string(json, "content", "")), uuid(string(created, "creator", "")), time(string(created, "time", "")),
            uuid(string(modified, "modifier", "")), time(string(modified, "time", "")));
    }

    /**
     * The page as 3.5 wrote it, with &amp; colour codes.
     */
    static String write(final Page page) {
        final JsonObject json = new JsonObject();
        json.addProperty("content", ampersands(page.content()));
        final JsonObject created = new JsonObject();
        created.addProperty("creator", page.creator().toString());
        created.addProperty("time", page.created().toString());
        json.add("created", created);
        json.addProperty("index", page.index());
        final JsonObject modified = new JsonObject();
        modified.addProperty("modifier", page.modifier().toString());
        modified.addProperty("time", page.modified().toString());
        json.add("last_modified", modified);
        json.addProperty("name", ampersands(page.name()));
        return GSON.toJson(json);
    }

    private static boolean save(final Page page) {
        final Path file = files.computeIfAbsent(page.id(), id -> page.owner() == null ? PAGES.resolve(id + EXTENSION)
            : PLAYER_PAGES.resolve(page.owner().toString()).resolve(id.substring(id.lastIndexOf('/') + 1) + EXTENSION));
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, write(page), StandardCharsets.UTF_8);
            return true;
        } catch (final IOException e) {
            Almura.LOGGER.error("Couldn't save the guide page {}", file, e);
            return false;
        }
    }

    private static boolean delete(final Page page) {
        final Path file = files.remove(page.id());
        try {
            if (file != null) {
                Files.deleteIfExists(file);
            }
            return true;
        } catch (final IOException e) {
            Almura.LOGGER.error("Couldn't delete the guide page {}", file, e);
            return false;
        }
    }

    private static String string(final JsonObject json, final String key, final String fallback) {
        final JsonElement value = json.get(key);
        return value != null && value.isJsonPrimitive() ? value.getAsString() : fallback;
    }

    private static UUID uuid(final String text) {
        try {
            return UUID.fromString(text);
        } catch (final IllegalArgumentException e) {
            return NOBODY;
        }
    }

    private static Instant time(final String text) {
        try {
            return Instant.parse(text);
        } catch (final DateTimeParseException e) {
            return Instant.now();
        }
    }

    // 3.5 saved & colour codes; its pages may have either
    static String sections(final String text) {
        return text.replaceAll("&([0-9a-fk-orA-FK-OR])", "§$1");
    }

    static String ampersands(final String text) {
        return text.replaceAll("§([0-9a-fk-orA-FK-OR])", "&$1");
    }

    static String plain(final String text) {
        return text.replaceAll("§[0-9a-fk-orA-FK-OR]", "");
    }

    // ---- What a player may do

    private static boolean has(final ServerPlayer player, final PermissionNode<Boolean> node) {
        return PermissionAPI.getPermission(player, node);
    }

    /**
     * A server page's own permission (0 see, 1 change, 2 remove), or for a page added since the server started, being an operator.
     */
    private static boolean pagePermission(final ServerPlayer player, final Page page, final int which) {
        final PermissionNode<Boolean>[] nodes = pageNodes.get(page.id());
        return nodes != null ? has(player, nodes[which]) : isOperator(player);
    }

    private static boolean isOwner(final ServerPlayer player, final Page page) {
        return player.getUUID().equals(page.owner());
    }

    static boolean canSee(final ServerPlayer player, final Page page) {
        return page.playerGuide() ? isOwner(player, page) || has(player, ADMIN) : pagePermission(player, page, 0);
    }

    static boolean canEdit(final ServerPlayer player, final Page page) {
        return page.playerGuide() ? isOwner(player, page) || has(player, ADMIN) : pagePermission(player, page, 1);
    }

    static boolean canRemove(final ServerPlayer player, final Page page) {
        return page.playerGuide() ? isOwner(player, page) || has(player, ADMIN) : pagePermission(player, page, 2);
    }

    // As 3.5: only its owner moves a player's guide, only an administrator a server page
    static boolean canMove(final ServerPlayer player, final Page page) {
        return page.playerGuide() ? isOwner(player, page) : has(player, ADMIN);
    }

    private static Abilities abilities(final ServerPlayer player) {
        return new Abilities(has(player, ADD), has(player, PLAYER_ADD), has(player, ADMIN), has(player, REMOVE));
    }

    // Server pages, then the player's own guides, then other players'
    private static int group(final ServerPlayer player, final Page page) {
        return !page.playerGuide() ? 0 : isOwner(player, page) ? 1 : 2;
    }

    /**
     * The pages the player may see: server pages, their own guides, then other players'; each by index, then by name without its colours.
     */
    private static List<Page> visible(final ServerPlayer player) {
        return pages.values().stream().filter(page -> canSee(player, page))
            .sorted(Comparator.<Page>comparingInt(page -> group(player, page)).thenComparingInt(Page::index)
                .thenComparing(page -> plain(page.name()).toLowerCase(Locale.ROOT)))
            .toList();
    }

    // ---- Messages to the player

    /**
     * Sends the player the pages they may see, opening the guide at a page.
     */
    public static void sendListing(final ServerPlayer player, final OpenType open, final @Nullable String show) {
        final List<Entry> entries = new ArrayList<>();
        final MinecraftServer server = player.level().getServer();
        for (final Page page : visible(player)) {
            entries.add(new Entry(page.id(), page.name(), page.playerGuide(), isOwner(player, page),
                page.owner() == null ? "" : name(server, page.owner()), canEdit(player, page), canRemove(player, page), canMove(player, page)));
        }
        PacketDistributor.sendToPlayer(player, new ClientboundListing(open, entries, abilities(player), show == null ? "" : show));
    }

    private static void refreshEveryoneBut(final MinecraftServer server, final @Nullable ServerPlayer but) {
        for (final ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player != but) {
                sendListing(player, OpenType.REFRESH, null);
            }
        }
    }

    /**
     * The player asks to open the guide.
     */
    public static void open(final ServerPlayer player) {
        // The guide lives on Almura's server; in a released build single-player has none, as on 3.5
        if (FMLEnvironment.isProduction() && player.level().getServer().isSingleplayer()) {
            player.sendSystemMessage(Component.literal("This is only available on the Almura server, not in singleplayer."));
            return;
        }
        if (!has(player, OPEN)) {
            player.sendSystemMessage(Component.literal("Access denied, missing permission: ")
                .append(Component.literal("almura.guide.open").withStyle(ChatFormatting.AQUA))
                .append(Component.literal(".").withStyle(ChatFormatting.WHITE)));
            return;
        }
        sendListing(player, OpenType.PLAYER, null);
    }

    /**
     * The player asks for a page: sent if they may see it, else the list again.
     */
    public static void openPage(final ServerPlayer player, final String id) {
        final Page page = pages.get(id);
        if (page == null || !canSee(player, page)) {
            sendListing(player, OpenType.REFRESH, null);
            return;
        }
        final MinecraftServer server = player.level().getServer();
        PacketDistributor.sendToPlayer(player, new ClientboundPage(page.id(), page.index(), page.name(), page.content(),
            name(server, page.creator()), page.created().toEpochMilli(), name(server, page.modifier()), page.modified().toEpochMilli(),
            page.playerGuide(), canEdit(player, page)));
    }

    /**
     * A player's name, as the server last saw it, or the start of their UUID, as 3.5 showed it.
     */
    private static String name(final MinecraftServer server, final UUID uuid) {
        if (uuid.equals(NOBODY)) {
            return "";
        }
        final ServerPlayer online = server.getPlayerList().getPlayer(uuid);
        if (online != null) {
            return online.getGameProfile().name();
        }
        return server.services().nameToIdCache().get(uuid).map(profile -> profile.name()).orElse(uuid.toString().substring(0, 8));
    }

    private static void fail(final ServerPlayer player, final ChangeType change, final String id, final String message) {
        PacketDistributor.sendToPlayer(player, new ClientboundChangeResult(change, false, id, message));
    }

    // ---- Changes

    /**
     * The player adds, changes or removes a page, as 3.5 checked it.
     */
    public static void change(final ServerPlayer player, final ServerboundChange request) {
        final ChangeType change = request.change();
        final String name = request.name().trim();
        final String content = request.content().length() > GuideMessages.MAX_CONTENT ? request.content().substring(0, GuideMessages.MAX_CONTENT)
            : request.content();
        final boolean playerAdd = change == ChangeType.ADD && request.playerGuide();
        if (request.id().isEmpty() && !playerAdd) {
            fail(player, change, request.id(), "The page needs an ID.");
            return;
        }
        if (change != ChangeType.REMOVE && plain(name).isBlank()) {
            fail(player, change, request.id(), "The page needs a name.");
            return;
        }
        final MinecraftServer server = player.level().getServer();
        final Instant now = Instant.now();
        final Page changed;
        if (change == ChangeType.ADD) {
            if (playerAdd) {
                if (!has(player, PLAYER_ADD)) {
                    fail(player, change, "", "You may not add guides of your own.");
                    return;
                }
                final List<Page> own = pages.values().stream().filter(page -> isOwner(player, page)).toList();
                final int limit = Math.max(0, PermissionAPI.getPermission(player, PLAYER_LIMIT));
                if (own.size() >= limit) {
                    fail(player, change, "", "You have as many guides of your own as you may (" + limit + ").");
                    return;
                }
                final String id = playerGuideId(player.getUUID(), name);
                final int index = own.stream().mapToInt(Page::index).max().orElse(0) + 1;
                changed = new Page(id, index, name, content, player.getUUID(), now, player.getUUID(), now, player.getUUID());
            } else {
                if (pages.containsKey(request.id())) {
                    sendListing(player, OpenType.REFRESH, null);
                    fail(player, change, request.id(), "A page with that ID already exists.");
                    return;
                }
                if (!has(player, ADD)) {
                    fail(player, change, request.id(), "You may not add pages.");
                    return;
                }
                if (!SERVER_ID.matcher(request.id()).matches()) {
                    fail(player, change, request.id(), "A page's ID may only have a-z, 0-9, _ and -.");
                    return;
                }
                changed = new Page(request.id(), request.index(), name, content, player.getUUID(), now, player.getUUID(), now);
            }
        } else {
            final Page page = pages.get(request.id());
            if (page == null) {
                sendListing(player, OpenType.REFRESH, null);
                return;
            }
            if (change == ChangeType.MODIFY && !canEdit(player, page)) {
                fail(player, change, page.id(), "You may not change this page.");
                return;
            }
            if (change == ChangeType.REMOVE) {
                if (!canRemove(player, page)) {
                    fail(player, change, page.id(), "You may not remove this page.");
                    return;
                }
                if (!delete(page)) {
                    fail(player, change, page.id(), "The page couldn't be removed.");
                    return;
                }
                pages.remove(page.id());
                PacketDistributor.sendToPlayer(player, new ClientboundChangeResult(change, true, page.id(), "Removed."));
                sendListing(player, OpenType.REFRESH, null);
                refreshEveryoneBut(server, player);
                return;
            }
            // A server page's index comes with the change; a player's guide keeps its place
            changed = new Page(page.id(), page.playerGuide() ? page.index() : request.index(), name, content, page.creator(), page.created(),
                player.getUUID(), now, page.owner());
        }
        if (!save(changed)) {
            fail(player, change, changed.id(), "The page couldn't be saved.");
            return;
        }
        pages.put(changed.id(), changed);
        PacketDistributor.sendToPlayer(player, new ClientboundChangeResult(change, true, changed.id(), "Saved."));
        sendListing(player, OpenType.REFRESH, changed.id());
        refreshEveryoneBut(server, player);
        // As 3.5: everyone who reads a changed server page hears of it
        if (change == ChangeType.MODIFY && !changed.playerGuide()) {
            final String message = "The Guide: (" + plain(changed.name()) + ") has been updated!";
            for (final ServerPlayer reader : server.getPlayerList().getPlayers()) {
                if (canSee(reader, changed)) {
                    ServerNotifications.sendPopup(reader, "Guide Update", message, 10);
                }
            }
        }
    }

    /**
     * A player guide's id, from its name, as 3.5 made it: lower case letters and digits, the rest {@code _}, 32 at most, numbered when the
     * owner has one so named.
     */
    static String playerGuideId(final UUID owner, final String name) {
        String base = plain(name).toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "_").replaceAll("^_+|_+$", "");
        if (base.length() > PLAYER_ID_LENGTH) {
            base = base.substring(0, PLAYER_ID_LENGTH);
        }
        if (base.isEmpty()) {
            base = "guide";
        }
        final String prefix = PLAYER_PREFIX + owner + "/";
        String id = prefix + base;
        for (int number = 2; pages.containsKey(id); number++) {
            id = prefix + base + "_" + number;
        }
        return id;
    }

    /**
     * The player moves a page up or down its group (server pages, their guides, another player's), swapping places with the page next to
     * it as they see them; a group with indexes alike is numbered 1, 2, 3... first.
     */
    public static void move(final ServerPlayer player, final String id, final boolean up) {
        final Page page = pages.get(id);
        if (page == null || !canMove(player, page)) {
            sendListing(player, OpenType.REFRESH, null);
            return;
        }
        final List<Page> group = new ArrayList<>(visible(player).stream()
            .filter(other -> group(player, other) == group(player, page) && Objects.equals(other.owner(), page.owner())).toList());
        final int at = group.indexOf(page);
        final int to = at + (up ? -1 : 1);
        if (at < 0 || to < 0 || to >= group.size()) {
            return;
        }
        if (group.stream().map(Page::index).distinct().count() < group.size()) {
            for (int i = 0; i < group.size(); i++) {
                group.set(i, withIndex(group.get(i), i + 1));
            }
        }
        final Page here = group.get(at), there = group.get(to);
        group.set(at, withIndex(here, there.index()));
        group.set(to, withIndex(there, here.index()));
        for (final Page moved : group) {
            if (moved.index() != pages.get(moved.id()).index()) {
                pages.put(moved.id(), moved);
                save(moved);
            }
        }
        sendListing(player, OpenType.REFRESH, null);
        refreshEveryoneBut(player.level().getServer(), player);
    }

    private static Page withIndex(final Page page, final int index) {
        return new Page(page.id(), index, page.name(), page.content(), page.creator(), page.created(), page.modifier(), page.modified(),
            page.owner());
    }

    // ---- Opening at login

    @SubscribeEvent
    public void onLoggedIn(final PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof final ServerPlayer player && has(player, OPEN_AT_LOGIN)) {
            loginOpens.put(player.getUUID(), LOGIN_DELAY_TICKS);
        }
    }

    @SubscribeEvent
    public void onLoggedOut(final PlayerEvent.PlayerLoggedOutEvent event) {
        loginOpens.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public void onServerTick(final ServerTickEvent.Post event) {
        if (loginOpens.isEmpty()) {
            return;
        }
        final List<UUID> due = new ArrayList<>();
        loginOpens.replaceAll((uuid, ticks) -> ticks - 1);
        loginOpens.forEach((uuid, ticks) -> {
            if (ticks <= 0) {
                due.add(uuid);
            }
        });
        for (final UUID uuid : due) {
            loginOpens.remove(uuid);
            final ServerPlayer player = event.getServer().getPlayerList().getPlayer(uuid);
            // Not when dead, as 3.5
            if (player != null && player.isAlive()) {
                sendListing(player, OpenType.LOGGED_IN, null);
            }
        }
    }

    // ---- Commands

    @SubscribeEvent
    public void onRegisterCommands(final RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    private static void register(final CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("almura")
            .then(Commands.literal("guide")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("refresh").executes(ServerGuide::refresh))
                .then(Commands.literal("force")
                    .then(Commands.argument("targets", EntityArgument.players())
                        .executes(context -> force(context, null))
                        .then(Commands.argument("page", StringArgumentType.word())
                            .suggests((context, builder) -> {
                                pages.values().stream().filter(page -> !page.playerGuide()).forEach(page -> builder.suggest(page.id()));
                                return builder.buildFuture();
                            })
                            .executes(context -> force(context, StringArgumentType.getString(context, "page"))))))));
    }

    private static int refresh(final CommandContext<CommandSourceStack> context) {
        final int count = load();
        refreshEveryoneBut(context.getSource().getServer(), null);
        context.getSource().sendSuccess(() -> Component.literal("Pages reloaded (" + count + ")."), true);
        return count;
    }

    private static int force(final CommandContext<CommandSourceStack> context, final @Nullable String page) throws CommandSyntaxException {
        final Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "targets");
        final String by = context.getSource().getTextName();
        for (final ServerPlayer player : targets) {
            sendListing(player, OpenType.COMMAND, page != null && pages.containsKey(page) ? page : null);
            ServerNotifications.sendPopup(player, "Guide", "Guide has been forced open by: §b" + by, 5);
        }
        context.getSource().sendSuccess(() -> Component.literal("Opened the guide for " + targets.size() + " player"
            + (targets.size() == 1 ? "" : "s") + "."), true);
        return targets.size();
    }
}
