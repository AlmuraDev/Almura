/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack;

import net.malisis.core.renderer.icon.ClippedIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public interface IBlockClipContainer extends IClipContainer {

    ClippedIcon[] getClipIcons(IBlockAccess access, int x, int y, int z, int metadata);
}
