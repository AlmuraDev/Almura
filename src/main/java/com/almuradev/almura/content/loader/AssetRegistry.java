/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.loader;

import com.almuradev.almura.Almura;
import com.almuradev.almura.Constants;
import com.almuradev.almura.content.Pack;
import com.almuradev.almura.registry.BuildableCatalogType;
import ninja.leaping.configurate.json.JSONConfigurationLoader;
import org.apache.commons.lang3.text.WordUtils;
import org.spongepowered.api.Sponge;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// TODO This works only on /run/assets/** currently. Need to have it read the jar as well
public final class AssetRegistry {

    private final Map<String, Pack> packsById = new LinkedHashMap<>();
    private final Map<Asset.Type, Map<Pack, List<Path>>> packsFilesByAssetType = new LinkedHashMap<>();
    private final Map<Pack, List<AssetContext>> contexualsInPack = new LinkedHashMap<>();
    private final Map<Asset.Type, Map<Pack, List<AssetContext>>> contextualsInPackByType = new LinkedHashMap<>();

    public void loadAssetFiles(Path sourcePath) throws IOException {

        this.packsFilesByAssetType.clear();

        for (Asset.Type assetType : Asset.Type.values()) {
            Files.walkFileTree(sourcePath, new AssetFilesOnlyVisitor(sourcePath, assetType));
        }
    }

    public void loadAssetContextuals() throws IOException {

        this.contextualsInPackByType.clear();

        for (Map.Entry<Asset.Type, Map<Pack, List<Path>>> assetTypeEntry : this.packsFilesByAssetType.entrySet()) {
            final Asset.Type assetType = assetTypeEntry.getKey();

            final Map<Pack, List<AssetContext>> assetContextualsByPack = this.contextualsInPackByType.computeIfAbsent(assetType, v -> new LinkedHashMap<>());

            for (Map.Entry<Pack, List<Path>> packEntry : assetTypeEntry.getValue().entrySet()) {
                final Pack pack = packEntry.getKey();
                final List<Path> packFiles = packEntry.getValue();

                final List<AssetContext> assetContexts = assetContextualsByPack.computeIfAbsent(pack, v -> new LinkedList<>());

                for (Path file : packFiles) {
                    final BuildableCatalogType.Builder builder = Sponge.getRegistry().createBuilder(assetType.getBuilderClass());

                    final String assetName = file.getFileName().toString().split("\\.")[0];

                    final Asset asset = new Asset(assetName, assetType, file, JSONConfigurationLoader.builder()
                            .setPath(file)
                            .build()
                            .load());

                    assetContexts.add(new AssetContext<>(pack, asset, builder));
                }

                this.contexualsInPack.computeIfAbsent(pack, v -> new LinkedList<>()).addAll(assetContexts);
            }
        }
    }

    public Map<Pack, List<AssetContext>> getPackAssetContextualsFor(Asset.Type type) {
        return Collections.unmodifiableMap(this.contextualsInPackByType.computeIfAbsent(type, k -> new LinkedHashMap<>()));
    }

    public Map<Asset.Type, Map<Pack, List<AssetContext>>> getAll() {
        return Collections.unmodifiableMap(this.contextualsInPackByType);
    }

    private final class AssetFilesOnlyVisitor implements FileVisitor<Path> {

        private final Path sourcePath;
        private final Asset.Type assetType;
        private final String assetName;

        AssetFilesOnlyVisitor(Path sourcePath, Asset.Type assetType) {
            this.sourcePath = sourcePath;
            this.assetType = assetType;
            this.assetName = this.assetType.name().toLowerCase(Locale.ENGLISH);
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            // Ignore the root of the packs folder
            final Path packSourcePath = dir.getParent();
            if (!packSourcePath.equals(this.sourcePath)) {
                Almura.instance.logger.debug("Scanning pack [{}] for files matching asset type [{}].", dir, this.assetName);
                return FileVisitResult.CONTINUE;
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            // Ignore the root of the packs folder
            final Path packSourcePath = file.getParent();
            if (packSourcePath.equals(this.sourcePath)) {
                return FileVisitResult.CONTINUE;
            }

            boolean matches = file.toString().endsWith("." + this.assetType.getExtension());

            if (!matches) {
                return FileVisitResult.CONTINUE;
            }

            final String packName = packSourcePath.getFileName().toString();

            final Pack pack = AssetRegistry.this.packsById.computeIfAbsent(packName, v -> Pack.builder().build(Constants.Plugin.ID + ":" + packName,
                    WordUtils.capitalize(packName)));

            Almura.instance.logger.debug("Evaluating [{}] as a potential asset.", file);

            AssetRegistry.this.packsFilesByAssetType.computeIfAbsent(this.assetType, k -> new LinkedHashMap<>()).computeIfAbsent(pack, k -> new
                    LinkedList<>()).add(file);

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
