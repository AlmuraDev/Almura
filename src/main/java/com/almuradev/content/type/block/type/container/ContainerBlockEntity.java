/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block.type.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

public final class ContainerBlockEntity extends TileEntity implements ISidedInventory {

    private static final String LIMIT_TAG = "Limit";
    private int limit;

    public ContainerBlockEntity(final int limit) {
        this.limit = limit;
    }

    @Override
    public void readFromNBT(final NBTTagCompound tag) {
        super.readFromNBT(tag);
        this.limit = tag.getInteger(LIMIT_TAG);
    }

    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound tag) {
        tag.setInteger(LIMIT_TAG, this.limit);
        return super.writeToNBT(tag);
    }

    @Override
    public int getSizeInventory() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public ItemStack getStackInSlot(final int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack decrStackSize(final int index, final int count) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStackFromSlot(final int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setInventorySlotContents(final int index, final ItemStack stack) {
    }

    @Override
    public int getInventoryStackLimit() {
        return this.limit;
    }

    @Override
    public boolean isUsableByPlayer(final EntityPlayer player) {
        return false;
    }

    @Override
    public void openInventory(final EntityPlayer player) {
    }

    @Override
    public void closeInventory(final EntityPlayer player) {
    }

    @Override
    public boolean isItemValidForSlot(final int index, final ItemStack stack) {
        return false;
    }

    @Override
    public int getField(final int id) {
        return 0;
    }

    @Override
    public void setField(final int id, final int value) {
    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public int[] getSlotsForFace(final EnumFacing direction) {
        return new int[0];
    }

    @Override
    public boolean canInsertItem(final int index, final ItemStack itemStackIn, final EnumFacing direction) {
        return false;
    }

    @Override
    public boolean canExtractItem(final int index, final ItemStack stack, final EnumFacing direction) {
        return false;
    }
}
