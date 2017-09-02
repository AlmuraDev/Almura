/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.registry;

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
