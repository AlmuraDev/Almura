/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.type.block.type.horizontal;

import com.almuradev.almura.content.type.block.type.BuildableBlockType;
import org.spongepowered.api.Sponge;

public interface HorizontalBlockType extends BuildableBlockType {

    @SuppressWarnings("unchecked")
    static Builder builder() {
        return Sponge.getRegistry().createBuilder(Builder.class);
    }

    interface Builder<T extends HorizontalBlockType, B extends Builder<T, B>> extends BuildableBlockType.Builder<T, B> {
    }
}
