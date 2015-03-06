/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack;

import com.almuradev.almura.Almura;
import com.almuradev.almura.Configuration;
import com.almuradev.almura.Filesystem;
import com.almuradev.almura.pack.model.PackModelContainer;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;

import java.io.IOException;
import java.io.InputStream;
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
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Filesystem.CONFIG_MODELS_PATH, Filesystem.MODEL_FILES_ONLY_FILTER)) {
            for (Path path : stream) {
                try {
                    String name = path.getFileName().toString();
                    boolean shape = name.endsWith(".shape");
                    name = name.split(".shape")[0];

                    final PackModelContainer modelContainer = loadModelContainer(name, path, shape);
                    MODEL_CONTAINERS.add(modelContainer);
                } catch (IOException | IOException e) {
                    Almura.LOGGER.error("Failed to load model container [" + path + "] in [" + Filesystem.CONFIG_MODELS_PATH + "].", e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed filtering model files from [" + Filesystem.CONFIG_MODELS_PATH + "].", e);
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Filesystem.CONFIG_YML_PATH, Filesystem.DIRECTORIES_ONLY_FILTER)) {
            for (Path path : stream) {
                final Pack pack = loadPack(path);
                PACKS.put(pack.getName(), pack);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed filtering folders from [" + Filesystem.CONFIG_YML_PATH + "].", e);
        }

        for (Pack pack : PACKS.values()) {
            Almura.PROXY.onCreate(pack);
        }
    }

    public static PackModelContainer loadModelContainer(String name, Path root, boolean shape) throws IOException, IOException {
        final InputStream entry = Files.newInputStream(root);
        final YamlConfiguration reader = new YamlConfiguration(entry);
        reader.load();

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

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(root, Filesystem.YML_FILES_ONLY_FILTER)) {
            for (Path path : stream) {
                streamed.add(path);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed filtering files from [" + Filesystem.CONFIG_YML_PATH + "].", e);
        }

        for (Path path : streamed) {
            try {
                final InputStream entry = Files.newInputStream(path);
                final YamlConfiguration reader = new YamlConfiguration(entry);
                reader.load();

                final String type = reader.getChild(PackKeys.TYPE.getKey()).getString(PackKeys.TYPE.getDefaultValue()).toUpperCase();

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
                        final Block container = PackCreator.createContainerBlock(pack, name, reader);
                        pack.blocks.add(container);
                        break;
                    default:
                        Almura.LOGGER
                                .warn("Unknown type [" + type + "] in file [" + path.getFileName()
                                        + "]. Valid types are [ITEM, FOOD, BLOCK, CROP, CONTAINER].");
                        continue;
                }
                entry.close();
            } catch (IOException | IOException e) {
                if (Configuration.DEBUG_MODE || Configuration.DEBUG_PACKS_MODE) {
                    Almura.LOGGER.error("Failed to load [" + path + "] for pack [" + pack.getName() + "].", e);
                } else {
                    Almura.LOGGER.error("Failed to load [" + path + "] for pack [" + pack.getName() + "].");
                }
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

    /**
     * INTERNAL USE ONLY
     */
    public void addItem(Item item) {
        items.add(item);
    }

    public List<Item> getItems() {
        return Collections.unmodifiableList(items);
    }

    @Override
    public boolean equals(Object o) {
        return this == o || !(o == null || getClass() != o.getClass()) && name.equals(((Pack) o).name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "ContentPack {name= [" + name + "], blocks= " + blocks + ", items= " + items + "}";
    }
}