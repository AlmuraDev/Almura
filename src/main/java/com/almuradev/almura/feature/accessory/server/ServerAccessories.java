/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.accessory.server;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.accessory.AccessoryCatalog;
import com.almuradev.almura.feature.accessory.AccessoryType;
import com.almuradev.almura.feature.accessory.network.AccessoryMessages.ClientboundOpen;
import com.almuradev.almura.feature.accessory.network.AccessoryMessages.ClientboundWorn;
import com.almuradev.almura.feature.notification.server.ServerNotifications;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import net.neoforged.neoforge.server.permission.nodes.PermissionTypes;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The accessories on the server, as 3.5's {@code ServerAccessoryManager}: what each player wears, kept in
 * {@code config/almura/accessories.json}, sent to everyone when they join and whenever it changes, and the window opened with the types the
 * player may wear.
 * <p>
 * Permissions, as 3.5's (a permission plugin such as LuckPerms grants them; without one only operators have them):
 * {@code almura.accessory.<id>} for each accessory (bracelet, cathat, ears, halo, notchhat, sunglasses, tail, tophat, wings). An accessory
 * the player no longer has the permission for isn't shown.
 */
public final class ServerAccessories {

    private static final Path FILE = FMLPaths.CONFIGDIR.get().resolve(Almura.ID).resolve("accessories.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final Map<AccessoryType, PermissionNode<Boolean>> NODES = new EnumMap<>(AccessoryType.class);

    static {
        for (final AccessoryType type : AccessoryType.values()) {
            NODES.put(type, new PermissionNode<>(Almura.ID, type.permissionPath(), PermissionTypes.BOOLEAN,
                (player, uuid, context) -> player != null && player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)));
        }
    }

    private static Map<UUID, Map<AccessoryType, String>> worn;

    @SubscribeEvent
    public void onPermissionNodes(final PermissionGatherEvent.Nodes event) {
        NODES.values().forEach(event::addNodes);
    }

    /**
     * The player joining is told what everyone online wears, and everyone else what they wear.
     */
    @SubscribeEvent
    public void onLoggedIn(final PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof final ServerPlayer player)) {
            return;
        }
        final MinecraftServer server = player.level().getServer();
        for (final ServerPlayer other : server.getPlayerList().getPlayers()) {
            final Map<String, String> accessories = visible(other);
            if (!accessories.isEmpty()) {
                PacketDistributor.sendToPlayer(player, new ClientboundWorn(other.getUUID(), accessories));
            }
        }
        final Map<String, String> own = visible(player);
        if (!own.isEmpty()) {
            final ClientboundWorn message = new ClientboundWorn(player.getUUID(), own);
            for (final ServerPlayer other : server.getPlayerList().getPlayers()) {
                if (other != player) {
                    PacketDistributor.sendToPlayer(other, message);
                }
            }
        }
    }

    @SubscribeEvent
    public void onLoggedOut(final PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof final ServerPlayer player)) {
            return;
        }
        final ClientboundWorn message = new ClientboundWorn(player.getUUID(), Map.of());
        for (final ServerPlayer other : player.level().getServer().getPlayerList().getPlayers()) {
            if (other != player) {
                PacketDistributor.sendToPlayer(other, message);
            }
        }
    }

    /**
     * Opens the Accessories window of the player, with the types they may wear.
     */
    public static void open(final ServerPlayer player) {
        final List<String> allowed = new ArrayList<>();
        for (final AccessoryType type : AccessoryType.values()) {
            if (PermissionAPI.getPermission(player, NODES.get(type)) && !AccessoryCatalog.variants(type).isEmpty()) {
                allowed.add(type.id());
            }
        }
        PacketDistributor.sendToPlayer(player, new ClientboundOpen(allowed, visible(player)));
    }

    /**
     * Wears a variant of a type, or takes the type off when the variant is empty, then tells everyone online.
     */
    public static void select(final ServerPlayer player, final String typeId, final String variant) {
        final AccessoryType type = AccessoryType.byId(typeId);
        if (type == null) {
            return;
        }
        if (!PermissionAPI.getPermission(player, NODES.get(type))) {
            ServerNotifications.sendPopup(player, "§cAccessories", "You don't have permission to wear " + type.displayName() + ".", 5);
            return;
        }
        final Map<UUID, Map<AccessoryType, String>> all = load();
        if (variant.isEmpty()) {
            final Map<AccessoryType, String> accessories = all.get(player.getUUID());
            if (accessories != null) {
                accessories.remove(type);
                if (accessories.isEmpty()) {
                    all.remove(player.getUUID());
                }
            }
        } else {
            if (!AccessoryCatalog.contains(type, variant)) {
                return;
            }
            all.computeIfAbsent(player.getUUID(), key -> new EnumMap<>(AccessoryType.class)).put(type, variant);
        }
        save();
        PacketDistributor.sendToAllPlayers(new ClientboundWorn(player.getUUID(), visible(player)));
    }

    /**
     * @return what the player wears and still has the permission for, by id
     */
    private static Map<String, String> visible(final ServerPlayer player) {
        final Map<String, String> visible = new HashMap<>();
        final Map<AccessoryType, String> accessories = load().get(player.getUUID());
        if (accessories != null) {
            for (final Map.Entry<AccessoryType, String> entry : accessories.entrySet()) {
                if (PermissionAPI.getPermission(player, NODES.get(entry.getKey()))) {
                    visible.put(entry.getKey().id(), entry.getValue());
                }
            }
        }
        return visible;
    }

    private static Map<UUID, Map<AccessoryType, String>> load() {
        if (worn != null) {
            return worn;
        }
        worn = new HashMap<>();
        if (Files.notExists(FILE)) {
            return worn;
        }
        try (BufferedReader reader = Files.newBufferedReader(FILE, StandardCharsets.UTF_8)) {
            final JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            for (final Map.Entry<String, JsonElement> player : json.entrySet()) {
                final Map<AccessoryType, String> accessories = new EnumMap<>(AccessoryType.class);
                for (final Map.Entry<String, JsonElement> accessory : player.getValue().getAsJsonObject().entrySet()) {
                    final AccessoryType type = AccessoryType.byId(accessory.getKey());
                    if (type != null) {
                        accessories.put(type, accessory.getValue().getAsString());
                    }
                }
                if (!accessories.isEmpty()) {
                    worn.put(UUID.fromString(player.getKey()), accessories);
                }
            }
        } catch (final Exception e) {
            Almura.LOGGER.error("Couldn't read worn accessories from {}", FILE, e);
        }
        return worn;
    }

    private static void save() {
        final JsonObject json = new JsonObject();
        load().forEach((player, accessories) -> {
            final JsonObject entry = new JsonObject();
            accessories.forEach((type, variant) -> entry.addProperty(type.id(), variant));
            json.add(player.toString(), entry);
        });
        try {
            Files.createDirectories(FILE.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(FILE, StandardCharsets.UTF_8)) {
                GSON.toJson(json, writer);
            }
        } catch (final Exception e) {
            Almura.LOGGER.error("Couldn't save worn accessories to {}", FILE, e);
        }
    }
}
