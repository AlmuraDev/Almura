package com.almuradev.almura.content.loader;

import com.almuradev.almura.content.AssetType;
import com.almuradev.almura.content.Pack;
import com.almuradev.almura.content.loader.task.StageTask;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class AssetPipeline {

    private final Map<LoaderPhase, Map<AssetType, List<StageTask>>> stagesByAssetTypeByPhase = new HashMap<>();

    public AssetPipeline registerStage(LoaderPhase phase, AssetType assetType, Class<? extends StageTask> clazz) {
        StageTask task = null;
        try {
            task = clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            // TODO Could silence these...
            e.printStackTrace();
        }

        if (task == null) {
            // TODO Tasks require a no-arg constructor
            throw new IllegalArgumentException();
        }
        this.stagesByAssetTypeByPhase
                .computeIfAbsent(phase, k -> new HashMap<>())
                .computeIfAbsent(assetType, k -> new LinkedList<>())
                .add(task);

        return this;
    }

    public AssetPipeline process(LoaderPhase phase, AssetRegistry registry) {

        // TODO Log to debugger that we're processing all files
        final Map<AssetType, List<StageTask>> assetTypeListMap = this.stagesByAssetTypeByPhase.get(phase);
        if (assetTypeListMap != null) {

            for (Map.Entry<AssetType, Map<Pack, List<AssetContext>>> assetTypeMapEntry : registry.getAll().entrySet()) {

                final List<StageTask> stageTasks = assetTypeListMap.get(assetTypeMapEntry.getKey());

                for (Map.Entry<Pack, List<AssetContext>> packListEntry : assetTypeMapEntry.getValue().entrySet()) {

                    for (AssetContext assetContext : packListEntry.getValue()) {

                        for (StageTask stageTask : stageTasks) {
                            stageTask.execute(assetContext);
                        }
                    }
                }
            }
        }

        return this;
    }
}
