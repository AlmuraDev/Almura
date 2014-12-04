/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack;

import com.almuradev.almura.Almura;
import com.almuradev.almura.Filesystem;
import com.almuradev.almura.pack.block.PackBlock;
import com.almuradev.almura.pack.item.PackFood;
import com.almuradev.almura.pack.item.PackItem;
import com.almuradev.almura.pack.model.PackShape;
import com.flowpowered.cerealization.config.ConfigurationException;
import com.flowpowered.cerealization.config.yaml.YamlConfiguration;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.item.Item;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContentPack {

    private static final Map<String, ContentPack> PACKS = new HashMap<>();
    //TODO Better home for this
    protected static final List<PackShape> SHAPES = Lists.newArrayList();
    protected final List<Block> blocks = Lists.newArrayList();
    protected final List<Item> items = Lists.newArrayList();
    private final String name;

    public ContentPack(String name) {
        this.name = name;
    }

    public static Map<String, ContentPack> getPacks() {
        return Collections.unmodifiableMap(PACKS);
    }

    public static void loadAllContent() {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Filesystem.CONFIG_MODELS_PATH, Filesystem.MODEL_FILES_ONLY_FILTER)) {
            for (Path path : stream) {
                try {
                    loadModel(path);
                } catch (ConfigurationException e) {
                    Almura.LOGGER.error("Failed to load model [" + path + "] in [" + Filesystem.CONFIG_MODELS_PATH + "].", e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed filtering model files from [" + Filesystem.CONFIG_MODELS_PATH + "].", e);
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Filesystem.CONFIG_YML_PATH, Filesystem.DIRECTORIES_ONLY_FILTER)) {
            for (Path path : stream) {
                loadPack(path);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed filtering folders from [" + Filesystem.CONFIG_YML_PATH + "].", e);
        }

        for (ContentPack pack : PACKS.values()) {
            for (Block block : pack.blocks) {
                Almura.PROXY.onPostCreate(block);
            }

            for (Item item : pack.items) {
                Almura.PROXY.onPostCreate(item);
            }

            Almura.LOGGER.info("Loaded -> " + pack);
        }
    }

    public static void loadModel(Path root) throws ConfigurationException, IOException {
        final InputStream entry = Files.newInputStream(root);
        final YamlConfiguration reader = new YamlConfiguration(entry);
        reader.load();
        SHAPES.add(PackShape.createFromReader(root.getFileName().toString().split(".shape")[0], reader));
    }

    public static ContentPack loadPack(Path root) {
        final String smpName = root.getName(root.getNameCount() - 1).toString();
        final ContentPack pack = new ContentPack(smpName);
        PACKS.put(smpName, pack);

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(root, Filesystem.YML_FILES_ONLY_FILTER)) {
            for (Path path : stream) {
                try {
                    if (path.getFileName().toString().endsWith(".yml")) {
                        final InputStream entry = Files.newInputStream(path);
                        final YamlConfiguration reader = new YamlConfiguration(entry);
                        reader.load();

                        final String type = reader.getChild("Type").getString();
                        if (type == null) {
                            continue;
                        }

                        final String name = path.getFileName().toString().split(".yml")[0];

                        switch (type) {
                            case "Item":
                                final PackItem item = PackItem.createFromReader(pack, name, reader);
                                pack.items.add(item);
                                Almura.PROXY.onCreate(item);
                                break;
                            case "Food":
                                final PackFood food = PackFood.createFromReader(pack, name, reader);
                                pack.items.add(food);
                                Almura.PROXY.onCreate(food);
                                break;
                            case "Block":
                                final Block block = PackBlock.createFromReader(pack, name, reader);
                                pack.blocks.add(block);
                                Almura.PROXY.onCreate(block);
                                break;
                            default:
                                continue;
                        }
                        entry.close();
                    }
                } catch (ConfigurationException e) {
                    Almura.LOGGER.error("Failed to load yml [" + path + "] for pack [" + pack.getName() + "]", e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed in filtering out yml files. This could mean a critical filesystem error!", e);
        }

        return pack;
    }

    public String getName() {
        return name;
    }

    public Block getBlock(String identifier) {
        for (Block block : blocks) {
            if (block.getUnlocalizedName().equals("tile." + getName() + "." + identifier)) {
                return block;
            }
        }

        return null;
    }

    public List<Block> getBlocks() {
        return Collections.unmodifiableList(blocks);
    }

    public static List<PackShape> getShapes() {
        return Collections.unmodifiableList(SHAPES);
    }

    @Override
    public boolean equals(Object o) {
        return this == o || !(o == null || getClass() != o.getClass()) && name.equals(((ContentPack) o).name);
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