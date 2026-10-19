/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.biome.asm.mixin.core.minecraftforge.client;

import com.almuradev.almura.feature.biome.BiomeChunk;
import com.almuradev.almura.feature.biome.BiomeColors;
import com.almuradev.almura.feature.biome.BiomeUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
@Mixin(value = ForgeHooksClient.class, remap = false)
public abstract class MixinForgeHooksClient {

    /**
     * Position of the column getSkyBlendColour is looking at. getSkyBlendColour calls getTemperature(pos)
     * and then getSkyColorByTemp(temperature) for every column, and only the first one gets the position.
     */
    @Nullable private static BlockPos almura$skyColumnPos;

    // (remap = true: ForgeHooksClient isn't obfuscated, but Biome's methods are)
    @Redirect(method = "getSkyBlendColour",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/biome/Biome;getTemperature(Lnet/minecraft/util/math/BlockPos;)F", remap = true))
    private static float redirectGetTemperature(Biome biome, BlockPos pos) {
        almura$skyColumnPos = pos;
        final BiomeChunk chunk = BiomeUtil.getChunk(pos);
        if (chunk == null) {
            return biome.getTemperature(pos);
        }

        return chunk.getTemperature(pos, biome);
    }

    @Redirect(method = "getSkyBlendColour",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/biome/Biome;getSkyColorByTemp(F)I", remap = true))
    private static int redirectGetSkyColorByTemp(Biome biome, float temperature) {
        final BlockPos pos = almura$skyColumnPos;
        almura$skyColumnPos = null;
        if (pos != null) {
            // TerrainControl biomes use their SkyColor setting, like TerrainControl's own biomes always did
            final Integer skyColor = BiomeColors.getSkyColor(pos);
            if (skyColor != null) {
                return skyColor;
            }
        }
        return biome.getSkyColorByTemp(temperature);
    }
}
