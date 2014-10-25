/**
 * This file is part of AlmuraMod, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almuramod;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Filesystem {
    public static final Path CONFIG_PATH = Paths.get("config" + File.separator + AlmuraMod.MOD_ID.toLowerCase());
    public static final Path SETTINGS_PATH = Paths.get(CONFIG_PATH.toString(), "settings.yml");
    public static final Path BLOCKS_PATH = Paths.get(CONFIG_PATH.toString(), "blocks");
    public static final Path ITEMS_PATH = Paths.get(CONFIG_PATH.toString(), "items");

    protected Filesystem() {}

    /**
     * Stolen from flow-engine, credits to Waterpicker (cause I'm lazy) edits by me.
     *
     * @param path Where to get URLs from
     * @param blob File extension to look for
     * @return Collection of urls found
     */
    public static Collection<URL> getURLs(Path path, String blob) {
        final List<URL> result = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path, blob)) {
            for (Path entry : stream) {
                result.add(entry.toUri().toURL());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return Collections.unmodifiableCollection(result);
    }

    public static Collection<Path> getWithin(Path path, String blob) {
        final List<Path> result = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path, blob)) {
            for (Path entry : stream) {
                result.add(entry);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return Collections.unmodifiableCollection(result);
    }
}
