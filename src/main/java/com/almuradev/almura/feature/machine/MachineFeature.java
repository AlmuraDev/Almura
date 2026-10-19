/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.machine;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.machine.processing.MachineType;
import com.almuradev.almura.feature.machine.processing.ProcessingBlock;
import com.almuradev.almura.feature.machine.processing.ProcessingGuiHandler;
import com.almuradev.almura.feature.machine.washplant.WashPlantCraftingRecipe;
import com.almuradev.almura.shared.inventory.AlmuraGuiHandler;
import com.almuradev.core.event.Witness;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.SoundEvent;
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
 * Registers Almura's machines: for every {@link MachineType} (seed processor, kitchen slicer, kitchen grinder) its block, item, tile entity
 * and GUI.
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
        for (final MachineType type : MachineType.values()) {
            if (!type.available()) {
                continue;
            }
            AlmuraGuiHandler.register(this.plugin, type.guiId(), new ProcessingGuiHandler());
            GameRegistry.registerTileEntity(type.tileEntityClass(), type.id());
        }
    }

    @SubscribeEvent
    public void onRegisterBlocks(final RegistryEvent.Register<Block> event) {
        MachineBlocks.PROCESSING.values().forEach(event.getRegistry()::register);
    }

    @SubscribeEvent
    public void onRegisterSounds(final RegistryEvent.Register<SoundEvent> event) {
        for (final ProcessingBlock block : MachineBlocks.PROCESSING.values()) {
            final SoundEvent sound = block.type().sound();
            if (sound != null) {
                event.getRegistry().register(sound);
            }
        }
    }

    @SubscribeEvent
    public void onRegisterRecipes(final RegistryEvent.Register<IRecipe> event) {
        // The wash plant recipe uses IC2's items, so its class is only touched when IC2 is installed
        if (MachineType.WASHPLANT.available()) {
            WashPlantCraftingRecipe.register();
        }
    }

    @SubscribeEvent
    public void onRegisterItems(final RegistryEvent.Register<Item> event) {
        for (final ProcessingBlock block : MachineBlocks.PROCESSING.values()) {
            final ItemBlock item = (ItemBlock) new ItemBlock(block).setRegistryName(block.type().id());
            event.getRegistry().register(item);

            if (FMLCommonHandler.instance().getSide().isClient()) {
                this.registerInventoryModel(item);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    private void registerInventoryModel(final ItemBlock item) {
        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }
}
