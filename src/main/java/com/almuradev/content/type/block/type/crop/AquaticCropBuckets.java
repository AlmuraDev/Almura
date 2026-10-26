/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.crop;

import com.almuradev.core.event.Witness;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * An aquatic crop in water reads as a still water source (BlockLiquid.LEVEL 0, so the water around it lies level instead of flowing towards
 * it), which an empty bucket would scoop up, deleting the plant: buckets leave crops alone.
 */
public final class AquaticCropBuckets implements Witness {

    @SubscribeEvent
    public void onFillBucket(final FillBucketEvent event) {
        final RayTraceResult target = event.getTarget();
        if (target == null || target.typeOfHit != RayTraceResult.Type.BLOCK) {
            return;
        }
        final IBlockState state = event.getWorld().getBlockState(target.getBlockPos());
        if (state.getBlock() instanceof CropBlockImpl && ((CropBlockImpl) state.getBlock()).isWaterState(state)) {
            event.setCanceled(true);
        }
    }
}
