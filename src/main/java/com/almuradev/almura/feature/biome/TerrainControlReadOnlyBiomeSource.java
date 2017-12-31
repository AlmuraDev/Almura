/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.biome;

import com.almuradev.almura.feature.hud.HeadUpDisplay;
import com.almuradev.almura.shared.event.Witness;
import com.google.common.base.MoreObjects;
import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.ConfigProvider;
import com.khorn.terraincontrol.configuration.ConfigToNetworkSender;
import com.khorn.terraincontrol.configuration.standard.PluginStandardValues;
import com.khorn.terraincontrol.forge.util.WorldHelper;
import com.khorn.terraincontrol.logging.LogMarker;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketCustomPayload;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import org.spongepowered.api.GameState;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.MoveEntityEvent;

import java.io.DataOutput;
import java.io.IOException;

import javax.annotation.Nullable;
import javax.inject.Inject;

public final class TerrainControlReadOnlyBiomeSource extends ReadOnlyBiomeSource {
    static final String TERRAIN_CONTROL_ID = "terraincontrol";
    private final HeadUpDisplay hud;

    @Inject
    private TerrainControlReadOnlyBiomeSource(final HeadUpDisplay hud) {
        this.hud = hud;
    }

    @Override
    public ReadOnlyBiome getBiome(final World world, final BlockPos pos) {
        @Nullable final LocalWorld lw = TerrainControl.getWorld(MoreObjects.firstNonNull(this.hud.worldName, WorldHelper.getName(world)));
        if (lw != null) {
            final LocalBiome biome = lw.getBiome(pos.getX(), pos.getZ());
            return new ReadOnlyBiome() {
                @Override
                public String name() {
                    return biome.getName();
                }

                @Override
                public float temperature(BlockPos pos) {
                    return biome.getTemperatureAt(pos.getX(), pos.getY(), pos.getZ());
                }
            };
        }
        return super.getBiome(world, pos);
    }

    public static class ClientNotifier implements Witness.Lifecycle {
        @Override
        public boolean lifecycleSubscribable(final GameState state) {
            return state == GameState.SERVER_STARTING && Loader.isModLoaded(TERRAIN_CONTROL_ID);
        }

        @Listener
        public void worldChange(final MoveEntityEvent.Teleport event) {
            final Entity entity = event.getTargetEntity();
            if (!(entity instanceof EntityPlayerMP)) {
                return;
            }

            final EntityPlayerMP player = (EntityPlayerMP) entity;

            final LocalWorld world = WorldHelper.toLocalWorld(player.getEntityWorld());
            if (world == null) {
                return;
            }

            final ConfigProvider config = world.getConfigs();

            final PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
            final DataOutput os = new ByteBufOutputStream(buf);
            try {
                os.writeInt(PluginStandardValues.ProtocolVersion);
                ConfigToNetworkSender.send(config, os);
            } catch (final IOException e) {
                TerrainControl.printStackTrace(LogMarker.FATAL, e);
            }

            final SPacketCustomPayload packet = new SPacketCustomPayload(PluginStandardValues.ChannelName, buf);
            player.connection.sendPacket(packet);
        }
    }
}
