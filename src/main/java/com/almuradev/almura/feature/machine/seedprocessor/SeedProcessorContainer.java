/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.machine.seedprocessor;

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
 * Input slot above, output slot below (laid out to match the seed processor GUI texture), then the player's inventory. Progress and
 * energy are synced to the client as window properties.
 */
public final class SeedProcessorContainer extends Container {

    public static final int PROPERTY_PROGRESS = 0;
    public static final int PROPERTY_ENERGY = 1;
    public static final int PROPERTY_CAPACITY = 2;
    public static final int PROPERTY_REQUIRES_POWER = 3;
    private static final int PROPERTY_COUNT = 4;

    private static final int MACHINE_SLOTS = 2;
    private static final int PLAYER_SLOTS = 36;

    private final SeedProcessorTileEntity machine;
    // Last values sent to listeners (server) / received from the server (client)
    private final int[] properties = new int[PROPERTY_COUNT];

    public SeedProcessorContainer(final InventoryPlayer playerInventory, final SeedProcessorTileEntity machine) {
        this.machine = machine;

        final IItemHandler inventory = machine.getInventory();
        this.addSlotToContainer(new SlotItemHandler(inventory, SeedProcessorTileEntity.INPUT_SLOT, 127, 17));
        this.addSlotToContainer(new SlotItemHandler(inventory, SeedProcessorTileEntity.OUTPUT_SLOT, 127, 62) {
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

    @Override
    public boolean canInteractWith(final EntityPlayer player) {
        return this.machine.isUsableByPlayer(player);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        final int[] current = {
                this.machine.getProgress(),
                (int) this.machine.getEnergy().stored(),
                (int) this.machine.getEnergy().capacity(),
                this.machine.getEnergy().requiresPower() ? 1 : 0
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
        } else if (SeedProcessorRecipes.isInput(stack)) {
            // Player -> input slot
            if (!this.mergeItemStack(stack, SeedProcessorTileEntity.INPUT_SLOT, SeedProcessorTileEntity.INPUT_SLOT + 1, false)) {
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
