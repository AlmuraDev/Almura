/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.client.keyboard.binder;

import com.google.inject.Inject;
import com.google.inject.Injector;
import net.kyori.membrane.facet.Enableable;

import java.util.Set;

public final class KeyBindingInstaller implements Enableable {

    private final Injector injector;
    private final Set<KeyBindingEntry> keybindings;

    @Inject
    public KeyBindingInstaller(final Injector injector, Set<KeyBindingEntry> keybindings) {
        this.injector = injector;
        this.keybindings = keybindings;
    }

    @Override
    public void enable() {
        this.keybindings.forEach((keybinding) -> keybinding.install(this.injector));
    }

    @Override
    public void disable() {
        // Commands cannot be removed
    }
}
