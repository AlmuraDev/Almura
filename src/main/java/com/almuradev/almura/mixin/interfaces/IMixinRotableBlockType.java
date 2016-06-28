/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.mixin.interfaces;

import net.minecraft.block.material.MapColor;

public interface IMixinRotableBlockType {

    void setMapColor(MapColor mapColor);
}
