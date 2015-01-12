/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack;

import com.almuradev.almura.pack.model.PackModelContainer;
import com.google.common.base.Optional;
import net.minecraft.world.IBlockAccess;

public interface IBlockModelContainer extends IModelContainer {

    Optional<PackModelContainer> getModelContainer(IBlockAccess access, int x, int y, int z, int metadata);
}
