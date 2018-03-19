/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.loader;

import com.almuradev.content.type.ContentType;
import com.google.inject.Injector;
import net.kyori.indigo.DetailedReportedException;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

abstract class SearchableSearchEntry extends SearchEntry {
    SearchableSearchEntry(final String description) {
        super(description);
    }

    abstract void search(final Injector injector, final Logger logger, final Set<ContentType> types);

    protected void search(final Injector injector, final Logger logger, final Set<ContentType> types, final String namespace, final Path path) {
        for (final ContentType type : types) {
            logger.debug("    Searching for '{}' content...", type.id());
            try {
                type.loader(injector).search(namespace, path.resolve(type.id()));
            } catch (final IOException e) {
                logger.error("Encountered an exception while searching for '{}' content", type.id());
            } catch (final DetailedReportedException e) {
                logger.error("{}:\n {}", e.getMessage(), e.report().toString());
            }
        }
    }

    static boolean exists(final Logger logger, final Path path) {
        if (!Files.exists(path)) {
            logger.debug("Ignoring path '{}' for content searching", path);
            return false;
        }
        return true;
    }
}
