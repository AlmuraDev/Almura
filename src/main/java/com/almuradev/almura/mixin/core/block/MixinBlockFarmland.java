/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.mixin.core.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Random;

@Mixin(BlockFarmland.class)
public abstract class MixinBlockFarmland extends Block {

    @Shadow @Final public static PropertyInteger MOISTURE;

    public MixinBlockFarmland(Material materialIn) {
        super(materialIn);
    }

    @Overwrite
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        int soilMoisture = ((Integer)state.getValue(MOISTURE)).intValue();

        if (!this.hasWater(worldIn, pos) && !worldIn.isRainingAt(pos.up()) && !(worldIn.getBiomeForCoordsBody(pos).getRainfall() > 0.5)) { // Add Biome rainfall.
            if (soilMoisture > 0) {
                worldIn.setBlockState(pos, state.withProperty(MOISTURE, Integer.valueOf(soilMoisture - 1)), 2);
            } else if (!this.hasCrops(worldIn, pos)) {
                this.turnToDirt(worldIn, pos);
            }
        } else if (soilMoisture < 7) {
            worldIn.setBlockState(pos, state.withProperty(MOISTURE, Integer.valueOf(7)), 2);
        }
    }

    @Overwrite
    private boolean hasCrops(World worldIn, BlockPos pos) {
        Block block = worldIn.getBlockState(pos.up()).getBlock();
        return block instanceof net.minecraftforge.common.IPlantable && canSustainPlant(worldIn.getBlockState(pos), worldIn, pos, net.minecraft.util.EnumFacing.UP, (net.minecraftforge.common.IPlantable)block);
    }

    @Overwrite
    private boolean hasWater(World worldIn, BlockPos pos) {
        for (BlockPos.MutableBlockPos blockpos$mutableblockpos : BlockPos.getAllInBoxMutable(pos.add(-4, -1, -4), pos.add(4, 1, 4))) {  //-1 was 0.  Look under dirt block for water source.
            if (worldIn.getBlockState(blockpos$mutableblockpos).getMaterial() == Material.WATER) {
                return true;
            }
        }

        return false;
    }

    @Overwrite
    private void turnToDirt(World p_190970_1_, BlockPos p_190970_2_) {
        IBlockState iblockstate = Blocks.DIRT.getDefaultState();
        p_190970_1_.setBlockState(p_190970_2_, iblockstate);
        AxisAlignedBB axisalignedbb = iblockstate.getCollisionBoundingBox(p_190970_1_, p_190970_2_).offset(p_190970_2_);

        for (Entity entity : p_190970_1_.getEntitiesWithinAABBExcludingEntity((Entity)null, axisalignedbb)) {
            entity.setPosition(entity.posX, axisalignedbb.maxY, entity.posZ);
        }
    }
}
