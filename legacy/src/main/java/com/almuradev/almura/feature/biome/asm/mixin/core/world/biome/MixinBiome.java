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
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Gives grass and leaves the colors of the TerrainControl biome at that position.
 *
 * <p>Injects instead of overwriting (the old overwrites were disabled because they didn't work with
 * OptiFine), so other mods that hook these methods still run. BiomeSwamp and BiomeMesa have their own
 * versions of these methods, see {@link MixinBiomeSwamp} and {@link MixinBiomeMesa}.</p>
 */
@SideOnly(Side.CLIENT)
@Mixin(Biome.class)
public abstract class MixinBiome {

    @Inject(method = "getGrassColorAtPos", at = @At("HEAD"), cancellable = true)
    private void almura$onGetGrassColorAtPos(final BlockPos pos, final CallbackInfoReturnable<Integer> cir) {
        final Integer color = BiomeColors.getGrassColor((Biome) (Object) this, pos);
        if (color != null) {
            cir.setReturnValue(color);
        }
    }

    @Inject(method = "getFoliageColorAtPos", at = @At("HEAD"), cancellable = true)
    private void almura$onGetFoliageColorAtPos(final BlockPos pos, final CallbackInfoReturnable<Integer> cir) {
        final Integer color = BiomeColors.getFoliageColor((Biome) (Object) this, pos);
        if (color != null) {
            cir.setReturnValue(color);
        }
    }
}
