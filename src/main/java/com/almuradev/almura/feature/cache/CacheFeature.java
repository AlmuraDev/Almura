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
import com.almuradev.almura.feature.cache.client.tileentity.renderer.CacheTileEntityRenderer;
import com.almuradev.almura.shared.capability.IMultiSlotItemHandler;
import com.almuradev.almura.shared.capability.impl.MultiSlotItemHandler;
import com.almuradev.almura.shared.tileentity.SingleSlotTileEntity;
import com.almuradev.almura.shared.capability.ISingleSlotItemHandler;
import com.almuradev.almura.shared.capability.impl.SingleSlotItemHandler;
import com.almuradev.almura.shared.event.Witness;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.GameState;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;

import java.text.NumberFormat;
import java.util.Locale;

public final class CacheFeature extends Witness.Impl implements Witness.Lifecycle {

    public static final NumberFormat format = NumberFormat.getNumberInstance(Locale.US);

    @Override
    public boolean lifecycleSubscribable(GameState state) {
        return state == GameState.PRE_INITIALIZATION;
    }

    @Listener
    public void onGamePreInitialization(GamePreInitializationEvent event) {
        GameRegistry.registerTileEntity(SingleSlotTileEntity.class, Almura.ID + ":cache");

        if (FMLCommonHandler.instance().getSide().isClient()) {
            this.bindBlockRenderer();
        }
    }

    @SubscribeEvent
    public void onRegisterItems(RegistryEvent.Register<Item> event) {
        // TODO do this better

        ItemBlock item = (ItemBlock) new ItemBlock(CacheBlocks.WOOD).setRegistryName(CacheBlocks.WOOD.getRegistryName());

        event.getRegistry().register(item);

        if (FMLCommonHandler.instance().getSide().isClient()) {
            this.registerInventoryModel(item, item.getRegistryName());
        }

        item = (ItemBlock) new ItemBlock(CacheBlocks.IRON).setRegistryName(CacheBlocks.IRON.getRegistryName());

        event.getRegistry().register(item);

        if (FMLCommonHandler.instance().getSide().isClient()) {
            this.registerInventoryModel(item, item.getRegistryName());
        }

        item = (ItemBlock) new ItemBlock(CacheBlocks.GOLD).setRegistryName(CacheBlocks.GOLD.getRegistryName());

        event.getRegistry().register(item);

        if (FMLCommonHandler.instance().getSide().isClient()) {
            this.registerInventoryModel(item, item.getRegistryName());
        }

        item = (ItemBlock) new ItemBlock(CacheBlocks.DIAMOND).setRegistryName(CacheBlocks.DIAMOND.getRegistryName());

        event.getRegistry().register(item);

        if (FMLCommonHandler.instance().getSide().isClient()) {
            this.registerInventoryModel(item, item.getRegistryName());
        }

        item = (ItemBlock) new ItemBlock(CacheBlocks.NETHER).setRegistryName(CacheBlocks.NETHER.getRegistryName());

        event.getRegistry().register(item);

        if (FMLCommonHandler.instance().getSide().isClient()) {
            this.registerInventoryModel(item, item.getRegistryName());
        }

        item = (ItemBlock) new ItemBlock(CacheBlocks.ENDER).setRegistryName(CacheBlocks.ENDER.getRegistryName());

        event.getRegistry().register(item);

        if (FMLCommonHandler.instance().getSide().isClient()) {
            this.registerInventoryModel(item, item.getRegistryName());
        }
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

    @SideOnly(Side.CLIENT)
    private void bindBlockRenderer() {
        ClientRegistry.bindTileEntitySpecialRenderer(SingleSlotTileEntity.class, new CacheTileEntityRenderer());
    }

    @SideOnly(Side.CLIENT)
    private void registerInventoryModel(ItemBlock item, ResourceLocation blockStateLocation) {
        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(blockStateLocation, "inventory"));
    }


    @SubscribeEvent
    public void onPlayerCraftedItem(PlayerEvent.ItemCraftedEvent event) {
        if (event.player.getEntityWorld().isRemote) {
            return;
        }

        final IInventory craftMatrix = event.craftMatrix;
        final ItemStack result = event.crafting;
        final Item resultItemType = result.getItem();

        // We only care about caches
        if (!(resultItemType instanceof ItemBlock) || !(((ItemBlock) resultItemType).getBlock() instanceof CacheBlock)) {
            return;
        }

        final CacheBlock resultCacheBlock = (CacheBlock) ((ItemBlock) resultItemType).getBlock();

        final ItemStack cacheStack = craftMatrix.getStackInSlot(4);

        // Safety check to ensure the cache stack is in the middle
        if (cacheStack.isEmpty() || !(cacheStack.getItem() instanceof ItemBlock) || !(((ItemBlock) cacheStack.getItem()).getBlock() instanceof
                CacheBlock)) {
            return;
        }

        final NBTTagCompound compound = cacheStack.getSubCompound("tag");

        if (compound == null) {
            return;
        }

        if (!compound.hasKey("Cache")) {
            return;
        }

        final NBTTagCompound cacheCompound = compound.getCompoundTag("Cache");

        if (!cacheCompound.hasKey(Almura.ID + ":single_slot")) {
            return;
        }

        final NBTTagCompound slotCompound = cacheCompound.getCompoundTag(Almura.ID + ":single_slot");

        if (!slotCompound.hasKey("Slot")) {
            return;
        }

        // Phew, we made it...

        // Set the new slot limit
        final int newSlotLimit = resultCacheBlock.getSlotLimit();
        final NBTTagCompound newSlotCompound = slotCompound.copy();
        newSlotCompound.setInteger("SlotLimit", newSlotLimit);

        // Copy the old compound and set the new single slot compound
        final NBTTagCompound newCompound = cacheStack.getTagCompound().copy();

        // Really not clean but its late and I'll make it better later..
        newCompound.getCompoundTag("tag").getCompoundTag("Cache").setTag(Almura.ID + ":single_slot", newSlotCompound);

        result.setTagCompound(newCompound);
    }
}
