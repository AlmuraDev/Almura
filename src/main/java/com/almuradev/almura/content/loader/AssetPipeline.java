/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.loader;

import com.almuradev.almura.content.Pack;
import com.almuradev.shared.registry.AbstractBuilder;
import com.almuradev.shared.registry.catalog.BuildableCatalogType;
import ninja.leaping.configurate.ConfigurationNode;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class AssetPipeline {

    private final Map<LoaderPhase, Map<Asset.Type, List<AssetFactory>>> stagesByAssetTypeByPhase = new HashMap<>();
    private final Logger logger;

    @Inject
    private AssetPipeline(final Logger logger) {
        this.logger = logger;
    }

    public <C extends BuildableCatalogType, B extends AbstractBuilder<C, B>, F extends AssetFactory<C, B>> AssetPipeline registerFactory(LoaderPhase phase, F factory, Iterable<Asset.Type> types) {
        Map<Asset.Type, List<AssetFactory>> factories = this.stagesByAssetTypeByPhase.computeIfAbsent(phase, k -> new HashMap<>());
        for (final Asset.Type type : types) {
            factories.computeIfAbsent(type, k -> new LinkedList<>()).add(factory);
        }

        return this;
    }

    public AssetPipeline process(final LoaderPhase phase, final AssetRegistry registry) {
        // TODO Log to debugger that we're processing all files
        final Map<Asset.Type, List<AssetFactory>> types = this.stagesByAssetTypeByPhase.get(phase);
        if (types != null) {
            for (final Map.Entry<Asset.Type, Map<Pack, List<AssetContext>>> entries : registry.getAll().entrySet()) {
                final Asset.Type type = entries.getKey();
                @Nullable final List<AssetFactory> factories = types.get(type);
                if (factories == null) {
                    continue;
                }

                for (final Map.Entry<Pack, List<AssetContext>> entry : entries.getValue().entrySet()) {
                    this.logger.info("Processing assets of type [{}] in pack [{}] for phase [{}].", type.name(),
                            entry.getKey().getName(), phase.name());

                    int contextualCount = 0;

                    for (final AssetContext context : entry.getValue()) {
                        final Pack pack = context.getPack();
                        final Asset asset = context.getAsset();
                        final ConfigurationNode config = asset.getConfigurationNode();
                        final AbstractBuilder builder = context.getBuilder();

                        for (final AssetFactory factory : factories) {
                            factory.configure(pack, asset, config, builder);
                        }

                        contextualCount++;
                    }

                    this.logger.info("Processed [{}] [{}] in pack [{}] for phase [{}].", contextualCount, entries.getKey
                                    ().name(), entry.getKey().getName(), phase.name());
                }
            }
        }

        return this;
    }
}
