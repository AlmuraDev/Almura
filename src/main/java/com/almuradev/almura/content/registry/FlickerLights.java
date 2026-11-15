/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.content.registry;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * Connects the content's flickering lights ({@code flicker}) to what drives their flicker, as 3.5's did. Only the client installs a
 * {@link Driver}; a server world always gets the steady light, so gameplay (crops, spawning) never sees a flicker, single-player included.
 */
public final class FlickerLights {

    static @Nullable BlockEntityType<FlickerLightBlockEntity> blockEntityType;
    private static volatile @Nullable Driver driver;

    private FlickerLights() {
    }

    public static void setDriver(final @Nullable Driver driver) {
        FlickerLights.driver = driver;
    }

    public static @Nullable BlockEntityType<FlickerLightBlockEntity> blockEntityType() {
        return blockEntityType;
    }

    /**
     * The light the block at the position casts right now: its steady light, flickering in a client's world.
     */
    static int lightValue(final BlockGetter level, final BlockPos pos, final int steady) {
        final Driver current = driver;
        if (current == null || !(level instanceof final Level world) || !world.isClientSide()) {
            return steady;
        }
        return current.lightValue(pos, steady);
    }

    static void loaded(final @Nullable Level level, final BlockPos pos) {
        final Driver current = driver;
        if (current != null && level != null && level.isClientSide()) {
            current.add(pos.immutable());
        }
    }

    static void unloaded(final @Nullable Level level, final BlockPos pos) {
        final Driver current = driver;
        if (current != null && level != null && level.isClientSide()) {
            current.remove(pos.immutable());
        }
    }

    /**
     * What drives the flicker: the client's.
     */
    public interface Driver {

        void add(BlockPos pos);

        void remove(BlockPos pos);

        int lightValue(BlockPos pos, int steady);
    }

    /**
     * Marks a flickering light, so the client knows where they are. Holds nothing and doesn't tick.
     */
    public static final class FlickerLightBlockEntity extends BlockEntity {

        public FlickerLightBlockEntity(final BlockPos pos, final BlockState state) {
            super(blockEntityType, pos, state);
        }

        @Override
        public void onLoad() {
            super.onLoad();
            loaded(this.level, this.worldPosition);
        }

        @Override
        public void setRemoved() {
            super.setRemoved();
            unloaded(this.level, this.worldPosition);
        }

        @Override
        public void onChunkUnloaded() {
            super.onChunkUnloaded();
            unloaded(this.level, this.worldPosition);
        }
    }
}
