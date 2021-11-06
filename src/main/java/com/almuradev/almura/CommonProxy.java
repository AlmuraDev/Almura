/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura;

import com.almuradev.almura.feature.claim.ClaimModule;
import com.almuradev.almura.shared.plugin.Plugin;
import com.google.inject.Injector;
import net.kyori.membrane.facet.internal.Facets;

/**
 * The abstract bootstrap.
 */
public abstract class CommonProxy {

    private Facets facets;
    private Injector injector;

    protected final void construct(final Plugin plugin, final Injector injector) {
        this.injector = this.createInjector(plugin, injector);
        this.facets = this.injector.getInstance(Facets.class);
        this.facets.enable();
    }

    final void destruct() {
        this.facets.disable();
    }

    protected abstract Injector createInjector(final Plugin plugin, final Injector parent);
}
