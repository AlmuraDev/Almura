/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.lighting.client;

import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

/**
 * One light's flicker: long steady stretches broken by random power trouble.
 *
 * <ul>
 *     <li>Stutter: the fixture blinks for a moment. Too fast to relight the room, so only the fixture changes.</li>
 *     <li>Brownout: the light sags for a second or two, dimming the room a little.</li>
 *     <li>Dropout: the light cuts out briefly, darkening the room.</li>
 * </ul>
 */
@SideOnly(Side.CLIENT)
final class FlickerState {

    /** Set by {@link #tick()} when the light cast into the room changed and needs relighting. */
    static final int CAST_CHANGED = 1;
    /** Set by {@link #tick()} when a power event started, for the buzz. */
    static final int EVENT_STARTED = 2;

    private enum Mode { STEADY, STUTTER, BROWNOUT, DROPOUT }

    private static final float BROWNOUT_CAST = 0.6f;

    private final Random random;
    private Mode mode = Mode.STEADY;
    private int ticksLeft;
    private float brightness = 1f;
    private float previousBrightness = 1f;
    private float cast = 1f;

    FlickerState(final BlockPos pos) {
        // Mixing in the time means two lights at the same spot in different sessions don't repeat each other
        this.random = new Random(pos.toLong() * 0x9E3779B97F4A7C15L ^ System.nanoTime());
        // Start part way through a steady stretch so lights placed together don't wake up together
        this.ticksLeft = 20 + this.random.nextInt(600);
    }

    int tick() {
        this.previousBrightness = this.brightness;
        int flags = 0;

        if (--this.ticksLeft <= 0) {
            flags |= this.nextMode();
        }

        switch (this.mode) {
            case STUTTER:
                // Blink between full and weak, never holding either for long
                if (this.random.nextInt(2) == 0) {
                    this.brightness = this.brightness > 0.5f ? 0.1f + this.random.nextFloat() * 0.3f : 1f;
                }
                break;
            case BROWNOUT:
                this.brightness = 0.45f + this.random.nextFloat() * 0.2f;
                break;
            case DROPOUT:
                // Mostly dark, with the odd weak spark of power
                this.brightness = this.random.nextInt(12) == 0 ? 0.3f : 0.03f;
                break;
            default:
                this.brightness = 1f;
                break;
        }
        return flags;
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
     * Puts the light back to steady, e.g. when the player moves away or turns flickering off.
     *
     * @return true if the light cast into the room changed
     */
    boolean reset() {
        final boolean castChanged = this.cast != 1f;
        this.mode = Mode.STEADY;
        this.ticksLeft = 100 + this.random.nextInt(600);
        this.brightness = 1f;
        this.previousBrightness = 1f;
        this.cast = 1f;
        return castChanged;
    }

    /**
     * @return the light cast into the room, from the light's steady value
     */
    int getLightValue(final int steadyLight) {
        return Math.round(steadyLight * this.cast);
    }

    /**
     * @return how bright the fixture is drawn, from 0 to 1, smoothed between ticks
     */
    float getBrightness(final float partialTicks) {
        return this.previousBrightness + (this.brightness - this.previousBrightness) * partialTicks;
    }
}
