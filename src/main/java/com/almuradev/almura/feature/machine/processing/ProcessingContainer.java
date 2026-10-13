/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.machine.processing;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import java.util.Arrays;

import javax.annotation.Nonnull;

/**
 * Input slot above, output slot below (laid out to match the machine GUI texture), then the player's inventory. Progress, energy and the
 * selected output are synced to the client as window properties; the GUI's output selector buttons come back through
 * {@link #enchantItem} (vanilla's container button message, as the enchanting table uses), so no extra packets are needed.
 */
public final class ProcessingContainer extends Container {

    public static final int PROPERTY_PROGRESS = 0;
    public static final int PROPERTY_ENERGY = 1;
    public static final int PROPERTY_CAPACITY = 2;
    public static final int PROPERTY_REQUIRES_POWER = 3;
    public static final int PROPERTY_SELECTED = 4;
    public static final int PROPERTY_STARTED = 5;
    private static final int PROPERTY_COUNT = 6;

    /** Output selector button: previous output. */
    public static final int BUTTON_PREVIOUS = 0;
    /** Output selector button: next output. */
    public static final int BUTTON_NEXT = 1;
    /** Start / Stop (when the input has a choice of outputs). */
    public static final int BUTTON_START_STOP = 2;

    private static final int MACHINE_SLOTS = 2;
    private static final int PLAYER_SLOTS = 36;

    private final ProcessingTileEntity machine;
    // Last values sent to listeners (server) / received from the server (client)
    private final int[] properties = new int[PROPERTY_COUNT];

    public ProcessingContainer(final InventoryPlayer playerInventory, final ProcessingTileEntity machine) {
        this.machine = machine;

        final IItemHandler inventory = machine.getInventory();
        this.addSlotToContainer(new SlotItemHandler(inventory, ProcessingTileEntity.INPUT_SLOT, 127, 17));
        this.addSlotToContainer(new SlotItemHandler(inventory, ProcessingTileEntity.OUTPUT_SLOT, 127, 62) {
            @Override
            public boolean isItemValid(@Nonnull final ItemStack stack) {
                // Output only
                return false;
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlotToContainer(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            this.addSlotToContainer(new Slot(playerInventory, column, 8 + column * 18, 142));
        }

        // Make sure the first detectAndSendChanges sends everything
        Arrays.fill(this.properties, -1);
    }

    public ProcessingTileEntity machine() {
        return this.machine;
    }

    @Override
    public boolean canInteractWith(final EntityPlayer player) {
        return this.machine.isUsableByPlayer(player);
    }

    @Override
    public boolean enchantItem(final EntityPlayer player, final int id) {
        if (id == BUTTON_PREVIOUS || id == BUTTON_NEXT) {
            this.machine.cycleSelection(id == BUTTON_NEXT ? 1 : -1);
            return true;
        }
        if (id == BUTTON_START_STOP) {
            this.machine.toggleStarted();
            return true;
        }
        return false;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        final int[] current = {
                this.machine.getProgress(),
                (int) this.machine.getEnergy().stored(),
                (int) this.machine.getEnergy().capacity(),
                this.machine.getEnergy().requiresPower() ? 1 : 0,
                this.machine.getSelected(),
                this.machine.isStarted() ? 1 : 0
        };

        for (int i = 0; i < current.length; i++) {
            if (current[i] != this.properties[i]) {
                for (final IContainerListener listener : this.listeners) {
                    listener.sendWindowProperty(this, i, current[i]);
                }
                this.properties[i] = current[i];
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void updateProgressBar(final int id, final int data) {
        if (id >= 0 && id < this.properties.length) {
            this.properties[id] = data;
        }
    }

    /**
     * Client side: the last value the server sent for a property.
     */
    public int property(final int id) {
        return Math.max(0, this.properties[id]);
    }

    @Override
    public ItemStack transferStackInSlot(final EntityPlayer player, final int index) {
        final Slot slot = this.inventorySlots.get(index);
        if (slot == null || !slot.getHasStack()) {
            return ItemStack.EMPTY;
        }

        final ItemStack stack = slot.getStack();
        final ItemStack original = stack.copy();

        if (index < MACHINE_SLOTS) {
            // Machine -> player inventory
            if (!this.mergeItemStack(stack, MACHINE_SLOTS, MACHINE_SLOTS + PLAYER_SLOTS, true)) {
                return ItemStack.EMPTY;
            }
            slot.onSlotChange(stack, original);
        } else if (this.machine.type().recipes().isInput(stack)) {
            // Player -> input slot
            if (!this.mergeItemStack(stack, ProcessingTileEntity.INPUT_SLOT, ProcessingTileEntity.INPUT_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index < MACHINE_SLOTS + 27) {
            // Main inventory -> hotbar
            if (!this.mergeItemStack(stack, MACHINE_SLOTS + 27, MACHINE_SLOTS + PLAYER_SLOTS, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.mergeItemStack(stack, MACHINE_SLOTS, MACHINE_SLOTS + 27, false)) {
            // Hotbar -> main inventory
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.putStack(ItemStack.EMPTY);
        } else {
            slot.onSlotChanged();
        }

        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, stack);
        return original;
    }
}
