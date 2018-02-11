package com.almuradev.almura.asm.mixin.core.world.gen.feature;

import com.almuradev.almura.feature.generator.RandomPlantStateGenerator;
import com.almuradev.content.type.block.BlockUpdateFlag;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenTallGrass;
import net.minecraft.world.gen.feature.WorldGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Random;

@Mixin(value = WorldGenTallGrass.class, priority = 999)
public abstract class MixinWorldGenTallGrass extends WorldGenerator {

    @Shadow private final IBlockState tallGrassState;

    public MixinWorldGenTallGrass(BlockTallGrass.EnumType p_i45629_1_) {
        this.tallGrassState = Blocks.TALLGRASS.getDefaultState().withProperty(BlockTallGrass.TYPE, p_i45629_1_);  //This is never called so shadowing the field works when in production mode.
    }

    /**
     * @author Dockter - Mike Howe
     * @reason Introduce new Plants into Tall grass decorator.
     */
    @Override
    @Overwrite
    public boolean generate(World worldIn, Random rand, BlockPos position) {
        final Random random = new Random();

        for (IBlockState iblockstate = worldIn.getBlockState(position); (iblockstate.getMaterial() == Material.AIR || iblockstate.getMaterial() == Material.LEAVES) && position.getY() > 0; iblockstate = worldIn.getBlockState(position)) {
            position = position.down();
        }

        for (int i = 0; i < 128; ++i) {
            final BlockPos blockpos = position.add(rand.nextInt(8) - rand.nextInt(8), rand.nextInt(4) - rand.nextInt(4), rand.nextInt(8) - rand.nextInt(8));

            if (worldIn.isAirBlock(blockpos) && Blocks.TALLGRASS.canBlockStay(worldIn, blockpos, this.tallGrassState)) {

                // Notice:  the TallGrass Generator runs on Temperate and Ice Biomes.

                if (worldIn.getBiome(blockpos).getTemperature(blockpos) <= 0.05) { // Snow
                    if (random.nextInt(25) + 1 == 1) {
                        final IBlockState iceFlowerState = RandomPlantStateGenerator.randomIceFlower(worldIn, blockpos, random);
                        if (iceFlowerState != null) {
                            worldIn.setBlockState(blockpos, iceFlowerState, BlockUpdateFlag.UPDATE_NEIGHBORS | BlockUpdateFlag.UPDATE_CLIENTS);
                        }
                    } else {
                        worldIn.setBlockState(blockpos, this.tallGrassState, 2);
                    }
                }

                if (worldIn.getBiome(blockpos).getTemperature(blockpos) >= 0.5 && worldIn.getBiome(blockpos).getTemperature(blockpos) <= 0.95) { // Normal
                    final int chance = random.nextInt(100) + 1 ; // Random between 1 and 11

                    if (chance == 1) {
                        final IBlockState cropState = RandomPlantStateGenerator.randomCrop(worldIn, blockpos, random);
                        if (cropState != null) {
                            worldIn.setBlockState(blockpos, cropState, BlockUpdateFlag.UPDATE_NEIGHBORS | BlockUpdateFlag.UPDATE_CLIENTS);
                        }
                    } else if (chance == 2) {
                        final IBlockState flowerState = RandomPlantStateGenerator.randomFlower(worldIn, blockpos, random);
                        if (flowerState != null) {
                            worldIn.setBlockState(blockpos, flowerState, BlockUpdateFlag.UPDATE_NEIGHBORS | BlockUpdateFlag.UPDATE_CLIENTS);
                        }
                    } else {
                        worldIn.setBlockState(blockpos, this.tallGrassState, 2);
                    }

                }
            }
        }

        return true;
    }
}