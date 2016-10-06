/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2016 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.special.block;

import static com.google.common.base.Preconditions.checkNotNull;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

public final class CachesTileEntity extends TileEntity implements IInventory {

    public static final String TAG_CACHE = "Cache";
    public static final String TAG_CACHE_MAX_STACK_SIZE = "MaxStackSize";
    public static final String TAG_CACHE_CONTENTS = "Contents";

    // Packet-only
    private static final String TAG_CACHE_STACK_SIZE = "StackSize";

    public static final int DEFAULT_MAX_STACK_SIZE = 64;

    private ItemStack cache;
    private int maxStackSize;

    @SideOnly(Side.CLIENT)
    private int stackSize;

    @SideOnly(Side.CLIENT)
    private boolean isEmpty;

    @SideOnly(Side.CLIENT)
    private boolean hasContents;

    @SideOnly(Side.CLIENT)
    private boolean isFull;

    public CachesTileEntity() {
        this.maxStackSize = DEFAULT_MAX_STACK_SIZE; //fallback
    }

    public CachesTileEntity(int maxStackSize) {
        this.maxStackSize = maxStackSize;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        if (compound.hasKey(TAG_CACHE)) {
            final NBTTagCompound cacheCompound = compound.getCompoundTag(TAG_CACHE);
            if (cacheCompound.hasKey(TAG_CACHE_MAX_STACK_SIZE)) {
                this.maxStackSize = cacheCompound.getInteger(TAG_CACHE_MAX_STACK_SIZE);
            }
            if (cacheCompound.hasKey(TAG_CACHE_CONTENTS)) {
                this.cache = ItemStack.loadItemStackFromNBT(cacheCompound.getCompoundTag(TAG_CACHE_CONTENTS));
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);

        if (this.cache != null) {
            final NBTTagCompound cacheCompound = new NBTTagCompound();
            cacheCompound.setInteger(TAG_CACHE_MAX_STACK_SIZE, this.maxStackSize);
            final NBTTagCompound cacheContentsCompound = new NBTTagCompound();
            this.cache.writeToNBT(cacheContentsCompound);
            cacheCompound.setTag(TAG_CACHE_CONTENTS, cacheContentsCompound);
            compound.setTag(TAG_CACHE, cacheCompound);
        }
    }

    @Override
    public void markDirty() {
        if (!this.worldObj.isRemote) {
            this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
        }
        super.markDirty();
    }

    @Override
    public Packet getDescriptionPacket() {
        final NBTTagCompound sync = new NBTTagCompound();
        sync.setInteger(TAG_CACHE_STACK_SIZE, this.cache == null ? -1 : this.cache.stackSize);
        sync.setInteger(TAG_CACHE_MAX_STACK_SIZE, this.maxStackSize);
        return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 0, sync);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        if (pkt.getNbtCompound().hasKey(TAG_CACHE_STACK_SIZE)) {
            this.stackSize = pkt.getNbtCompound().getInteger(TAG_CACHE_STACK_SIZE);
        } else {
            this.stackSize = -1;
        }
        if (pkt.getNbtCompound().hasKey(TAG_CACHE_MAX_STACK_SIZE)) {
            this.maxStackSize = pkt.getNbtCompound().getInteger(TAG_CACHE_MAX_STACK_SIZE);
        } else {
            this.maxStackSize = 0;
        }

        this.isEmpty = this.stackSize == -1;
        this.hasContents = !this.isEmpty;
        this.isFull = this.stackSize == this.maxStackSize;

        this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
    }

    @Override
    public int getSizeInventory() {
        return 1;
    }

    @Override
    public ItemStack getStackInSlot(int slotIn) {
        if (slotIn == 0) {
            return null;
        }

        return this.cache;
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        if (index != 0) {
            return null;
        }

        ItemStack ret = this.cache;
        if (count >= cache.stackSize) {
            this.cache = null;
        } else {
            ret = ret.splitStack(count);
        }

        this.markDirty();
        return ret;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int index) {
        return null;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        if (index == 0) {
            boolean isDirty = stack != this.cache || (this.cache != null && this.cache.stackSize != stack.stackSize);
            this.cache = stack;

            if (isDirty) {
                this.markDirty();
            }
        }
    }

    @Override
    public String getInventoryName() {
        return null;
    }

    @Override
    public boolean isCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return this.maxStackSize; // TODO Configurable
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return worldObj.getTileEntity(xCoord, yCoord, zCoord) == this
                && player.getDistanceSq((double) xCoord + 0.5D, (double) yCoord + 0.5D, (double) zCoord + 0.5D) <= 64.0D;
    }

    @Override
    public void openChest() {

    }

    @Override
    public void closeChest() {
        if (!worldObj.isRemote) {
            markDirty();
        }
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        if (index != 0) {
            return false;
        }

        // Don't let automated systems overwrite cache stack
        return this.cache == null || this.cache.isItemEqual(stack);
    }

    ItemStack getCache() {
        return this.cache;
    }

    @SideOnly(Side.CLIENT)
    public int getServerStackSize() {
        return this.stackSize;
    }

    @SideOnly(Side.CLIENT)
    public int getServerMaxStackSize() {
        return this.maxStackSize;
    }

    @SideOnly(Side.CLIENT)
    public boolean isEmptyOnServer() {
        return this.isEmpty;
    }

    @SideOnly(Side.CLIENT)
    public boolean hasContentsOnServer() {
        return this.hasContents;
    }

    @SideOnly(Side.CLIENT)
    public boolean isFullOnServer() {
        return this.isFull;
    }

    protected void setInventoryStackLimit(int limit) {
        this.maxStackSize = limit;
    }

    /**
     * Merges the provided {@link ItemStack} into this cache.
     *
     * <p>
     * Return values are:
     * - Cache is null then stack is consumed and we return null.
     * - Merging stack size + cache stack size is greater than {@link IInventory#getInventoryStackLimit()}, return stack with remainder
     * - If former and remainder is lesser than or equal to 0, return null.
     *
     * @param toMerge The stack to merge
     * @return See above
     */
    public ItemStack mergeStackIntoSlot(ItemStack toMerge) {
        checkNotNull(toMerge);

        if (this.cache == null) {
            this.setInventorySlotContents(0, toMerge);
            return null;
        }

        if (toMerge.stackSize + this.cache.stackSize > this.getInventoryStackLimit()) {
            this.cache.stackSize = this.getInventoryStackLimit();
            toMerge.stackSize = (this.cache.stackSize + toMerge.stackSize) - this.getInventoryStackLimit();
        } else {
            this.cache.stackSize += toMerge.stackSize;
            toMerge.stackSize -= toMerge.stackSize;
        }

        if (toMerge.stackSize == 0) {
            return null;
        }

        return toMerge;
    }
}
