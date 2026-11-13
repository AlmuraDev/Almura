/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.biome;

import com.almuradev.almura.feature.biome.network.ClientboundBiomeInformationPacket;
import com.almuradev.almura.shared.network.NetworkConfig;
import com.almuradev.core.event.Witness;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.event.world.LoadWorldEvent;
import org.spongepowered.api.event.world.UnloadWorldEvent;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

public final class BiomeServerFeature implements Witness {

    private final ChannelBinding.IndexedMessageChannel network;
    private Map<Integer, BiomeConfig> biomeConfigs;

    @Inject
    public BiomeServerFeature(final @ChannelId(NetworkConfig.CHANNEL) ChannelBinding.IndexedMessageChannel network) {
        this.network = network;
    }

    @Listener(order = Order.LAST)
    public void onClientConnectionEventJoin(ClientConnectionEvent.Join event, @Getter("getTargetEntity") Player player) {
        network.sendTo(player, new ClientboundBiomeInformationPacket(this.biomeConfigs));
    }

    @Listener(order = Order.LAST)
    public void onLoadWorld(LoadWorldEvent event) {
        this.biomeConfigs = this.getBiomeConfigs();
        this.network.sendToAll(new ClientboundBiomeInformationPacket(this.getBiomeConfigs()));
    }

    @Listener(order = Order.LAST)
    public void onUnloadWorld(UnloadWorldEvent event) {
        this.biomeConfigs = this.getBiomeConfigs();
        this.network.sendToAll(new ClientboundBiomeInformationPacket(this.getBiomeConfigs()));
    }

    private Map<Integer, BiomeConfig> getBiomeConfigs() {
        final ForgeRegistry<Biome> registry = (ForgeRegistry<Biome>) ForgeRegistries.BIOMES;
        final List<Biome> values = registry.getValues();

        final Map<Integer, BiomeConfig> biomes = new HashMap<>(values.size());
        for (Biome biome : values) {
            biomes.put(registry.getID(biome), BiomeConfig.of(biome));
        }

        return biomes;
    }
}
