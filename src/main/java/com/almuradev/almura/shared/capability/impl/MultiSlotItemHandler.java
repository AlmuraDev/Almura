/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.capability.impl;

import com.almuradev.almura.shared.capability.IMultiSlotItemHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;

public class MultiSlotItemHandler extends ItemStackHandler implements IMultiSlotItemHandler {

    @Override
    public void resize(int slotCount) {
        final NonNullList<ItemStack> stacks = this.stacks;
        this.setSize(slotCount);
        for (int i = 0; i < slotCount; i++) {
            if (i >= stacks.size()) {
                break;
            }

            final ItemStack stack = stacks.get(i);
            this.stacks.set(i, stack);
        }
    }

    public static final class Storage implements Capability.IStorage<IMultiSlotItemHandler> {

        @Nullable
        @Override
        public NBTBase writeNBT(Capability<IMultiSlotItemHandler> capability, IMultiSlotItemHandler instance, EnumFacing side) {

            final int slotCount = instance.getSlots();

            final NBTTagCompound compound = new NBTTagCompound();
            compound.setInteger("SlotCount", slotCount);

            final NBTTagList slotList = new NBTTagList();

            for (int slot = 0; slot < slotCount; slot++) {
                final ItemStack stackInSlot = instance.getStackInSlot(slot);
                if (!stackInSlot.isEmpty()) {
                    final NBTTagCompound slotCompound = new NBTTagCompound();
                    slotCompound.setInteger("Slot", slot);
                    stackInSlot.writeToNBT(slotCompound);
                    slotList.appendTag(slotCompound);
                }
            }

            compound.setTag("Slots", slotList);

            return compound;
        }

        @Override
        public void readNBT(Capability<IMultiSlotItemHandler> capability, IMultiSlotItemHandler instance, EnumFacing side, NBTBase nbt) {

            final NBTTagCompound compound = (NBTTagCompound) nbt;
            final int slotCount = compound.getInteger("SlotCount");

            instance.resize(slotCount);

            final NBTTagList slotList = compound.getTagList("Slots", Constants.NBT.TAG_COMPOUND);
            for (int slot = 0; slot < slotList.tagCount(); slot++) {
                final NBTTagCompound slotCompound = slotList.getCompoundTagAt(slot);
                final int slotIndex = slotCompound.getInteger("Slot");

                if (slotIndex >= 0 && slotIndex < instance.getSlots()) {
                    instance.setStackInSlot(slotIndex, new ItemStack(slotCompound));
                }
            }
        }
    }
}
