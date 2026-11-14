/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.guide.server;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.guide.network.GuideMessages.ClientboundListing;
import com.almuradev.almura.feature.guide.network.GuideMessages.ClientboundPage;
import com.almuradev.almura.feature.guide.network.GuideMessages.Entry;
import com.almuradev.almura.feature.guide.network.GuideMessages.OpenType;
import com.almuradev.almura.feature.notification.server.ServerNotifications;
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
import java.util.UUID;
import java.util.stream.Stream;

/**
 * The Guide's pages on the server, as 3.5's {@code ServerPageManager}: one file a page under {@code config/almura/guide/pages} (3.5's
 * {@code .conf} files, which are JSON, can be copied in as they are), sent to the players who may see them.
 * <p>
 * Permissions, as 3.5's (a permission plugin such as LuckPerms grants them; without one only operators have them, as Sponge gave a player
 * nothing it wasn't granted):
 * <ul>
 *     <li>{@code almura.guide.open}: open the guide (the Features menu's Guide button);</li>
 *     <li>{@code almura.guide.open_at_login}: the guide opens a few seconds after joining (if the player's Show Guide on Login is on);</li>
 *     <li>{@code almura.guide.page.<id>}: see a page (for the pages there when the server started; a page added later is for operators until
 *     the next start).</li>
 * </ul>
 * Commands, operators only until Almura's command permissions are ported: {@code /almura guide refresh} reloads the pages, and
 * {@code /almura guide force <players> [page]} opens the guide on players.
 * <p>
 * Editing (adding, changing, removing and moving pages) and players' own guides are 3.5's too and come next.
 */
public final class ServerGuide {

    private static final Path PAGES = FMLPaths.CONFIGDIR.get().resolve(Almura.ID).resolve("guide").resolve("pages");
    private static final String EXTENSION = ".conf";
    private static final UUID NOBODY = new UUID(0L, 0L);
    // As 3.5: the guide opens 100 ticks after joining
    private static final int LOGIN_DELAY_TICKS = 100;

    public static final PermissionNode<Boolean> OPEN = node("guide.open");
    public static final PermissionNode<Boolean> OPEN_AT_LOGIN = node("guide.open_at_login");

    private static final Map<String, Page> pages = new LinkedHashMap<>();
    private static final Map<String, PermissionNode<Boolean>> pageNodes = new HashMap<>();
    private static final Map<UUID, Integer> loginOpens = new HashMap<>();

    /**
     * A page: its file's name is its id.
     */
    public record Page(String id, int index, String name, String content, UUID creator, Instant created, UUID modifier, Instant modified) {
    }

    private static PermissionNode<Boolean> node(final String path) {
        return new PermissionNode<>(Almura.ID, path, PermissionTypes.BOOLEAN,
            (player, uuid, context) -> player != null && player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER));
    }

    @SubscribeEvent
    public void onPermissionNodes(final PermissionGatherEvent.Nodes event) {
        // The pages are read here, at the server's start, so each gets its permission
        load();
        event.addNodes(OPEN, OPEN_AT_LOGIN);
        for (final String id : pages.keySet()) {
            final PermissionNode<Boolean> node = node("guide.page." + id);
            pageNodes.put(id, node);
            event.addNodes(node);
        }
    }

    /**
     * Reads every page.
     *
     * @return how many there are
     */
    public static int load() {
        pages.clear();
        try {
            Files.createDirectories(PAGES);
            try (Stream<Path> files = Files.walk(PAGES)) {
                for (final Path file : (Iterable<Path>) files.filter(path -> path.toString().endsWith(EXTENSION))::iterator) {
                    final String name = file.getFileName().toString();
                    final String id = name.substring(0, name.length() - EXTENSION.length());
                    try {
                        pages.put(id, read(id, Files.readString(file, StandardCharsets.UTF_8)));
                    } catch (final IOException | RuntimeException e) {
                        Almura.LOGGER.error("Couldn't read the guide page {}", file, e);
                    }
                }
            }
        } catch (final IOException e) {
            Almura.LOGGER.error("Couldn't read the guide's pages in {}", PAGES, e);
        }
        Almura.LOGGER.info("Loaded {} guide page(s)", pages.size());
        return pages.size();
    }

    /**
     * A page from 3.5's file: {@code index}, {@code name}, {@code content}, {@code created} and {@code last_modified} (each a UUID and an
     * ISO time). Its text keeps § colour codes; 3.5's &amp; ones are turned into them.
     */
    static Page read(final String id, final String text) {
        final JsonObject json = JsonParser.parseString(text).getAsJsonObject();
        final JsonObject created = json.get("created") instanceof final JsonObject object ? object : new JsonObject();
        final JsonObject modified = json.get("last_modified") instanceof final JsonObject object ? object : new JsonObject();
        return new Page(id, json.has("index") ? json.get("index").getAsInt() : 0, colours(string(json, "name", id)),
            colours(string(json, "content", "")), uuid(string(created, "creator", "")), time(string(created, "time", "")),
            uuid(string(modified, "modifier", "")), time(string(modified, "time", "")));
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
    private static String colours(final String text) {
        return text.replaceAll("&([0-9a-fk-orA-FK-OR])", "§$1");
    }

    private static String plain(final String text) {
        return text.replaceAll("§[0-9a-fk-orA-FK-OR]", "");
    }

    private static boolean has(final ServerPlayer player, final PermissionNode<Boolean> node) {
        return PermissionAPI.getPermission(player, node);
    }

    /**
     * Whether the player may see the page: its permission, or for a page added since the server started, being an operator.
     */
    private static boolean canSee(final ServerPlayer player, final Page page) {
        final PermissionNode<Boolean> node = pageNodes.get(page.id());
        return node != null ? has(player, node) : player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
    }

    /**
     * The pages the player may see: by index, then by name without its colours.
     */
    private static List<Page> visible(final ServerPlayer player) {
        return pages.values().stream().filter(page -> canSee(player, page))
            .sorted(Comparator.comparingInt(Page::index).thenComparing(page -> plain(page.name()).toLowerCase(Locale.ROOT)))
            .toList();
    }

    /**
     * Sends the player the pages they may see, opening the guide at a page.
     */
    public static void sendListing(final ServerPlayer player, final OpenType open, final @Nullable String show) {
        final List<Entry> entries = new ArrayList<>();
        for (final Page page : visible(player)) {
            entries.add(new Entry(page.id(), page.name()));
        }
        PacketDistributor.sendToPlayer(player, new ClientboundListing(open, entries, show == null ? "" : show));
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
        PacketDistributor.sendToPlayer(player, new ClientboundPage(page.id(), page.name(), page.content(), name(player.level().getServer(),
            page.creator()), page.created().toEpochMilli(), name(player.level().getServer(), page.modifier()), page.modified().toEpochMilli()));
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
                                pages.keySet().forEach(builder::suggest);
                                return builder.buildFuture();
                            })
                            .executes(context -> force(context, StringArgumentType.getString(context, "page"))))))));
    }

    private static int refresh(final CommandContext<CommandSourceStack> context) {
        final int count = load();
        for (final ServerPlayer player : context.getSource().getServer().getPlayerList().getPlayers()) {
            sendListing(player, OpenType.REFRESH, null);
        }
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
