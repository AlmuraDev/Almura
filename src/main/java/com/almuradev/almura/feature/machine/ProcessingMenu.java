/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.machine;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * A processing machine's menu, as 3.5's {@code ProcessingContainer}: the input slot above, the output slot below (laid out on 3.5's
 * screen), then the player's inventory. What the machine will make sits in a slot of its own that can't be touched (26.3's clients don't
 * have the recipes, so the server shows it). Progress, the selected output, Start and how many outputs there are come to the client as
 * the menu's data; the selector's and Start's buttons come back as vanilla's menu button clicks, as the enchanting table's.
 */
public final class ProcessingMenu extends AbstractContainerMenu {

    /** Previous output. */
    public static final int BUTTON_PREVIOUS = 0;
    /** Next output. */
    public static final int BUTTON_NEXT = 1;
    /** Start / Stop (when the input has a choice of outputs). */
    public static final int BUTTON_START_STOP = 2;
    // The machine's two slots, then the preview, then the player's 36
    private static final int MACHINE_SLOTS = 2, PREVIEW_SLOT = 2, PLAYER_START = 3, PLAYER_SLOTS = 36;

    private final Container machine;
    private final ContainerData data;
    private final MachineType type;

    ProcessingMenu(final MenuType<ProcessingMenu> menuType, final int containerId, final Inventory inventory, final Container machine,
        final Container preview, final ContainerData data, final MachineType type) {
        super(menuType, containerId);
        this.machine = machine;
        this.data = data;
        this.type = type;
        this.addSlot(new Slot(machine, ProcessingBlockEntity.INPUT_SLOT, 127, 17) {
            @Override
            public boolean mayPlace(final ItemStack stack) {
                return machine.canPlaceItem(this.getContainerSlot(), stack);
            }
        });
        this.addSlot(new Slot(machine, ProcessingBlockEntity.OUTPUT_SLOT, 127, 62) {
            @Override
            public boolean mayPlace(final ItemStack stack) {
                // Output only
                return false;
            }
        });
        this.addSlot(new Slot(preview, 0, 79, 26) {
            @Override
            public boolean mayPlace(final ItemStack stack) {
                return false;
            }

            @Override
            public boolean mayPickup(final Player player) {
                return false;
            }

            @Override
            public boolean isHighlightable() {
                return false;
            }
        });
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            this.addSlot(new Slot(inventory, column, 8 + column * 18, 142));
        }
        this.addDataSlots(data);
    }

    /**
     * The client's menu: the machine the server named, its slots filled by the server.
     */
    static ProcessingMenu client(final MenuType<ProcessingMenu> menuType, final int containerId, final Inventory inventory, final int type) {
        // On the client the input takes anything; the server says no, and puts it back
        return new ProcessingMenu(menuType, containerId, inventory, new SimpleContainer(MACHINE_SLOTS) {
            @Override
            public boolean canPlaceItem(final int slot, final ItemStack stack) {
                return slot == ProcessingBlockEntity.INPUT_SLOT;
            }
        }, new SimpleContainer(1), new SimpleContainerData(ProcessingBlockEntity.DATA_COUNT), MachineType.byOrdinal(type));
    }

    public MachineType type() {
        return this.type;
    }

    /**
     * What the server last said for a piece of the machine's data ({@code ProcessingBlockEntity.DATA_...}).
     */
    public int data(final int index) {
        return this.data.get(index);
    }

    /**
     * The output the machine will make, or nothing.
     */
    public ItemStack preview() {
        return this.getSlot(PREVIEW_SLOT).getItem();
    }

    @Override
    public boolean stillValid(final Player player) {
        return this.machine.stillValid(player);
    }

    @Override
    public boolean clickMenuButton(final Player player, final int id) {
        if (!(this.machine instanceof final ProcessingBlockEntity entity)) {
            return false;
        }
        if (id == BUTTON_PREVIOUS || id == BUTTON_NEXT) {
            entity.cycleSelection(id == BUTTON_NEXT ? 1 : -1);
            return true;
        }
        if (id == BUTTON_START_STOP) {
            entity.toggleStarted();
            return true;
        }
        return false;
    }

    @Override
    public ItemStack quickMoveStack(final Player player, final int index) {
        final Slot slot = this.slots.get(index);
        if (index == PREVIEW_SLOT || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        final ItemStack stack = slot.getItem();
        final ItemStack original = stack.copy();
        if (index < MACHINE_SLOTS) {
            // Machine to the player's inventory
            if (!this.moveItemStackTo(stack, PLAYER_START, PLAYER_START + PLAYER_SLOTS, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(stack, original);
        } else if (this.slots.get(ProcessingBlockEntity.INPUT_SLOT).mayPlace(stack)) {
            // To the input
            if (!this.moveItemStackTo(stack, ProcessingBlockEntity.INPUT_SLOT, ProcessingBlockEntity.INPUT_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index < PLAYER_START + 27) {
            // Main inventory to the hotbar
            if (!this.moveItemStackTo(stack, PLAYER_START + 27, PLAYER_START + PLAYER_SLOTS, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(stack, PLAYER_START, PLAYER_START + 27, false)) {
            // Hotbar to the main inventory
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        return original;
    }
}
