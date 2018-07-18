/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.cactus;

import com.almuradev.content.type.block.BlockUpdateFlag;
import com.almuradev.content.type.block.state.LazyBlockState;
import com.almuradev.content.util.WeightedLazyBlockState;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockCactus;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenCactus;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class CactusFeature extends WorldGenCactus implements Cactus {
    private final List<WeightedLazyBlockState> cacti;

    CactusFeature(final List<WeightedLazyBlockState> cacti) {
        this.cacti = cacti;
    }

    @Override
    public boolean generate(final World world, final Random random, final BlockPos origin) {
        return this.generate(world, random, origin, Collections.emptyList());
    }

    public boolean generate(final World world, final Random random, BlockPos origin, final List<LazyBlockState> requires) {
        for (int i = 0; i < 10; i++) {
            final BlockPos targetPos =
                    origin.add(random.nextInt(8) - random.nextInt(8), random.nextInt(4) - random.nextInt(4), random.nextInt(8) - random.nextInt(8));

            final IBlockState existingState = world.getBlockState(targetPos);

            if (existingState.getBlock().isAir(existingState, world, targetPos)) {

                // Place the block
                Collections.shuffle(this.cacti);
                final IBlockState cactusState = WeightedRandom.getRandomItem(world.rand, this.cacti).getLazyBlockState().get();
                if (this.canPlace(world, targetPos, cactusState, requires)) {
                    world.setBlockState(targetPos, cactusState, BlockUpdateFlag.UPDATE_CLIENTS);

                    // Cacti generation is about stacking, lets stack!
                    int j = 1 + random.nextInt(random.nextInt(2) + 1); // Since we handle placing the bottom block, lower the bound by 1

                    for (int k = 0; k < j; ++k) {
                        final BlockPos upPos = targetPos.up(k);

                        if (this.canPlace(world, upPos, cactusState, requires)) {
                            world.setBlockState(targetPos, cactusState, BlockUpdateFlag.UPDATE_CLIENTS);
                        }
                    }
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
