/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2015 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.lang;

import com.google.common.collect.Maps;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class LanguageRegistry {

    /*
     * Language -> Key (example: itemGroup.fruit.apple) -> value (example: "Apple")
     */
    private static final Map<Languages, Map<String, String>> languageMap = Maps.newHashMap();

    public static String put(Languages lang, String key, String value) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Attempted to put a key that was null or empty!");
        }
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Attempted to put a value for " + key + " that was null or empty!");
        }

        Map<String, String> keyMap = languageMap.get(lang);
        if (keyMap == null) {
            keyMap = new HashMap<>();
            languageMap.put(lang, keyMap);
        }

        return keyMap.put(key, value);
    }

    public static Map<String, String> get(Languages lang) {
        final Map<String, String> map = languageMap.get(lang);
        return Collections.unmodifiableMap(map == null ? Maps.<String, String>newHashMap() : map);
    }

    public static void injectIntoForge() {
        for (Map.Entry<Languages, Map<String, String>> entry : languageMap.entrySet()) {
            Map<String, String> injectMap = new HashMap<>();
            for (Map.Entry<String, String> keyEntry : entry.getValue().entrySet()) {
                injectMap.put(keyEntry.getKey(), keyEntry.getValue());
            }
            cpw.mods.fml.common.registry.LanguageRegistry.instance().injectLanguage(entry.getKey().value(), (HashMap<String, String>) injectMap);
        }
    }
}
