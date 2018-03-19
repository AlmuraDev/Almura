/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.generation.type.feature.tree;

import com.almuradev.content.component.delegate.Delegate;
import com.almuradev.content.type.block.state.LazyBlockState;
import com.almuradev.content.type.tree.AbstractTreeFeature;
import com.almuradev.content.type.tree.Tree;
import com.almuradev.content.util.DoubleRangeFunctionPredicatePair;
import com.almuradev.core.event.Witness;
import com.almuradev.toolbox.util.math.DoubleRange;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

@Witness.Scope(Witness.Scope.Type.TERRAIN_GEN)
public class TreeGeneratorImpl implements TreeGenerator, Witness {
    private final List<String> worlds;
    private final List<DoubleRangeFunctionPredicatePair<Biome>> biomes;
    private final List<LazyBlockState> requires;
    private final Delegate<Tree> tree;
    @Nullable private final Delegate<Tree> bigTree;
    @Nullable private final List<DoubleRangeFunctionPredicatePair<Biome>> bigTreeChances;

    TreeGeneratorImpl(final List<String> worlds, final List<DoubleRangeFunctionPredicatePair<Biome>> biomes, final List<LazyBlockState> requires, final Delegate<Tree> tree, @Nullable final Delegate<Tree> bigTree, @Nullable final List<DoubleRangeFunctionPredicatePair<Biome>> bigTreeChances) {
        this.worlds = worlds;
        this.biomes = biomes;
        this.requires = requires;
        this.tree = tree;
        this.bigTree = bigTree;
        this.bigTreeChances = bigTreeChances;
    }

    @SubscribeEvent
    public void decorate(final DecorateBiomeEvent.Decorate event) {
        if (event.getType() != DecorateBiomeEvent.Decorate.EventType.TREE || event.getResult() == Event.Result.DENY) {
            return;
        }

        final World world = event.getWorld();
        if (!this.in(world)) {
            return;
        }

        final DoubleRange chance = DoubleRangeFunctionPredicatePair.range(this.biomes, world.getBiome(event.getPos()));
        if (chance == null || chance.max() == 0) {
            return;
        }

        final Random random = event.getRand();
        if (random.nextDouble() <= (chance.random(random) / 100d)) {
            final int x = random.nextInt(16) + 8;
            final int z = random.nextInt(16) + 8;
            final BlockPos pos = world.getHeight(event.getPos().add(x, 0, z));
            this.tree(world.getBiome(pos), random).generate(world, random, pos, this.requires);
            event.setResult(Event.Result.DENY);
        }
    }

    private AbstractTreeFeature tree(final Biome biome, final Random random) {
        if(this.bigTree != null && this.bigTreeChances != null && random.nextDouble() <= (DoubleRangeFunctionPredicatePair.rangeOrRandom(this.bigTreeChances, biome).random(random) / 100d)) {
            return (AbstractTreeFeature) this.bigTree.require();
        }
        return (AbstractTreeFeature) this.tree.require();
    }

    private boolean in(final World world) {
        if (this.worlds.isEmpty()) {
            return true;
        }
        for (final String name : this.worlds) {
            if (((org.spongepowered.api.world.World) world).getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }
}
