/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.content.type.block.type.horizontal;

import com.almuradev.almura.content.type.block.type.BuildableBlockType;
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
