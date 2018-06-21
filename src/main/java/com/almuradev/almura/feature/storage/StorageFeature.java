/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.storage;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.storage.block.StorageBlocks;
import com.almuradev.almura.shared.tileentity.MultiSlotTileEntity;
import com.almuradev.core.event.Witness;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.GameState;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;

import javax.inject.Inject;

public final class StorageFeature extends Witness.Impl implements Witness.Lifecycle {

    private final Almura plugin;

    @Inject
    public StorageFeature(final Almura plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean lifecycleSubscribable(GameState state) {
        return state == GameState.PRE_INITIALIZATION;
    }

    @Listener
    public void onGamePreInitialization(GamePreInitializationEvent event) {
//        NetworkRegistry.INSTANCE.registerGuiHandler(this.plugin, new CapabilityDualItemHandlerGuiHandler());
//        GameRegistry.registerTileEntity(MultiSlotTileEntity.class, Almura.ID + ":storage");
    }

    @SubscribeEvent
    public void onRegisterItems(RegistryEvent.Register<Item> event) {
        ItemBlock item = null;

//        for (int j = 0; j < 4; ++j) {
//            if (j == 0)
//                item = (ItemBlock) new ItemBlock(StorageBlocks.DOCK_CHEST).setRegistryName(StorageBlocks.DOCK_CHEST.getRegistryName());
//            if (j == 1)
//                item = (ItemBlock) new ItemBlock(StorageBlocks.BIRCH_STORAGE_CRATE).setRegistryName(StorageBlocks.BIRCH_STORAGE_CRATE.getRegistryName());
//            if (j == 2)
//                item = (ItemBlock) new ItemBlock(StorageBlocks.OAK_STORAGE_CRATE).setRegistryName(StorageBlocks.OAK_STORAGE_CRATE.getRegistryName());
//            if (j == 3)
//                item = (ItemBlock) new ItemBlock(StorageBlocks.SPRUCE_STORAGE_CRATE).setRegistryName(StorageBlocks.SPRUCE_STORAGE_CRATE.getRegistryName());
//
//            event.getRegistry().register(item);
//
//            if (FMLCommonHandler.instance().getSide().isClient()) {
//                this.registerInventoryModel(item, item.getRegistryName());
//            }
//        }
    }

    @SubscribeEvent
    public void onRegisterBlocks(RegistryEvent.Register<Block> event) {
//        event.getRegistry().register(StorageBlocks.DOCK_CHEST);
//        event.getRegistry().register(StorageBlocks.BIRCH_STORAGE_CRATE);
//        event.getRegistry().register(StorageBlocks.OAK_STORAGE_CRATE);
//        event.getRegistry().register(StorageBlocks.SPRUCE_STORAGE_CRATE);
    }

    @SideOnly(Side.CLIENT)
    private void registerInventoryModel(ItemBlock item, ResourceLocation blockStateLocation) {
        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(blockStateLocation, "inventory"));
    }
}
