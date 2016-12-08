/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class FileSystem {

    public static final Path PATH_CONFIG = Paths.get(".").resolve("config").resolve(Almura.PLUGIN_ID);
    public static final Path PATH_CONFIG_CLIENT = PATH_CONFIG.resolve("client.conf");
    public static final Path PATH_CONFIG_PACKS = PATH_CONFIG.resolve("packs");

    public static void construct() {
        if (Files.notExists(PATH_CONFIG_PACKS)) {
            try {
                Files.createDirectories(PATH_CONFIG_PACKS);
            } catch (IOException e) {
                throw new RuntimeException("Failed to create [" + PATH_CONFIG_PACKS + "]!", e);
            }
        }
    }
}
