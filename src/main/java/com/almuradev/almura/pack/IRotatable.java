package com.almuradev.almura.pack;

import net.minecraft.world.World;

public interface IRotatable extends IPackObject {
    boolean canMirrorRotate(World world, int x, int y, int z, int metadata);
    boolean canRotate(World world, int x, int y, int z, int metadata);
}
