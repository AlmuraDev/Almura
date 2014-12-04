/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack;

import net.minecraft.world.World;

public interface IRotatable extends IPackObject {
    boolean canMirrorRotate(World world, int x, int y, int z, int metadata);
    boolean canRotate(World world, int x, int y, int z, int metadata);
}
