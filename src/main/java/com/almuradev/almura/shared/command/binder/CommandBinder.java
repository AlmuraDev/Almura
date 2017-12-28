/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.command.binder;

import com.google.inject.Binder;
import com.google.inject.multibindings.Multibinder;
import org.spongepowered.api.command.CommandCallable;

public final class CommandBinder {

    private final Multibinder<RootCommandEntry> root;
    private final Multibinder<ChildCommandEntry> children;

    public static CommandBinder create(final Binder binder) {
        return new CommandBinder(binder);
    }

    private CommandBinder(final Binder binder) {
        this.root = Multibinder.newSetBinder(binder, RootCommandEntry.class);
        this.children = Multibinder.newSetBinder(binder, ChildCommandEntry.class);
    }

    public CommandBinder root(final CommandCallable callable, final String... aliases) {
        this.root.addBinding().toInstance(new RootCommandEntry(callable, aliases));
        return this;
    }

    public CommandBinder child(final CommandCallable callable, final String... aliases) {
        this.children.addBinding().toInstance(new ChildCommandEntry(callable, aliases));
        return this;
    }
}
