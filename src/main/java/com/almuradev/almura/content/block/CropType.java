/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.block;

import org.spongepowered.api.Sponge;

public interface CropType extends BuildableBlockType {

    static Builder builder() {
        return Sponge.getRegistry().createBuilder(Builder.class);
    }

    interface Builder extends BuildableBlockType.Builder<CropType, Builder> {

    }
}
