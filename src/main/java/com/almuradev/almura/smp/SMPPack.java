/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.smp;

import com.almuradev.almura.Almura;
import com.almuradev.almura.Configuration;
import com.almuradev.almura.Filesystem;
import com.almuradev.almura.blocks.yaml.YamlBlock;
import com.flowpowered.cerealization.config.ConfigurationException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
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
    private final List<YamlBlock> blocks;

    public SMPPack(String name, List<YamlBlock> blocks) {
        this.name = name;
        this.blocks = blocks;
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
        final ZipInputStream stream = new ZipInputStream(new FileInputStream(root.toFile()));
        final List<YamlBlock> blocks = new ArrayList<>();

        for (ZipEntry zipEntry; (zipEntry = stream.getNextEntry()) != null; ) {
            if (zipEntry.getName().endsWith(".yml")) {
                final InputStream entry = zipFile.getInputStream(zipEntry);
                blocks.add(YamlBlock.createFromSMPStream(zipEntry.getName().split(".yml")[0], entry));
            } else if (zipEntry.getName().endsWith(".png") && Configuration.IS_CLIENT) {
                //TODO Figure out how to separate block and items pngs
                Filesystem.writeTo(zipEntry.getName(), stream, Filesystem.ASSETS_TEXTURES_BLOCKS_SMPS_PATH);
            } else if (zipEntry.getName().endsWith(".shape") && Configuration.IS_CLIENT) {
                Filesystem.writeTo(zipEntry.getName(), stream, Filesystem.ASSETS_MODELS_BLOCKS_SHAPES_PATH);
            }

            stream.closeEntry();
        }

        stream.close();

        final String smpName = root.toFile().getName().split(".smp")[0];
        final SMPPack pack = new SMPPack(smpName, blocks);
        PACKS.put(smpName, pack);
        if (Configuration.IS_DEBUG) {
            Almura.LOGGER.info("Loaded " + pack);
        }
        return pack;
    }

    public SMPPack getPack(String name) {
        return PACKS.get(name);
    }

    public String getName() {
        return name;
    }

    public List<YamlBlock> getBlocks() {
        return Collections.unmodifiableList(blocks);
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
        return "SMPPack{name= [" + name + "], blocks= " + blocks + "}";
    }
}