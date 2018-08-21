package com.almuradev.almura.feature.exchange.client.gui.component;

import com.almuradev.almura.shared.client.ui.component.UIDynamicList;
import com.almuradev.almura.shared.item.BasicVanillaStack;
import com.almuradev.almura.shared.item.VanillaStack;
import net.malisis.core.client.gui.MalisisGui;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;

/**
 * An implementation of IItemHandler for the Exchange framework. Pay careful attention to the fact that all {@link ItemStack} maniuplations
 * MUST be resync'd back to the {@link VanillaStack} at the call site.
 */
public final class UIItemList extends UIDynamicList<VanillaStack> implements IItemHandler {

    public static final VanillaStack VANILLA_STACK_EMPTY = new BasicVanillaStack(ItemStack.EMPTY);

    private final int slots;
    private final int maxSlotStackSize;
    private final boolean enforceStackLimit;

    public UIItemList(final MalisisGui gui, final boolean enforceStackLimit, final int slots, final int maxSlotStackSize, final int width,
            final int height) {
        super(gui, width, height);
        this.slots = slots;
        this.maxSlotStackSize = maxSlotStackSize;
        this.enforceStackLimit = enforceStackLimit;
    }

    @Override
    public int getSlots() {
        return this.slots;
    }

    public VanillaStack getStackFromSlot(int slot) {
        if (slot >= this.getSize()) {
            return VANILLA_STACK_EMPTY;
        }

        final VanillaStack stackInSlot = this.getItems().get(slot);
        if (stackInSlot == null) {
            return VANILLA_STACK_EMPTY;
        }

        return stackInSlot;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return this.getStackFromSlot(slot).asRealStack();
    }

    @Override
    @Nonnull
    public ItemStack insertItem(final int slot, @Nonnull final ItemStack stack, final boolean simulate) {
        if (simulate) {
            throw new UnsupportedOperationException("Simulation not supported!");
        }

        // Copy before changes because Vanilla
        final ItemStack diffStack = stack.copy();

        int amountUsed = 0;

        final ItemStack stackInSlot = this.getStackInSlot(slot);

        final int stackInSlotLimit = this.enforceStackLimit ? stackInSlot.getMaxStackSize() : this.maxSlotStackSize;
        if (ItemHandlerHelper.canItemStacksStack(stackInSlot, stack)) {
            final int toAdd = Math.min(Math.min(stackInSlotLimit, stack.getCount()), stackInSlotLimit - stackInSlot.getCount());

            amountUsed += toAdd;
            stackInSlot.grow(toAdd);
            this.markDirty();
        } else if (stackInSlot.isEmpty()) {
            final VanillaStack newStack = new BasicVanillaStack(stack);
            newStack.setQuantity(Math.min(stackInSlotLimit, stack.getCount()));
            this.addItem(slot, newStack);
            amountUsed += newStack.getQuantity();
        }

        diffStack.setCount(stack.getCount() - amountUsed);

        return diffStack;
    }

    @Nonnull
    public ItemStack extractItem(final int slot, final int amount, final boolean simulate) {
        if (simulate) {
            throw new UnsupportedOperationException("Simulation not supported!");
        }

        if (amount <= 0) {
            return ItemStack.EMPTY;
        }

        final ItemStack stackInSlot = this.getStackInSlot(slot);

        final int stackInSlotLimit = this.enforceStackLimit ? stackInSlot.getMaxStackSize() : this.maxSlotStackSize;

        final int toTake = Math.min(stackInSlot.getCount(), amount);
        final ItemStack extractedItem = stackInSlot.copy();
        extractedItem.setCount(Math.min(stackInSlotLimit, toTake));

        // Shrink after a copy is made to avoid copying air
        stackInSlot.shrink(toTake);

        if (stackInSlot.isEmpty()) {
            this.removeItem(slot);
        }

        return extractedItem;
    }

    @Override
    public int getSlotLimit(int slot) {
        return this.maxSlotStackSize;
    }
}
