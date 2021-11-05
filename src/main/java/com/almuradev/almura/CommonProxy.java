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
    private Plugin plugin;

    protected final void construct(final Plugin plugin, final Injector injector) {
        this.injector = this.createInjector(plugin, injector);
        this.facets = this.injector.getInstance(Facets.class);
        this.plugin = plugin;
        this.facets.enable();
    }

    protected final void preInit() {
        // Put modules here that are required to load later on in the startup phase.
        this.injector.getParent().createChildInjector(new ClaimModule(this.plugin));
        //this.injector = this.injector.createChildInjector(new ClaimModule());

    }

    final void destruct() {
        this.facets.disable();
    }

    protected abstract Injector createInjector(final Plugin plugin, final Injector parent);
}
