/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.grass;

import com.almuradev.content.type.block.state.LazyBlockState;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenTallGrass;
import net.minecraftforge.common.IPlantable;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public final class GrassFeature extends WorldGenTallGrass implements Grass {

    private final List<LazyBlockState> grasses;

    GrassFeature(final List<LazyBlockState> grasses) {
        super(BlockTallGrass.EnumType.GRASS);

        this.grasses = grasses;
    }

    @Override
    public boolean generate(final World world, final Random rand, final BlockPos pos) {
        return this.generate(world, rand, pos, Collections.emptyList());
    }

    public boolean generate(final World world, final Random rand, final BlockPos pos, final List<LazyBlockState> requires) {
        final BlockPos.MutableBlockPos mutPos = new BlockPos.MutableBlockPos(pos);

        // Find starting point
        for (IBlockState blockState = world.getBlockState(mutPos); (blockState.getBlock().isAir(blockState, world, mutPos) || blockState
                .getBlock().isLeaves(blockState, world, mutPos)) && mutPos.getY() > 0; blockState = world.getBlockState(mutPos)) {
            mutPos.setPos(mutPos.getX(), mutPos.getY() - 1, mutPos.getZ());
        }

        // Place randomly around origin
        final BlockPos origin = new BlockPos(mutPos);

        for (int i = 0; i < 128; i++) {
            final BlockPos targetPos = origin.add(rand.nextInt(8) - rand.nextInt(8), rand.nextInt(4) -
                    rand.nextInt(4), rand.nextInt(8) - rand.nextInt(8));

            final IBlockState existingState = world.getBlockState(targetPos);
            final IBlockState underState = world.getBlockState(targetPos.down());

            if (existingState.getBlock().isAir(existingState, world, targetPos)) {
                this.grasses.stream()
                        .collect(Collectors.collectingAndThen(Collectors.toList(), collected -> {
                            Collections.shuffle(collected);
                            return collected.stream();
                        })).findFirst().ifPresent(grass -> {

                    final IBlockState grassState = grass.get();
                    if (this.canPlace(world, targetPos, EnumFacing.UP, underState, grassState, requires)) {
                        world.setBlockState(targetPos, grassState, 2);
                    }
                });
            }
        }
        return true;
    }

    private boolean canPlace(final IBlockAccess access, final BlockPos pos, final EnumFacing facing, final IBlockState underState,
            final IBlockState toPlaceState, final List<LazyBlockState> requires) {
        // TODO Even if requires test passes, should we STILL do a check against sustaining plants in-case we're planting an IPlantable via config?
        if (!requires.isEmpty()) {
            for (final LazyBlockState lbs : requires) {
                if (lbs.partialTest(underState)) {
                    return true;
                }
            }
            return false;
        }

        return !(toPlaceState.getBlock() instanceof IPlantable) || underState.getBlock()
                .canSustainPlant(underState, access, pos, facing, (IPlantable) toPlaceState.getBlock());

    }
}
