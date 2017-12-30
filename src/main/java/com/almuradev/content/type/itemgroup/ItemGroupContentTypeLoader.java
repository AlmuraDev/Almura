/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.itemgroup;

import com.almuradev.almura.shared.event.Witness;
import com.almuradev.content.loader.SingleTypeContentLoader;
import net.minecraft.block.Block;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.inject.Singleton;

@Singleton
public final class ItemGroupContentTypeLoader extends SingleTypeContentLoader<ItemGroup, ItemGroup.Builder> implements Witness, SingleTypeContentLoader.Translated {

    @SubscribeEvent
    public void construction(final RegistryEvent.Register<Block> event) {
        this.build();

        // Registration is not necessary here - automatically done in constructor via mixin
    }

    @Override
    public String buildTranslationKey(final String namespace, final Iterable<String> components, final String key) {
        return "itemGroup." + namespace + '.' + key;
    }
}
