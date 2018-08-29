/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.accessory;

import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.almura.feature.accessory.network.ClientboundPlayerSelectedAccessoriesPacket;
import com.almuradev.almura.feature.accessory.registry.AccessoryTypeRegistryModule;
import com.almuradev.almura.feature.accessory.type.Accessory;
import com.almuradev.almura.shared.network.NetworkConfig;
import com.almuradev.core.event.Witness;
import net.kyori.membrane.facet.Activatable;
import org.spongepowered.api.Game;
import org.spongepowered.api.GameState;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class ServerAccessoryManager extends Witness.Impl implements Activatable, Witness.Lifecycle {

    private final Game game;
    private final ChannelBinding.IndexedMessageChannel network;
    private final AccessoryTypeRegistryModule accessoryRegistry;

    private Map<UUID, Map<AccessoryType, Accessory>> selectedAccessories = new HashMap<>();

    @Inject
    public ServerAccessoryManager(final Game game, @ChannelId(NetworkConfig.CHANNEL) final ChannelBinding.IndexedMessageChannel network,
        final AccessoryTypeRegistryModule accessoryRegistry) {
        this.game = game;
        this.network = network;
        this.accessoryRegistry = accessoryRegistry;
    }

    @Override
    public boolean lifecycleSubscribable(GameState state) {
        return state == GameState.SERVER_STARTING;
    }

    @Override
    public boolean active() {
        return this.game.isServerAvailable();
    }

    @Listener
    public void onGameStartingServer(final GameStartingServerEvent event) {
        this.selectedAccessories.clear();

        // TODO Load selected accessories from database server
    }

    @Listener
    public void onClientConnectionEventJoin(final ClientConnectionEvent.Join event, @Getter("getTargetEntity") final Player player) {
        // TODO TEST CODE
        this.getAccessoryTypesFor(player).forEach((type) -> {
            try {
                this.putSelectedAccessory(player.getUniqueId(), type);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        this.game.getServer().getOnlinePlayers().forEach(p -> {
            final Map<AccessoryType, Accessory> accessories = this.selectedAccessories.get(p.getUniqueId());

            this.network.sendTo(player, new ClientboundPlayerSelectedAccessoriesPacket(p.getUniqueId(), accessories == null ? null :
                accessories.entrySet()
                    .stream().map(kv -> kv.getValue().getType().getId())
                    .collect(Collectors.toSet())));
        });

        final Map<AccessoryType, Accessory> accessories = this.selectedAccessories.get(player.getUniqueId());

        this.game.getServer().getOnlinePlayers().stream().filter(p -> !p.getUniqueId().equals(player.getUniqueId())).forEach(p -> this.network
            .sendTo(p, new ClientboundPlayerSelectedAccessoriesPacket(p.getUniqueId(), accessories == null ? null :
                accessories.entrySet()
                    .stream()
                    .map(kv -> kv.getValue().getType().getId())
                    .collect(Collectors.toSet()))));
    }

    @Listener
    public void onClientConnectionEventDisconnect(final ClientConnectionEvent.Disconnect event, @Getter("getTargetEntity") final Player player) {
        this.selectedAccessories.remove(player.getUniqueId());

        final UUID uniqueId = player.getUniqueId();
        this.network.sendToAll(new ClientboundPlayerSelectedAccessoriesPacket(uniqueId, null));
    }

    public Set<AccessoryType> getAccessoryTypesFor(final Player player) {
        checkNotNull(player);

        final Set<AccessoryType> accessoryTypes = new HashSet<>();

        this.accessoryRegistry.getAll().forEach((type) -> {
            final String permission = "almura.accessory." + type.getId().split(":")[1];

            if (player.hasPermission(permission)) {
                accessoryTypes.add(type);
            }
        });

        return accessoryTypes;
    }

    public Optional<Map<AccessoryType, Accessory>> getSelectedAccessoriesFor(final UUID uniqueId) {
        checkNotNull(uniqueId);

        return Optional.ofNullable(this.selectedAccessories.get(uniqueId));
    }

    public void putSelectedAccessory(final UUID uniqueId, final AccessoryType type) throws Exception {
        checkNotNull(uniqueId);
        checkNotNull(type);

        final Accessory accessory = type.getAccessoryClass().getConstructor(UUID.class, AccessoryType.class).newInstance(uniqueId, type);

        final Map<AccessoryType, Accessory> selectedAccessories = this.selectedAccessories.computeIfAbsent(uniqueId, k -> new HashMap<>());
        selectedAccessories.put(type, accessory);
    }
}
