/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack;

import com.almuradev.almura.Almura;
import com.almuradev.almura.Configuration;
import com.almuradev.almura.Filesystem;
import com.almuradev.almura.pack.crop.PackCrops;
import com.almuradev.almura.pack.model.PackShape;
import com.flowpowered.cerealization.config.ConfigurationException;
import com.flowpowered.cerealization.config.yaml.YamlConfiguration;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Pack {

    private static final List<PackShape> SHAPES = Lists.newArrayList();
    private static final Map<String, Pack> PACKS = new HashMap<>();
    protected final List<Block> blocks = Lists.newArrayList();
    protected final List<Item> items = Lists.newArrayList();
    private final String name;

    public Pack(String name) {
        this.name = name;
    }

    public static Map<String, Pack> getPacks() {
        return Collections.unmodifiableMap(PACKS);
    }

    public static List<PackShape> getShapes() {
        return Collections.unmodifiableList(SHAPES);
    }

    public static void loadAllContent() {
        if (Configuration.IS_CLIENT) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(Filesystem.CONFIG_MODELS_PATH, Filesystem.MODEL_FILES_ONLY_FILTER)) {
                for (Path path : stream) {
                    try {
                        final PackShape shape = loadShape(path);
                        if (shape != null) {
                            SHAPES.add(shape);
                        }
                    } catch (IOException | ConfigurationException e) {
                        Almura.LOGGER.error("Failed to load model [" + path + "] in [" + Filesystem.CONFIG_MODELS_PATH + "].", e);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed filtering model files from [" + Filesystem.CONFIG_MODELS_PATH + "].", e);
            }
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

    public static PackShape loadShape(Path root) throws ConfigurationException, IOException {
        final InputStream entry = Files.newInputStream(root);
        final YamlConfiguration reader = new YamlConfiguration(entry);
        reader.load();
        return PackCreator.createShapeFromReader(root.getFileName().toString().split(".shape")[0], reader);
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
            throw new RuntimeException("Failed filtering yml files from [" + Filesystem.CONFIG_YML_PATH + "].", e);
        }

        Collections.sort(streamed, new Comparator<Path>() {
            @Override
            public int compare(Path o1, Path o2) {
                return o1.getFileName().toString().compareTo(o2.getFileName().toString());
            }
        });

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
                    default:
                        Almura.LOGGER
                                .warn("Unknown yml type [" + type + "] in file [" + path.getFileName() + "]. Valid types are [ITEM, FOOD, BLOCK, CROP].");
                        continue;
                }
                entry.close();
            } catch (IOException | ConfigurationException e) {
                if (Configuration.DEBUG_MODE || Configuration.DEBUG_PACKS_MODE) {
                    Almura.LOGGER.error("Failed to load yml [" + path + "] for pack [" + pack.getName() + "].", e);
                } else {
                    Almura.LOGGER.warn("Failed to load yml [" + path + "] for pack [" + pack.getName() + "].");
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
     * @param block
     */
    public void addBlock(Block block) {
        blocks.add(block);
    }

    /**
     * INTERNAL USE ONLY
     * @param item
     */
    public void addItem(Item item) {
        items.add(item);
    }

    public List<Item> getItems() {
        return Collections.unmodifiableList(items);
    }

    public void injectShapes() {
        for (Block block : blocks) {
            if (block instanceof IShapeContainer) {
                if (block instanceof PackCrops) {
                    ((PackCrops) block).setShape(null);
                    continue;
                }
                PackShape shape = null;
                for (PackShape s : SHAPES) {
                    if (s.getName().equalsIgnoreCase(((IShapeContainer) block).getShapeName())) {
                        shape = s;
                        break;
                    }
                }
                if (shape != null) {
                    ((IShapeContainer) block).setShape(shape);
                } else if (((IShapeContainer) block).getShapeName() != null && ((IShapeContainer) block).getShapeName().isEmpty() && (Configuration.DEBUG_MODE || Configuration.DEBUG_PACKS_MODE)) {
                        Almura.LOGGER
                                .warn("Shape [" + ((IShapeContainer) block).getShapeName() + "] was not found in [" + Filesystem.CONFIG_MODELS_PATH
                                        .toString() + "]. Will render as a basic cube.");
                }
            }
        }
        for (Item item : items) {
            if (item instanceof IShapeContainer) {
                PackShape shape = null;
                for (PackShape s : SHAPES) {
                    if (s.getName().equalsIgnoreCase(((IShapeContainer) item).getShapeName())) {
                        shape = s;
                        break;
                    }
                }
                if (shape != null) {
                    ((IShapeContainer) item).setShape(shape);
                } else if (((IShapeContainer) item).getShapeName() != null && ((IShapeContainer) item).getShapeName().isEmpty() && (Configuration.DEBUG_MODE || Configuration.DEBUG_PACKS_MODE)) {
                    Almura.LOGGER
                            .warn("Shape [" + ((IShapeContainer) item).getShapeName() + "] was not found in [" + Filesystem.CONFIG_MODELS_PATH
                                    .toString() + "]. Will render as a basic item.");
                }
            }
        }
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