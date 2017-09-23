/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.content.type.block.type.slab;

import com.almuradev.almura.content.type.block.type.BuildableBlockType;
import org.spongepowered.api.Sponge;

public interface SlabType extends BuildableBlockType {

    static SlabType.Builder builder() {
        return Sponge.getRegistry().createBuilder(SlabType.Builder.class);
    }

    interface Builder extends BuildableBlockType.Builder<SlabType, SlabType.Builder> {

    }
}
