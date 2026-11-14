/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.content.pack;

import com.almuradev.almura.content.convert.ContentPack;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jspecify.annotations.Nullable;

import java.io.InputStream;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;

/**
 * Serves Almura's content pack ({@link ContentPack}) to the game: assets on the client, data on the server. The pack is built the first
 * time it is read.
 */
final class ContentPackResources implements PackResources {

    private final PackLocationInfo location;
    private final Supplier<ContentPack> pack;

    ContentPackResources(final PackLocationInfo location, final Supplier<ContentPack> pack) {
        this.location = location;
        this.pack = pack;
    }

    private static String root(final PackType type) {
        return type == PackType.CLIENT_RESOURCES ? "assets" : "data";
    }

    @Override
    public @Nullable IoSupplier<InputStream> getRootResource(final String... path) {
        return null;
    }

    @Override
    public @Nullable IoSupplier<InputStream> getResource(final PackType type, final Identifier location) {
        final String path = root(type) + "/" + location.getNamespace() + "/" + location.getPath();
        final ContentPack pack = this.pack.get();
        return pack.has(path) ? () -> pack.open(path) : null;
    }

    @Override
    public void listResources(final PackType type, final String namespace, final String directory, final ResourceOutput output) {
        final String prefix = root(type) + "/" + namespace + "/";
        final ContentPack pack = this.pack.get();
        for (final Map.Entry<String, Object> entry : pack.under(prefix + directory + "/").entrySet()) {
            final String path = entry.getKey();
            final Identifier id = Identifier.tryBuild(namespace, path.substring(prefix.length()));
            if (id != null) { // An invalid name would fail the whole reload
                output.accept(id, () -> pack.open(path));
            }
        }
    }

    @Override
    public Set<String> getNamespaces(final PackType type) {
        final Set<String> namespaces = new TreeSet<>();
        final String prefix = root(type) + "/";
        for (final String path : this.pack.get().under(prefix).keySet()) {
            final int slash = path.indexOf('/', prefix.length());
            if (slash > 0) {
                namespaces.add(path.substring(prefix.length(), slash));
            }
        }
        return namespaces;
    }

    @Override
    public <T> @Nullable T getMetadataSection(final MetadataSectionType<T> type) {
        return null;
    }

    @Override
    public PackLocationInfo location() {
        return this.location;
    }

    @Override
    public void close() {
        // The pack is kept for the next reload
    }
}
