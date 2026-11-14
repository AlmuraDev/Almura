/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.mixin;

import com.almuradev.almura.content.registry.ContentGeneration;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Vanilla's trees don't grow in a chunk where world generation grew one of Almura's, as 1.12's decoration event let 3.5 deny them.
 * Saplings are never held back (only world generation marks a chunk).
 */
@Mixin(TreeFeature.class)
public abstract class TreeFeatureMixin {

    @Inject(method = "place", at = @At("HEAD"), cancellable = true)
    private void almura$heldBack(final WorldGenLevel level, final ChunkGenerator generator, final RandomSource random, final BlockPos origin,
        final CallbackInfoReturnable<Boolean> cir) {
        if (ContentGeneration.taken(level, ContentGeneration.Kind.TREES)) {
            cir.setReturnValue(false);
        }
    }
}
