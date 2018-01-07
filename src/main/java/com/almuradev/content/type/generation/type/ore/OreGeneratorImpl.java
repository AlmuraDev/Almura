/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.generation.type.ore;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraftforge.fml.common.IWorldGenerator;
import org.slf4j.Logger;

import java.util.List;
import java.util.Random;

import javax.inject.Inject;

public final class OreGeneratorImpl implements IWorldGenerator, OreGenerator {
    @Inject private static Logger logger;
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
                logger.debug("Dimension '{}' does not match definition requirement of '{}'", world.provider.getDimensionType().getName(), definition.dimension());
                continue;
            }

            final WorldGenMinable feature = new WorldGenMinable((IBlockState) definition.block().get(), definition.size());
            for (int i = 0, length = definition.count(); i < length; i++) {
                final int x =  cx * 16 + random.nextInt(16);
                final int y = random.nextInt(64);
                final int z = cz * 16 + random.nextInt(16);
                feature.generate(world, random, new BlockPos(x, y, z));
            }
        }
    }
}
