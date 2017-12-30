/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item.type.seed.mixin.impl;

import com.almuradev.content.type.block.mixin.impl.MixinBlock;
import com.almuradev.content.type.item.type.seed.SeedItem;
import com.almuradev.toolbox.util.math.DoubleRange;
import com.almuradev.toolbox.util.math.IntRange;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.spongepowered.api.GameRegistry;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Collections;
import java.util.Iterator;
import java.util.Random;
import java.util.stream.Collectors;

@Mixin(BlockTallGrass.class)
public abstract class MixinBlockTallGrass extends MixinBlock {

    @javax.inject.Inject private static GameRegistry registry;

    /**
     * @author Zidane - Chris Sanders
     * @reason Add in content seeds to drop list for Tall Grass
     */
    @Overwrite(remap = false)
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {

        final Random random = ((World) world).rand;

        // Forge Start - Lookup seed each time and then do random check. Almura handles its own chance code
        ItemStack modSeed = net.minecraftforge.common.ForgeHooks.getGrassSeed(random, fortune);
        if (!modSeed.isEmpty() && random.nextInt(8) == 0) {
            drops.add(modSeed);
        }
        // Forge End

        // Almura Start
        // Don't double up with Vanilla/mod drops
        if (!drops.isEmpty()) {
            return;
        }

        final Biome biome = ((World) world).getBiome(pos);

        final Iterator<ItemType> iter = registry.getAllOf(ItemType.class)
                .stream()
                .filter(itemType -> itemType instanceof SeedItem && ((SeedItem) itemType).getGrass() != null)
                .collect(Collectors.collectingAndThen(Collectors.toList(), collected -> {
                    Collections.shuffle(collected);
                    return collected.stream();
                }))
                .iterator();

        while (iter.hasNext()) {
            final ItemType itemType = iter.next();
            final SeedItem seed = (SeedItem) itemType;
            final IntRange amountRange = seed.getGrass().getOrLoadAmountRequiredRangeForBiome(biome);

            if (amountRange != null) {
                final int stackSize = amountRange.random(random);

                final DoubleRange chanceRange = seed.getGrass().getOrLoadChanceRangeForBiome(biome);

                if (chanceRange != null) {
                    final double chance = chanceRange.random(random);

                    if (random.nextDouble() <= (chance / 100)) {
                        drops.add((ItemStack) (Object) org.spongepowered.api.item.inventory.ItemStack.of(itemType, stackSize));
                        return;
                    }
                } else {
                    drops.add((ItemStack) (Object) org.spongepowered.api.item.inventory.ItemStack.of(itemType, stackSize));
                    return;
                }
            }
        }
        // Almura End
    }
}
