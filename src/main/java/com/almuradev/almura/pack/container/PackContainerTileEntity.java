/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.container;

import com.almuradev.almura.pack.node.ContainerNode;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

public class PackContainerTileEntity extends TileEntity implements IInventory {

    private static final String TAG_STRING_TITLE = "Title";
    private static final String TAG_INT_SIZE = "Size";
    private static final String TAG_INT_MAX_STACK_SIZE = "MaxStackSize";
    private static final String TAG_LIST_ITEMS = "Items";
    private static final String TAG_BYTE_SLOT = "Slot";
    private static final String TAG_BOOLEAN_FULL = "Full";
    private ItemStack[] contents;
    private String title = null;
    private int size = Integer.MIN_VALUE;
    private int maxStackSize = Integer.MIN_VALUE;
    private boolean hasEmptySlots = true;

    public PackContainerTileEntity() {
    }

    public PackContainerTileEntity(ContainerNode containerNode) {
        this.title = containerNode.getTitle();
        this.size = containerNode.getSize();
        this.maxStackSize = containerNode.getMaxStackSize();
        contents = new ItemStack[size];
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (title == null) {
            if (compound.hasKey(TAG_STRING_TITLE)) {
                title = compound.getString(TAG_STRING_TITLE);
            } else {
                title = "";
            }
        }
        if (size == Integer.MIN_VALUE) {
            if (compound.hasKey(TAG_INT_SIZE)) {
                size = compound.getInteger(TAG_INT_SIZE);
            } else {
                size = 9;
            }
        }
        if (maxStackSize == Integer.MIN_VALUE) {
            if (compound.hasKey(TAG_INT_MAX_STACK_SIZE)) {
                maxStackSize = compound.getInteger(TAG_INT_MAX_STACK_SIZE);
            } else {
                maxStackSize = 64;
            }
        }
        contents = new ItemStack[size];
        if (compound.hasKey(TAG_LIST_ITEMS)) {
            final NBTTagList nbttaglist = compound.getTagList(TAG_LIST_ITEMS, 10);

            for (int i = 0; i < nbttaglist.tagCount(); ++i) {
                final NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
                int j = nbttagcompound1.getByte(TAG_BYTE_SLOT) & 255;

                if (j >= 0 && j < contents.length) {
                    contents[j] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
                }
            }
        }
        if (compound.hasKey(TAG_BOOLEAN_FULL)) {
            hasEmptySlots = compound.getBoolean(TAG_BOOLEAN_FULL);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setString(TAG_STRING_TITLE, title);
        compound.setInteger(TAG_INT_SIZE, size);
        compound.setInteger(TAG_INT_MAX_STACK_SIZE, maxStackSize);
        final NBTTagList nbttaglist = new NBTTagList();

        for (int i = 0; i < contents.length; ++i) {
            final ItemStack slotStack = contents[i];
            if (slotStack != null) {
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                nbttagcompound1.setByte(TAG_BYTE_SLOT, (byte) i);
                slotStack.writeToNBT(nbttagcompound1);
                nbttaglist.appendTag(nbttagcompound1);
            }
        }

        compound.setTag(TAG_LIST_ITEMS, nbttaglist);
        boolean hasEmptySlots = false;
        for (ItemStack stack : contents) {
            if (stack == null) {
                hasEmptySlots = true;
            }
        }
        this.hasEmptySlots = hasEmptySlots;
        compound.setBoolean(TAG_BOOLEAN_FULL, hasEmptySlots);
    }

    @Override
    public Packet getDescriptionPacket() {
        final NBTTagCompound sync = new NBTTagCompound();
        sync.setBoolean(TAG_BOOLEAN_FULL, hasEmptySlots);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, sync);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        if (pkt.func_148857_g().hasKey(TAG_BOOLEAN_FULL)) {
            hasEmptySlots = pkt.func_148857_g().getBoolean(TAG_BOOLEAN_FULL);

            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
    }

    @Override
    public int getSizeInventory() {
        return size;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return contents[slot];
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        final ItemStack slotStack = contents[slot];

        if (slotStack != null) {
            ItemStack itemstack;

            if (slotStack.stackSize <= amount) {
                itemstack = slotStack;
                contents[slot] = null;
                markDirty();
                return itemstack;
            } else {
                itemstack = slotStack.splitStack(amount);

                if (slotStack.stackSize == 0) {
                    contents[slot] = null;
                }

                markDirty();
                return itemstack;
            }
        } else {
            return null;
        }
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        ItemStack slotStack = contents[slot];
        if (slotStack != null) {
            contents[slot] = null;
            return slotStack;
        } else {
            return null;
        }
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        contents[slot] = stack;

        if (stack != null && stack.stackSize > this.getInventoryStackLimit()) {
            stack.stackSize = getInventoryStackLimit();
        }

        markDirty();
    }

    @Override
    public String getInventoryName() {
        return title;
    }

    @Override
    public boolean hasCustomInventoryName() {
        return title != null;
    }

    @Override
    public int getInventoryStackLimit() {
        return maxStackSize;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return worldObj.getTileEntity(xCoord, yCoord, zCoord) == this
               && player.getDistanceSq((double) xCoord + 0.5D, (double) yCoord + 0.5D, (double) zCoord + 0.5D) <= 64.0D;
    }

    @Override
    public void openInventory() {

    }

    @Override
    public void closeInventory() {
        if (!worldObj.isRemote) {
            boolean hasEmptySlots = false;

            for (ItemStack stack : contents) {
                if (stack == null) {
                    hasEmptySlots = true;
                }
            }

            this.hasEmptySlots = hasEmptySlots;
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return true;
    }

    public boolean hasEmptySlots() {
        return hasEmptySlots;
    }

    public ItemStack[] getContents() {
        return contents;
    }
}
