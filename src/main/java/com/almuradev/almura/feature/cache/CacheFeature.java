/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.cache;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.cache.block.CacheBlock;
import com.almuradev.almura.feature.cache.block.CacheBlocks;
import com.almuradev.almura.feature.cache.client.tileentity.renderer.CacheItemRenderer;
import com.almuradev.almura.feature.cache.client.tileentity.renderer.CacheTileEntityRenderer;
import com.almuradev.almura.shared.tileentity.SingleSlotTileEntity;
import com.almuradev.core.event.Witness;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.GameState;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameLoadCompleteEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;

import java.text.NumberFormat;
import java.util.Locale;

import static java.util.Objects.requireNonNull;

public final class CacheFeature extends Witness.Impl implements Witness.Lifecycle {

    public static final NumberFormat format = NumberFormat.getNumberInstance(Locale.US);

    @Override
    public boolean lifecycleSubscribable(GameState state) {
        return state == GameState.PRE_INITIALIZATION;
    }

    @Listener
    public void onGamePreInitialization(GamePreInitializationEvent event) {
        GameRegistry.registerTileEntity(SingleSlotTileEntity.class, new ResourceLocation(Almura.ID, "cache"));

        if (FMLCommonHandler.instance().getSide().isClient()) {
            this.bindBlockRenderer();
        }
    }

    @SubscribeEvent
    public void onRegisterItems(final RegistryEvent.Register<Item> event) {
        registerItem(event, CacheBlocks.WOOD);
        registerItem(event, CacheBlocks.IRON);
        registerItem(event, CacheBlocks.GOLD);
        registerItem(event, CacheBlocks.DIAMOND);
        registerItem(event, CacheBlocks.NETHER);
        registerItem(event, CacheBlocks.ENDER);
    }

    private void registerItem(final RegistryEvent.Register<Item> event, final CacheBlock cacheBlock) {
        final ResourceLocation registryName = requireNonNull(cacheBlock.getRegistryName());
        final ItemBlock item = (ItemBlock) new ItemBlock(cacheBlock).setRegistryName(registryName);

        event.getRegistry().register(item);
    }

    @SubscribeEvent
    public void onRegisterBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(CacheBlocks.WOOD);
        event.getRegistry().register(CacheBlocks.IRON);
        event.getRegistry().register(CacheBlocks.GOLD);
        event.getRegistry().register(CacheBlocks.DIAMOND);
        event.getRegistry().register(CacheBlocks.NETHER);
        event.getRegistry().register(CacheBlocks.ENDER);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onModelRegistry(ModelRegistryEvent event) {
        registerModel(Item.getItemFromBlock(CacheBlocks.WOOD));
        registerModel(Item.getItemFromBlock(CacheBlocks.IRON));
        registerModel(Item.getItemFromBlock(CacheBlocks.GOLD));
        registerModel(Item.getItemFromBlock(CacheBlocks.DIAMOND));
        registerModel(Item.getItemFromBlock(CacheBlocks.NETHER));
        registerModel(Item.getItemFromBlock(CacheBlocks.ENDER));
    }

    private void registerModel(Item item) {
        item.setTileEntityItemStackRenderer(CacheItemRenderer.INSTANCE);
        this.registerInventoryModel(item, requireNonNull(item.getRegistryName()));
    }

    @SideOnly(Side.CLIENT)
    private void bindBlockRenderer() {
        ClientRegistry.bindTileEntitySpecialRenderer(SingleSlotTileEntity.class, new CacheTileEntityRenderer());
    }

    @SideOnly(Side.CLIENT)
    private void registerInventoryModel(Item item, ResourceLocation blockName) {
        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(blockName, "inventory"));
    }

    @SideOnly(Side.CLIENT)
    @Listener
    public void loadComplete(GameLoadCompleteEvent event) {
        IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(new ItemStack(CacheBlocks.DIAMOND), null, null);
        if (!model.isBuiltInRenderer()) {
            throw new RuntimeException("NOT Builtin");
        }
    }
}
