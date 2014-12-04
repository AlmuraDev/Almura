/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.core.mixin.impl;

import com.almuradev.almura.core.mixin.Mixin;
import com.almuradev.almura.core.mixin.Overwrite;
import net.minecraft.block.BlockWall;
import net.minecraft.world.IBlockAccess;

@Mixin(BlockWall.class)
public abstract class MixinBlockWall {

    /**
     * Return whether an adjacent block can connect to a wall.
     */
    @Overwrite
    public boolean canConnectWallTo(IBlockAccess access, int x, int y, int z) {
        return access.getBlock(x, y, z) instanceof BlockWall;
    }
}
