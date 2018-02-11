package com.almuradev.almura.asm.mixin.core.world.gen.feature;

import com.almuradev.almura.feature.generator.RandomPlantStateGenerator;
import com.almuradev.almura.feature.generator.RandomSaplingStateGenerator;
import com.almuradev.content.type.block.BlockUpdateFlag;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenFlowers;
import net.minecraft.world.gen.feature.WorldGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Random;

@Mixin(value = WorldGenFlowers.class, priority = 999)
public class MixinWorldGenFlowers extends WorldGenerator
{
    @Shadow private BlockFlower flower;
    @Shadow private IBlockState state;

    //ToDO: 2/11/2018 -> This mixin si not active, waiting on Content update from Kashike to be able to use this.

    /**
     * @author Dockter - Mike Howe
     * @reason Possibility of using this as a make shift saplling generator.
     */

    @Override
    @Overwrite
    public boolean generate(World worldIn, Random rand, BlockPos position) {
        final Random random = new Random();
        for (int i = 0; i < 64; ++i) {
            BlockPos blockpos = position.add(rand.nextInt(8) - rand.nextInt(8), rand.nextInt(4) - rand.nextInt(4), rand.nextInt(8) - rand.nextInt(8));

            if (worldIn.isAirBlock(blockpos) && (!worldIn.provider.isNether() || blockpos.getY() < 255) && this.flower.canBlockStay(worldIn, blockpos, this.state)) {
                if (random.nextInt(25) + 1 == 1) {
                    final IBlockState saplingState = RandomSaplingStateGenerator.randomSapling(worldIn, blockpos, random);
                    if (saplingState != null) {
                        worldIn.setBlockState(blockpos, saplingState, BlockUpdateFlag.UPDATE_NEIGHBORS | BlockUpdateFlag.UPDATE_CLIENTS);
                    }
                } else {
                    worldIn.setBlockState(blockpos, this.state, 2);
                }
            }
        }

        return true;
    }
}