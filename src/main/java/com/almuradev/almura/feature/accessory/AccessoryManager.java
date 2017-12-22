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
import com.almuradev.almura.shared.event.Witness;
import com.almuradev.almura.shared.network.NetworkConfig;
import net.kyori.membrane.facet.Activatable;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.Game;
import org.spongepowered.api.GameState;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;

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
public final class AccessoryManager extends Witness.Impl implements Activatable, Witness.Lifecycle {

    private final Game game;
    private final PluginContainer container;
    private final ChannelBinding.IndexedMessageChannel network;
    private final AccessoryTypeRegistryModule accessoryStore;

    // Selected Accessories
    private Map<UUID, Map<AccessoryType, Accessory>> selectedAccessories = new HashMap<>();

    @Inject
    public AccessoryManager(final Game game, final PluginContainer container, @ChannelId(NetworkConfig.CHANNEL) final ChannelBinding
            .IndexedMessageChannel network, final AccessoryTypeRegistryModule accessoryStore) {
        this.game = game;
        this.container = container;
        this.network = network;
        this.accessoryStore = accessoryStore;
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
    public void onGameStartingServer(GameStartingServerEvent event) {
        this.selectedAccessories.clear();

        // TODO Load selected accessories from database server
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onClientConnectedToServer(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        this.selectedAccessories.clear();
    }

    @Listener
    public void onClientConnectionEventJoin(ClientConnectionEvent.Join event, @Getter("getTargetEntity") Player player) throws Exception {
        this.getAccessoryTypesFor(player).forEach((type) -> {
            try {
                this.putSelectedAccessory(player.getUniqueId(), type);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Send joining player all selected accessories
        Task.builder()
                .async()
                .delayTicks(40)
                .execute(() -> {
                    // TODO Need a bulk packet

                    this.game.getServer().getOnlinePlayers().forEach((p) -> {
                        this.network.sendTo(player, this.createPlayerSelectedAccessoriesPacket(p.getUniqueId()));
                    });
                })
                .submit(this.container);

        // Send everyone else joining player's selected accessories
        this.game.getServer().getOnlinePlayers().stream().filter((p) -> !p.getUniqueId().equals(player.getUniqueId())).forEach((p) -> {
            this.network.sendTo(p, this.createPlayerSelectedAccessoriesPacket(player.getUniqueId()));
        });
    }

    @Listener
    public void onClientConnectionEventDisconnect(ClientConnectionEvent.Disconnect event, @Getter("getTargetEntity") Player player) {
        this.removeSelectedAccessoriesFor(player.getUniqueId());

        this.network.sendToAll(this.createPlayerSelectedAccessoriesPacket(player.getUniqueId()));
    }

    public Set<AccessoryType> getAccessoryTypesFor(Player player) {
        checkNotNull(player);

        final Set<AccessoryType> accessoryTypes = new HashSet<>();

        this.accessoryStore.getAll().forEach((type) -> {
            final String permission = "almura.accessory." + type.getId().split(":")[1];

            if (player.hasPermission(permission)) {
                accessoryTypes.add(type);
            }
        });

        return accessoryTypes;
    }

    public Optional<Map<AccessoryType, Accessory>> getSelectedAccessoriesFor(UUID uniqueId) {
        checkNotNull(uniqueId);

        return Optional.ofNullable(this.selectedAccessories.get(uniqueId));
    }

    public void removeSelectedAccessoriesFor(UUID uniqueId) {
        checkNotNull(uniqueId);

        this.selectedAccessories.remove(uniqueId);
    }

    public void putSelectedAccessory(UUID uniqueId, AccessoryType type) throws Exception {
        checkNotNull(uniqueId);
        checkNotNull(type);

        final Accessory accessory = type.getAccessoryClass().getConstructor(UUID.class, AccessoryType.class).newInstance(uniqueId, type);

        final Map<AccessoryType, Accessory> selectedAccessories = this.selectedAccessories.computeIfAbsent(uniqueId, k -> new HashMap<>());
        selectedAccessories.put(type, accessory);
    }

    @SideOnly(Side.CLIENT)
    public void putSelectedAccessory(UUID uniqueId, String typeId) throws Exception {
        checkNotNull(uniqueId);
        checkNotNull(typeId);

        final AccessoryType type = this.accessoryStore.getById(typeId).orElse(null);
        if (type == null) {
            return;
        }

        this.putSelectedAccessory(uniqueId, type);
    }

    private ClientboundPlayerSelectedAccessoriesPacket createPlayerSelectedAccessoriesPacket(UUID uniqueId) {
        return new ClientboundPlayerSelectedAccessoriesPacket(uniqueId, this.selectedAccessories.getOrDefault(uniqueId, new HashMap<>()).keySet()
                .stream().map((CatalogType::getId)).collect(Collectors.toSet()));
    }
}
