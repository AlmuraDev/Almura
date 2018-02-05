/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.registry;

import net.minecraft.util.ResourceLocation;
import ninja.leaping.configurate.ConfigurationNode;

import java.util.Locale;

import javax.annotation.Nullable;

public final class ResourceLocations {

    private static final String MINECRAFT_NAMESPACE = "minecraft";
    public static final char NAMESPACE_SEPARATOR = ':';
    public static final int NOT_FOUND = -1;

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
        return findNamespace(string, MINECRAFT_NAMESPACE);
    }

    public static String findNamespace(final String string, final String defaultValue) {
        final int index = string.indexOf(NAMESPACE_SEPARATOR);
        if (index == NOT_FOUND) {
            return defaultValue;
        }
        return string.substring(0, index);
    }

    public static String requireNamespace(final String string) {
        final int index = string.indexOf(NAMESPACE_SEPARATOR);
        if (index == NOT_FOUND) {
            throw new IllegalArgumentException("Expected a namespaced string");
        }
        return string.substring(0, index);
    }

    public static String findValue(final String string) {
        final int index = string.indexOf(NAMESPACE_SEPARATOR);
        if (index == NOT_FOUND) {
            return string;
        }
        return string.substring(index + 1, string.length());
    }

    public static ResourceLocation buildResourceLocationPath(final String string, @Nullable final String parent) {
        final String namespace = requireNamespace(string);
        String value = findValue(string);
        if (value.indexOf('/') == -1 && parent != null) {
            value = parent + '/' + value;
        }
        return new ResourceLocation(namespace, value);
    }

    public static String requireNamespaced(final String string) {
        if (string.indexOf(':') == -1) {
            throw new IllegalArgumentException("Expected a namespaced string");
        }
        return string;
    }

    public static ResourceLocation createNamespaced(final ConfigurationNode config) {
        return createNamespaced(config.getString());
    }

    public static ResourceLocation createNamespaced(final String string) {
        return new ResourceLocation(requireNamespaced(string));
    }
}
