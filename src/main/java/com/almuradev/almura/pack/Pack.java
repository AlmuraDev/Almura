/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2015 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack;

import com.almuradev.almura.Almura;
import com.almuradev.almura.Configuration;
import com.almuradev.almura.LogHelper;
import com.almuradev.almura.pack.model.PackModelContainer;
import com.almuradev.almura.util.FileSystem;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Pack {

    private static final List<PackModelContainer> MODEL_CONTAINERS = Lists.newArrayList();
    private static final Map<String, Pack> PACKS = new HashMap<>();
    protected final List<Block> blocks = Lists.newArrayList();
    protected final List<Item> items = Lists.newArrayList();
    private final String name;

    public Pack(String name) {
        this.name = name;
    }

    public static List<PackModelContainer> getModelContainers() {
        return Collections.unmodifiableList(MODEL_CONTAINERS);
    }

    public static Map<String, Pack> getPacks() {
        return Collections.unmodifiableMap(PACKS);
    }

    public static void loadAllContent() {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(FileSystem.CONFIG_MODELS_PATH, FileSystem.FILTER_MODEL_FILES_ONLY)) {
            for (Path path : stream) {
                try {
                    String name = path.getFileName().toString();
                    boolean shape = name.endsWith(".shape");
                    name = name.split(".shape")[0];

                    final PackModelContainer modelContainer = loadModelContainer(name, path, shape);
                    MODEL_CONTAINERS.add(modelContainer);
                } catch (IOException e) {
                    Almura.LOGGER.error("Failed to load model container [" + path + "] in [" + FileSystem.CONFIG_MODELS_PATH + "].", e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed filtering model files from [" + FileSystem.CONFIG_MODELS_PATH + "].", e);
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(FileSystem.CONFIG_YML_PATH, FileSystem.FILTER_DIRECTORIES_ONLY)) {
            for (Path path : stream) {
                final Pack pack = loadPack(path);
                PACKS.put(pack.getName(), pack);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed filtering folders from [" + FileSystem.CONFIG_YML_PATH + "].", e);
        }

        for (Pack pack : PACKS.values()) {
            Almura.PROXY.onCreate(pack);
        }
    }

    public static PackModelContainer loadModelContainer(String name, Path root, boolean shape) throws IOException {
        final ConfigurationNode reader = YAMLConfigurationLoader.builder().setFile(root.toFile()).build().load();

        final PackModelContainer modelContainer = PackCreator.createModelContainerFromReader(name, reader);
        if (Configuration.IS_CLIENT && shape) {
            PackCreator.loadShapeIntoModelContainer(modelContainer, name, reader);
        }
        return modelContainer;
    }

    public static Pack loadPack(Path root) {
        final String smpName = root.getName(root.getNameCount() - 1).toString();
        final Pack pack = new Pack(smpName);

        List<Path> streamed = Lists.newArrayList();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(root, FileSystem.FILTER_YAML_FILES_ONLY)) {
            for (Path path : stream) {
                streamed.add(path);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed filtering files from [" + FileSystem.CONFIG_YML_PATH + "].", e);
        }

        for (Path path : streamed) {
            try {
                final ConfigurationNode reader = YAMLConfigurationLoader.builder().setFile(path.toFile()).build().load();

                final String type = reader.getNode(PackKeys.TYPE.getKey()).getString(PackKeys.TYPE.getDefaultValue()).toUpperCase();

                final String name = path.getFileName().toString().split(".yml")[0];

                switch (type) {
                    case "ITEM":
                        final Item item = PackCreator.createItemFromReader(pack, name, reader);
                        pack.items.add(item);
                        break;
                    case "FOOD":
                        final ItemFood food = PackCreator.createFoodFromReader(pack, name, reader);
                        pack.items.add(food);
                        break;
                    case "BLOCK":
                        final Block block = PackCreator.createBlockFromReader(pack, name, reader);
                        pack.blocks.add(block);
                        break;
                    case "CROP":
                        final Block crop = PackCreator.createCropFromReader(pack, name, reader);
                        pack.blocks.add(crop);
                        break;
                    case "CONTAINER":
                        final Block container = PackCreator.createContainerBlockFromReader(pack, name, reader);
                        pack.blocks.add(container);
                        break;
                    case "SAPLING":
                        final Block sapling = PackCreator.createSaplingFromReader(pack, name, reader);
                        pack.blocks.add(sapling);
                        break;
                    case "LEAVES":
                        final Block leaves = PackCreator.createLeavesFromReader(pack, name, reader);
                        pack.blocks.add(leaves);
                        break;
                    case "STAIRS":
                        final Block stairs = PackCreator.createStairsFromReader(pack, name, reader);
                        pack.blocks.add(stairs);
                        break;
                    default:
                        Almura.LOGGER
                                .warn("Unknown type [" + type + "] in file [" + path.getFileName()
                                        + "]. Valid types are [ITEM, FOOD, BLOCK, CROP, CONTAINER, SAPLING, LEAVES, STAIRS].");
                }
            } catch (IOException e) {
                LogHelper.logPackWarnOrError("Failed to load [" + path + "] for pack [" + pack.getName() + "].", Optional.<Throwable>of(e));
            }
        }
        return pack;
    }

    public String getName() {
        return name;
    }

    public List<Block> getBlocks() {
        return Collections.unmodifiableList(blocks);
    }

    public List<Item> getItems() {
        return Collections.unmodifiableList(items);
    }

    /**
     * INTERNAL USE ONLY
     */
    public void addItem(Item item) {
        items.add(item);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Pack && name.equals(((Pack) o).getName());
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("name", name)
                .add("blocks", blocks)
                .add("items", items)
                .toString();
    }
}