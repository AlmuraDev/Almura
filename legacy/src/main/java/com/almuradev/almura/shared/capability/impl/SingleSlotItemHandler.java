/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.capability.impl;

import com.almuradev.almura.feature.cache.asm.interfaces.IMixinItemStack;
import com.almuradev.almura.shared.capability.ISingleSlotItemHandler;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SingleSlotItemHandler implements ISingleSlotItemHandler {

    private int slotLimit;
    private ItemStack stack = ItemStack.EMPTY;

    public SingleSlotItemHandler() {
        this.slotLimit = 1;
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        if (slot != 0) {
            return ItemStack.EMPTY;
        }

        return this.stack;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        // Bail if we're inserting into a bad slot index
        if (slot != 0) {
            return stack;
        }

        // Bail if we're passed an empty stack
        if (stack.isEmpty()) {
            return stack;
        }

        int limit = this.getSlotLimit(slot);

        if (!this.stack.isEmpty()) {
            // These stacks cannot stack (mismatch) so return passed stack
            if (!ItemHandlerHelper.canItemStacksStack(stack, this.stack)) {
                return stack;
            }

            limit -= this.stack.getCount();
        }

        // If limit is 0, we can't stack anymore, return passed stack
        if (limit <= 0) {
            return stack;
        }

        boolean reachedLimit = stack.getCount() > limit;

        if (!simulate) {

            // If stored stack is empty then set stored stack up to our limit or just passed stack (if less than limit)
            if (this.stack.isEmpty()) {
                this.stack = reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, limit) : stack.copy();
            } else {
                // If stored stack is not empty, grow stack to limit
                this.stack.grow(reachedLimit ? limit : stack.getCount());
            }

            this.onSlotChange();
        }

        // Return a diff stack of the passed in stack or an empty one if we consumed the whole stack
        return reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - limit) : ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        // If amount 0 or bad slot index, return empty stack
        if (amount == 0 || slot != 0) {
            return ItemStack.EMPTY;
        }

        // If stored stack is empty, return empty stack
        if (this.stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        final int toExtract = Math.min(amount, this.getSlotLimit(slot));

        // If stored stack count is less than toExtract...
        if (this.stack.getCount() <= toExtract) {
            final ItemStack toReturn = this.stack.copy();

            // ...set stored stack to empty
            if (!simulate) {
                this.stack = ItemStack.EMPTY;

                this.onSlotChange();
            }
            return toReturn;
        } else {
            // Set stored stack size to the amount remaining if we're not simulating
            if (!simulate) {
                this.stack = ItemHandlerHelper.copyStackWithSize(this.stack, this.stack.getCount() - toExtract);

                this.onSlotChange();
            }

            // Return a stack with size set to the amount extracted
            return ItemHandlerHelper.copyStackWithSize(this.stack, toExtract);
        }
    }

    @Override
    public int getSlotLimit(int slot) {
        if (slot != 0) {
            return 0;
        }

        return this.slotLimit;
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        // Bail if a bad slot index
        if (slot != 0) {
            return;
        }

        // If we're passed an empty stack, empty the stored stack
        if (stack.isEmpty()) {
            this.stack = stack;
        } else {
            // Simulate inserting the stack
            final ItemStack resultStack = this.insertItem(slot, stack, true);

            // Bail if the result stack is equal to the passed stack as that means we couldn't insert
            if (ItemStack.areItemStacksEqual(stack, resultStack)) {
                return;
            }

            this.stack = stack;
        }

        this.onSlotChange();
    }

    public void setSlotLimit(int slotLimit) {
        this.slotLimit = slotLimit;
    }

    protected void onSlotChange() {

    }

    // Mod API
    public static final class Storage implements Capability.IStorage<ISingleSlotItemHandler> {

        @Nullable
        @Override
        public NBTBase writeNBT(Capability<ISingleSlotItemHandler> capability, ISingleSlotItemHandler instance, EnumFacing side) {
            final NBTTagCompound compound = new NBTTagCompound();
            compound.setInteger("SlotLimit", instance.getSlotLimit(0));
            final ItemStack stack = instance.getStackInSlot(0);
            if (!stack.isEmpty()) {
                final NBTTagCompound stackCompound = this.writeItemStackNBT(stack, new NBTTagCompound());
                compound.setTag("Slot", stackCompound);
            }
            return compound;
        }

        @Override
        public void readNBT(Capability<ISingleSlotItemHandler> capability, ISingleSlotItemHandler instance, EnumFacing side, NBTBase nbt) {
            final NBTTagCompound compound = (NBTTagCompound) nbt;

            if (!(instance instanceof SingleSlotItemHandler)) {
                return;
            }

            final int slotLimit = compound.getInteger("SlotLimit");
            // TODO Maybe allow this to be set in public API? I'll consider it
            ((SingleSlotItemHandler) instance).setSlotLimit(slotLimit);

            instance.setStackInSlot(0, ItemStack.EMPTY);

            if (compound.hasKey("Slot")) {
                final NBTTagCompound slotCompound = compound.getCompoundTag("Slot");

                final ItemStack stack = this.readItemStackFromNBT(slotCompound);
                if (!stack.isEmpty()) {
                    instance.setStackInSlot(0, stack);
                }
            }
        }

        private NBTTagCompound writeItemStackNBT(ItemStack stack, NBTTagCompound nbt) {
            if (stack == null || nbt == null) {
                return nbt;
            }

            ResourceLocation resourcelocation = Item.REGISTRY.getNameForObject(stack.getItem());
            nbt.setString("id", resourcelocation == null ? "minecraft:air" : resourcelocation.toString());
            nbt.setInteger("Count", stack.getCount());
            nbt.setShort("Damage", (short) stack.getItemDamage());

            if (stack.getTagCompound() != null)
            {
                nbt.setTag("tag", stack.getTagCompound());
            }

            final CapabilityDispatcher capabilities = ((IMixinItemStack) (Object) stack).getCapabilities();

            if (capabilities != null) {
                NBTTagCompound cnbt = capabilities.serializeNBT();
                // Todo: this may be wrong.
                if (!cnbt.isEmpty()) nbt.setTag("ForgeCaps", cnbt);
            }

            return nbt;
        }

        public ItemStack readItemStackFromNBT(NBTTagCompound compound) {
            if (compound == null) {
                return ItemStack.EMPTY;
            }
            final NBTTagCompound capNBT = compound.hasKey("ForgeCaps") ? compound.getCompoundTag("ForgeCaps") : null;
            final Item item = compound.hasKey("id", 8) ? Item.getByNameOrId(compound.getString("id")) : Item.getItemFromBlock(Blocks.AIR);
            final int stackSize = compound.getInteger("Count");
            final int itemDamage = Math.max(0, compound.getShort("Damage"));

            if (item == null) {
                return ItemStack.EMPTY;
            }

            final ItemStack resultStack = new ItemStack(item, stackSize, itemDamage, capNBT);

            if (compound.hasKey("tag", 10)) {
                resultStack.setTagCompound(compound.getCompoundTag("tag"));
            }

            return resultStack;
        }
    }
}
