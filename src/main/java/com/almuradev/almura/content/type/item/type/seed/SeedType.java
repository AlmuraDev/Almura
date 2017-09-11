/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.type.item.type.seed;

import com.almuradev.almura.content.type.item.type.BuildableItemType;
import org.spongepowered.api.Sponge;

public interface SeedType extends BuildableItemType {

    static SeedType.Builder builder() {
        return Sponge.getRegistry().createBuilder(SeedType.Builder.class);
    }

    interface Builder extends BuildableItemType.Builder<SeedType, SeedType.Builder> {

    }
}
