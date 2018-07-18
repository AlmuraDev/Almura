/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item;

import com.almuradev.content.loader.MultiTypeContentLoader;
import com.almuradev.content.registry.ResourceLocations;
import com.almuradev.core.event.Witness;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;
import org.spongepowered.api.CatalogType;

import javax.inject.Singleton;

@Singleton
public final class ItemContentTypeLoader extends MultiTypeContentLoader<ItemGenre, ContentItem, ContentItem.Builder<ContentItem>, ItemContentProcessor<ContentItem, ContentItem.Builder<ContentItem>>> implements MultiTypeContentLoader.Translated<ItemGenre>, Witness {
    private static final String NORMAL = "normal";
    private static String current = "not available";

    @SubscribeEvent
    public void items(final RegistryEvent.Register<Item> event) {
        this.build();

        final IForgeRegistry<Item> registry = event.getRegistry();
        for (final Entry<ItemGenre, ContentItem, ContentItem.Builder<ContentItem>> entry : this.entries.values()) {
            registry.register((Item) entry.value);
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void models(final ModelRegistryEvent event) {
        for (final Entry<ItemGenre, ContentItem, ContentItem.Builder<ContentItem>> entry : this.entries.values()) {
            current = entry.value.toString();
            final ModelResourceLocation mrl = fromCatalog(entry.value);
            ModelLoader.setCustomModelResourceLocation((Item) entry.value, 0, mrl);
        }
    }

    @SideOnly(Side.CLIENT)
    private static ModelResourceLocation fromCatalog(final CatalogType catalog) {
        if (catalog == null || catalog.getId() == null) {
            System.out.println("Debug - Model Loading Error for: " + current);
        }
        final String string = catalog.getId();
        return new ModelResourceLocation(
                0,
                ResourceLocations.findNamespace(string),
                ResourceLocations.findValue(string),
                NORMAL
        );
    }

    @Override
    public String buildTranslationKey(final String namespace, final ItemGenre type, final Iterable<String> components, final String key) {
        return this.buildTranslationKey("item", namespace, type, components, key);
    }
}
