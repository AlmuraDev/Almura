/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.asm.mixin.accessors.item;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemBlock.class)
public interface ItemBlockAccessor {
    // public-f net.minecraft.item.ItemBlock field_150939_a # block
    @Accessor("block") Block accessor$getBlock();
    @Final @Accessor("block") void accessor$setBlock(Block block);
}
