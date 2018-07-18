/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.deadbush;

import com.almuradev.content.type.block.BlockUpdateFlag;
import com.almuradev.content.type.block.state.LazyBlockState;
import com.almuradev.content.util.WeightedLazyBlockState;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockCactus;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenDeadBush;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class DeadBushFeature extends WorldGenDeadBush implements DeadBush {
    private final List<WeightedLazyBlockState> deadBushes;

    DeadBushFeature(final List<WeightedLazyBlockState> deadBushes) {
        this.deadBushes = deadBushes;
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

        for (int i = 0; i < 4; i++) {
            final BlockPos targetPos =
                    origin.add(random.nextInt(8) - random.nextInt(8), random.nextInt(4) - random.nextInt(4), random.nextInt(8) - random.nextInt(8));

            final IBlockState existingState = world.getBlockState(targetPos);

            if (existingState.getBlock().isAir(existingState, world, targetPos)) {
                Collections.shuffle(this.deadBushes);
                final IBlockState deadBushState = WeightedRandom.getRandomItem(world.rand, this.deadBushes).getLazyBlockState().get();
                if (this.canPlace(world, targetPos, deadBushState, requires)) {
                    world.setBlockState(targetPos, deadBushState, BlockUpdateFlag.UPDATE_CLIENTS);
                }
            }
        }

        return true;
    }

    private boolean canPlace(final IBlockAccess access, final BlockPos pos, final IBlockState toPlaceState, final List<LazyBlockState> requires) {
        World world;

        if (access instanceof ChunkCache) {
            world = ((ChunkCache) access).world;
        } else {
            world = (World) access;
        }

        final Block toPlaceBlock = toPlaceState.getBlock();

        boolean canPlace = true;

        if (!requires.isEmpty()) {
            final IBlockState underState = access.getBlockState(pos.down());

            boolean found = false;

            for (final LazyBlockState lbs : requires) {
                if (lbs.partialTest(underState)) {
                    found = true;
                    break;
                }
            }

            canPlace = found;
        }

        if (canPlace) {
            // Why Vanilla lol
            if (toPlaceBlock instanceof BlockCactus) {
                canPlace = ((BlockCactus) toPlaceBlock).canBlockStay(world, pos);
            } else if (toPlaceBlock instanceof BlockBush) {
                canPlace = ((BlockBush) toPlaceBlock).canBlockStay(world, pos, toPlaceState);
            }
        }

        return canPlace;
    }
}
