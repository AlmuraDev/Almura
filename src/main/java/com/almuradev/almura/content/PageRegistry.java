/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;

import java.util.Collections;
import java.util.Map;

public class PageRegistry {

    private static final Map<String, Page> PAGES = Maps.newHashMap();

    public static Optional<Page> getPage(String identifier) {
        return Optional.fromNullable(PAGES.get(identifier));
    }

    public static Page putPage(Page page) {
        return PAGES.put(page.getIdentifier(), page);
    }

    public static Optional<Page> removePage(String identifier) {
        return Optional.fromNullable(PAGES.remove(identifier));
    }

    public static Map<String, Page> getAll() {
        return Collections.unmodifiableMap(PAGES);
    }

    public static void clear() {
        PAGES.clear();
    }
}
