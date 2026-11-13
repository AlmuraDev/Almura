/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.flicker;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * Connects flickering content lights to whatever drives their flicker. Only the client installs a {@link Driver};
 * without one (e.g. on a server) lights report their steady light.
 */
public final class FlickerLights {

    @Nullable private static volatile Driver driver;

    private FlickerLights() {
    }

    public static void setDriver(@Nullable final Driver driver) {
        FlickerLights.driver = driver;
    }

    /**
     * @return the light the block at the position casts right now
     */
    public static int getLightValue(final IBlockAccess world, final BlockPos pos, final int steadyLight) {
        final Driver current = driver;
        // A server world always gets the steady light, so gameplay (crops, spawning) never sees a flicker
        if (current == null || (world instanceof World && !((World) world).isRemote)) {
            return steadyLight;
        }
        return current.getLightValue(pos, steadyLight);
    }

    static void onLoad(final World world, final BlockPos pos) {
        final Driver current = driver;
        if (current != null && world.isRemote) {
            current.add(pos.toImmutable());
        }
    }

    static void onUnload(final World world, final BlockPos pos) {
        final Driver current = driver;
        if (current != null && world.isRemote) {
            current.remove(pos.toImmutable());
        }
    }

    public interface Driver {

        void add(BlockPos pos);

        void remove(BlockPos pos);

        int getLightValue(BlockPos pos, int steadyLight);
    }
}
