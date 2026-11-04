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
import com.almuradev.content.util.DoubleRanges;
import com.almuradev.core.event.Witness;
import com.almuradev.toolbox.util.math.DoubleRange;
import net.minecraft.block.material.Material;
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
    /**
     * Where a tree may grow unless its config says otherwise: anywhere it doesn't snow. Vanilla snows below 0.15, and
     * {@link Biome#getTemperature(BlockPos)} already accounts for height, so cold mountain tops are excluded too.
     */
    static final DoubleRange DEFAULT_TEMPERATURE = DoubleRange.range(0.15d, Double.MAX_VALUE);

    private final List<String> worlds;
    private final List<DoubleRangeFunctionPredicatePair<Biome>> biomes;
    private final List<LazyBlockState> requires;
    private final DoubleRange temperature;
    private final Delegate<Tree> tree;
    @Nullable private final Delegate<Tree> bigTree;
    @Nullable private final List<DoubleRangeFunctionPredicatePair<Biome>> bigTreeChances;

    TreeGeneratorImpl(final List<String> worlds, final List<DoubleRangeFunctionPredicatePair<Biome>> biomes, final List<LazyBlockState> requires, final DoubleRange temperature, final Delegate<Tree> tree, @Nullable final Delegate<Tree> bigTree, @Nullable final List<DoubleRangeFunctionPredicatePair<Biome>> bigTreeChances) {
        this.worlds = worlds;
        this.biomes = biomes;
        this.requires = requires;
        this.temperature = temperature;
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

        // Judge the spot the tree would actually grow on, not the chunk corner: in a TerrainControl world every biome
        // is in the "terraincontrol" namespace, so the biome list alone lets fruit trees onto glaciers and peaks.
        final Random random = event.getRand();
        final int x = random.nextInt(16) + 8;
        final int z = random.nextInt(16) + 8;
        final BlockPos pos = world.getHeight(event.getPos().add(x, 0, z));
        final Biome biome = world.getBiome(pos);

        final DoubleRange chance = DoubleRangeFunctionPredicatePair.range(this.biomes, biome);
        if (chance == null || chance.max() == 0 || !this.climateAllows(world, biome, pos)) {
            return;
        }

        if (random.nextDouble() <= (DoubleRanges.random(chance, random) / 100d)) {
            this.tree(biome, random).generate(world, random, pos, this.requires);
            event.setResult(Event.Result.DENY);
        }
    }

    private boolean climateAllows(final World world, final Biome biome, final BlockPos pos) {
        if (!this.temperature.contains(biome.getTemperature(pos))) {
            return false;
        }
        // Never on snow or ice, whatever the biome claims
        final Material ground = world.getBlockState(pos.down()).getMaterial();
        final Material here = world.getBlockState(pos).getMaterial();
        return ground != Material.ICE && ground != Material.PACKED_ICE && ground != Material.CRAFTED_SNOW && ground != Material.SNOW
                && here != Material.SNOW;
    }

    private AbstractTreeFeature tree(final Biome biome, final Random random) {
        if(this.bigTree != null && this.bigTreeChances != null && random.nextDouble() <= (DoubleRanges.random(DoubleRangeFunctionPredicatePair.rangeOrRandom(this.bigTreeChances, biome), random) / 100d)) {
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
