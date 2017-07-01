/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.block;

import org.spongepowered.api.Sponge;

public interface SlabType extends BuildableBlockType {

    static SlabType.Builder builder() {
        return Sponge.getRegistry().createBuilder(SlabType.Builder.class);
    }

    interface Builder extends BuildableBlockType.Builder<SlabType, SlabType.Builder> {

    }
}
