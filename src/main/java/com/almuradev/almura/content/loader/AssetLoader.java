/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.loader;

import com.almuradev.almura.Constants;
import com.almuradev.almura.content.AssetType;
import com.almuradev.almura.content.Pack;
import com.almuradev.almura.content.exception.ContentConstructionException;
import com.almuradev.almura.content.type.block.component.sound.BlockSoundGroup;
import com.almuradev.almura.content.type.block.type.BuildableBlockType;
import com.almuradev.almura.content.type.item.group.ItemGroup;
import com.almuradev.almura.content.type.item.type.BuildableItemType;
import com.almuradev.almura.registry.AlmuraModelResourceLocation;
import com.almuradev.almura.registry.BuildableCatalogType;
import com.almuradev.almura.registry.Registries;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.item.ItemType;

import java.util.List;
import java.util.Map;

public final class AssetLoader {

    private final AssetRegistry registry;

    public AssetLoader(AssetRegistry registry) {
        this.registry = registry;

        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onRegisterBlocks(RegistryEvent.Register<Block> event) {
        this.with(AssetType.BLOCK_SOUNDGROUP, (Enjoyer<BlockSoundGroup, BlockSoundGroup.Builder>) (pack, asset, builder, context) -> {
            final BlockSoundGroup group = builder.build();
            context.setCatalog(group);
            pack.add(group);
        });

        this.with(AssetType.BLOCK, (Enjoyer<BuildableBlockType, BuildableBlockType.Builder>) (pack, asset, builder, context) -> {
            final BuildableBlockType block = builder.build(pack.getId() + '/' + asset.getName());
            event.getRegistry().register((Block) block);
            context.setCatalog(block);
            pack.add(block);
        });
    }

    @SubscribeEvent
    public void onRegisterItems(RegistryEvent.Register<Item> event) {
        // ItemBlocks
        this.with(AssetType.BLOCK, (Enjoyer<BuildableBlockType, BuildableBlockType.Builder>) (pack, asset, builder, context) -> {
            if (context.getCatalog() == null) {
                // TODO Likely need to warn here, should be impossible as blocks come first
                return;
            }

            final Block block = (Block) context.getCatalog();
            final Item item = new ItemBlock(block).setRegistryName(block.getRegistryName());

            event.getRegistry().register(item);

            pack.add((BuildableItemType) item);
        });

        // Items
        this.with(AssetType.ITEM, (Enjoyer<BuildableItemType, BuildableItemType.Builder>) (pack, asset, builder, context) -> {
            final BuildableItemType item = builder.build(pack.getId() + '/' + asset.getName());
            event.getRegistry().register((Item) item);
            context.setCatalog(item);
            pack.add(item);
        });
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onRegisterModels(ModelRegistryEvent event) {
        this.with(AssetType.BLOCK, (Enjoyer<BuildableBlockType, BuildableBlockType.Builder>) (pack, asset, builder, context) -> {
            final ItemType item = Registries.findCatalog(ItemType.class, context.getCatalog().getId()).orElse(null);
            if (item != null) {
                ModelLoader.setCustomModelResourceLocation((Item) item, 0, new ModelResourceLocation(context.getCatalog().getId(),
                    "normal"));
            }
        });

        this.with(AssetType.ITEM, (Enjoyer<BuildableItemType, BuildableItemType.Builder>) (pack, asset, builder, context) -> {
            final ModelResourceLocation mrl = new AlmuraModelResourceLocation(context.getCatalog());
            ModelLoader.setCustomModelResourceLocation((Item) context.getCatalog(), 0, mrl);
        });
    }

    public void registerSpongeOnlyCatalogTypes() {
        this.with(AssetType.ITEMGROUP, (Enjoyer<ItemGroup, ItemGroup.Builder>) (pack, asset, builder, context) -> {
            final ItemGroup group = builder.build(Constants.Plugin.ID + ':' + asset.getName(), asset.getName());
            context.setCatalog(group);
        });
    }

    private <C extends BuildableCatalogType, B extends BuildableCatalogType.Builder> void with(final AssetType type, final Enjoyer<C, B> enjoyer) {
        for (final Map.Entry<Pack, List<AssetContext>> entry : this.registry.getPackAssetContextualsFor(type).entrySet()) {
            final Pack pack = entry.getKey();
            for (final AssetContext context : entry.getValue()) {
                final Asset asset = context.getAsset();
                try {
                    enjoyer.accept(pack, asset, (B) context.getBuilder(), context);
                } catch(final Exception e) {
                    final String message = "Encountered a critical exception while constructing game content."
                        + '\n' + "Asset:"
                        + '\n' + '\t' + "Name: " + asset.getName()
                        + '\n' + '\t' + "Type: " + asset.getType()
                        + '\n' + '\t' + "Path: " + asset.getPath();
                    throw new ContentConstructionException(message, e);
                }
            }
        }
    }

    private interface Enjoyer<C extends BuildableCatalogType, B extends BuildableCatalogType.Builder> {

        void accept(final Pack pack, final Asset asset, final B builder, final AssetContext<C, B> context);
    }
}
