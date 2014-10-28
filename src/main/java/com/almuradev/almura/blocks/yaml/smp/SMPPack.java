package com.almuradev.almura.blocks.yaml.smp;

import com.almuradev.almura.Filesystem;
import com.almuradev.almura.blocks.yaml.YamlBlock;
import com.flowpowered.cerealization.config.ConfigurationException;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class SMPPack {
    private static final Map<String, SMPPack> PACKS = new HashMap<>();

    public SMPPack getPack(String name) {
        return PACKS.get(name);
    }

    public static Map<String, SMPPack> getPacks() {
        return Collections.unmodifiableMap(PACKS);
    }

    public static void create(Path root) throws IOException, ConfigurationException {
        final ZipInputStream stream = new ZipInputStream(new FileInputStream(root.toFile()));
        final List<YamlBlock> blocks = new ArrayList<>();

        for (ZipEntry zipEntry; (zipEntry = stream.getNextEntry()) != null; ) {
            // Create block for each yml, give it to the pack
            if (zipEntry.getName().endsWith(".yml")) {
                blocks.add(YamlBlock.create(zipEntry.getName().split(".yml")[0], stream));
            } else if (zipEntry.getName().endsWith(".png")) {
                //TODO Figure out how to separate block and items pngs
                Filesystem.writeTo(zipEntry.getName(), stream, Filesystem.ASSETS_TEXTURES_BLOCKS_SMPS_PATH);
            } else if (zipEntry.getName().endsWith(".shape")) {
                Filesystem.writeTo(zipEntry.getName(), stream, Filesystem.ASSETS_MODELS_BLOCKS_SHAPES_PATH);
            }

            // Close the zip stream for this entry
            stream.closeEntry();
        }

        final String smpName = root.toFile().getName().split("smp")[0];
        PACKS.put(smpName, new SMPPack(smpName, blocks));
    }

    private final String name;
    private final List<YamlBlock> blocks;

    public SMPPack(String name, List<YamlBlock> blocks) {
        this.name = name;
        this.blocks = blocks;
    }

    public String getName() {
        return name;
    }

    public List<YamlBlock> getBlocks() {
        return Collections.unmodifiableList(blocks);
    }
}