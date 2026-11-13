/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.biome.asm.mixin.core.feature.hud.screen.origin.component.panel.debug;

import com.almuradev.almura.feature.biome.BiomeChunk;
import com.almuradev.almura.feature.biome.BiomeUtil;
import com.almuradev.almura.feature.hud.screen.origin.component.panel.debug.InformationDebugPanel;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@SideOnly(Side.CLIENT)
@Mixin(value = InformationDebugPanel.class, remap = false)
public abstract class MixinInformationDebugPanel {

    /**
     * @author Zidane - Chris Sanders
     * @reason Make Origin Debug HUD biome namespaced id.
     */
    @Overwrite
    private String getBiomeRegistryName(Biome biome, BlockPos pos) {
        final BiomeChunk chunk = BiomeUtil.getChunk(pos);
        if (chunk == null) {
            return biome.getRegistryName().toString();
        }

        return chunk.getBiomeRegistryName(pos, biome);
    }

    /**
     * @author Dockter
     * @reason Make Origin Debug HUD biome name.
     */
    @Overwrite
    private String getBiomeName(Biome biome, BlockPos pos) {
        final BiomeChunk chunk = BiomeUtil.getChunk(pos);
        if (chunk == null) {
            return biome.getBiomeName();
        }

        return chunk.getBiomeName(pos, biome);
    }

    /**
     * @author Zidane - Chris Sanders
     * @reason Make Origin Debug HUD use biome temperature.
     */
    @Overwrite
    private float getTemperature(Biome biome, BlockPos pos) {
        final BiomeChunk chunk = BiomeUtil.getChunk(pos);
        if (chunk == null) {
            return biome.getTemperature(pos);
        }

        return chunk.getTemperature(pos, biome);
    }

    /**
     * @author Zidane - Chris Sanders
     * @reason Make Origin Debug HUD use biome rainfall.
     */
    @Overwrite
    private float getRainfall(Biome biome, BlockPos pos) {
        final BiomeChunk chunk = BiomeUtil.getChunk(pos);
        if (chunk == null) {
            return biome.getRainfall();
        }

        return chunk.getRainfall(pos, biome);
    }
}
