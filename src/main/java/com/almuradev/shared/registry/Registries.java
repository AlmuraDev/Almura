/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.shared.registry;

import org.spongepowered.api.CatalogType;
import org.spongepowered.api.Sponge;

import java.util.Optional;

public final class Registries {

    private Registries() {
    }

    public static  <C extends CatalogType> Optional<C> findCatalog(final Class<C> clazz, final String id) {
        return Sponge.getRegistry().getType(clazz, id);
    }
}
