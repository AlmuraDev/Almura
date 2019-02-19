/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.patch;

import com.almuradev.almura.Almura;
import com.almuradev.core.event.Witness;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.List;
import java.util.Map;

public class ContentMover implements Witness {
  private static final String EXPECTED_NAMESPACE = Almura.ID;
  private static final Map<String, String> BLOCKS = ImmutableMap.<String, String>builder()
    .build();

  @SubscribeEvent
  public void remapBlocks(final RegistryEvent.MissingMappings<Block> event) {
    this.remap(event.getRegistry(), event.getMappings(), BLOCKS);
  }

  private <T extends IForgeRegistryEntry<T>> void remap(final IForgeRegistry<T> registry, final List<RegistryEvent.MissingMappings.Mapping<T>> mappings, final Map<String, String> map) {
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
