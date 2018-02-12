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
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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
        final Random random = new Random();

        int soilMoisture = ((Integer)state.getValue(MOISTURE)).intValue();
        int revertChance = random.nextInt(25);

        if (!this.hasWater(worldIn, pos) && !worldIn.isRainingAt(pos.up()) && !(worldIn.getBiomeForCoordsBody(pos).getRainfall() > 0.5)) { // Add Biome rainfall.
            if (soilMoisture > 0 && revertChance == 1) { // Slow down the revert to account for longer lasting rain.
                worldIn.setBlockState(pos, state.withProperty(MOISTURE, Integer.valueOf(soilMoisture - 1)), 2);
            } else if (!this.hasCrops(worldIn, pos)) {
                // ToDo:  We need a hook into GP for a Soil Flag like we did with Res-Protect.
                //turnToDirt(worldIn, pos);
            }
        } else if (soilMoisture < 7) {
            worldIn.setBlockState(pos, state.withProperty(MOISTURE, Integer.valueOf(7)), 2);
        }
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
