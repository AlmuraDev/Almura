/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.machine.processing.client;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

/**
 * One of a machine's moving parts (e.g. the slicer's blade or a seed processor roller), kept out of the block model and drawn spinning around
 * its axle by {@link SpinningPartRenderer}.
 */
@SideOnly(Side.CLIENT)
final class SpinningPart {

    final ResourceLocation modelLocation;
    // A point on the axle and its direction, in block units (matching the models, which face north)
    final float axleX;
    final float axleY;
    final float axleZ;
    final float axisX;
    final float axisY;
    final float axisZ;
    // Negative to turn the other way
    final float degreesPerTick;

    /** Baked when models are (see MachinePartsClient); null until then. */
    @Nullable IBakedModel model;

    /**
     * @param model the part's OBJ model, e.g. "block/machine/kitchen_slicer_blade.obj"
     * @param axle a point on the axle (x, y, z)
     * @param axis the axle's direction (x, y, z)
     * @param degreesPerTick spin speed while running, negative to turn the other way
     */
    SpinningPart(final ResourceLocation model, final float[] axle, final float[] axis, final float degreesPerTick) {
        this.modelLocation = model;
        this.axleX = axle[0];
        this.axleY = axle[1];
        this.axleZ = axle[2];
        this.axisX = axis[0];
        this.axisY = axis[1];
        this.axisZ = axis[2];
        this.degreesPerTick = degreesPerTick;
    }
}
