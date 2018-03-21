/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.biome.asm.mixin.core.world.biome;

import com.almuradev.almura.feature.biome.BiomeChunk;
import com.almuradev.almura.feature.biome.BiomeUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeColorHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@SideOnly(Side.CLIENT)
@Mixin(value = BiomeColorHelper.class)
public abstract class MixinBiomeColorHelper {

    private static boolean areWater = false;

    /**
     * @author Zidane - Chris Sanders
     * @reason Just a hack, nothing special..
     */
    @Overwrite
    public static int getWaterColorAtPos(IBlockAccess blockAccess, BlockPos pos)
    {
        areWater = true;
        try {
            return getColorAtPos(blockAccess, pos, BiomeColorHelper.WATER_COLOR);
        } finally {
            areWater = false;
        }

    }

    /**
     * @author Zidane - Chris Sanders
     * @reason Have water respect our config
     */
    @Overwrite
    private static int getColorAtPos(IBlockAccess blockAccess, BlockPos pos, BiomeColorHelper.ColorResolver colorResolver)
    {
        int i = 0;
        int j = 0;
        int k = 0;

        for (BlockPos.MutableBlockPos blockpos$mutableblockpos : BlockPos.getAllInBoxMutable(pos.add(-1, 0, -1), pos.add(1, 0, 1)))
        {
            final Biome biome = blockAccess.getBiome(blockpos$mutableblockpos);

            int l;

            if (areWater) {
                final BiomeChunk biomeChunk = BiomeUtil.getChunk(blockpos$mutableblockpos);
                if (biomeChunk == null) {
                    l = colorResolver.getColorAtPos(biome, blockpos$mutableblockpos);
                } else {
                    l = biomeChunk.getWaterColor(pos, biome);
                }
            } else {
                l = colorResolver.getColorAtPos(biome, blockpos$mutableblockpos);
            }

            i += (l & 16711680) >> 16;
            j += (l & 65280) >> 8;
            k += l & 255;
        }

        return (i / 9 & 255) << 16 | (j / 9 & 255) << 8 | k / 9 & 255;
    }
}
