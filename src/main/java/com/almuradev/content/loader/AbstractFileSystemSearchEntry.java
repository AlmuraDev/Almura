/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.loader;

import com.almuradev.content.ContentConfig;
import com.google.inject.Injector;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

abstract class AbstractFileSystemSearchEntry extends SearchableSearchEntry {

    final String id = UUID.randomUUID().toString().substring(24);
    private final Set<String> namespaces = new HashSet<>();
    protected final Path path;

    AbstractFileSystemSearchEntry(final Path path) {
        super(path.toAbsolutePath().toString());
        this.path = path;
    }

    @Override
    void search(final Injector injector, final Logger logger) {
        try {
            try(final DirectoryStream<Path> stream = Files.newDirectoryStream(this.path)) {
                for (final Path path : stream) {
                    this.namespaces.add(path.getFileName().toString());
                }
            }
        } catch (final IOException e) {
            logger.error("Encountered an exception while discovering namespaces", e);
        }

        for (final String namespace : this.namespaces) {
            final Path path = this.path.resolve(namespace).resolve(ContentConfig.CONTENT_DIRECTORY);
            this.searchFileSystem0(injector, logger, namespace, path);
        }
    }

    Set<String> namespaces() {
        return this.namespaces;
    }

    private void searchFileSystem0(final Injector injector, final Logger logger, final String namespace, final Path path) {
        if (!exists(logger, path)) {
            return;
        }
        logger.debug("Searching file system path '{}' for content...", path.toAbsolutePath());
        this.search(injector, logger, namespace, path);
    }
}
