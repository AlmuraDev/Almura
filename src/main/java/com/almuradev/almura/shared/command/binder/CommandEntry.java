/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.command.binder;

import org.spongepowered.api.command.CommandCallable;

public abstract class CommandEntry {

    protected final CommandCallable callable;
    protected final String[] aliases;

    CommandEntry(final CommandCallable callable, final String[] aliases) {
        this.callable = callable;
        this.aliases = aliases;
    }
}
