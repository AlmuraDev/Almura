/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.generation.type.ore;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraftforge.fml.common.IWorldGenerator;

import java.util.List;
import java.util.Random;

public final class OreGeneratorImpl implements IWorldGenerator, OreGenerator {
    private final int weight;
    private final List<OreDefinition> definitions;

    OreGeneratorImpl(final int weight, final List<OreDefinition> definitions) {
        this.weight = weight;
        this.definitions = definitions;
    }

    @Override
    public int weight() {
        return this.weight;
    }

    @Override
    public void generate(final Random random, final int cx, final int cz, final World world, final IChunkGenerator generator, final IChunkProvider source) {
        if (world.isRemote) {
            return;
        }

        for (final OreDefinition definition : this.definitions) {
            if (!definition.accepts(world.provider)) {
                continue;
            }

            final WorldGenMinable feature = new WorldGenMinable(((Block) definition.block().get()).getDefaultState(), definition.size());
            for (int i = 0, length = definition.count(); i < length; i++) {
                final int x =  cx * 16 + random.nextInt(16);
                final int y = random.nextInt(64);
                final int z = cz * 16 + random.nextInt(16);
                feature.generate(world, random, new BlockPos(x, y, z));
            }
        }
    }
}
