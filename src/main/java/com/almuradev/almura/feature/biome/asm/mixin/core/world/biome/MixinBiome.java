/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.biome.asm.mixin.core.world.biome;

import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Biome.class)
public abstract class MixinBiome {

    /**
     * @author Zidane - Chris Sanders
     * @reason Have Grass color use temperature/rainfall.
     */

    //Todo: this code works as long as you don't use optifine...
    /*
    @Overwrite
    @SideOnly(Side.CLIENT)
    public int getGrassColorAtPos(BlockPos pos) {
        final BiomeChunk chunk = BiomeUtil.getChunk(pos);

        float temperature;
        float rainfall;

        if (chunk == null) {
            temperature = ((Biome) (Object) this).getTemperature(pos);
            rainfall = ((Biome) (Object) this).getRainfall();
        } else {
            temperature = chunk.getTemperature(pos, (Biome) (Object) this);
            rainfall = chunk.getRainfall(pos, (Biome) (Object) this);
        }

        double d0 = (double) MathHelper.clamp(temperature, 0.0F, 1.0F);
        double d1 = (double) MathHelper.clamp(rainfall, 0.0F, 1.0F);
        return ((Biome) (Object) this).getModdedBiomeGrassColor(ColorizerGrass.getGrassColor(d0, d1));
    } */

    /**
     * @author Zidane - Chris Sanders
     * @reason Have Foilage color use temperature/rainfall.
     */

    //Todo: this code works as long as you don't use optifine...

    /*
    @Overwrite
    @SideOnly(Side.CLIENT)
    public int getFoliageColorAtPos(BlockPos pos) {
        final BiomeChunk chunk = BiomeUtil.getChunk(pos);

        float temperature;
        float rainfall;

        if (chunk == null) {
            temperature = ((Biome) (Object) this).getTemperature(pos);
            rainfall = ((Biome) (Object) this).getRainfall();
        } else {
            temperature = chunk.getTemperature(pos, (Biome) (Object) this);
            rainfall = chunk.getRainfall(pos, (Biome) (Object) this);
        }

        double d0 = (double) MathHelper.clamp(temperature, 0.0F, 1.0F);
        double d1 = (double) MathHelper.clamp(rainfall, 0.0F, 1.0F);
        return ((Biome) (Object) this).getModdedBiomeFoliageColor(ColorizerFoliage.getFoliageColor(d0, d1));
    } */
}
