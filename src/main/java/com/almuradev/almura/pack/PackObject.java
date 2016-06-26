/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack;

import org.spongepowered.api.CatalogType;
import org.spongepowered.api.util.ResettableBuilder;

public interface PackObject extends CatalogType {

    /**
     * Returns if this {@link PackObject} was built by the Pack system.
     * @return True if built from a pack, false if from Vanilla/other mods
     */
    default boolean fromPack() {
        return false;
    }

    interface Builder<P extends PackObject, B extends Builder<P, B>> extends ResettableBuilder<P, B> {
        P build(String id, String name);
    }
}
