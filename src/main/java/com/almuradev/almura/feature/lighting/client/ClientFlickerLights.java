/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.lighting.client;

import com.almuradev.almura.Almura;
import com.almuradev.almura.content.registry.FlickerLights;
import com.almuradev.almura.core.client.config.AlmuraClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Drives the content's flickering lights on the client, as 3.5's {@code ClientFlickerManager} did: ticks each nearby light's flicker,
 * relights the room on its bigger power events (a few a tick, as relighting costs section rebuilds), and plays a buzz when the power
 * falters. The server never sees any of it. Client only.
 */
public final class ClientFlickerLights implements FlickerLights.Driver {

    // Lights further than this from the player stay steady
    private static final double ACTIVE_RANGE_SQ = 48 * 48;
    private static final double SOUND_RANGE_SQ = 16 * 16;
    private static final int MAX_RELIGHTS_PER_TICK = 8;
    private static final int MAX_SOUNDS_PER_TICK = 2;
    private static final SoundEvent FLICKER_SOUND = SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(Almura.ID, "light.flicker"));

    // Read from the light engine through lightValue
    private final Map<BlockPos, FlickerState> lights = new ConcurrentHashMap<>();
    private final Deque<BlockPos> relights = new ArrayDeque<>();
    private final Set<BlockPos> queued = new HashSet<>();
    private final Random random = new Random();

    private static @org.jspecify.annotations.Nullable ClientFlickerLights instance;

    public static void install() {
        final ClientFlickerLights lights = new ClientFlickerLights();
        instance = lights;
        FlickerLights.setDriver(lights);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(lights);
    }

    /**
     * How bright the fixture at the position is drawn, 0 to 1, smoothed between ticks: 1 for a steady light.
     */
    public static float brightness(final BlockPos pos, final float partialTicks) {
        final ClientFlickerLights lights = instance;
        final FlickerState state = lights == null ? null : lights.lights.get(pos);
        return state == null ? 1f : state.brightness(partialTicks);
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
    public int lightValue(final BlockPos pos, final int steady) {
        final FlickerState state = this.lights.get(pos);
        return state == null ? steady : state.lightValue(steady);
    }

    @SubscribeEvent
    public void onLevelUnload(final LevelEvent.Unload event) {
        if (event.getLevel().isClientSide()) {
            this.lights.clear();
            this.relights.clear();
            this.queued.clear();
        }
    }

    @SubscribeEvent
    public void onClientTick(final ClientTickEvent.Post event) {
        final Minecraft minecraft = Minecraft.getInstance();
        final ClientLevel level = minecraft.level;
        final Entity viewer = minecraft.getCameraEntity();
        if (level == null || viewer == null || minecraft.isPaused() || this.lights.isEmpty()) {
            return;
        }
        final boolean enabled = AlmuraClientConfig.get(AlmuraClientConfig.FLICKERING_LIGHTS);
        int sounds = 0;
        for (final Map.Entry<BlockPos, FlickerState> entry : this.lights.entrySet()) {
            final BlockPos pos = entry.getKey();
            final FlickerState state = entry.getValue();
            final double distanceSq = viewer.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
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
                this.buzz(minecraft, pos);
                sounds++;
            }
        }
        for (int i = 0; i < MAX_RELIGHTS_PER_TICK && !this.relights.isEmpty(); i++) {
            final BlockPos pos = this.relights.poll();
            this.queued.remove(pos);
            if (level.isLoaded(pos)) {
                // The client's light engine runs its updates each tick and marks the sections to draw again
                level.getChunkSource().getLightEngine().checkBlock(pos);
            }
        }
    }

    private void queueRelight(final BlockPos pos) {
        if (this.queued.add(pos)) {
            this.relights.add(pos);
        }
    }

    private void buzz(final Minecraft minecraft, final BlockPos pos) {
        final float volume = 0.15f + this.random.nextFloat() * 0.1f;
        final float pitch = 0.8f + this.random.nextFloat() * 0.4f;
        minecraft.getSoundManager().play(new SimpleSoundInstance(FLICKER_SOUND, SoundSource.BLOCKS, volume, pitch, RandomSource.create(), pos));
    }

    /**
     * One light's flicker, as 3.5's: long steady stretches broken by power trouble. A stutter blinks the fixture for a moment (too fast to
     * relight the room); a brownout sags the light for a second or two, dimming the room a little; a dropout cuts it out briefly.
     */
    static final class FlickerState {

        static final int CAST_CHANGED = 1;
        static final int EVENT_STARTED = 2;
        private static final float BROWNOUT_CAST = 0.6f;

        private enum Mode { STEADY, STUTTER, BROWNOUT, DROPOUT }

        private final Random random;
        private Mode mode = Mode.STEADY;
        private int ticksLeft;
        private float cast = 1f;
        // How bright the fixture is drawn this tick and the last
        private float brightness = 1f, previousBrightness = 1f;

        FlickerState(final BlockPos pos) {
            // The time mixed in, so two lights at a spot in different sessions don't repeat each other
            this.random = new Random(pos.asLong() * 0x9E3779B97F4A7C15L ^ System.nanoTime());
            // Part way through a steady stretch, so lights placed together don't wake up together
            this.ticksLeft = 20 + this.random.nextInt(600);
        }

        int tick() {
            this.previousBrightness = this.brightness;
            final int flags = --this.ticksLeft <= 0 ? this.nextMode() : 0;
            switch (this.mode) {
                // Blinks between full and weak, never holding either for long
                case STUTTER -> {
                    if (this.random.nextInt(2) == 0) {
                        this.brightness = this.brightness > 0.5f ? 0.1f + this.random.nextFloat() * 0.3f : 1f;
                    }
                }
                case BROWNOUT -> this.brightness = 0.45f + this.random.nextFloat() * 0.2f;
                // Mostly dark, with the odd weak spark of power
                case DROPOUT -> this.brightness = this.random.nextInt(12) == 0 ? 0.3f : 0.03f;
                default -> this.brightness = 1f;
            }
            return flags;
        }

        float brightness(final float partialTicks) {
            return this.previousBrightness + (this.brightness - this.previousBrightness) * partialTicks;
        }

        private int nextMode() {
            final float before = this.cast;
            int flags = 0;
            if (this.mode != Mode.STEADY) {
                // After a dropout the light sometimes struggles back on
                if (this.mode == Mode.DROPOUT && this.random.nextBoolean()) {
                    this.start(Mode.STUTTER, 6 + this.random.nextInt(10), 1f);
                } else {
                    this.start(Mode.STEADY, 100 + this.random.nextInt(600), 1f);
                }
            } else {
                final int roll = this.random.nextInt(100);
                if (roll < 60) {
                    this.start(Mode.STUTTER, 6 + this.random.nextInt(14), 1f);
                } else if (roll < 85) {
                    this.start(Mode.BROWNOUT, 30 + this.random.nextInt(60), BROWNOUT_CAST);
                } else {
                    this.start(Mode.DROPOUT, 10 + this.random.nextInt(40), 0f);
                }
                flags |= EVENT_STARTED;
            }
            if (this.cast != before) {
                flags |= CAST_CHANGED;
            }
            return flags;
        }

        private void start(final Mode mode, final int ticks, final float cast) {
            this.mode = mode;
            this.ticksLeft = ticks;
            this.cast = cast;
        }

        /**
         * Back to steady (the player moved away, or flickering was turned off).
         *
         * @return whether the light cast into the room changed
         */
        boolean reset() {
            final boolean changed = this.cast != 1f;
            this.mode = Mode.STEADY;
            this.ticksLeft = 100 + this.random.nextInt(600);
            this.cast = 1f;
            this.brightness = 1f;
            this.previousBrightness = 1f;
            return changed;
        }

        int lightValue(final int steady) {
            return Math.round(steady * this.cast);
        }
    }
}
