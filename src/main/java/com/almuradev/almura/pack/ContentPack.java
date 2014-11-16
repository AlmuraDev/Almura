/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack;

import com.almuradev.almura.Almura;
import com.almuradev.almura.Configuration;
import com.almuradev.almura.Filesystem;
import com.almuradev.almura.pack.block.PackBlock;
import com.almuradev.almura.pack.block.PackModelBlock;
import com.almuradev.almura.pack.item.PackFood;
import com.almuradev.almura.pack.item.PackItem;
import com.almuradev.almura.pack.model.PackModel;
import com.almuradev.almura.pack.model.PackShape;
import com.flowpowered.cerealization.config.ConfigurationException;
import com.flowpowered.cerealization.config.yaml.YamlConfiguration;
import net.malisis.core.renderer.model.MalisisModel;
import net.minecraft.block.Block;
import net.minecraft.item.Item;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ContentPack {

    private static final Map<String, ContentPack> PACKS = new HashMap<>();
    private final String name;
    protected List<Block> blocks;
    protected List<Item> items;
    protected List<PackShape> shapes;
    protected List<PackModel> models;

    public ContentPack(String name) {
        this.name = name;
    }

    public static ContentPack getPack(String name) {
        return PACKS.get(name);
    }

    public static Map<String, ContentPack> getPacks() {
        return Collections.unmodifiableMap(PACKS);
    }

    public static void loadAllContent() {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Filesystem.CONFIG_PACKS_PATH, Filesystem.DIRECTORIES_ONLY_FILTER)) {
            for (Path path : stream) {
                try {
                    create(path);
                } catch (ConfigurationException | IOException e) {
                    if (Configuration.DEBUG_MODE) {
                        Almura.LOGGER.error("Failed to load " + path + " as a content pack", e);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ContentPack create(Path root) throws IOException, ConfigurationException {
        final String smpName = root.getName(root.getNameCount() - 1).toString();
        final ContentPack pack = new ContentPack(smpName);
        PACKS.put(smpName, pack);

        final List<Block> blocks = new LinkedList<>();
        final List<Item> items = new LinkedList<>();
        final List<PackShape> shapes = new ArrayList<>();
        final List<PackModel> models = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(root, Filesystem.PACK_FILES_ONLY_FILTER)) {
            for (Path path : stream) {
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
                            items.add(item);
                            Almura.PROXY.onCreate(item);
                            break;
                        case "Food":
                            final PackFood food = PackFood.createFromReader(pack, name, reader);
                            items.add(food);
                            Almura.PROXY.onCreate(food);
                            break;
                        case "Block":
                            final Block block;
                            if (reader.hasChild("model")) {
                                block = PackModelBlock.createFromReader(pack, name, reader);
                            } else {
                                block = PackBlock.createFromReader(pack, name, reader);
                            }
                            blocks.add(block);
                            Almura.PROXY.onCreate(block);
                            break;
                        default:
                            continue;
                    }
                    entry.close();
                } else if (path.getFileName().toString().endsWith(".shape")) {
                    final InputStream entry = Files.newInputStream(path);
                    final YamlConfiguration reader = new YamlConfiguration(entry);
                    reader.load();
                    shapes.add(PackShape.createFromReader(path.getFileName().toString().split(".shape")[0], reader));
                    entry.close();
                } else if (path.getFileName().toString().endsWith(".obj")) {
                    models.add(PackModel.createFromReader(pack, path.getFileName().toString().split(".obj")[0]));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        pack.items = items;
        pack.blocks = blocks;
        pack.shapes = shapes;
        pack.models = models;

        if (Configuration.DEBUG_MODE) {
            Almura.LOGGER.info("Loaded -> " + pack);
        }

        for (Block block : blocks) {
            Almura.PROXY.onPostCreate(block);
        }

        for (Item item : items) {
            Almura.PROXY.onPostCreate(item);
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

    public List<PackShape> getShapes() {
        return Collections.unmodifiableList(shapes);
    }

    public List<PackModel> getModels() {
        return Collections.unmodifiableList(models);
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
        return "ContentPack {name= [" + name + "], blocks= " + blocks + ", items= " + items + ", shapes= " + shapes + "}";
    }
}