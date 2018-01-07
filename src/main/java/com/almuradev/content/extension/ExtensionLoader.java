/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.extension;

import java.nio.file.Path;

public interface ExtensionLoader {
    Push push(final Path path);

    interface Push {
        <E extends Extension> Push with(final E extension);
    }
}
