/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.registry;

import java.util.Locale;

public final class ResourceLocations {

    private static final String MINECRAFT_NAMESPACE = "minecraft";
    private static final char NAMESPACE_SEPARATOR = ':';
    static final int NOT_FOUND = -1;

    private ResourceLocations() {
    }

    public static String toLowerCase(final String id) {
        return id.toLowerCase(Locale.ENGLISH);
    }

    public static String defaultedNamespace(final String id) {
        if (id.indexOf(NAMESPACE_SEPARATOR) == NOT_FOUND) {
            return MINECRAFT_NAMESPACE + NAMESPACE_SEPARATOR + id;
        }
        return id;
    }

    public static String findNamespace(final String string) {
        final int index = string.indexOf(NAMESPACE_SEPARATOR);
        if (index == NOT_FOUND) {
            return MINECRAFT_NAMESPACE;
        }
        return string.substring(0, index);
    }
}
