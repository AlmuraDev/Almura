/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack;

import net.malisis.core.renderer.icon.ClippedIcon;
import net.minecraft.world.World;

public interface IClipContainer extends IPackObject {

    ClippedIcon[] getClipIcons(World world, int x, int y, int z, int metadata);
}
