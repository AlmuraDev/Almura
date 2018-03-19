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
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.spongepowered.api.GameRegistry;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Collections;
import java.util.Random;
import java.util.stream.Collectors;

import javax.inject.Inject;

@Mixin(BlockTallGrass.class)
public abstract class MixinBlockTallGrass extends MixinBlock {
    @Inject private static GameRegistry registry;

    /**
     * @author Zidane - Chris Sanders
     * @reason Add in content seeds to drop list for Tall Grass
     */
    @Overwrite(remap = false)
    public void getDrops(final NonNullList<ItemStack> drops, final IBlockAccess access, final BlockPos pos, final IBlockState state, final int fortune) {

        final World world;

        if (access instanceof ChunkCache) {
            world = ((ChunkCache) access).world;
        } else if (access instanceof World) {
            world = (World) access;
        } else {
            return;
        }

        final Random random = world.rand;

        // Roll 1 is Vanilla's 1/8 chance to drop a seed
        final int roll1 = random.nextInt(8);

        if (roll1 == 0) {
            // Forge Start - Lookup seed each time and then do random check. Almura handles its own chance code
            final ItemStack modSeed = net.minecraftforge.common.ForgeHooks.getGrassSeed(random, fortune);
            if (!modSeed.isEmpty()) {
                drops.add(modSeed);

                // Forge End
                
                // Almura Start
                // Don't double up with Vanilla/mod drops
                return;
            }

            final Biome biome = world.getBiome(pos);

            // Roll 2 is shuffling Almura seeds and picking the first one after shuffling
            registry.getAllOf(ItemType.class)
                    .stream()
                    .filter(itemType -> itemType instanceof SeedItem && ((SeedItem) itemType).getGrass() != null)
                    .collect(Collectors.collectingAndThen(Collectors.toList(), collected -> {
                        Collections.shuffle(collected);
                        return collected.stream();
                    }))
                    .findFirst()
                    .ifPresent((itemType) -> {
                        final SeedItem seed = (SeedItem) itemType;
                        final IntRange amountRange = seed.getGrass().getOrLoadAmountRequiredRangeForBiome(biome);

                        if (amountRange != null) {
                            final int stackSize = amountRange.random(random);

                            final DoubleRange chanceRange = seed.getGrass().getOrLoadChanceRangeForBiome(biome);

                            if (chanceRange != null) {
                                final double chance = chanceRange.random(random);

                                // Roll 3 is allowing the seed configuration to determine the chance for the drop
                                if (random.nextDouble() <= (chance / 100)) {
                                    drops.add((ItemStack) (Object) org.spongepowered.api.item.inventory.ItemStack.of(itemType, stackSize));
                                }
                            } else {
                                drops.add((ItemStack) (Object) org.spongepowered.api.item.inventory.ItemStack.of(itemType, stackSize));
                            }
                        }
                    });
        }
        // Almura End
    }
}
