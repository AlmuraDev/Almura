/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.shared.registry.binder;

import com.google.inject.Injector;
import org.spongepowered.api.GameRegistry;

/**
 * An abstract registry binder entry.
 */
abstract class AbstractEntry {

    /**
     * Install this entry into the registry.
     *
     * @param injector the injector
     * @param registry the registry
     */
    public abstract void install(final Injector injector, final GameRegistry registry);
}
