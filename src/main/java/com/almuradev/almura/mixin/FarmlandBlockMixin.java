/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Farmland as Almura's 3.5 kept it ({@code MixinBlockFarmland}):
 * <ul>
 *     <li>it never turns back into dirt for drying out, crops on it or not (trampling still turns it);</li>
 *     <li>away from water and rain it dries a step only one random tick in ten, and not at all in a biome with more than 0.5 downfall
 *     or near an irrigation pipe ({@code almura:horizontal/agriculture/...} within 6 around, 1 to 6 above);</li>
 *     <li>rain wets it, and each farmland around it at even odds;</li>
 *     <li>water a level below it counts, as well as on its level and the one above.</li>
 * </ul>
 */
@Mixin(FarmlandBlock.class)
public abstract class FarmlandBlockMixin {

    @Unique private static final String almura$AGRICULTURE = "almura:horizontal/agriculture/";
    @Unique private static final Identifier almura$PIPE = Identifier.parse("almura:horizontal/agriculture/pipe");
    @Unique private static @Nullable Set<Block> almura$irrigation;

    @Shadow
    private static boolean isNearWater(final LevelReader level, final BlockPos pos) {
        throw new AssertionError();
    }

    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void almura$randomTick(final BlockState state, final ServerLevel level, final BlockPos pos, final RandomSource random,
        final CallbackInfo ci) {
        ci.cancel();
        final int moisture = state.getValue(FarmlandBlock.MOISTURE);
        final int revertChance = random.nextInt(10);
        // Read only when it changes the outcome: the scan covers a thousand blocks
        Boolean pipeNear = null;
        boolean setMaxMoisture = false;
        final boolean rainingHere = level.isRainingAt(pos.above());
        if (!isNearWater(level, pos) && !rainingHere && !(level.getBiome(pos).value().getModifiedClimateSettings().downfall() > 0.5f)) {
            if (moisture > 0 && revertChance == 1) {
                pipeNear = almura$irrigationNear(level, pos);
                if (!pipeNear) {
                    level.setBlock(pos, state.setValue(FarmlandBlock.MOISTURE, moisture - 1), Block.UPDATE_CLIENTS);
                }
            }
            // Vanilla turns dry farmland without a crop into dirt here; Almura's never does
        } else if (moisture < 7) {
            setMaxMoisture = true;
        }
        if (rainingHere) {
            for (final BlockPos at : BlockPos.betweenClosed(pos.offset(-1, 0, -1), pos.offset(1, 0, 1))) {
                if (!level.isLoaded(at)) {
                    continue;
                }
                final BlockState around = level.getBlockState(at);
                if (around.getBlock() instanceof FarmlandBlock && around.getValue(FarmlandBlock.MOISTURE) < 7 && random.nextInt(2) == 1) {
                    level.setBlock(at, around.setValue(FarmlandBlock.MOISTURE, 7), Block.UPDATE_CLIENTS);
                }
            }
        }
        if (moisture < 7 && (setMaxMoisture || (pipeNear != null ? pipeNear : almura$irrigationNear(level, pos)))) {
            level.setBlock(pos, state.setValue(FarmlandBlock.MOISTURE, 7), Block.UPDATE_CLIENTS);
        }
    }

    /**
     * Water is looked for from a level below the farmland, not from its own level.
     */
    @ModifyArg(method = "isNearWater", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;offset(III)Lnet/minecraft/core/BlockPos;",
        ordinal = 0), index = 1)
    private static int almura$waterBelow(final int y) {
        return -1;
    }

    @Unique
    private static boolean almura$irrigationNear(final ServerLevel level, final BlockPos pos) {
        if (almura$irrigation == null) {
            // Only once the content's pipes exist; any agriculture block counts, as on 3.5
            almura$irrigation = BuiltInRegistries.BLOCK.containsKey(almura$PIPE) ? BuiltInRegistries.BLOCK.stream()
                .filter(block -> BuiltInRegistries.BLOCK.getKey(block).toString().startsWith(almura$AGRICULTURE)).collect(Collectors.toSet())
                : Set.of();
        }
        if (almura$irrigation.isEmpty()) {
            return false;
        }
        for (final BlockPos at : BlockPos.betweenClosed(pos.offset(-6, 1, -6), pos.offset(6, 6, 6))) {
            if (level.isLoaded(at) && almura$irrigation.contains(level.getBlockState(at).getBlock())) {
                return true;
            }
        }
        return false;
    }
}
