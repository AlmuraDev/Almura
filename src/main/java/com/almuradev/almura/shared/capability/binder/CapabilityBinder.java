/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.capability.binder;

import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import net.minecraftforge.common.capabilities.Capability;

import java.util.concurrent.Callable;

public final class CapabilityBinder {

    private final Multibinder<CapabilityEntry> capabilities;

    public static CapabilityBinder create(final Binder binder) {
        return new CapabilityBinder(binder);
    }

    private CapabilityBinder(final Binder binder) {
        this.capabilities = Multibinder.newSetBinder(binder, new TypeLiteral<CapabilityEntry>() {});
    }

    /**
     * Registers a new {@link Capability}
     * @param capabilityClass The capability class, usually a marker interface
     * @param capabilityStorage The capability storage, used to handle persistence
     * @param instanceFactory The instance factory, used to create capability instances when passed the capability class
     * @param <T> The capability type, usually a marker interface
     * @return This object, for chaining
     */
    public <T> CapabilityBinder register(Class<? extends T> capabilityClass, Capability.IStorage<? extends T> capabilityStorage, Callable<? extends
                T> instanceFactory) {
        this.capabilities.addBinding().toInstance(new CapabilityEntry((Class) capabilityClass, (Capability.IStorage) capabilityStorage,
                (Callable) instanceFactory));
        return this;
    }
}
