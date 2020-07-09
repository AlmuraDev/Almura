/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.asm.mixin.accessors.client.block;

import net.minecraft.block.BlockLeaves;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockLeaves.class)
public interface BlockLeavesAccessor {
    //public net.minecraft.block.BlockLeaves field_185686_c # leavesFancy
    @Accessor("leavesFancy") boolean accessor$getIsLeavesFancy();
    @Accessor("leavesFancy") void accessor$setIsLeavesFancy(boolean value);
}
