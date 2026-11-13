/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.lighting.client;

import com.almuradev.almura.Almura;
import com.almuradev.almura.core.client.config.ClientConfiguration;
import com.almuradev.content.type.block.flicker.FlickerLightTileEntity;
import com.almuradev.content.type.block.flicker.FlickerLights;
import com.almuradev.core.event.Witness;
import com.almuradev.toolbox.config.map.MappedConfiguration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Drives flickering lights on the client: ticks each nearby light's flicker, relights the room on the bigger power events
 * (within a per-tick budget), and plays a buzz when the power falters. The server never sees any of this.
 */
@SideOnly(Side.CLIENT)
@Singleton
public final class ClientFlickerManager implements Witness, FlickerLights.Driver {

    /** Lights further than this from the player stay steady. */
    private static final double ACTIVE_RANGE_SQ = 48 * 48;
    private static final double SOUND_RANGE_SQ = 16 * 16;
    /** Room relights cost chunk rebuilds, so only this many happen per tick; the rest wait their turn. */
    private static final int MAX_RELIGHTS_PER_TICK = 8;
    private static final int MAX_SOUNDS_PER_TICK = 2;
    private static final SoundEvent FLICKER_SOUND = new SoundEvent(new ResourceLocation(Almura.ID, "light.flicker"));

    private final MappedConfiguration<ClientConfiguration> configAdapter;
    // Read from chunk render threads through getLightValue
    private final Map<BlockPos, FlickerState> lights = new ConcurrentHashMap<>();
    private final Deque<BlockPos> relightQueue = new ArrayDeque<>();
    private final Set<BlockPos> queued = new HashSet<>();
    private final Random random = new Random();

    @Inject
    private ClientFlickerManager(final MappedConfiguration<ClientConfiguration> configAdapter) {
        this.configAdapter = configAdapter;
        FlickerLights.setDriver(this);
        ClientRegistry.bindTileEntitySpecialRenderer(FlickerLightTileEntity.class, new FlickerLightRenderer(this));
    }

    @Override
    public void add(final BlockPos pos) {
        this.lights.putIfAbsent(pos, new FlickerState(pos));
    }

    @Override
    public void remove(final BlockPos pos) {
        this.lights.remove(pos);
    }

    @Override
    public int getLightValue(final BlockPos pos, final int steadyLight) {
        final FlickerState state = this.lights.get(pos);
        return state == null ? steadyLight : state.getLightValue(steadyLight);
    }

    float getBrightness(final BlockPos pos, final float partialTicks) {
        final FlickerState state = this.lights.get(pos);
        return state == null ? 1f : state.getBrightness(partialTicks);
    }

    @SubscribeEvent
    public void onWorldUnload(final WorldEvent.Unload event) {
        if (event.getWorld().isRemote) {
            this.lights.clear();
            this.relightQueue.clear();
            this.queued.clear();
        }
    }

    @SubscribeEvent
    public void onClientTick(final TickEvent.ClientTickEvent event) {
        final Minecraft client = Minecraft.getMinecraft();
        final World world = client.world;
        final Entity viewer = client.getRenderViewEntity();
        if (event.phase != TickEvent.Phase.END || world == null || viewer == null || client.isGamePaused() || this.lights.isEmpty()) {
            return;
        }

        final boolean enabled = this.configAdapter.get().general.flickeringLights;
        int sounds = 0;

        for (final Map.Entry<BlockPos, FlickerState> entry : this.lights.entrySet()) {
            final BlockPos pos = entry.getKey();
            final FlickerState state = entry.getValue();
            final double distanceSq = viewer.getDistanceSqToCenter(pos);

            if (!enabled || distanceSq > ACTIVE_RANGE_SQ) {
                if (state.reset()) {
                    this.queueRelight(pos);
                }
                continue;
            }

            final int flags = state.tick();
            if ((flags & FlickerState.CAST_CHANGED) != 0) {
                this.queueRelight(pos);
            }
            if ((flags & FlickerState.EVENT_STARTED) != 0 && distanceSq <= SOUND_RANGE_SQ && sounds < MAX_SOUNDS_PER_TICK) {
                this.playBuzz(client, pos);
                sounds++;
            }
        }

        for (int i = 0; i < MAX_RELIGHTS_PER_TICK && !this.relightQueue.isEmpty(); i++) {
            final BlockPos pos = this.relightQueue.poll();
            this.queued.remove(pos);
            if (world.isBlockLoaded(pos)) {
                world.checkLightFor(EnumSkyBlock.BLOCK, pos);
            }
        }
    }

    private void queueRelight(final BlockPos pos) {
        if (this.queued.add(pos)) {
            this.relightQueue.add(pos);
        }
    }

    private void playBuzz(final Minecraft client, final BlockPos pos) {
        final float volume = 0.15f + this.random.nextFloat() * 0.1f;
        final float pitch = 0.8f + this.random.nextFloat() * 0.4f;
        client.getSoundHandler().playSound(new PositionedSoundRecord(FLICKER_SOUND, SoundCategory.BLOCKS, volume, pitch, pos));
    }
}
