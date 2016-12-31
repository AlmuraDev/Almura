/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.block.rotatable;

import com.almuradev.almura.block.BuildableBlockType;
import org.spongepowered.api.Sponge;

public interface HorizontalType extends BuildableBlockType {

    @SuppressWarnings("unchecked")
    static Builder builder() {
        return Sponge.getRegistry().createBuilder(Builder.class);
    }

    interface Builder<HORIZONTAL extends HorizontalType, BUILDER extends Builder<HORIZONTAL, BUILDER>> extends BuildableBlockType.Builder<HORIZONTAL,
            BUILDER> {
    }
}
