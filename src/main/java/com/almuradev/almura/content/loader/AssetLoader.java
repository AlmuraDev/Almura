/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.loader;

import com.almuradev.almura.Almura;
import com.almuradev.almura.BuildableCatalogType;
import com.almuradev.almura.Constants;
import com.almuradev.almura.content.AssetType;
import com.almuradev.almura.content.Pack;
import com.almuradev.almura.content.loader.stage.LoaderStage;
import com.almuradev.almura.content.loader.stage.task.StageTask;
import com.almuradev.almura.content.loader.stage.task.TaskExecutionFailedException;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import org.apache.commons.lang3.text.WordUtils;
import org.spongepowered.api.Sponge;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

// TODO This works only on /run/assets/** currently. Need to have it read the jar
public class AssetLoader {

    private final List<LoaderStage> loaderStages = new LinkedList<>();
    private final Map<String, Pack> packs = new LinkedHashMap<>();
    private final Map<Pack, Map<AssetType, List<Path>>> assetFilesByTypeInPack = new LinkedHashMap<>();
    private final Map<Pack, List<AssetContext>> contexualsInPack = new LinkedHashMap<>();

    public void registerLoaderStage(LoaderStage loaderStage) {
        this.loaderStages.add(loaderStage);
    }

    public void buildAssets(Path sourcePath) throws IOException {
        this.assetFilesByTypeInPack.clear();

        // Compile together files for asset types
        for (AssetType assetType : AssetType.values()) {
            Files.walkFileTree(sourcePath, new AssetFilesOnlyVisitor(sourcePath, assetType));
        }

        // Build asset contexuals
        for (Map.Entry<Pack, Map<AssetType, List<Path>>> packEntry : this.assetFilesByTypeInPack.entrySet()) {
            final Pack pack = packEntry.getKey();
            final Map<AssetType, List<Path>> assetFilesByType = packEntry.getValue();

            final List<AssetContext> assets = this.contexualsInPack.computeIfAbsent(pack, v -> new LinkedList<>());

            for (Map.Entry<AssetType, List<Path>> assetTypeEntry : assetFilesByType.entrySet()) {
                final AssetType assetType = assetTypeEntry.getKey();
                final List<Path> assetFiles = assetTypeEntry.getValue();

                for (Path assetFile : assetFiles) {

                    final BuildableCatalogType.Builder builder = Sponge.getRegistry().createBuilder(assetType.getBuilderClass());

                    final String assetName = assetFile.getFileName().toString().split("\\.")[0];

                    final Asset asset = new Asset(assetName, assetType, assetFile, HoconConfigurationLoader.builder()
                            .setPath(assetFile)
                            .build()
                            .load());

                    assets.add(new AssetContext<>(pack, asset, builder));
                }
            }
        }

        final Set<Map.Entry<Pack, List<AssetContext>>> assetEntries = this.contexualsInPack.entrySet();

        // Commence stages
        for (LoaderStage stage : this.loaderStages) {
            for (StageTask<?, ?> task : stage.getStageTasks()) {
                for (Map.Entry<Pack, List<AssetContext>> packEntry : assetEntries) {
                    for (AssetContext assetContext : packEntry.getValue()) {
                        final AssetType assetType = assetContext.getAsset().getAssetType();

                        // Don't process asset types who this stage isn't for
                        if (!stage.isAssetTypeValid(assetType)) {
                            continue;
                        }

                        // Don't process tasks that aren't for this stage
                        if (!assetType.hasDeserializationTask(task)) {
                            continue;
                        }

                        try {
                            task.execute(assetContext);
                        } catch (TaskExecutionFailedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private final class AssetFilesOnlyVisitor implements FileVisitor<Path> {

        private final Path sourcePath;
        private final AssetType assetType;
        private final String assetName;
        private final Pattern pattern;

        AssetFilesOnlyVisitor(Path sourcePath, AssetType assetType) {
            this.sourcePath = sourcePath;
            this.assetType = assetType;
            this.assetName = this.assetType.name().toLowerCase(Locale.ENGLISH);
            this.pattern = Pattern.compile(".*\\." + this.assetName + "$");
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            Almura.instance.logger.debug("Scanning pack [{}] for files matching asset type [{}].", dir, this.assetName);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            // Ignore the root of the packs folder
            final Path packSourcePath = file.getParent();
            if (packSourcePath.equals(this.sourcePath)) {
                return FileVisitResult.CONTINUE;
            }

            final String packName = packSourcePath.getFileName().toString();

            final Pack pack = AssetLoader.this.packs.computeIfAbsent(packName, v -> Pack.builder().build(Constants.Plugin.ID + ":" + packName,
                    WordUtils.capitalize(packName)));

            Almura.instance.logger.debug("Evaluating [{}] as a potential asset.", file);
            boolean matches = file.toString().matches(this.pattern.pattern());

            if (matches) {
                AssetLoader.this.assetFilesByTypeInPack.computeIfAbsent(pack, k -> new LinkedHashMap<>()).computeIfAbsent(this.assetType, k -> new
                        LinkedList<>()).add(file);
            }

            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            Almura.instance.logger.debug("[{}] does not match asset type [{}]. Skipping...", file, this.assetName, exc);

            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            if (exc != null) {
                Almura.instance.logger.error("Failed to visit [{}]! Skipping...", dir, exc);
            }

            return FileVisitResult.CONTINUE;
        }
    }
}
