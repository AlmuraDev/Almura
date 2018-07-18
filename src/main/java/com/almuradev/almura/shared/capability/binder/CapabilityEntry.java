/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.capability.binder;

import com.google.inject.Injector;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;

import java.util.concurrent.Callable;

public final class CapabilityEntry {

    private Class<Object> capabilityClazz;
    private Capability.IStorage<Object> capabilityStorage;
    private Callable<Object> factory;

    CapabilityEntry(Class<Object> capabilityClazz, Capability.IStorage<Object> capabilityStorage, Callable<Object> factory) {
        this.capabilityClazz = capabilityClazz;
        this.capabilityStorage = capabilityStorage;
        this.factory = factory;
    }

    public void install(final Injector injector) {
        CapabilityManager.INSTANCE.register(this.capabilityClazz, this.capabilityStorage, this.factory);
    }
}
