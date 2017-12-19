/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura;

import com.google.inject.Injector;
import net.kyori.membrane.facet.internal.Facets;

/**
 * The abstract bootstrap.
 */
public abstract class CommonProxy {

    private Facets facets;

    protected final void construct(final Injector injector) {
        this.facets = this.createInjector(injector).getInstance(Facets.class);
        this.facets.enable();
    }

    final void destruct() {
        this.facets.disable();
    }

    protected abstract Injector createInjector(final Injector parent);
}
