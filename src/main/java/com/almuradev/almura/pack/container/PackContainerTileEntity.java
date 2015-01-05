package com.almuradev.almura.pack.container;

import com.almuradev.almura.pack.node.ContainerNode;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;

public class PackContainerTileEntity extends TileEntity implements IInventory {
    private static final String TAG_LIST_ITEMS = "Items";
    private static final String TAG_BYTE_SLOT = "Slot";
    private final ContainerNode containerNode;
    private final ItemStack[] contents;

    public PackContainerTileEntity(ContainerNode containerNode) {
        this.containerNode = containerNode;
        contents = new ItemStack[containerNode.getSize() + 9];
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        final NBTTagList nbttaglist = compound.getTagList(TAG_LIST_ITEMS, 10);

        for (int i = 0; i < nbttaglist.tagCount(); ++i) {
            final NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
            int j = nbttagcompound1.getByte(TAG_BYTE_SLOT) & 255;

            if (j >= 0 && j < containerNode.getSize()) {
                contents[i] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);

        final NBTTagList nbttaglist = new NBTTagList();

        for (int i = 0; i < contents.length; ++i) {
            final ItemStack slotStack = contents[i];
            if (slotStack != null) {
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                nbttagcompound1.setByte(TAG_BYTE_SLOT, (byte)i);
                slotStack.writeToNBT(nbttagcompound1);
                nbttaglist.appendTag(nbttagcompound1);
            }
        }

        compound.setTag(TAG_LIST_ITEMS, nbttaglist);
    }

    @Override
    public int getSizeInventory() {
        return containerNode.getSize();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return contents[slot];
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        final ItemStack slotStack = contents[slot];

        if (slotStack != null)
        {
            ItemStack itemstack;

            if (slotStack.stackSize <= amount)
            {
                itemstack = slotStack;
                contents[slot] = null;
                this.markDirty();
                return itemstack;
            }
            else
            {
                itemstack = slotStack.splitStack(amount);

                if (slotStack.stackSize == 0)
                {
                    contents[slot] = null;
                }

                this.markDirty();
                return itemstack;
            }
        }
        else
        {
            return null;
        }
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        ItemStack slotStack = contents[slot];
        if (slotStack != null) {
            contents[slot] = null;
            return slotStack;
        }
        else {
            return null;
        }
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        contents[slot] = stack;

        if (stack != null && stack.stackSize > this.getInventoryStackLimit()) {
            stack.stackSize = this.getInventoryStackLimit();
        }

        this.markDirty();
    }

    @Override
    public String getInventoryName() {
        return containerNode.getTitle();
    }

    @Override
    public boolean hasCustomInventoryName() {
        return containerNode.useDisplayNameOfContainerAsTitle();
    }

    @Override
    public int getInventoryStackLimit() {
        return containerNode.getMaxStackSize();
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return this.worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord) == this && player.getDistanceSq((double) this.xCoord + 0.5D, (double) this.yCoord + 0.5D, (double) this.zCoord + 0.5D) <= 64.0D;
    }

    @Override
    public void openInventory() {

    }

    @Override
    public void closeInventory() {

    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return true;
    }
}
