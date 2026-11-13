/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.cache.recipe;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.cache.block.CacheBlock;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;

public final class CacheRecipe extends ShapedRecipes {
    public CacheRecipe(final String group, final int width, final int height, final NonNullList<Ingredient> ingredients, final ItemStack result) {
        super(group, width, height, ingredients, result);
    }

    @Override
    public ItemStack getCraftingResult(final InventoryCrafting inv) {
        final ItemStack ret = super.getCraftingResult(inv);
        modifyStack(inv, ret);
        return ret;
    }

    private void modifyStack(final InventoryCrafting inventory, final ItemStack result) {
        ItemStack cacheStack = ItemStack.EMPTY;

        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            final ItemStack inSlot = inventory.getStackInSlot(i);
            if (inSlot.getItem() instanceof ItemBlock && ((ItemBlock) inSlot.getItem()).getBlock() instanceof CacheBlock) {
                cacheStack = inSlot;
                break;
            }
        }

        if (cacheStack.isEmpty()) {
            return;
        }

        CacheBlock resultCacheBlock = (CacheBlock) ((ItemBlock) result.getItem()).getBlock();

        final NBTTagCompound root = cacheStack.getTagCompound();

        if (root == null || !root.hasKey("tag")) {
            return;
        }

        final NBTTagCompound compound = root.getCompoundTag("tag");

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
