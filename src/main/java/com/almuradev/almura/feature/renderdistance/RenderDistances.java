/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.renderdistance;

import com.almuradev.almura.core.client.config.AlmuraClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Almura's render distances, as 3.5's: chests, sign text, item frames and caches past their distance (in blocks, from the camera) aren't
 * drawn. 0 means no limit, leaving vanilla's.
 */
public final class RenderDistances {

    private RenderDistances() {
    }

    /**
     * @return whether something this far (squared, in blocks) from the camera is past the setting's distance
     */
    public static boolean beyond(final ModConfigSpec.IntValue setting, final double distanceSquared) {
        final int blocks = AlmuraClientConfig.get(setting);
        return blocks > 0 && distanceSquared > (double) blocks * blocks;
    }

    /**
     * @return whether a point is past the setting's distance from the camera
     */
    public static boolean beyond(final ModConfigSpec.IntValue setting, final double x, final double y, final double z) {
        final Vec3 camera = Minecraft.getInstance().gameRenderer.mainCamera().position();
        return beyond(setting, camera.distanceToSqr(x, y, z));
    }
}
