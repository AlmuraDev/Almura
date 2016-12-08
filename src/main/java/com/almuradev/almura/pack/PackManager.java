/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack;

import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.almura.Almura;
import com.almuradev.almura.FileSystem;
import com.almuradev.almura.api.block.BuildableBlockType;
import com.almuradev.almura.api.creativetab.CreativeTab;
import com.almuradev.almura.configuration.MappedConfigurationAdapter;
import com.almuradev.almura.configuration.serializer.CreativeTabSerializer;
import com.almuradev.almura.configuration.type.BlockConfiguration;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class PackManager {

    static final Map<String, Pack> packsById = new HashMap<>();

    public static void loadPacks(Path rootDirectory) {
        checkNotNull(rootDirectory);

        final CatalogTypeFileVisitor visitor = new CatalogTypeFileVisitor();
        try {
            Files.walkFileTree(rootDirectory, visitor);
        } catch (IOException ioe) {
            Almura.instance.logger.error("Exception caught walking file tree [{}]!", rootDirectory.getFileName().toString(), ioe);
            return;
        }

        for (Map.Entry<String, List<Path>> candidatesByPackEntry : visitor.getCandidates().entrySet()) {
            final Pack.Builder builder = Pack.builder();

            for (Path candidate : candidatesByPackEntry.getValue()) {
                switch (candidate.getFileName().toString().split("\\.")[1].toUpperCase()) {
                    case "BLOCK":
                        try {
                            builder.object(createBlockType(candidate));
                        } catch (IOException | ObjectMappingException ex) {
                            ex.printStackTrace();
                        }
                }
            }

            final Pack pack = builder.build(candidatesByPackEntry.getKey(), candidatesByPackEntry.getKey());
            packsById.put(candidatesByPackEntry.getKey(), pack);
        }
    }

    private static BuildableBlockType createBlockType(Path file) throws IOException, ObjectMappingException {
        final ConfigurationOptions options = ConfigurationOptions.defaults().setSerializers(TypeSerializers.getDefaultSerializers().newChild()
                .registerType(TypeToken.of(CreativeTab.class), new CreativeTabSerializer()));
        final MappedConfigurationAdapter<BlockConfiguration> adapter = new MappedConfigurationAdapter<>(BlockConfiguration.class, options, file);
        adapter.load();

        final String parentName = file.getParent().getFileName().toString();
        final String fileName = file.getFileName().toString().split("\\.")[0];

        final BlockConfiguration configuration = adapter.getConfig();
        final BuildableBlockType.Builder builder = BuildableBlockType.builder()
                .unlocalizedName(parentName + "." + fileName)
                .hardness(configuration.general.hardness)
                .resistance(configuration.general.resistance);
        if (configuration.general.creativeTab.enabled) {
            builder.creativeTab(configuration.general.creativeTab.tab);
        }
        return builder.build(parentName + "/" + fileName, configuration.general.title);
    }

    private static boolean isValidLogicCandidateType(Path file) {
        final String extension = file.getFileName().toString().split("\\.")[1];
        return "block".equalsIgnoreCase(extension);
    }

    private static final class CatalogTypeFileVisitor implements FileVisitor<Path> {

        final Map<String, List<Path>> candidatesByPack = new HashMap<>();

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            if (!dir.equals(FileSystem.PATH_CONFIG_PACKS)) {
                Almura.instance.logger.info("Reading pack [{}] for catalog type candidates...", dir.getFileName().toString());
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Almura.instance.logger.info("Reading catalog type candidate [{}]...", file.getFileName().toString());
            if (isValidLogicCandidateType(file)) {
                final String packName = file.getParent().getFileName().toString();
                final List<Path> candidates = this.candidatesByPack.getOrDefault(packName, new LinkedList<>());
                if (candidates.isEmpty()) {
                    this.candidatesByPack.put(packName, candidates);
                }
                candidates.add(file);
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            Almura.instance.logger.error("An error occurred reading catalog type candidate [{}]!", file.getFileName().toString(), exc);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            if (!dir.equals(FileSystem.PATH_CONFIG_PACKS)) {
                final List<Path> candidates = this.candidatesByPack.getOrDefault(dir.getFileName().toString(), new LinkedList<>());
                Almura.instance.logger.info("Found [{}] catalog type(s).", candidates.size());
            }
            return FileVisitResult.CONTINUE;
        }

        Map<String, List<Path>> getCandidates() {
            return this.candidatesByPack;
        }
    }
}
