/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.asm.mixin.accessors.block;

import net.minecraft.block.BlockLeaves;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BlockLeaves.class)
public interface BlockLeavesAccessor {
    //public net.minecraft.block.BlockLeaves field_185686_c # leavesFancy
    @Accessor("leavesFancy") boolean accessor$getIsLeavesFancy();
    @Accessor("leavesFancy") void accessor$setIsLeavesFancy(boolean value);

    @Accessor("surroundings") int[] accessor$getSurroundings();
    @Accessor("surroundings") void accessor$setSurroundings(int[] surroundings);

    @Invoker("destroy") void invoker$destroy(World world, BlockPos pos);
}
