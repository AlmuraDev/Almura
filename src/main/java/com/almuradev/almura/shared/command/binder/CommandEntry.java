/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.command.binder;

import com.google.inject.Injector;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.spec.CommandSpec;

public final class CommandEntry {

    private final CommandCallable command;
    private final String[] aliases;

    CommandEntry(CommandCallable command, String... aliases) {
        this.command = command;
        this.aliases = aliases;
    }
    public void install(final Injector injector, final CommandSpec.Builder rootCommand) {
        rootCommand.child(this.command, this.aliases);
    }
}
