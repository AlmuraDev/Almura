/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block;

import com.almuradev.content.loader.MultiTypeContentLoader;
import com.almuradev.content.registry.ContentBuilder;
import com.almuradev.core.event.Witness;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;
import org.spongepowered.api.GameRegistry;
import org.spongepowered.api.item.ItemType;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class BlockContentTypeLoader extends MultiTypeContentLoader<BlockGenre, ContentBlock, ContentBlock.Builder<ContentBlock, BlockStateDefinition, BlockStateDefinition.Builder<BlockStateDefinition>>, BlockContentProcessor<ContentBlock, ContentBlock.Builder<ContentBlock, BlockStateDefinition, BlockStateDefinition.Builder<BlockStateDefinition>>, BlockStateDefinition, BlockStateDefinition.Builder<BlockStateDefinition>>> implements Witness, MultiTypeContentLoader.Translated<BlockGenre> {
    private final GameRegistry gr;

    @Inject
    private BlockContentTypeLoader(final GameRegistry gr) {
        this.gr = gr;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void blocks(final RegistryEvent.Register<Block> event) {
        this.build();

        final IForgeRegistry<Block> registry = event.getRegistry();
        this.entries.values().forEach(entry -> registry.register((Block) entry.value));
    }

    @SubscribeEvent
    public void items(final RegistryEvent.Register<Item> event) {
        if (this.build()) {
            this.logger.warn("Content blocks were not registered before content items");
        }

        final IForgeRegistry<Item> registry = event.getRegistry();
        this.entries.values().forEach(entry -> {
            // TODO(kashike): configurable
            if (!(entry.value instanceof ContentBlock.InInventory)) {
                return;
            }
            final Item item = ((ContentBlock.InInventory) entry.value).asBlockItem();
            registry.register(item);
        });
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void models(final ModelRegistryEvent event) {
        this.entries.values().forEach(entry -> {
            final ContentBlock block = entry.value;
            final ResourceLocation path = new ResourceLocation(
                    entry.builder.string(ContentBuilder.StringType.NAMESPACE),
                    entry.builder.string(ContentBuilder.StringType.SEMI_ABSOLUTE_PATH)
            );

            if (block instanceof SpecialBlockStateBlock) {
                ((SpecialBlockStateBlock) entry.value).blockStateDefinitionLocation(path);
            }

            if (block instanceof StateMappedBlock) {
                ModelLoader.setCustomStateMapper((Block) block, ((StateMappedBlock) block).createStateMapper());
            }

            @Nullable final ItemType item = this.gr.getType(ItemType.class, entry.value.getId()).orElse(null);
            if (item != null) {
                ModelLoader.setCustomModelResourceLocation((Item) item, 0, new ModelResourceLocation(path, "inventory"));
            }
        });
    }

    @Override
    public String buildTranslationKey(final String namespace, final BlockGenre type, final Iterable<String> components,final String key) {
        return this.buildTranslationKey("tile", namespace, type, components, key);
    }
}
