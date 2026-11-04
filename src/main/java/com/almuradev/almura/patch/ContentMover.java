/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.patch;

import com.almuradev.almura.Almura;
import com.almuradev.content.loader.RootContentLoader;
import com.almuradev.core.event.Witness;
import com.google.gson.Gson;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContentMover implements Witness {
  private static final String EXPECTED_NAMESPACE = Almura.ID;
  private static final String MIGRATIONS_FILE = "migrations.json";
  private static final Gson GSON = new Gson();
  private Map<String, String> migrations = new HashMap<>();
  @Contract(pure = true)
  @Inject
  public ContentMover(@Named("assets") final @NotNull Path assets, final Logger logger) {
    final Path file = assets.resolve(RootContentLoader.FILESYSTEM_ASSETS).resolve(MIGRATIONS_FILE);
    try {
      this.migrations = GSON.fromJson(Files.newBufferedReader(file), ContentMover.class).migrations;
    } catch (IOException e) {
      logger.warn("Unable to read block migration file", e);
    }
  }

  @SubscribeEvent
  public void remapBlocks(@NotNull final RegistryEvent.MissingMappings<Block> event) {
    this.remap(event.getRegistry(), event.getMappings(), this.migrations);
  }

  @SubscribeEvent
  public void remapItems(@NotNull final RegistryEvent.MissingMappings<Item> event) {
    this.remap(event.getRegistry(), event.getMappings(), this.migrations);
  }

  private <T extends IForgeRegistryEntry<T>> void remap(final IForgeRegistry<T> registry, final @NotNull List<RegistryEvent.MissingMappings.Mapping<T>> mappings, final Map<String, String> map) {
    for(final RegistryEvent.MissingMappings.Mapping<T> mapping : mappings) {
      final ResourceLocation oldKey = mapping.key;
      if(!oldKey.getNamespace().equals(EXPECTED_NAMESPACE)) {
        continue;
      }
      final String newKey = map.get(oldKey.toString());
      if(newKey != null) {
        final T newValue = registry.getValue(new ResourceLocation(newKey));
        if(newValue != null) {
          mapping.remap(newValue);
        } else {
          mapping.fail();
        }
      }
    }
  }
}
