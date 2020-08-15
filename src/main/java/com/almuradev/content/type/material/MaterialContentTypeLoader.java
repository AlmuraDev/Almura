/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.material;

import com.almuradev.content.loader.SingleTypeContentLoader;
import com.almuradev.core.event.Witness;
import net.minecraft.block.Block;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class MaterialContentTypeLoader extends SingleTypeContentLoader<Material, Material.Builder> implements Witness {
    private final MaterialRegistryModule module;

    @Inject
    private MaterialContentTypeLoader(final MaterialRegistryModule module) {
        this.module = module;
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void blocks(final RegistryEvent.Register<Block> event) {
        this.build();

        for (final Entry<Material, Material.Builder> entry : this.entries.values()) {
             this.module.registerAdditionalCatalog(entry.value);
        }
    }
}
