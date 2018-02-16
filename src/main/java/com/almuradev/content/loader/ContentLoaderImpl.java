/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.loader;

import com.almuradev.content.ContentConfig;
import com.almuradev.content.registry.CatalogedContent;
import com.almuradev.content.registry.ContentBuilder;
import com.google.common.base.Splitter;
import com.google.inject.Injector;
import net.kyori.indigo.DetailedReport;
import net.kyori.indigo.DetailedReportCategory;
import net.kyori.lunar.function.ThrowingRunnable;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.json.JSONConfigurationLoader;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.Nullable;
import javax.inject.Inject;

abstract class ContentLoaderImpl<C extends CatalogedContent, B extends ContentBuilder<C>, E extends ContentLoaderImpl.Entry<C, B>> implements ContentFinder<C, B>, ContentLoader {

    private static final PathMatcher JSON_MATCHER = FileSystems.getDefault().getPathMatcher("glob:**.json");
    static final Splitter SLASH_SPLITTER = Splitter.on('/').trimResults().omitEmptyStrings();
    @Inject Injector injector;
    @Inject protected Logger logger;
    @Inject TranslationManager translationManager;
    @Inject RecipeManager recipeManager;
    private final Set<String> names = new HashSet<>();
    protected final Map<String, E> entries = new HashMap<>();
    protected Stage stage = Stage.SEARCH;

    @Nullable
    final ConfigurationNode config(final Path path) {
        try {
            return JSONConfigurationLoader.builder()
                    .setPath(path)
                    .build()
                    .load();
        } catch (final IOException e) {
            this.logger.error("Encountered an exception while creating a configuration node for '{}'", path.toAbsolutePath(), e);
        }
        return null;
    }

    String configureBuilder(final String namespace, final Path path, final B builder) {
        final String internalId = internalId(path);
        final String id = id(path);
        builder.id(namespace, internalId, id);
        return internalId;
    }

    @Override
    public Optional<B> findBuilder(final String id) {
        return Optional.ofNullable(this.entries.get(id)).map(entry -> entry.builder);
    }

    public final boolean build() {
        return this.build(BuildType.NORMAL);
    }

    public final boolean build(final BuildType type) {
        if (this.stage == Stage.SEARCH) {
            final List<E> entries = new ArrayList<>(this.entries.values());
            if (type == BuildType.SORTED) {
                Collections.sort(entries);
            }
            for (final E entry : entries) {
                final B builder = entry.builder;
                this.catching(() -> entry.value = builder.build(), "Encountered a critical exception while constructing game content.", dr -> entry.populate(dr));
            }
            this.clean();
            this.stage = Stage.READY;
            return true;
        }
        return false;
    }

    private void clean() {
        this.names.clear();
        this.clean0();
    }

    abstract void clean0();

    final void catching(final ThrowingRunnable<Throwable> runnable, final String message, final Consumer<DetailedReport> filler) {
        try {
            runnable.throwingRun();
        } catch (final Throwable t) {
            final DetailedReport report = DetailedReport.create(message, t);
            report.category("loader").detail("class", this.getClass().getName());
            filler.accept(report);
            this.logger.error("{}:\n {}", report.message(), report.toString());
        }
    }

    protected final boolean queue(final Path path, final boolean longIndent) {
        final String indent = longIndent ? "            " : "        ";
        if (JSON_MATCHER.matches(path) && !path.getParent().getFileName().toString().equals("_recipes")) {
            if (this.names.add(pathName(path))) {
                this.logger.debug("{}Found {}", indent, path.toAbsolutePath());
                return true;
            }
        } else {
            this.logger.debug("{}Ignoring invalid {}", indent, path.toAbsolutePath());
            return false;
        }
        this.logger.debug("{}Ignoring duplicate {}", indent, path.toAbsolutePath());
        return false;
    }

    private static String internalId(final Path path) {
        String string = pathName(path).replace('\\', '/');
        int index = string.indexOf('/', string.indexOf('/') + 1);
        string = string.substring(index + 1, string.length());
        index = string.lastIndexOf('.');
        return string.substring(0, index);
    }

    private static String id(final Path path) {
        final String string = path.getFileName().toString();
        final int index = string.lastIndexOf('.');
        return string.substring(0, index);
    }

    private static String pathName(final Path path) {
        final String string = path.toString();
        final int index = string.lastIndexOf(ContentConfig.CONTENT_DIRECTORY);
        if (index > -1) {
            return string.substring(index + 2);
        }
        return string;
    }

    public enum BuildType {
        NORMAL,
        SORTED;
    }

    public static class Entry<C extends CatalogedContent, B extends ContentBuilder<C>> implements Comparable<Entry<C, B>> {

        @Nullable private final Path path;
        final String id;
        public final B builder;
        final ConfigurationNode config;
        public C value;

        public Entry(final String id, final B builder, @Nullable final Path path, final ConfigurationNode config) {
            this.id = id;
            this.path = path;
            this.builder = builder;
            this.config = config;
        }

        void populate(final DetailedReport dr) {
            final DetailedReportCategory drc = dr.category("asset");
            drc.detail("id", this.id);
            drc.detail("type", this.path != null ? "physical" : "virtual");
            if (this.path != null) {
                drc.detail("path", this.path.toAbsolutePath().toString());
            }
            drc.detail("builder", this.builder.getClass());
        }

        @Override
        public int compareTo(final Entry<C, B> that) {
            return this.id.compareTo(that.id);
        }
    }

    enum Stage {
        SEARCH,
        READY;
    }
}
