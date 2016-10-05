/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2016 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.special.block;

import com.almuradev.almura.Almura;
import com.almuradev.almura.lang.LanguageRegistry;
import com.almuradev.almura.lang.Languages;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

public final class CachesBlock extends BlockContainer {

    private final int initialStackSize;

    public CachesBlock(String unlocalizedName, String displayName, int initialStackSize, CreativeTabs tabs) {
        super(Material.rock);
        this.initialStackSize = initialStackSize;
        setUnlocalizedName(unlocalizedName);
        setTextureName(Almura.MOD_ID + ":textures/caches");
        setHardness(2.5f);
        LanguageRegistry.put(Languages.ENGLISH_AMERICAN, getUnlocalizedName() + ".name", displayName);
        if (tabs != null) {
            setCreativeTab(tabs);
        }
        GameRegistry.registerBlock(this, unlocalizedName);
    }

    @Override
    public boolean onBlockActivated(World worldIn, int x, int y, int z, EntityPlayer player, int side, float subX, float subY, float subZ) {
        if (!worldIn.isRemote) {
            final TileEntity te = worldIn.getTileEntity(x, y, z);
            if (!(te instanceof CachesTileEntity)) {
                return false;
            }

            final ItemStack cache = ((CachesTileEntity) te).getCache();
            final ItemStack held = player.getHeldItem();

            // If you right-click with empty hand, withdrawal stacks of whatever the Player inventory maximum stack size is or whatever the
            // cache has left, whichever is lower.
            if (held == null && cache != null) {
                final int cacheSize = cache.stackSize;

                ItemStack toAdd = new ItemStack(cache.getItem(), player.inventory.getInventoryStackLimit() >
                        cacheSize ? cacheSize : player.inventory.getInventoryStackLimit(), cache.getMetadata());

                if (!player.inventory.addItemStackToInventory(toAdd)) {
                    player.addChatComponentMessage(new ChatComponentText("Cannot withdrawal from cache as your inventory is full!"));
                    return false;
                } else {
                    if (toAdd.stackSize == 0) {
                        toAdd = null;
                    }

                    ((CachesTileEntity) te).setInventorySlotContents(0, toAdd);
                }

            // If you right click with filled hand and empty cache, merge it.
            } else if (held != null && cache == null) {
                final int heldStackSize = held.stackSize;
                player.setCurrentItemOrArmor(0, ((CachesTileEntity) te).mergeStackIntoSlot(held));
                player.addChatComponentMessage(new ChatComponentText("Added " + (player.getHeldItem() == null ? heldStackSize : player
                        .getHeldItem().stackSize - heldStackSize) + " to the cache."));

            // If you right click with filled hand and a cache who is not empty, merge a stack from our inventory into the cache.
            } else if (held != null) {
                // Search inventory for items that are similar to the cache (only non-matching aspect is the stack size)
                for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
                    final ItemStack slotStack = player.inventory.getStackInSlot(i);
                    if (slotStack == null) {
                        continue;
                    }

                    // Slot stack matches cache stack, merge it
                    if (slotStack.isItemEqual(cache)) {
                        final int slotStackSize = slotStack.stackSize;
                        final ItemStack remaining = ((CachesTileEntity) te).mergeStackIntoSlot(slotStack);
                        if (remaining == null) {
                            player.inventory.setInventorySlotContents(i, null);
                            player.addChatComponentMessage(new ChatComponentText("Added " + slotStackSize + " to the cache."));
                        } else {
                            player.addChatComponentMessage(new ChatComponentText("Added " + (slotStackSize - remaining.stackSize) + " to the "
                                    + "cache."));
                        }
                        break;
                    }
                }

            }

            player.openContainer.detectAndSendChanges();
        }
        return true;
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new CachesTileEntity(this.initialStackSize);
    }
}
