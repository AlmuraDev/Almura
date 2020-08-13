/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.asm.mixin.core.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import java.util.Random;

@Mixin(value = BlockFarmland.class, priority = 999)
public abstract class MixinBlockFarmland extends Block {

    @Final @Shadow public static PropertyInteger MOISTURE;
    @Shadow private boolean hasCrops(final World world, final BlockPos pos) { return false; }
    @Shadow private boolean hasWater(final World world, final BlockPos pos) { return false; }
    @Shadow protected static void turnToDirt(final World world, final BlockPos pos) { }

    public MixinBlockFarmland(final Material material) {
        super(material);
    }

    /**
     * @author Dockter - Mike Howe
     * @reason Prevent farmland "decay" into dirt based on property
     */
    @Override
    @Overwrite
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        //if (!worldIn.isRemote) {
            final Random random = new Random();
            int soilMoisture = ((Integer) state.getValue(MOISTURE)).intValue();
            int revertChance = random.nextInt(25);

            if (!this.hasWater(worldIn, pos) && !worldIn.isRainingAt(pos.up()) && !(worldIn.getBiomeForCoordsBody(pos).getRainfall() > 0.5)) { // Add Biome rainfall.
                if (soilMoisture > 0 && revertChance == 1) { // Slow down the revert to account for longer lasting rain.
                    worldIn.setBlockState(pos, state.withProperty(MOISTURE, Integer.valueOf(soilMoisture - 1)), 2);
                } else if (!this.hasCrops(worldIn, pos)) {
                    // ToDo:  We need a hook into GP for a Soil Flag like we did with Res-Protect.
                    //turnToDirt(worldIn, pos);
                }
            } else if (soilMoisture < 7) {
                // Set original ticked location to high moisture.
                worldIn.setBlockState(pos, state.withProperty(MOISTURE, Integer.valueOf(7)), 2);
            }
            // Scan additional blocks around the original for possible updates if it is raining.
            if (worldIn.isRainingAt(pos.up())) {
                int spreadChance;
                for (BlockPos.MutableBlockPos blockpos$mutableblockpos : BlockPos.getAllInBoxMutable(pos.add(-1, 0, -1), pos.add(1, 0, 1))) {
                    if (((WorldServer) worldIn).getChunkProvider().chunkExists(pos.getX() >> 4, pos.getZ() >> 4)) {
                        if (worldIn.getBlockState(blockpos$mutableblockpos).getBlock() == Blocks.FARMLAND && (Integer) (worldIn.getBlockState(blockpos$mutableblockpos).getValue(MOISTURE)).intValue() < 7) {
                            spreadChance = random.nextInt(2);
                            if (spreadChance == 1 || blockpos$mutableblockpos == pos) {
                                worldIn.setBlockState(blockpos$mutableblockpos, worldIn.getBlockState(blockpos$mutableblockpos).withProperty(MOISTURE, Integer.valueOf(7)), 2);
                            }
                        }
                    }
                }
            }
            // Scan for additional custom water sources
            if (soilMoisture < 1) {
                Block pipe = null;
                Block spinkler = null;
                pipe = Block.getBlockFromName("almura:horizontal/agriculture/pipe");
                spinkler = Block.getBlockFromName("almura:horizontal/agriculture/pipe");

                if (pipe != null && spinkler != null) {
                    for (BlockPos.MutableBlockPos blockpos$mutableblockpos : BlockPos.getAllInBoxMutable(pos.add(-6, 1, -6), pos.add(6, 1, 6))) {
                        if (worldIn.isBlockLoaded(blockpos$mutableblockpos)) {
                            Block posBlock = worldIn.getBlockState(blockpos$mutableblockpos).getBlock();
                            if (posBlock == pipe || posBlock == spinkler) {
                                worldIn.setBlockState(pos, state.withProperty(MOISTURE, Integer.valueOf(7)), 2);
                                return;
                            }
                        }
                    }
                }
            }
       // }
    }

    @ModifyConstant(
            method = "hasWater(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Z",
            constant = @Constant(intValue = 0, ordinal = 0)
    )
    private int checkUnderBlockForWater(final int zero) {
        // Look under dirt block for water source.
        return -1;
    }
}
