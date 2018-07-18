/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.capability.binder;

import com.almuradev.core.event.Witness;
import com.google.inject.Injector;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;

import java.util.Set;

import javax.inject.Inject;

public final class CapabilityInstaller implements Witness {

    private final Injector injector;
    private final Set<CapabilityEntry> capabilities;

    @Inject
    public CapabilityInstaller(final Injector injector, Set<CapabilityEntry> capabilities) {
        this.injector = injector;
        this.capabilities = capabilities;
    }

    @Listener
    public void onGameInitialization(GameInitializationEvent event) {
        this.capabilities.forEach((capability) -> capability.install(this.injector));
    }
}
