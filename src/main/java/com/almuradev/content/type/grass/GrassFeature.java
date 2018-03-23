/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.grass;

import com.almuradev.content.type.block.BlockUpdateFlag;
import com.almuradev.content.type.block.state.LazyBlockState;
import com.almuradev.content.util.WeightedLazyBlockState;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenTallGrass;
import net.minecraftforge.common.IPlantable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public final class GrassFeature extends WorldGenTallGrass implements Grass {
    private final List<WeightedLazyBlockState> grasses;

    GrassFeature(final List<WeightedLazyBlockState> grasses) {
        super(BlockTallGrass.EnumType.GRASS);
        this.grasses = grasses;
    }

    @Override
    public boolean generate(final World world, final Random random, final BlockPos origin) {
        return this.generate(world, random, origin, Collections.emptyList());
    }

    public boolean generate(final World world, final Random random, BlockPos origin, final List<LazyBlockState> requires) {
        final BlockPos.MutableBlockPos mutPos = new BlockPos.MutableBlockPos(origin);

        // Find starting point
        for (IBlockState state = world.getBlockState(mutPos);
             (state.getBlock().isAir(state, world, mutPos) || state.getBlock().isLeaves(state, world, mutPos)) && mutPos.getY() > 0;
             state = world.getBlockState(mutPos)) {
            mutPos.setPos(mutPos.getX(), mutPos.getY() - 1, mutPos.getZ());
        }

        // Place randomly around origin
        origin = new BlockPos(mutPos);

        for (int i = 0; i < 128; i++) {
            final BlockPos targetPos =
                    origin.add(random.nextInt(8) - random.nextInt(8), random.nextInt(4) - random.nextInt(4), random.nextInt(8) - random.nextInt(8));

            final IBlockState existingState = world.getBlockState(targetPos);
            final IBlockState underState = world.getBlockState(targetPos.down());

            if (existingState.getBlock().isAir(existingState, world, targetPos)) {
                Collections.shuffle(this.grasses);
                final IBlockState grassState = WeightedRandom.getRandomItem(world.rand, this.grasses).getLazyBlockState().get();
                if (this.canPlace(world, targetPos, EnumFacing.UP, underState, grassState, requires)) {
                    world.setBlockState(targetPos, grassState, BlockUpdateFlag.UPDATE_CLIENTS);
                }
            }
        }

        return true;
    }

    private boolean canPlace(final IBlockAccess world, final BlockPos pos, final EnumFacing facing, final IBlockState underState, final IBlockState toPlaceState, final List<LazyBlockState> requires) {
        // TODO Even if requires test passes, should we STILL do a check against sustaining plants in-case we're planting an IPlantable via config?
        if (!requires.isEmpty()) {
            for (final LazyBlockState lbs : requires) {
                if (lbs.partialTest(underState)) {
                    return true;
                }
            }
            return false;
        }

        return !(toPlaceState.getBlock() instanceof IPlantable) || underState.getBlock().canSustainPlant(underState, world, pos, facing, (IPlantable) toPlaceState.getBlock());
    }
}
