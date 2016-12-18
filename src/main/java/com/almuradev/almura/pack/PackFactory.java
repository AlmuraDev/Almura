/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack;

import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.almura.Almura;
import com.almuradev.almura.FileSystem;
import com.almuradev.almura.api.BuildableCatalogType;
import com.almuradev.almura.api.block.BuildableBlockType;
import com.almuradev.almura.api.creativetab.CreativeTab;
import com.almuradev.almura.configuration.MappedConfigurationAdapter;
import com.almuradev.almura.configuration.serializer.CreativeTabSerializer;
import com.almuradev.almura.configuration.type.BlockConfiguration;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.api.Sponge;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

public final class PackFactory {

    // BLOCK -> (BASIC, BuildableCatalogType.Builder)
    private final Map<PackFileType, Set<Pair<PackLogicType, ? super BuildableCatalogType.Builder>>> buildersByType = new HashMap<>();
    private final Map<String, Pack> packsById = new HashMap<>();

    public <T extends BuildableCatalogType.Builder> void registerBuilder(PackFileType fileType, PackLogicType logicType, Class<T> builderClass,
            Supplier<? extends T> supplier) {
        final T builder = supplier.get();

//        if (!builder.getClass().equals(builderClass)) {
//            throw new IllegalArgumentException("Supplier object must be of same type as builder!");
//        }

        Set<Pair<PackLogicType, ? super BuildableCatalogType.Builder>> builders = buildersByType.get(fileType);
        if (builders == null) {
            builders = new HashSet<>();
            buildersByType.put(fileType, builders);
        }

        builders.add(new ImmutablePair<>(logicType, supplier.get()));
        Sponge.getRegistry().registerBuilderSupplier(builderClass, supplier);
    }

    public <T extends BuildableCatalogType.Builder> Optional<T> get(PackFileType fileType, PackLogicType logicType) {
        final Set<Pair<PackLogicType, ? super BuildableCatalogType.Builder>> builders = this.buildersByType.get(fileType);
        Pair<PackLogicType, ? super BuildableCatalogType.Builder> builder = null;
        if (builders != null) {
            for (Pair<PackLogicType, ? super BuildableCatalogType.Builder> current : builders) {
                if (current.getLeft().equals(logicType)) {
                    builder = current;
                    break;
                }
            }
        }

        return Optional.ofNullable((T) (builder == null ? null : builder.getRight()));
    }

    public void loadPacks(Path rootDirectory) {
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
                final String filetype = candidate.getFileName().toString().split("\\.")[1].toUpperCase();
                final PackFileType type = PackFileType.from(filetype).orElse(null);

                if (type != null) {
                    switch (type) {
                        case BLOCK:
                            try {
                                final BuildableBlockType blockType = createBlockType(candidate);
                                // TODO Log null state
                                if (blockType != null) {
                                    builder.object(blockType);
                                }
                            } catch (IOException | ObjectMappingException ex) {
                                ex.printStackTrace();
                            }
                    }
                }
            }

            final Pack pack = builder.build(candidatesByPackEntry.getKey(), candidatesByPackEntry.getKey());
            packsById.put(candidatesByPackEntry.getKey(), pack);
        }
    }

    private BuildableBlockType createBlockType(Path file) throws IOException, ObjectMappingException {
        final ConfigurationOptions options = ConfigurationOptions.defaults().setSerializers(TypeSerializers.getDefaultSerializers().newChild()
                .registerType(TypeToken.of(CreativeTab.class), new CreativeTabSerializer()));
        final MappedConfigurationAdapter<BlockConfiguration> adapter = new MappedConfigurationAdapter<>(BlockConfiguration.class, options, file);
        adapter.load();

        final String parentName = file.getParent().getFileName().toString();
        final String fileName = file.getFileName().toString().split("\\.")[0];

        final BlockConfiguration configuration = adapter.getConfig();

        // TODO Make a factory object that determines the builder and uses common code
        final PackLogicType logicType = PackLogicType.from(configuration.general.type).orElse(null);
        if (logicType == null) {
            return null;
        }

        final Optional<BuildableBlockType.Builder> optBuilder = this.get(PackFileType.BLOCK, logicType);

        if (!optBuilder.isPresent()) {
            return null;
        }

        final BuildableBlockType.Builder builder = optBuilder.get();

        builder
                .unlocalizedName(parentName + "." + fileName)
                .hardness(configuration.general.hardness)
                .resistance(configuration.general.resistance);

        if (configuration.general.creativeTab.enabled) {
            builder.creativeTab(configuration.general.creativeTab.tab);
        }

        return builder.build(parentName + "/" + fileName, configuration.general.title);
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
            final String extension = file.getFileName().toString().split("\\.")[1];

            if (PackFileType.from(extension).isPresent()) {
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
