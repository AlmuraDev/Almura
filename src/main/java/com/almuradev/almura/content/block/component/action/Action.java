/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.block.component.action;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import java.util.Random;

@FunctionalInterface
public interface Action {

    void apply(final EntityPlayer player, final Block block, final BlockPos pos, final Random random, final ItemStack stack);
}
