/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2015 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.generator;

import com.almuradev.almura.Almura;
import cpw.mods.fml.common.IWorldGenerator;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.feature.WorldGenMinable;

import java.util.Random;

public class PackGenerator implements IWorldGenerator {
    private final Block replacement;

    public PackGenerator() {
        this.replacement = GameRegistry.findBlock(Almura.MOD_ID, "Ores\\marbleOre");
    }

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
        if (!world.isRemote && world.provider instanceof WorldProviderSurface) {
            // Replacement | Meta | Ore Vein Size | Target
            final WorldGenMinable genMinable = new WorldGenMinable(this.replacement, 0, 5, Blocks.stone);
            for (int numVeins = 0; numVeins < 3; numVeins++) {
                final int blockX =  (chunkX * 16) + random.nextInt(16);
                final int blockY = random.nextInt(64);
                final int blockZ = (chunkZ * 16) + random.nextInt(16);

                //System.out.println("Calling Generate of Marble Ore. X [" + blockX + "], Y [" + blockY + "], Z [" + blockZ + "].");
                genMinable.generate(world, random, blockX, blockY, blockZ);
            }
        }
    }
}
