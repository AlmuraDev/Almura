/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.type.block.type.crop;

import com.almuradev.almura.content.type.block.type.BuildableBlockType;
import org.spongepowered.api.Sponge;

public interface CropBlockType extends BuildableBlockType {

    static Builder builder() {
        return Sponge.getRegistry().createBuilder(Builder.class);
    }

    interface Builder<T extends CropBlockType, B extends Builder<T, B>> extends BuildableBlockType.Builder<T, B> {

    }
}