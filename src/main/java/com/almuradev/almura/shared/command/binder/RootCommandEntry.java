/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.command.binder;

import org.spongepowered.api.command.CommandCallable;

final class RootCommandEntry extends CommandEntry {

    RootCommandEntry(final CommandCallable callable, final String[] aliases) {
        super(callable, aliases);
    }
}
