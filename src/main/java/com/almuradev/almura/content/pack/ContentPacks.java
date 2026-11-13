/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.content.pack;

import com.almuradev.almura.content.convert.ContentPack;
import com.google.common.base.Suppliers;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.PackMetadataResources;
import net.minecraft.world.flag.FeatureFlagSet;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import org.jspecify.annotations.Nullable;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Adds Almura's content pack ({@code almura_content}) for resources on the client and for data: always on, above vanilla's, hidden from the
 * pack lists. Each half is built the first time the game reads it.
 */
public final class ContentPacks {

    private static final String ID = "almura_content";

    private ContentPacks() {
    }

    public static void register(final IEventBus modBus, final Supplier<ContentPack> assets, final Supplier<ContentPack> data) {
        final Supplier<ContentPack> builtAssets = Suppliers.memoize(assets::get);
        final Supplier<ContentPack> builtData = Suppliers.memoize(data::get);
        modBus.addListener((AddPackFindersEvent event) -> {
            final Supplier<ContentPack> pack = event.getPackType() == PackType.CLIENT_RESOURCES ? builtAssets : builtData;
            final PackLocationInfo location = new PackLocationInfo(ID, Component.literal("Almura Content"), PackSource.BUILT_IN, Optional.empty());
            final ContentPackResources resources = new ContentPackResources(location, pack);
            event.addRepositorySource(consumer -> consumer.accept(new Pack(location, new Pack.ResourcesSupplier() {
                @Override
                public PackMetadataResources openMetadata(final PackLocationInfo info) {
                    return new NoMetadata(info);
                }

                @Override
                public Stream<PackResources> openResources(final PackLocationInfo info, final Pack.Metadata metadata) {
                    return Stream.of(resources);
                }
            }, new Pack.Metadata(Component.literal("Almura's blocks, items and their models"), PackCompatibility.COMPATIBLE,
                FeatureFlagSet.of(), List.of(), true), new PackSelectionConfig(true, Pack.Position.TOP, false))));
        });
    }

    /**
     * The pack's metadata is given with it; nothing is read.
     */
    private record NoMetadata(PackLocationInfo location) implements PackMetadataResources {

        @Override
        public @Nullable IoSupplier<InputStream> getRootResource(final String... path) {
            return null;
        }

        @Override
        public <T> @Nullable T getMetadataSection(final MetadataSectionType<T> type) {
            return null;
        }

        @Override
        public void close() {
        }
    }
}
