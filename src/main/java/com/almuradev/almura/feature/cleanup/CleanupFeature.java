/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.cleanup;

import com.almuradev.almura.Almura;
import com.almuradev.core.event.Witness;
import net.minecraft.block.Block;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

public final class CleanupFeature implements Witness {

    private final Logger logger;

    @Inject
    public CleanupFeature(final Logger logger) {
        this.logger = logger;
    }

    @SubscribeEvent
    public void onMissingMapping(RegistryEvent.MissingMappings<Block> event) throws IOException {

        final List<RegistryEvent.MissingMappings.Mapping<Block>> missingMappings = event.getAllMappings()
                .stream()
                .filter(mapping -> mapping.key.getResourceDomain().equalsIgnoreCase(Almura.ID))
                .collect(Collectors.toList());

        // No dummies
        missingMappings.forEach(RegistryEvent.MissingMappings.Mapping::ignore);

        // Can I issue a quick friendly reminder to BACKUP YOUR LEVEL.DAT IN YOUR ROOT WORLD DIRECTORY?
        // Seriously, the following is so fragile and so dangerous, its alarming..
        final File saveFile = DimensionManager.getCurrentSaveRootDirectory();
        if (saveFile != null) {
            final Path savePath = saveFile.toPath();
            final Path levelPath = savePath.resolve("level.dat");

            if (Files.exists(levelPath)) {
                final NBTTagCompound tag;
                try(final InputStream is = Files.newInputStream(levelPath)) {
                    tag = CompressedStreamTools.readCompressed(is);
                }
                final NBTTagList ids = tag.getCompoundTag("FML").getCompoundTag("Registries").getCompoundTag("minecraft:blocks").getTagList("ids", 10);
                final Iterator<NBTBase> iterator = ids.iterator();

                while (iterator.hasNext()) {
                    final NBTTagCompound mapping = (NBTTagCompound) iterator.next();
                    if (mapping.getString("K").startsWith(Almura.ID + ":")) {
                        final ResourceLocation location = new ResourceLocation(mapping.getString("K"));
                        if (missingMappings.stream().filter(missingMapping -> missingMapping.key.equals(location)).findFirst().orElse(null) != null) {
                            this.logger.warn("Removing '{}' permanently.", location);
                            iterator.remove();
                        }
                    }
                }

                CompressedStreamTools.writeCompressed(tag, new FileOutputStream(levelPath.toFile()));
            }
        }
    }
}
