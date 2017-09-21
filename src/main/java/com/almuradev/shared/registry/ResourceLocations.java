/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.shared.registry;

import net.minecraft.util.ResourceLocation;

import java.util.Locale;

import javax.annotation.Nullable;

public final class ResourceLocations {

    private static final String MINECRAFT_NAMESPACE = "minecraft";
    private static final char NAMESPACE_SEPARATOR = ':';
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

    public static String findValue(final String string) {
        final int index = string.indexOf(NAMESPACE_SEPARATOR);
        if (index == NOT_FOUND) {
            return string;
        }
        return string.substring(index + 1, string.length());
    }

    public static ResourceLocation buildResourceLocationPath(final String string, final String defaultNamespace, @Nullable final String parent) {
        final String namespace = findNamespace(string, defaultNamespace);
        String value = findValue(string);
        if (value.indexOf('/') == -1 && parent != null) {
            value = parent + '/' + value;
        }
        return new ResourceLocation(namespace, value);
    }
}
