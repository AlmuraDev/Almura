/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.machine.seedprocessor;

import com.almuradev.almura.feature.machine.energy.MachineEnergy;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Turns one input item into the seeds a crafting table would give for it, over {@link #PROCESS_TICKS} ticks, using
 * {@link #ENERGY_PER_TICK} EU per tick while working when IndustrialCraft 2 is installed (free otherwise). Like a furnace, progress pauses
 * while there is no power and resets when the input is removed.
 */
public final class SeedProcessorTileEntity extends TileEntity implements ITickable {

    public static final int INPUT_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;

    // About 1.65 seconds per item
    public static final int PROCESS_TICKS = 33;
    // 6 EU/t x 33 ticks = 198 EU per item (LV accepts up to 32 EU/t)
    public static final double ENERGY_PER_TICK = 6;
    // Kept below Short.MAX_VALUE: container window properties are sent to the client as shorts
    public static final double ENERGY_CAPACITY = 10_000;
    // IC2 tier 1 = LV, up to 32 EU/t
    public static final int ENERGY_TIER = 1;

    private static final String TAG_INVENTORY = "Inventory";
    private static final String TAG_PROGRESS = "Progress";

    private final ItemStackHandler inventory = new ItemStackHandler(2) {
        @Override
        public boolean isItemValid(final int slot, @Nonnull final ItemStack stack) {
            return slot == INPUT_SLOT && SeedProcessorRecipes.isInput(stack);
        }

        @Override
        protected void onContentsChanged(final int slot) {
            if (slot == INPUT_SLOT) {
                SeedProcessorTileEntity.this.outputDirty = true;
            }
            SeedProcessorTileEntity.this.markDirty();
        }
    };
    // Hoppers and pipes, as seen facing the front: insert into the input on the left, extract the output on the right
    private final IItemHandler inputAccess = new SlotAccess(INPUT_SLOT, true, false);
    private final IItemHandler outputAccess = new SlotAccess(OUTPUT_SLOT, false, true);

    // Power from the rear, top or bottom
    private final MachineEnergy energy = MachineEnergy.create(this, ENERGY_CAPACITY, ENERGY_TIER,
            side -> side == EnumFacing.UP || side == EnumFacing.DOWN || side == this.facing().getOpposite());

    private int progress;
    // Output for the current input, recomputed only when the input slot changes
    private ItemStack output = ItemStack.EMPTY;
    private boolean outputDirty = true;
    // Whether the block currently shows its active textures; read from the block state on the first tick so it isn't toggled needlessly
    private boolean active;
    private boolean activeKnown;

    public IItemHandler getInventory() {
        return this.inventory;
    }

    public MachineEnergy getEnergy() {
        return this.energy;
    }

    public int getProgress() {
        return this.progress;
    }

    public boolean isUsableByPlayer(final EntityPlayer player) {
        return this.world.getTileEntity(this.pos) == this && player.getDistanceSq(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D,
                this.pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void update() {
        if (this.world.isRemote) {
            return;
        }

        if (!this.activeKnown) {
            final IBlockState state = this.world.getBlockState(this.pos);
            this.active = state.getBlock() instanceof SeedProcessorBlock && state.getValue(SeedProcessorBlock.ACTIVE);
            this.activeKnown = true;
        }

        this.energy.update();
        this.refreshOutput();

        boolean worked = false;

        if (!this.canProcess()) {
            // Nothing to process (or nowhere to put it): start over next time, like a furnace
            if (this.progress != 0) {
                this.progress = 0;
                this.markDirty();
            }
        } else if (this.energy.use(ENERGY_PER_TICK)) {
            // Without power progress simply pauses
            worked = true;
            this.progress++;
            if (this.progress >= PROCESS_TICKS) {
                this.process();
                this.progress = 0;
                // The input just changed, so check against what's left
                this.refreshOutput();
            }
            this.markDirty();
        }

        // Active while working and able to keep going. Checked after finishing an item, so it stays active straight into the next one
        // instead of switching off for the tick progress resets (which caused a flicker).
        final boolean active = worked && this.canProcess();
        if (active != this.active) {
            this.active = active;
            SeedProcessorBlock.setActive(this.world, this.pos, active);
        }
    }

    private void refreshOutput() {
        if (this.outputDirty) {
            this.output = SeedProcessorRecipes.outputFor(this.inventory.getStackInSlot(INPUT_SLOT));
            this.outputDirty = false;
        }
    }

    private boolean canProcess() {
        if (this.output.isEmpty()) {
            return false;
        }

        final ItemStack current = this.inventory.getStackInSlot(OUTPUT_SLOT);
        if (current.isEmpty()) {
            return true;
        }

        return ItemHandlerHelper.canItemStacksStack(current, this.output)
                && current.getCount() + this.output.getCount() <= Math.min(current.getMaxStackSize(), this.inventory.getSlotLimit(OUTPUT_SLOT));
    }

    private void process() {
        final ItemStack current = this.inventory.getStackInSlot(OUTPUT_SLOT);
        if (current.isEmpty()) {
            this.inventory.setStackInSlot(OUTPUT_SLOT, this.output.copy());
        } else {
            final ItemStack grown = current.copy();
            grown.grow(this.output.getCount());
            this.inventory.setStackInSlot(OUTPUT_SLOT, grown);
        }

        this.inventory.extractItem(INPUT_SLOT, 1, false);
    }

    @Override
    public void onLoad() {
        this.energy.onLoad();
    }

    @Override
    public void invalidate() {
        super.invalidate();
        this.energy.invalidate();
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        this.energy.onChunkUnload();
    }

    @Override
    public boolean shouldRefresh(final World world, final BlockPos pos, final IBlockState oldState, final IBlockState newState) {
        // Switching between idle and active changes the block state, keep the tile entity (and its inventory) when it does
        return oldState.getBlock() != newState.getBlock();
    }

    @Override
    public void readFromNBT(final NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.inventory.deserializeNBT(compound.getCompoundTag(TAG_INVENTORY));
        this.progress = compound.getInteger(TAG_PROGRESS);
        this.energy.readFromNBT(compound);
        this.outputDirty = true;
    }

    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setTag(TAG_INVENTORY, this.inventory.serializeNBT());
        compound.setInteger(TAG_PROGRESS, this.progress);
        this.energy.writeToNBT(compound);
        return compound;
    }

    /**
     * The direction the front of the machine faces.
     */
    private EnumFacing facing() {
        if (this.world == null) {
            return EnumFacing.NORTH;
        }
        final IBlockState state = this.world.getBlockState(this.pos);
        return state.getBlock() instanceof SeedProcessorBlock ? state.getValue(BlockHorizontal.FACING) : EnumFacing.NORTH;
    }

    /**
     * Item access for a side: the input on the machine's left and the output on its right (as seen looking at the front), the whole
     * inventory for no side (internal use), nothing elsewhere.
     */
    @Nullable
    private IItemHandler itemAccess(@Nullable final EnumFacing side) {
        if (side == null) {
            return this.inventory;
        }
        final EnumFacing front = this.facing();
        if (side == front.rotateY()) {
            return this.inputAccess;
        }
        if (side == front.rotateYCCW()) {
            return this.outputAccess;
        }
        return null;
    }

    @Override
    public boolean hasCapability(@Nonnull final Capability<?> capability, @Nullable final EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return this.itemAccess(facing) != null;
        }
        return super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull final Capability<T> capability, @Nullable final EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            final IItemHandler handler = this.itemAccess(facing);
            return handler == null ? null : CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(handler);
        }
        return super.getCapability(capability, facing);
    }

    /**
     * Exposes a single slot of the inventory to automation, allowing only insertion or only extraction.
     */
    private final class SlotAccess implements IItemHandler {

        private final int slot;
        private final boolean insert;
        private final boolean extract;

        SlotAccess(final int slot, final boolean insert, final boolean extract) {
            this.slot = slot;
            this.insert = insert;
            this.extract = extract;
        }

        @Override
        public int getSlots() {
            return 1;
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot(final int slot) {
            return SeedProcessorTileEntity.this.inventory.getStackInSlot(this.slot);
        }

        @Nonnull
        @Override
        public ItemStack insertItem(final int slot, @Nonnull final ItemStack stack, final boolean simulate) {
            return this.insert ? SeedProcessorTileEntity.this.inventory.insertItem(this.slot, stack, simulate) : stack;
        }

        @Nonnull
        @Override
        public ItemStack extractItem(final int slot, final int amount, final boolean simulate) {
            return this.extract ? SeedProcessorTileEntity.this.inventory.extractItem(this.slot, amount, simulate) : ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(final int slot) {
            return SeedProcessorTileEntity.this.inventory.getSlotLimit(this.slot);
        }
    }
}
