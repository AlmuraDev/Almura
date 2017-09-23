/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
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
