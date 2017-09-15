/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.shared.registry.binder;

import com.google.inject.Injector;
import net.kyori.membrane.facet.Enableable;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.GameRegistry;

import java.util.Set;

import javax.inject.Inject;

/**
 * A facet that installs registry entries into the registry.
 */
public final class RegistryInstaller implements Enableable {

    private final Injector injector;
    private final GameRegistry registry;
    private final Set<ModuleEntry<? extends CatalogType>> modules;
    private final Set<BuilderEntry<?>> builders;

    @Inject
    public RegistryInstaller(final Injector injector, final GameRegistry registry, final Set<ModuleEntry<? extends CatalogType>> modules, final Set<BuilderEntry<?>> builders) {
        this.injector = injector;
        this.registry = registry;
        this.modules = modules;
        this.builders = builders;
    }

    @Override
    public void enable() {
        this.modules.forEach(module -> module.install(this.injector, this.registry));
        this.builders.forEach(builder -> builder.install(this.injector, this.registry));
    }

    @Override
    public void disable() {
        // cannot remove from the registry
    }
}
