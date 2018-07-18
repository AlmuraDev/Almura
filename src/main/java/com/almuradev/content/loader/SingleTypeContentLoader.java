/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.loader;

import static com.google.common.base.Preconditions.checkState;

import com.almuradev.content.registry.CatalogedContent;
import com.almuradev.content.registry.ContentBuilder;
import com.almuradev.toolbox.config.processor.ConfigProcessor;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * An abstract implementation of a content loader that has a single type.
 */
public abstract class SingleTypeContentLoader<C extends CatalogedContent, B extends ContentBuilder<C>> extends ContentLoaderImpl<C, B, ContentLoaderImpl.Entry<C, B>> implements SingleTypeExternalContentProcessor<C, B> {
    private final TypeToken<B> builder = new TypeToken<B>(this.getClass()) {};
    @Inject private Set<ConfigProcessor<? extends B>> processors;
    private final Set<Entry<C, B>> queue = new HashSet<>();

    @Override
    public final void search(final String namespace, final Path path) throws IOException {
        checkState(this.stage == Stage.SEARCH, "loader is not searching");
        final boolean translations = this instanceof Translated;
        Files.walkFileTree(path, new ContentVisitor(this.logger) {
            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                if (SingleTypeContentLoader.this.queue(file, false)) {
                    final Entry<C, B> entry = SingleTypeContentLoader.this.entry(namespace, file);
                    if (entry != null) {
                        SingleTypeContentLoader.this.queue.add(entry);
                    }
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(final Path directory, final BasicFileAttributes attributes) throws IOException {
                if (directory.getFileName().toString().equals(RecipeManager.DIRECTORY)) {
                    SingleTypeContentLoader.this.recipeManager.push(directory);
                }
                if (translations) {
                    if (directory.getFileName().toString().equals(TranslationManager.DIRECTORY)) {
                        final Iterable<String> components = SLASH_SPLITTER.split(path.relativize(directory.getParent()).toString().replace('\\', '/'));
                        SingleTypeContentLoader.this.translationManager.pushSource(directory, key -> ((Translated) SingleTypeContentLoader.this).buildTranslationKey(namespace, components, key));
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    @Nullable
    private Entry<C, B> entry(final String namespace, final Path path) {
        @Nullable final ConfigurationNode config = this.config(path);
        if (config != null) {
            final B builder = this.builder();
            final String id = this.configureBuilder(namespace, path, builder);
            return new Entry<>(id, builder, path, config);
        }
        return null;
    }

    @Override
    public void process() {
        this.queue.forEach(entry -> {
            // WARNING: IntelliJ is dumb - this CANNOT be replaced with a method reference.
            this.catching(() -> this.process(entry.config, entry.builder), "Encountered an exception while processing content", (dr) -> entry.populate(dr));
            this.entries.put(entry.id, entry);
        });
    }

    protected final B builder() {
        return (B) this.injector.getInstance(this.builder.getRawType());
    }

    @Override
    public int load() throws IOException {
        for (final Entry<C, B> entry : this.entries.values()) {
            this.postProcess(entry.config, entry.builder);
        }
        return this.entries.size();
    }

    @Override
    void clean0() {
        this.queue.clear();
    }

    @Override
    public B processExternal(final String namespace, final ConfigurationNode config, final String id) {
        final B builder = this.builder();
        builder.id(namespace, id, id);
        this.process(config, builder);
        this.postProcess(config, builder);
        this.entries.put(id, new Entry<>(id, builder, null, config));
        return builder;
    }

    private void process(final ConfigurationNode config, final B builder) {
        for (final ConfigProcessor<? extends B> processor : this.processors) {
            ((ConfigProcessor<B>) processor).process(config, builder);
        }
    }

    private void postProcess(final ConfigurationNode config, final B builder) {
        for (final ConfigProcessor<? extends B> processor : this.processors) {
            ((ConfigProcessor<B>) processor).postProcess(config, builder);
        }
    }

    public interface Translated {
        String buildTranslationKey(final String namespace, final Iterable<String> components, final String key);
    }
}
