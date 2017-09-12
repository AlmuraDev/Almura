/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.loader;

import com.almuradev.almura.Almura;
import com.almuradev.almura.content.Pack;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class AssetPipeline {

    private final Map<LoaderPhase, Map<Asset.Type, List<StageTask>>> stagesByAssetTypeByPhase = new HashMap<>();

    public AssetPipeline registerStage(LoaderPhase phase, Class<? extends StageTask> clazz, Asset.Type... types) {
        final StageTask task;
        try {
            task = clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }

        if (task == null) {
            // TODO Tasks require a no-arg constructor
            throw new IllegalArgumentException("Task '" + clazz + "' requires a no-args constructor!");
        }

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

                        Almura.instance.logger.info("Processing assets of type [{}] in pack [{}] for phase [{}].", assetTypeMapEntry.getKey().name(),
                                packListEntry.getKey().getName(), phase.name());

                        int contextualCount = 0;

                        for (AssetContext assetContext : packListEntry.getValue()) {

                            for (StageTask stageTask : stageTasks) {
                                stageTask.execute(assetContext);
                            }

                            contextualCount++;
                        }

                        Almura.instance.logger.info("Processed [{}] [{}] in pack [{}] for phase [{}].", contextualCount, assetTypeMapEntry.getKey
                                        ().name(), packListEntry.getKey().getName(), phase.name());
                    }
                }
            }
        }

        return this;
    }
}
