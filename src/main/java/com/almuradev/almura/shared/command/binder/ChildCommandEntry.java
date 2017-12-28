/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.command.binder;

import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.spec.CommandSpec;

final class ChildCommandEntry extends CommandEntry {

    ChildCommandEntry(final CommandCallable callable, final String[] aliases) {
        super(callable, aliases);
    }

    void insert(final CommandSpec.Builder builder) {
        builder.child(this.callable, this.aliases);
    }
}
