/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.lang;

import com.almuradev.almura.Almura;
import com.almuradev.almura.Configuration;
import cpw.mods.fml.common.registry.LanguageRegistry;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class LanguageManager {

    /*
     * Language -> Key (example: itemGroup.fruit.apple) -> value (example: "Apple")
     */
    private final Map<Languages, Map<String, String>> languageMap = new HashMap<>();

    public String put(Languages lang, String key, String value) {
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

    public Map<String, String> get(Languages lang) {
        return Collections.unmodifiableMap(languageMap.get(lang));
    }

    public void register() {
        for (Map.Entry<Languages, Map<String, String>> entry : languageMap.entrySet()) {
            Map<String, String> injectMap = new HashMap<>();
            for (Map.Entry<String, String> keyEntry : entry.getValue().entrySet()) {
                injectMap.put(keyEntry.getKey(), keyEntry.getValue());
                if (Configuration.IS_DEBUG) {
                    Almura.LOGGER.info("Registering language entry {" + entry.getKey() + " -> " + keyEntry.getKey() + " = " + keyEntry.getValue() + "}");
                }
            }
            LanguageRegistry.instance().injectLanguage(entry.getKey().value(), (HashMap<String, String>) injectMap);
        }
    }
}
