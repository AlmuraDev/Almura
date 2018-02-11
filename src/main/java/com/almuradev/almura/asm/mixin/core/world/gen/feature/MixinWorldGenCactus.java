package com.almuradev.almura.asm.mixin.core.world.gen.feature;

import com.almuradev.almura.feature.generator.RandomPlantStateGenerator;
import com.almuradev.content.type.block.BlockUpdateFlag;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenCactus;
import net.minecraft.world.gen.feature.WorldGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Random;

@Mixin(value = WorldGenCactus.class, priority = 999)
public class MixinWorldGenCactus extends WorldGenerator {
    /**
     * @author Dockter - Mike Howe
     * @reason Introduce new Desert Plants into Cactus Generator.
     */
    @Override
    @Overwrite
    public boolean generate(World worldIn, Random rand, BlockPos position) {
        final Random random = new Random();

        for (int i = 0; i < 10; ++i) {
            BlockPos blockpos = position.add(rand.nextInt(8) - rand.nextInt(8), rand.nextInt(4) - rand.nextInt(4), rand.nextInt(8) - rand.nextInt(8));

            if (worldIn.isAirBlock(blockpos)) {
                int j = 1 + rand.nextInt(rand.nextInt(3) + 1);

                for (int k = 0; k < j; ++k) {
                    if (Blocks.CACTUS.canBlockStay(worldIn, blockpos)) {
                        final int chance = random.nextInt(10)+ 1;

                        if (chance == 1) {
                            final IBlockState desertPlantState = RandomPlantStateGenerator.randomDesertPlant(worldIn, blockpos, random);
                            if (desertPlantState != null) {
                                worldIn.setBlockState(blockpos, desertPlantState, BlockUpdateFlag.UPDATE_NEIGHBORS | BlockUpdateFlag.UPDATE_CLIENTS);
                            }
                        } else {
                            worldIn.setBlockState(blockpos.up(k), Blocks.CACTUS.getDefaultState(), 2);
                        }
                    }
                }
            }
        }

        return true;
    }
}