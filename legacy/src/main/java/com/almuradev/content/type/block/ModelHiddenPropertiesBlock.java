/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block;

import net.minecraft.block.properties.IProperty;

import java.util.Collection;

/**
 * A block with properties that never change its look, left out of its model variant names (so its blockstate file needn't list them).
 * Content blocks with special blockstates get their variant names from {@link BlockStateMapper}, which honours this; a custom state mapper
 * on them would be bypassed.
 */
public interface ModelHiddenPropertiesBlock {
    Collection<IProperty<?>> modelHiddenProperties();
}
