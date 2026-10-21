/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.accessory;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The texture variants of each accessory type, read from assets/almura/accessories/catalog.json in the jar (so server and client agree on
 * it). The textures themselves are in the content folders, textures/accessory/&lt;type&gt;/&lt;variant&gt;.png.
 */
public final class AccessoryCatalog {

    private static final String CATALOG = "/assets/almura/accessories/catalog.json";
    private static Map<AccessoryType, List<String>> variants;

    public static List<String> variants(final AccessoryType type) {
        return load().getOrDefault(type, Collections.emptyList());
    }

    public static boolean contains(final AccessoryType type, final String variant) {
        return variants(type).contains(variant);
    }

    /**
     * @return A readable name of a variant: "tophat12" is "Style 12", "sun_black_1" is "Black 1", "top_hat" is "Top Hat"
     */
    public static String label(final AccessoryType type, final String variant) {
        String name = variant;
        for (final String prefix : new String[] { type.id(), "sun_", "ear" }) {
            if (name.startsWith(prefix) && name.length() > prefix.length()) {
                name = name.substring(prefix.length());
                break;
            }
        }
        name = name.replace('_', ' ').trim();
        if (name.matches("\\d+")) {
            return "Style " + name;
        }

        final StringBuilder label = new StringBuilder();
        for (final String word : name.split(" ")) {
            if (!word.isEmpty()) {
                label.append(label.length() == 0 ? "" : " ").append(word.substring(0, 1).toUpperCase(Locale.ENGLISH)).append(word.substring(1));
            }
        }
        return label.toString();
    }

    private static synchronized Map<AccessoryType, List<String>> load() {
        if (variants != null) {
            return variants;
        }

        final Map<AccessoryType, List<String>> loaded = new EnumMap<>(AccessoryType.class);
        try (final InputStream stream = AccessoryCatalog.class.getResourceAsStream(CATALOG)) {
            if (stream != null) {
                final JsonObject json = new JsonParser().parse(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
                for (final Map.Entry<String, JsonElement> entry : json.entrySet()) {
                    final AccessoryType type = AccessoryType.byId(entry.getKey());
                    if (type == null) {
                        continue;
                    }
                    final ImmutableList.Builder<String> names = ImmutableList.builder();
                    entry.getValue().getAsJsonArray().forEach(element -> names.add(element.getAsString()));
                    loaded.put(type, names.build());
                }
            }
        } catch (final Exception ignored) {
            // No catalog: no variants
        }
        variants = loaded;
        return variants;
    }

    private AccessoryCatalog() {
    }
}
