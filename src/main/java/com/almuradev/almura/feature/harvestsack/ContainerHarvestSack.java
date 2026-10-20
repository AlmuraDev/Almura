/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.harvestsack;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.items.SlotItemHandler;

/**
 * The sack's 27 slots above the player inventory, laid out like a single chest. The slot holding the open sack is locked so it can't
 * be moved (or swapped away with a number key) while its contents are shown.
 */
public final class ContainerHarvestSack extends Container {

    private static final int ROWS = SackInventory.SIZE / 9;

    private final EnumHand hand;
    private final ItemStack sack;
    private final int lockedSlot;
    private final int lockedHotbarIndex;

    public ContainerHarvestSack(final EntityPlayer player, final EnumHand hand) {
        this.hand = hand;
        this.sack = player.getHeldItem(hand);
        final SackInventory inventory = new SackInventory(this.sack);
        final InventoryPlayer playerInventory = player.inventory;

        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlotToContainer(new SlotItemHandler(inventory, column + row * 9, 8 + column * 18, 18 + row * 18));
            }
        }

        final int offset = (ROWS - 4) * 18;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlotToContainer(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 103 + row * 18 + offset));
            }
        }
        for (int column = 0; column < 9; column++) {
            this.addSlotToContainer(new Slot(playerInventory, column, 8 + column * 18, 161 + offset));
        }

        if (hand == EnumHand.MAIN_HAND) {
            this.lockedHotbarIndex = playerInventory.currentItem;
            this.lockedSlot = SackInventory.SIZE + 27 + this.lockedHotbarIndex;
        } else {
            this.lockedHotbarIndex = -1;
            this.lockedSlot = -1;
        }
    }

    public ItemStack getSack() {
        return this.sack;
    }

    @Override
    public boolean canInteractWith(final EntityPlayer player) {
        // Closes if the sack leaves the hand it was opened from
        return player.getHeldItem(this.hand) == this.sack;
    }

    @Override
    public ItemStack slotClick(final int slotId, final int dragType, final ClickType clickType, final EntityPlayer player) {
        if (this.lockedSlot >= 0 && (slotId == this.lockedSlot || (clickType == ClickType.SWAP && dragType == this.lockedHotbarIndex))) {
            return ItemStack.EMPTY;
        }
        return super.slotClick(slotId, dragType, clickType, player);
    }

    @Override
    public ItemStack transferStackInSlot(final EntityPlayer player, final int index) {
        final Slot slot = this.inventorySlots.get(index);
        if (slot == null || !slot.getHasStack() || index == this.lockedSlot) {
            return ItemStack.EMPTY;
        }

        final ItemStack stack = slot.getStack();
        final ItemStack original = stack.copy();
        if (index < SackInventory.SIZE) {
            if (!this.mergeItemStack(stack, SackInventory.SIZE, this.inventorySlots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.mergeItemStack(stack, 0, SackInventory.SIZE, false)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.putStack(ItemStack.EMPTY);
        } else {
            slot.onSlotChanged();
        }
        return original;
    }
}
