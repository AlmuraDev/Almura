/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.mixin;

import com.almuradev.almura.content.registry.ContentGeneration;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.SimpleBlockFeature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Vanilla places no flowers (or no grass and ferns) in a chunk where world generation scattered Almura's grass mix in their place, as
 * 1.12's decoration events let 3.5 deny them. Everything else vanilla scatters this way (berries, pumpkins, cane) is left alone.
 */
@Mixin(SimpleBlockFeature.class)
public abstract class SimpleBlockFeatureMixin {

    @WrapOperation(method = "place", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/level/levelgen/feature/stateproviders/BlockStateProvider;getOptionalState(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
    private @Nullable BlockState almura$heldBack(final BlockStateProvider provider, final LevelAccessor level, final RandomSource random,
        final BlockPos pos, final Operation<BlockState> original) {
        final BlockState state = original.call(provider, level, random, pos);
        if (state == null) {
            return null;
        }
        if (state.is(BlockTags.FLOWERS) && ContentGeneration.taken(level, ContentGeneration.Kind.FLOWERS)) {
            return null;
        }
        if ((state.is(Blocks.SHORT_GRASS) || state.is(Blocks.TALL_GRASS) || state.is(Blocks.FERN) || state.is(Blocks.LARGE_FERN))
            && ContentGeneration.taken(level, ContentGeneration.Kind.GRASS)) {
            return null;
        }
        return state;
    }
}
