/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.command.binder;

import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import org.spongepowered.api.command.CommandCallable;

public final class CommandBinder {

    private final Multibinder<CommandEntry> commands;

    public static CommandBinder create(final Binder binder) {
        return new CommandBinder(binder);
    }

    private CommandBinder(final Binder binder) {
        this.commands = Multibinder.newSetBinder(binder, new TypeLiteral<CommandEntry>() {});
    }

    public CommandBinder child(CommandCallable command, String... aliases) {
        this.commands.addBinding().toInstance(new CommandEntry(command, aliases));
        return this;
    }
}
