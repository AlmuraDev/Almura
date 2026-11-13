/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.biome.asm.mixin.core.world.biome;

import com.almuradev.almura.feature.biome.BiomeColors;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.biome.BiomeColorHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Gives water the color of the TerrainControl biome at that position.
 *
 * <p>Vanilla's water color only knows the biome (Biome.getWaterColor has no position), so this hooks the
 * position-based blend instead. It averages the 3x3 columns around the block like vanilla does, and
 * leaves the vanilla code alone when none of them is a TerrainControl biome.</p>
 */
@SideOnly(Side.CLIENT)
@Mixin(value = BiomeColorHelper.class)
public abstract class MixinBiomeColorHelper {

    @Inject(method = "getWaterColorAtPos", at = @At("HEAD"), cancellable = true)
    private static void almura$onGetWaterColorAtPos(final IBlockAccess blockAccess, final BlockPos pos, final CallbackInfoReturnable<Integer> cir) {
        int red = 0;
        int green = 0;
        int blue = 0;
        boolean anyTerrainControlBiome = false;

        for (final BlockPos.MutableBlockPos columnPos : BlockPos.getAllInBoxMutable(pos.add(-1, 0, -1), pos.add(1, 0, 1))) {
            Integer color = BiomeColors.getWaterColor(columnPos);
            if (color != null) {
                anyTerrainControlBiome = true;
            } else {
                color = blockAccess.getBiome(columnPos).getWaterColor();
            }

            red += (color & 0xFF0000) >> 16;
            green += (color & 0x00FF00) >> 8;
            blue += color & 0x0000FF;
        }

        if (anyTerrainControlBiome) {
            cir.setReturnValue((red / 9 & 255) << 16 | (green / 9 & 255) << 8 | blue / 9 & 255);
        }
    }
}
