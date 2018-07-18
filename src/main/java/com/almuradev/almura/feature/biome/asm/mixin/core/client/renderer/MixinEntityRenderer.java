/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.biome.asm.mixin.core.client.renderer;

import com.almuradev.almura.feature.biome.BiomeChunk;
import com.almuradev.almura.feature.biome.BiomeUtil;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@SideOnly(Side.CLIENT)
@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer {

    @Redirect(method = "renderRainSnow", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/biome/Biome;getTemperature"
            + "(Lnet/minecraft/util/math/BlockPos;)F"))
    private float redirectGetFloatTemperature(Biome biome, BlockPos pos) {
        final BiomeChunk chunk = BiomeUtil.getChunk(pos);
        if (chunk == null) {
            return biome.getTemperature(pos);
        }

        return chunk.getTemperature(pos, biome);
    }

    @Redirect(method = "addRainParticles", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/biome/Biome;getTemperature"
            + "(Lnet/minecraft/util/math/BlockPos;)F"))
    private float redirectGetTemperature(Biome biome, BlockPos pos) {
        final BiomeChunk chunk = BiomeUtil.getChunk(pos);
        if (chunk == null) {
            return biome.getTemperature(pos);
        }

        return chunk.getTemperature(pos, biome);
    }
}
