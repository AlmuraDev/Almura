/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.loader;

import java.nio.file.Path;

final class ManagedFileSystemEntry extends AbstractFileSystemSearchEntry {
    ManagedFileSystemEntry(final Path path) {
        super(path);
    }
}
