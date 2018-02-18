/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.generator;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

@SuppressWarnings("deprecation")
public class RandomSaplingStateGenerator {
    static boolean debug = false;

    public static IBlockState randomSapling(World worldIn, BlockPos blockPos, Random random) {
        String saplingName = "";
        int saplingAge = 0;
        int sapling = random.nextInt(10); // Total Crops

        if (sapling == 0) {
            saplingName = "almura:sapling/cherry_sapling";
        } else if (sapling == 1) {
            saplingName = "almura:sapling/grape_fruit_sapling";
        } else if (sapling == 2) {
            saplingName = "almura:sapling/apple_sapling";
        } else if (sapling == 3) {
            saplingName = "almura:sapling/kiwi_sapling";
        } else if (sapling == 4) {
            saplingName = "almura:sapling/lemon_sapling";
        } else if (sapling == 5) {
            saplingName = "almura:sapling/lime_sapling";
        } else if (sapling == 6) {
            saplingName = "almura:sapling/orange_sapling";
        } else if (sapling == 7) {
            saplingName = "almura:sapling/peach_sapling";
        } else if (sapling == 8) {
            saplingName = "almura:sapling/plum_sapling";
        } else if (sapling == 9) {
            saplingName = "almura:sapling/walnut_sapling";
        } else if (sapling == 10) {
            saplingName = "almura:sapling/green_apple_sapling";
        }
        
        if (debug) {
            System.out.println("RandomSaplingStateGenerator - Sapling: " + saplingName + " at: " + blockPos);
        }

        final Block block = Block.getBlockFromName(saplingName);
        if (block != null) {
            final IBlockState state = block.getStateFromMeta(saplingAge);
            return state;
        }
        return null;
    }
}
