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

import java.util.function.Supplier;

/**
 * A builder registry entry.
 *
 * @param <C> the type
 */
final class BuilderEntry<C> extends AbstractEntry {

    private final Class<C> type;
    private final Supplier<? extends C> supplier;

    BuilderEntry(final Class<C> type, final Supplier<? extends C> supplier) {
        this.type = type;
        this.supplier = supplier;
    }

    @Override
    public void install(final Injector injector, final GameRegistry registry) {
        registry.registerBuilderSupplier(this.type, this.supplier);
    }
}
