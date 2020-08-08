/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.biome.asm.mixin.core.world.biome;

import net.minecraft.world.biome.BiomeColorHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;

@SideOnly(Side.CLIENT)
@Mixin(value = BiomeColorHelper.class)
public abstract class MixinBiomeColorHelper {

    /**
     * @author Zidane - Chris Sanders
     * @reason Have water respect our config
     */

    //Todo: this code works as long as you don't use optifine...
    /*
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

            if (colorResolver == BiomeColorHelper.WATER_COLOR) {
                final BiomeChunk biomeChunk = BiomeUtil.getChunk(blockpos$mutableblockpos);
                if (biomeChunk == null) {
                    l = colorResolver.getColorAtPos(biome, blockpos$mutableblockpos);
                } else {
                    l = biomeChunk.getWaterColor(pos, biome);
                }
            } else {
                l = colorResolver.getColorAtPos(biome, blockpos$mutableblockpos);
            }

            if (blockAccess.getBlockState(blockpos$mutableblockpos).getMaterial() == Material.WATER) {
                final BiomeChunk biomeChunk = BiomeUtil.getChunk(blockpos$mutableblockpos);
                if (biomeChunk == null) {
                    l = colorResolver.getColorAtPos(biome, blockpos$mutableblockpos);
                } else {
                    l = biomeChunk.getWaterColor(pos, biome);
                }
            }

            i += (l & 16711680) >> 16;
            j += (l & 65280) >> 8;
            k += l & 255;
        }

        return (i / 9 & 255) << 16 | (j / 9 & 255) << 8 | k / 9 & 255;
    } */
}
