/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.loader;

import com.google.common.collect.Iterables;
import net.minecraft.util.text.translation.LanguageMap;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@SuppressWarnings("all")
public class TranslationManager {

    static final String DIRECTORY = "_translations";
    private static final String EXTENSION = ".properties";
    static final String DEFAULT_TRANSLATION_ID = "en_us";
    private final Map<Path, TranslationKeyFactory> sources = new HashMap<>();
    private final Logger logger;
    @Nullable private String cachedId;
    @Nullable private Map<String, String> cachedTranslations;

    @Inject
    private TranslationManager(final Logger logger) {
        this.logger = logger;
    }

    void pushSource(Path source, final TranslationKeyFactory keyFactory) {
        this.sources.put(source, keyFactory);
    }

    private Map<Path, TranslationKeyFactory> sources(final String id) {
        final Map<Path, TranslationKeyFactory> sources = new HashMap<>();
        for (final Map.Entry<Path, TranslationKeyFactory> entry : this.sources.entrySet()) {
            final Path source = entry.getKey().resolve(id + EXTENSION);
            if (Files.exists(source)) {
                sources.put(source, entry.getValue());
            }
        }
        return sources;
    }

    Map<String, String> get(final String id) {
        if (id.equals(this.cachedId) && this.cachedTranslations != null) {
            return this.cachedTranslations;
        }

        try {
            return this.get0(id);
        } catch (final IOException e) {
            this.logger.error("Encountered an exception loading content translations", e);
            return Collections.emptyMap();
        }
    }

    private Map<String, String> get0(final String id) throws IOException {
        final Map<String, String> map = new HashMap<>();
        int oldSize = 0;
        this.logger.debug("Loading translations for language '{}'", id);
        for (final Map.Entry<Path, TranslationKeyFactory> source : this.sources(id).entrySet()) {
            for (final String line : Files.readAllLines(source.getKey(), StandardCharsets.UTF_8)) {
                if (!line.isEmpty() && line.charAt(0) != '#') {
                    final String[] parts = Iterables.toArray(LanguageMap.EQUAL_SIGN_SPLITTER.split(line), String.class);
                    if (parts != null && parts.length == 2) {
                        final String key = source.getValue().buildTranslationKey(parts[0]);
                        final String value = LanguageMap.NUMERIC_VARIABLE_PATTERN.matcher(parts[1]).replaceAll("%$1s");
                        map.put(key, value);
                    }
                }
            }
            final int newSize = map.size();
            final int loaded = newSize - oldSize;
            this.logger.debug("    Loaded {} translation{} from '{}'", loaded, loaded == 1 ? "" : "s", source.getKey().toAbsolutePath());
            oldSize = newSize;
        }
        this.cachedId = id;
        this.cachedTranslations = map;
        return map;
    }
}