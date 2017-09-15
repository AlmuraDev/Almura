/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.loader;

import com.almuradev.almura.content.Pack;
import com.almuradev.shared.registry.catalog.BuildableCatalogType;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class AssetPipeline {

    private final Map<LoaderPhase, Map<Asset.Type, List<StageTask>>> stagesByAssetTypeByPhase = new HashMap<>();
    private final Logger logger;

    @Inject
    private AssetPipeline(final Logger logger) {
        this.logger = logger;
    }

    public <C extends BuildableCatalogType, B extends BuildableCatalogType.Builder<C, B>, F extends StageTask<C, B>> AssetPipeline registerStage(LoaderPhase phase, F task, Iterable<Asset.Type> types) {
        Map<Asset.Type, List<StageTask>> stages = this.stagesByAssetTypeByPhase.computeIfAbsent(phase, k -> new HashMap<>());
        for (final Asset.Type type : types) {
            stages.computeIfAbsent(type, k -> new LinkedList<>()).add(task);
        }

        return this;
    }

    public AssetPipeline process(LoaderPhase phase, AssetRegistry registry) {
        // TODO Log to debugger that we're processing all files
        final Map<Asset.Type, List<StageTask>> assetTypeListMap = this.stagesByAssetTypeByPhase.get(phase);
        if (assetTypeListMap != null) {

            for (Map.Entry<Asset.Type, Map<Pack, List<AssetContext>>> assetTypeMapEntry : registry.getAll().entrySet()) {

                final List<StageTask> stageTasks = assetTypeListMap.get(assetTypeMapEntry.getKey());

                if (stageTasks != null) {

                    for (Map.Entry<Pack, List<AssetContext>> packListEntry : assetTypeMapEntry.getValue().entrySet()) {

                        this.logger.info("Processing assets of type [{}] in pack [{}] for phase [{}].", assetTypeMapEntry.getKey().name(),
                                packListEntry.getKey().getName(), phase.name());

                        int contextualCount = 0;

                        for (AssetContext assetContext : packListEntry.getValue()) {

                            for (StageTask stageTask : stageTasks) {
                                stageTask.execute(assetContext);
                            }

                            contextualCount++;
                        }

                        this.logger.info("Processed [{}] [{}] in pack [{}] for phase [{}].", contextualCount, assetTypeMapEntry.getKey
                                        ().name(), packListEntry.getKey().getName(), phase.name());
                    }
                }
            }
        }

        return this;
    }
}
