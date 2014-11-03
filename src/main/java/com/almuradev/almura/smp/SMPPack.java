/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.smp;

import com.almuradev.almura.Almura;
import com.almuradev.almura.Configuration;
import com.almuradev.almura.Filesystem;
import com.almuradev.almura.smp.model.SMPShape;
import com.flowpowered.cerealization.config.ConfigurationException;
import com.flowpowered.cerealization.config.yaml.YamlConfiguration;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.malisis.core.renderer.element.Shape;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class SMPPack {

    private static final Map<String, SMPPack> PACKS = new HashMap<>();
    private final String name;
    protected List<SMPBlock> blocks;
    protected List<SMPShape> shapes;

    public SMPPack(String name) {
        this.name = name;
    }

    public static SMPPack getPack(String name) {
        return PACKS.get(name);
    }

    public static Map<String, SMPPack> getPacks() {
        return Collections.unmodifiableMap(PACKS);
    }

    public static void load() {
        for (Path path : Filesystem.getPaths(Filesystem.CONFIG_SMPS_PATH, "*.smp")) {
            try {
                create(path);
            } catch (ConfigurationException | IOException e) {
                if (Configuration.IS_DEBUG) {
                    Almura.LOGGER.error("Failed to load " + path + " as SMPPack", e);
                }
            }
        }
    }

    public static SMPPack create(Path root) throws IOException, ConfigurationException {
        final ZipFile zipFile = new ZipFile(root.toFile());

        final String smpName = root.toFile().getName().split(".smp")[0];
        final SMPPack pack = new SMPPack(smpName);
        PACKS.put(smpName, pack);

        final ZipInputStream stream = new ZipInputStream(new FileInputStream(root.toFile()));
        final List<SMPBlock> blocks = new ArrayList<>();
        final List<SMPShape> shapes = new ArrayList<>();

        for (ZipEntry zipEntry; (zipEntry = stream.getNextEntry()) != null; ) {
            if (zipEntry.getName().endsWith(".yml")) {
                final InputStream entry = zipFile.getInputStream(zipEntry);
                final YamlConfiguration reader = new YamlConfiguration(entry);
                reader.load();
                blocks.add(SMPBlock.createFromReader(pack, zipEntry.getName().split(".yml")[0], reader));
                entry.close();
            } else if (zipEntry.getName().endsWith(".shape")) {
                final InputStream entry = zipFile.getInputStream(zipEntry);
                final YamlConfiguration reader = new YamlConfiguration(entry);
                reader.load();
                shapes.add(SMPShape.createFromReader(zipEntry.getName().split(".shape")[0], reader));
                entry.close();
            }

            stream.closeEntry();
        }

        stream.close();

        pack.blocks = blocks;
        pack.shapes = shapes;

        if (Configuration.IS_DEBUG) {
            Almura.LOGGER.info("Loaded " + pack);
        }

        if (Configuration.IS_CLIENT) {
            for (SMPBlock block : blocks) {
                block.reloadShape();
            }
        }

        return pack;
    }

    public String getName() {
        return name;
    }

    public SMPBlock getBlock(String identifier) {
        for (SMPBlock block : blocks) {
            if (block.getUnlocalizedName().equals("tile." + getName() + "." + identifier)) {
                return block;
            }
        }

        return null;
    }

    public List<SMPBlock> getBlocks() {
        return Collections.unmodifiableList(blocks);
    }

    public List<SMPShape> getShapes() {
        return Collections.unmodifiableList(shapes);
    }

    /**
     * Refreshes shape information from the file
     */
    @SideOnly(Side.CLIENT)
    public void reloadIconsAndShape() throws IOException, ConfigurationException {
        shapes.clear();

        final Path smpFile = Paths.get(Filesystem.CONFIG_SMPS_PATH.toString(), name + ".smp");
        final ZipFile zipFile = new ZipFile(smpFile.toFile());
        final ZipInputStream stream = new ZipInputStream(new FileInputStream(smpFile.toFile()));

        for (ZipEntry zipEntry; (zipEntry = stream.getNextEntry()) != null; ) {
            if (zipEntry.getName().endsWith(".shape")) {
                final InputStream entry = zipFile.getInputStream(zipEntry);
                final YamlConfiguration reader = new YamlConfiguration(entry);
                reader.load();

                shapes.add(SMPShape.createFromReader(zipEntry.getName().split(".shape")[0], reader));
                entry.close();
            } else if (zipEntry.getName().endsWith(".yml")) {
                final SMPBlock block = getBlock(zipEntry.getName().split(".yml")[0]);
                if (block == null) {
                    continue;
                }

                final InputStream entry = zipFile.getInputStream(zipEntry);
                final YamlConfiguration reader = new YamlConfiguration(entry);
                reader.load();

                final Map<Integer, List<Integer>> texCoords = SMPUtil.extractCoordsFrom(reader);
                block.clippedIcons = null;
                block.clippedIcons = SMPUtil.generateClippedIconsFromCoords(block.getPack(), block.getIcon(0, 0), block.getTextureName(), texCoords);
                entry.close();
            }

            stream.closeEntry();
        }

        stream.close();

        if (Configuration.IS_DEBUG) {
            for (Shape s : shapes) {
                Almura.LOGGER.info("Loaded [" + s + "]");
            }
        }

        for (SMPBlock block : blocks) {
            block.reloadShape();
        }
    }

    @Override
    public boolean equals(Object o) {
        return this == o || !(o == null || getClass() != o.getClass()) && name.equals(((SMPPack) o).name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "SMPPack {name= [" + name + "], blocks= " + blocks + ", shapes= " + shapes + "}";
    }
}