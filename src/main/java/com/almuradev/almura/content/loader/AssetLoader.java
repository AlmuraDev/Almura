/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.loader;

import com.almuradev.almura.Constants;
import com.almuradev.almura.content.AssetType;
import com.almuradev.almura.content.Pack;
import com.almuradev.almura.content.block.BuildableBlockType;
import com.almuradev.almura.content.block.sound.BlockSoundGroup;
import com.almuradev.almura.content.item.BuildableItemType;
import com.almuradev.almura.content.item.group.ItemGroup;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;
import java.util.Map;

// TODO kashike, cleanup the duplication here a bit will ya?
public final class AssetLoader {

    private final AssetRegistry registry;

    public AssetLoader(AssetRegistry registry) {
        this.registry = registry;

        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onRegisterBlocks(RegistryEvent.Register<Block> event) {
        for (Map.Entry<Pack, List<AssetContext>> packByAssetTypeEntry : this.registry.getPackAssetContextualsFor(AssetType.BLOCK).entrySet()) {
            final Pack pack = packByAssetTypeEntry.getKey();

            for (AssetContext context : packByAssetTypeEntry.getValue()) {
                final Asset asset = context.getAsset();
                final BuildableBlockType.Builder builder = (BuildableBlockType.Builder) context.getBuilder();
                final String blockId = Constants.Plugin.ID + ":" + pack.getId() + "." + asset.getName();

                final BuildableBlockType blockType = builder.build(blockId);

                event.getRegistry().register((Block) blockType);

                context.setCatalog(blockType);

                pack.add(blockType);
            }
        }
    }

    @SubscribeEvent
    public void onRegisterItems(RegistryEvent.Register<Item> event) {
        // ItemBlocks
        for (Map.Entry<Pack, List<AssetContext>> packByAssetTypeEntry : this.registry.getPackAssetContextualsFor(AssetType.BLOCK).entrySet()) {
            final Pack pack = packByAssetTypeEntry.getKey();

            for (AssetContext context : packByAssetTypeEntry.getValue()) {
                if (context.getCatalog() == null) {
                    // TODO Likely need to warn here, should be impossible as blocks come first
                    continue;
                }

                final Block block = (Block) context.getCatalog();
                final Item item = new ItemBlock(block).setRegistryName(block.getRegistryName());

                event.getRegistry().register(item);

                pack.add((BuildableItemType) item);
            }
        }

        // Items
        for (Map.Entry<Pack, List<AssetContext>> packByAssetTypeEntry : this.registry.getPackAssetContextualsFor(AssetType.ITEM).entrySet()) {
            final Pack pack = packByAssetTypeEntry.getKey();

            for (AssetContext context : packByAssetTypeEntry.getValue()) {
                final Asset asset = context.getAsset();
                final BuildableItemType.Builder builder = (BuildableItemType.Builder) context.getBuilder();
                final String blockId = Constants.Plugin.ID + ":" + pack.getId() + "." + asset.getName();

                final BuildableItemType itemType = builder.build(blockId);

                event.getRegistry().register((Item) itemType);

                context.setCatalog(itemType);

                pack.add(itemType);
            }
        }
    }

    public void registerSpongeOnlyCatalogTypes() {
        // ItemGroups
        for (Map.Entry<Pack, List<AssetContext>> packByAssetTypeEntry : this.registry.getPackAssetContextualsFor(AssetType.ITEMGROUP).entrySet()) {
            for (AssetContext assetContext : packByAssetTypeEntry.getValue()) {
                final ItemGroup itemGroup = (ItemGroup) assetContext.getBuilder().build(Constants.Plugin.ID + ":" + assetContext.getAsset().getName
                                (), assetContext.getAsset().getName());
                assetContext.setCatalog(itemGroup);
            }
        }
    }
}
