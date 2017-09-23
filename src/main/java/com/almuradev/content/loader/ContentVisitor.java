/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.loader;

import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

abstract class ContentVisitor implements FileVisitor<Path> {

    private final Logger logger;

    ContentVisitor(final Logger logger) {
        this.logger = logger;
    }

    @Override
    public FileVisitResult preVisitDirectory(final Path directory, final BasicFileAttributes attributes) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(final Path file, final IOException exception) throws IOException {
        if (exception != null && !(exception instanceof NoSuchFileException)) {
            this.logger.error("Encountered an exception while visiting file '{}'", file, exception);
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(final Path directory, final IOException exception) throws IOException {
        if (exception != null && !(exception instanceof NoSuchFileException)) {
            this.logger.error("Encountered an exception while visiting directory '{}'", directory, exception);
        }
        return FileVisitResult.CONTINUE;
    }
}
