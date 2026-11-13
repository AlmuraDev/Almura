/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.harvestsack;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.ItemStackHandler;

/**
 * The contents of a harvesting sack, stored on the sack stack itself (so it travels with the item and syncs with the inventory). Only
 * accepts what sack harvestable crops drop.
 */
public final class SackInventory extends ItemStackHandler {

    public static final int SIZE = 27;
    private static final String TAG = "HarvestSack";

    private final ItemStack sack;

    public SackInventory(final ItemStack sack) {
        super(SIZE);
        this.sack = sack;
        final NBTTagCompound tag = sack.getSubCompound(TAG);
        if (tag != null) {
            this.deserializeNBT(tag);
            if (this.stacks.size() != SIZE) {
                this.setSize(SIZE);
            }
        }
    }

    @Override
    public boolean isItemValid(final int slot, final ItemStack stack) {
        return HarvestSackFeature.isHarvestItem(stack);
    }

    @Override
    public ItemStack insertItem(final int slot, final ItemStack stack, final boolean simulate) {
        if (!stack.isEmpty() && !this.isItemValid(slot, stack)) {
            return stack;
        }
        return super.insertItem(slot, stack, simulate);
    }

    @Override
    protected void onContentsChanged(final int slot) {
        this.sack.setTagInfo(TAG, this.serializeNBT());
    }
}
