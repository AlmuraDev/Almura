/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.api.block.rotable;

import com.almuradev.almura.api.block.BuildableBlockType;
import org.spongepowered.api.Sponge;

public interface RotableBlockType extends BuildableBlockType {

    @SuppressWarnings("unchecked")
    static Builder builder() {
        return Sponge.getRegistry().createBuilder(Builder.class);
    }

    interface Builder<ROTABLE extends RotableBlockType, BUILDER extends Builder<ROTABLE, BUILDER>> extends BuildableBlockType.Builder<ROTABLE,
            BUILDER> {

    }
}
