/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.machine;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.machine.seedprocessor.SeedProcessorBlock;
import com.almuradev.almura.feature.machine.seedprocessor.SeedProcessorGuiHandler;
import com.almuradev.almura.feature.machine.seedprocessor.SeedProcessorTileEntity;
import com.almuradev.almura.shared.inventory.AlmuraGuiHandler;
import com.almuradev.core.event.Witness;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.GameState;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;

import javax.inject.Inject;

/**
 * Registers Almura's machines: the seed processor block, its item, tile entity and GUI.
 */
public final class MachineFeature extends Witness.Impl implements Witness.Lifecycle {

    private final Almura plugin;

    @Inject
    public MachineFeature(final Almura plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean lifecycleSubscribable(final GameState state) {
        return state == GameState.PRE_INITIALIZATION;
    }

    @Listener
    public void onGamePreInitialization(final GamePreInitializationEvent event) {
        AlmuraGuiHandler.register(this.plugin, AlmuraGuiHandler.SEED_PROCESSOR, new SeedProcessorGuiHandler());
        GameRegistry.registerTileEntity(SeedProcessorTileEntity.class, SeedProcessorBlock.ID);
    }

    @SubscribeEvent
    public void onRegisterBlocks(final RegistryEvent.Register<Block> event) {
        event.getRegistry().register(MachineBlocks.SEED_PROCESSOR);
    }

    @SubscribeEvent
    public void onRegisterItems(final RegistryEvent.Register<Item> event) {
        final ItemBlock item = (ItemBlock) new ItemBlock(MachineBlocks.SEED_PROCESSOR).setRegistryName(SeedProcessorBlock.ID);
        event.getRegistry().register(item);

        if (FMLCommonHandler.instance().getSide().isClient()) {
            this.registerInventoryModel(item);
        }
    }

    @SideOnly(Side.CLIENT)
    private void registerInventoryModel(final ItemBlock item) {
        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(SeedProcessorBlock.ID, "inventory"));
    }
}
