/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack;

import com.almuradev.almura.pack.model.PackShape;
import net.minecraft.world.IBlockAccess;

public interface IBlockShapeContainer extends IShapeContainer {

    PackShape getShape(IBlockAccess access, int x, int y, int z, int metadata);
}
