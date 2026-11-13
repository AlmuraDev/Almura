/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.accessory;

import com.almuradev.almura.feature.accessory.network.ClientboundAccessoryGuiPacket;
import com.almuradev.almura.feature.accessory.network.ClientboundPlayerAccessoriesPacket;
import com.almuradev.almura.shared.network.NetworkConfig;
import com.almuradev.core.event.Witness;
import com.google.gson.*;
import net.kyori.membrane.facet.Activatable;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.GameState;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Keeps which accessories each player wears (one variant per type), saved to accessories.json in Almura's config folder, and tells every
 * client what everyone online wears so they can draw it.
 *
 * <p>Wearing a type needs its permission ({@link AccessoryType#permission()}). Choices for types a player lost the permission for are
 * kept but not shown.</p>
 */
public final class ServerAccessoryManager extends Witness.Impl implements Activatable, Witness.Lifecycle {

    private static final String FILE = "accessories.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Game game;
    private final Logger logger;
    private final Path file;
    private final ChannelBinding.IndexedMessageChannel network;
    private Map<UUID, Map<AccessoryType, String>> worn;

    @Inject
    private ServerAccessoryManager(final Game game, final Logger logger, @ConfigDir(sharedRoot = false) final Path configDir,
            @ChannelId(NetworkConfig.CHANNEL) final ChannelBinding.IndexedMessageChannel network) {
        this.game = game;
        this.logger = logger;
        this.file = configDir.resolve(FILE);
        this.network = network;
    }

    @Override
    public boolean active() {
        return this.game.isServerAvailable();
    }

    @Override
    public boolean lifecycleSubscribable(final GameState state) {
        return state == GameState.SERVER_STARTING;
    }

    @Listener(order = Order.LAST)
    public void onClientJoin(final ClientConnectionEvent.Join event, @Getter("getTargetEntity") final Player player) {
        for (final Player other : this.game.getServer().getOnlinePlayers()) {
            final Map<AccessoryType, String> accessories = this.visible(other);
            if (!accessories.isEmpty()) {
                this.network.sendTo(player, new ClientboundPlayerAccessoriesPacket(other.getUniqueId(), accessories));
            }
        }

        final Map<AccessoryType, String> own = this.visible(player);
        if (!own.isEmpty()) {
            final ClientboundPlayerAccessoriesPacket packet = new ClientboundPlayerAccessoriesPacket(player.getUniqueId(), own);
            for (final Player other : this.game.getServer().getOnlinePlayers()) {
                if (!other.getUniqueId().equals(player.getUniqueId())) {
                    this.network.sendTo(other, packet);
                }
            }
        }
    }

    @Listener
    public void onClientDisconnect(final ClientConnectionEvent.Disconnect event, @Getter("getTargetEntity") final Player player) {
        final ClientboundPlayerAccessoriesPacket packet = new ClientboundPlayerAccessoriesPacket(player.getUniqueId(), new EnumMap<>(AccessoryType.class));
        for (final Player other : this.game.getServer().getOnlinePlayers()) {
            if (!other.getUniqueId().equals(player.getUniqueId())) {
                this.network.sendTo(other, packet);
            }
        }
    }

    /**
     * Opens the accessory screen of the player with the types they may wear.
     */
    public void openGui(final Player player) {
        final List<AccessoryType> allowed = new ArrayList<>();
        for (final AccessoryType type : AccessoryType.values()) {
            if (player.hasPermission(type.permission()) && !AccessoryCatalog.variants(type).isEmpty()) {
                allowed.add(type);
            }
        }
        this.network.sendTo(player, new ClientboundAccessoryGuiPacket(allowed, this.visible(player)));
    }

    /**
     * Wears a variant of a type, or takes the type off when the variant is empty, then tells everyone online.
     */
    public void select(final Player player, final String typeId, final String variant) {
        final AccessoryType type = AccessoryType.byId(typeId);
        if (type == null) {
            return;
        }
        if (!player.hasPermission(type.permission())) {
            player.sendMessage(Text.of(TextColors.RED, "You don't have permission to wear ", type.displayName(), "."));
            return;
        }

        final Map<UUID, Map<AccessoryType, String>> all = this.load();
        if (variant.isEmpty()) {
            final Map<AccessoryType, String> accessories = all.get(player.getUniqueId());
            if (accessories != null) {
                accessories.remove(type);
                if (accessories.isEmpty()) {
                    all.remove(player.getUniqueId());
                }
            }
        } else {
            if (!AccessoryCatalog.contains(type, variant)) {
                return;
            }
            all.computeIfAbsent(player.getUniqueId(), k -> new EnumMap<>(AccessoryType.class)).put(type, variant);
        }
        this.save();

        final ClientboundPlayerAccessoriesPacket packet = new ClientboundPlayerAccessoriesPacket(player.getUniqueId(), this.visible(player));
        for (final Player other : this.game.getServer().getOnlinePlayers()) {
            this.network.sendTo(other, packet);
        }
    }

    /**
     * @return What the player wears and still has the permission for
     */
    private Map<AccessoryType, String> visible(final Player player) {
        final Map<AccessoryType, String> visible = new EnumMap<>(AccessoryType.class);
        final Map<AccessoryType, String> accessories = this.load().get(player.getUniqueId());
        if (accessories != null) {
            for (final Map.Entry<AccessoryType, String> entry : accessories.entrySet()) {
                if (player.hasPermission(entry.getKey().permission())) {
                    visible.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return visible;
    }

    private Map<UUID, Map<AccessoryType, String>> load() {
        if (this.worn != null) {
            return this.worn;
        }

        this.worn = new HashMap<>();
        if (Files.notExists(this.file)) {
            return this.worn;
        }
        try (final BufferedReader reader = Files.newBufferedReader(this.file, StandardCharsets.UTF_8)) {
            final JsonObject json = new JsonParser().parse(reader).getAsJsonObject();
            for (final Map.Entry<String, JsonElement> player : json.entrySet()) {
                final Map<AccessoryType, String> accessories = new EnumMap<>(AccessoryType.class);
                for (final Map.Entry<String, JsonElement> accessory : player.getValue().getAsJsonObject().entrySet()) {
                    final AccessoryType type = AccessoryType.byId(accessory.getKey());
                    if (type != null) {
                        accessories.put(type, accessory.getValue().getAsString());
                    }
                }
                if (!accessories.isEmpty()) {
                    this.worn.put(UUID.fromString(player.getKey()), accessories);
                }
            }
        } catch (final Exception ex) {
            this.logger.error("Failed to read worn accessories from '{}'.", this.file, ex);
        }
        return this.worn;
    }

    private void save() {
        final JsonObject json = new JsonObject();
        this.load().forEach((player, accessories) -> {
            final JsonObject entry = new JsonObject();
            accessories.forEach((type, variant) -> entry.addProperty(type.id(), variant));
            json.add(player.toString(), entry);
        });

        try {
            Files.createDirectories(this.file.getParent());
            try (final BufferedWriter writer = Files.newBufferedWriter(this.file, StandardCharsets.UTF_8)) {
                GSON.toJson(json, writer);
            }
        } catch (final Exception ex) {
            this.logger.error("Failed to save worn accessories to '{}'.", this.file, ex);
        }
    }
}
