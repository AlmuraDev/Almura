/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
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
