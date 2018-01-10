/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.loader;

import static com.google.common.base.Preconditions.checkState;

import com.almuradev.content.ContentType;
import com.almuradev.content.registry.CatalogedContent;
import com.almuradev.content.registry.ContentBuilder;
import com.almuradev.toolbox.config.processor.ConfigProcessor;
import com.google.common.base.MoreObjects;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.SetMultimap;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * An abstract implementation of a content loader that has a multiple types.
 */
public abstract class MultiTypeContentLoader<T extends Enum<T> & ContentType.MultiType<C, B>, C extends CatalogedContent, B extends ContentBuilder<C>, P extends ConfigProcessor<B>> extends ContentLoaderImpl<C, B, MultiTypeContentLoader.Entry<T, C, B>> implements MultiTypeExternalContentProcessor<T, C, B> {

    private final TypeToken<T> type = new TypeToken<T>(this.getClass()) {};
    private final SetMultimap<T, Entry<T, C, B>> queue = HashMultimap.create();
    private final T[] types;
    private final String name;
    @Inject private Map<T, Set<P>> processors;

    protected MultiTypeContentLoader() {
        this.types = (T[]) this.type.getRawType().getEnumConstants();
        this.name = this.type.getRawType().getAnnotation(ContentType.MultiType.Name.class).value();
    }

    @Override
    public final void search(final String namespace, final Path path) throws IOException {
        checkState(this.stage == Stage.SEARCH, "loader is not searching");
        for (final T type : this.types) {
            this.logger.debug("        Searching for '{}' {}...", type.id(), this.name);
            this.walk(namespace, type, path.resolve(type.id()));
        }
    }

    @Nullable
    private Entry<T, C, B> entry(final String namespace, final T type, final Path path) {
        @Nullable final ConfigurationNode config = this.config(path);
        if (config != null) {
            final B builder = this.builder(type);
            final String id = this.configureBuilder(namespace, path, builder);
            return new Entry<>(id, type, builder, path, config);
        }
        return null;
    }

    @Override
    public void process() {
        for (final T type : this.queue.keySet()) {
            this.queue.get(type).forEach(entry -> {
                this.catching(() -> this.process(type, entry.config, entry.builder), "Encountered an exception while processing content", (dr) -> entry.populate(dr));
                this.entries.put(entry.id, entry);
            });
        }
    }

    protected final <X extends B> X builder(final T type) {
        return (X) this.injector.getInstance(type.builder());
    }

    @Override
    public int load() throws IOException {
        for (final Entry<T, C, B> entry : this.entries.values()) {
            this.postProcess(entry.type, entry.config, entry.builder);
        }
        return this.entries.size();
    }

    @Override
    void clean0() {
        this.queue.clear();
    }

    @Override
    public <X extends B> X processExternal0(final String namespace, final T type, final ConfigurationNode config, final String id) {
        final X builder = this.builder(type);
        builder.id(namespace, id, id);
        this.process(type, config, builder);
        this.postProcess(type, config, builder);
        this.entries.put(id, new Entry<>(id, type, builder, null, config));
        return builder;
    }

    private void process(final T type, final ConfigurationNode config, final B builder) {
        for (final P processor : MoreObjects.<Set<P>>firstNonNull(this.processors.get(type), Collections.emptySet())) {
            processor.process(config, builder);
        }
    }

    private void postProcess(final T type, final ConfigurationNode config, final B builder) {
        for (final P processor : MoreObjects.<Set<P>>firstNonNull(this.processors.get(type), Collections.emptySet())) {
            processor.postProcess(config, builder);
        }
    }

    private void walk(final String namespace, final T type, final Path path) throws IOException {
        final boolean translations = this instanceof Translated<?>;
        Files.walkFileTree(path, new ContentVisitor(this.logger) {
            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attributes) throws IOException {
                if (MultiTypeContentLoader.this.queue(file, true)) {
                    @Nullable final Entry<T, C, B> entry = MultiTypeContentLoader.this.entry(namespace, type, file);
                    if (entry != null) {
                        MultiTypeContentLoader.this.queue.put(type, entry);
                    }
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(final Path directory, final BasicFileAttributes attributes) throws IOException {
                if (translations) {
                    if (directory.getFileName().toString().equals(TranslationManager.DIRECTORY)) {
                        final Iterable<String> components = SLASH_SPLITTER.trimResults().omitEmptyStrings().split(path.relativize(directory.getParent()).toString().replace('\\', '/'));
                        MultiTypeContentLoader.this.translationManager.pushSource(directory, key -> ((Translated<T>) MultiTypeContentLoader.this).buildTranslationKey(namespace, type, components, key));
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public interface Translated<T extends Enum<T> & ContentType.MultiType> {
        String buildTranslationKey(final String namespace, final T type, final Iterable<String> components, final String key);

        default String buildTranslationKey(final String what, final String namespace, final T type, final Iterable<String> components, final String key) {
            final StringBuilder sb = new StringBuilder();
            sb.append(what).append('.').append(namespace).append('.').append(type.id()).append('.');
            if (!Iterables.isEmpty(components)) {
                sb.append(DOT_JOINER.join(components)).append('.');
            }
            sb.append(key);
            return sb.toString();
        }
    }

    public static class Entry<T extends Enum<T> & ContentType.MultiType, C extends CatalogedContent, B extends ContentBuilder<C>> extends ContentLoaderImpl.Entry<C, B> {

        final T type;

        public Entry(final String id, final T type, final B builder, @Nullable final Path path, final ConfigurationNode config) {
            super(id, builder, path, config);
            this.type = type;
        }
    }
}
