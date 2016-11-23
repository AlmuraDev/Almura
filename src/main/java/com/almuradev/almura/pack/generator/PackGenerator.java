/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2016 AlmuraDev <http://github.com/AlmuraDev/>
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
    private final Block marbleOre;
    private final Block saltOre;

    public PackGenerator() {
        this.marbleOre = GameRegistry.findBlock(Almura.MOD_ID, "Ores\\marbleOre");
        this.saltOre = GameRegistry.findBlock(Almura.MOD_ID, "Ores\\saltOre");
    }

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
        if (!world.isRemote && world.provider instanceof WorldProviderSurface) {
            // Replacement | Meta | Ore Vein Size | Target
            final WorldGenMinable genMarble = new WorldGenMinable(this.marbleOre, 0, 5, Blocks.stone);
            for (int numVeins = 0; numVeins < 3; numVeins++) {
                final int blockX =  (chunkX * 16) + random.nextInt(16);
                final int blockY = random.nextInt(64);
                final int blockZ = (chunkZ * 16) + random.nextInt(16);

                //System.out.println("Calling Generate of Marble Ore. X [" + blockX + "], Y [" + blockY + "], Z [" + blockZ + "].");
                genMarble.generate(world, random, blockX, blockY, blockZ);
            }
            
            final WorldGenMinable genSalt = new WorldGenMinable(this.saltOre, 0, 10, Blocks.stone);
            for (int numVeins = 0; numVeins < 3; numVeins++) {
                final int blockX =  (chunkX * 16) + random.nextInt(16);
                final int blockY = random.nextInt(64);
                final int blockZ = (chunkZ * 16) + random.nextInt(16);

                //System.out.println("Calling Generate of Marble Ore. X [" + blockX + "], Y [" + blockY + "], Z [" + blockZ + "].");
                genSalt.generate(world, random, blockX, blockY, blockZ);
            }
        }
    }
}
