/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.loader;

import java.io.IOException;
import java.nio.file.Path;

/**
 * A content loader.
 */
public interface ContentLoader {

    /**
     * Search for content in the specified path.
     *
     * @param namespace the namespace
     * @param path the path to search in
     * @throws IOException if an exception occurred during searching
     */
    void search(final String namespace, final Path path) throws IOException;

    /**
     * Process queued content.
     */
    void process();

    /**
     * Load content from the specified path.
     *
     * @throws IOException if an exception occurred during loading
     * @return the amount of assets of this type that were loaded
     */
    int load() throws IOException;
}
