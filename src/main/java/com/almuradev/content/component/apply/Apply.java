/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.component.apply;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import java.util.Random;

/**
 * Something that be can be applied.
 */
@FunctionalInterface
public interface Apply<E extends Entity> {

    void apply(final E player, final Block block, final BlockPos pos, final Random random, final ItemStack stack);
}
