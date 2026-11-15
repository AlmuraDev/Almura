/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.accessory;

import com.almuradev.almura.Almura;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The variants of each accessory, as 3.5's: {@code assets/almura/accessories/catalog.json} in the jar, read once.
 */
public final class AccessoryCatalog {

    private static final String CATALOG = "/assets/almura/accessories/catalog.json";

    private static Map<AccessoryType, List<String>> variants;

    private AccessoryCatalog() {
    }

    public static List<String> variants(final AccessoryType type) {
        return load().getOrDefault(type, List.of());
    }

    public static boolean contains(final AccessoryType type, final String variant) {
        return variants(type).contains(variant);
    }

    /**
     * @return a readable name of a variant, as 3.5's: "tophat12" is "Style 12", "sun_black_1" is "Black 1", "top_hat" is "Top Hat"
     */
    public static String label(final AccessoryType type, final String variant) {
        String name = variant;
        for (final String prefix : new String[] {type.id(), "sun_", "ear"}) {
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
                label.append(label.isEmpty() ? "" : " ").append(word.substring(0, 1).toUpperCase(Locale.ENGLISH)).append(word.substring(1));
            }
        }
        return label.toString();
    }

    private static synchronized Map<AccessoryType, List<String>> load() {
        if (variants != null) {
            return variants;
        }
        final Map<AccessoryType, List<String>> loaded = new EnumMap<>(AccessoryType.class);
        try (InputStream stream = AccessoryCatalog.class.getResourceAsStream(CATALOG)) {
            if (stream != null) {
                final JsonObject json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
                for (final Map.Entry<String, JsonElement> entry : json.entrySet()) {
                    final AccessoryType type = AccessoryType.byId(entry.getKey());
                    if (type == null) {
                        continue;
                    }
                    final List<String> names = new ArrayList<>();
                    entry.getValue().getAsJsonArray().forEach(element -> names.add(element.getAsString()));
                    loaded.put(type, List.copyOf(names));
                }
            }
        } catch (final Exception e) {
            Almura.LOGGER.warn("Couldn't read the accessory catalog; no accessories can be worn", e);
        }
        variants = loaded;
        return variants;
    }
}
