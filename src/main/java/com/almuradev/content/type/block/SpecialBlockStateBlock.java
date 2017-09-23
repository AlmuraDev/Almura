/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block;

import net.minecraft.util.ResourceLocation;

public interface SpecialBlockStateBlock {

    ResourceLocation blockStateDefinitionLocation();

    void blockStateDefinitionLocation(final ResourceLocation location);
}
