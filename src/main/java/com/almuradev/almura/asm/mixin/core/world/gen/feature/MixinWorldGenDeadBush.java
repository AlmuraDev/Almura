/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.asm.mixin.core.world.gen.feature;

import com.almuradev.almura.feature.generator.RandomPlantStateGenerator;
import com.almuradev.content.type.block.BlockUpdateFlag;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenDeadBush;
import net.minecraft.world.gen.feature.WorldGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Random;

@Mixin(value = WorldGenDeadBush.class, priority = 999)
public abstract class MixinWorldGenDeadBush extends WorldGenerator {
    /**
     * @author Dockter - Mike Howe
     * @reason Introduce new Desert Plants into Dead Bush Generator.
     */
    @Override
    @Overwrite
    public boolean generate(World worldIn, Random rand, BlockPos position) {
        final Random random = new Random();

        for (IBlockState iblockstate = worldIn.getBlockState(position); (iblockstate.getMaterial() == Material.AIR || iblockstate.getMaterial() == Material.LEAVES) && position.getY() > 0; iblockstate = worldIn.getBlockState(position)) {
            position = position.down();
        }

        for (int i = 0; i < 4; ++i) {
            BlockPos blockpos = position.add(rand.nextInt(8) - rand.nextInt(8), rand.nextInt(4) - rand.nextInt(4), rand.nextInt(8) - rand.nextInt(8));

            if (worldIn.isAirBlock(blockpos) && Blocks.DEADBUSH.canBlockStay(worldIn, blockpos, Blocks.DEADBUSH.getDefaultState())) {
                final int chance = random.nextInt(4) + 1;

                if (chance == 1) {
                    final IBlockState desertPlantState = RandomPlantStateGenerator.randomDesertPlant(worldIn, blockpos, random);
                    if (desertPlantState != null) {
                        worldIn.setBlockState(blockpos, desertPlantState, BlockUpdateFlag.UPDATE_NEIGHBORS | BlockUpdateFlag.UPDATE_CLIENTS);
                    }
                } else {
                    worldIn.setBlockState(blockpos, Blocks.DEADBUSH.getDefaultState(), 2);
                }
            }
        }

        return true;
    }
}