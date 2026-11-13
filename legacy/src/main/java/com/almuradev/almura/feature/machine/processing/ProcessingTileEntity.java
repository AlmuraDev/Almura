/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.machine.processing;

import com.almuradev.almura.feature.machine.energy.MachineEnergy;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fluids.capability.templates.EmptyFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A furnace-like processing machine: turns the input into what its {@link MachineType}'s crafting recipes make, over {@link #PROCESS_TICKS}
 * ticks, using {@link #ENERGY_PER_TICK} EU per tick while working when IndustrialCraft 2 is installed (free otherwise). Progress pauses while
 * there is no power and resets when the input is removed. When the input has several recipes, the player picks the output in the GUI.
 * Machines that use water (see {@link MachineType#usesWater}) also need {@link MachineType#waterPerOperation} mB of water per operation, from
 * a tank filled by pipes from above or by water containers (buckets, cells, ...) put in the GUI.
 *
 * <p>Each machine has a small subclass only so it can be registered as its own tile entity.</p>
 */
public abstract class ProcessingTileEntity extends TileEntity implements ITickable {

    public static final int INPUT_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;
    // Machines that use water (see MachineType#usesWater) also have a slot for water containers and one for the emptied containers
    public static final int WATER_INPUT_SLOT = 2;
    public static final int WATER_OUTPUT_SLOT = 3;

    // About 1.65 seconds per operation
    public static final int PROCESS_TICKS = 33;
    // 6 EU/t x 33 ticks = 198 EU per operation (LV accepts up to 32 EU/t)
    public static final double ENERGY_PER_TICK = 6;
    // Kept below Short.MAX_VALUE: container window properties are sent to the client as shorts
    public static final double ENERGY_CAPACITY = 10_000;
    // IC2 tier 1 = LV, up to 32 EU/t
    public static final int ENERGY_TIER = 1;

    private static final Random RANDOM = new Random();
    // How often a machine with a working sound plays it while working
    private static final int SOUND_INTERVAL = 20;
    private static final String TAG_INVENTORY = "Inventory";
    private static final String TAG_PROGRESS = "Progress";
    private static final String TAG_SELECTED = "Selected";
    private static final String TAG_STARTED = "Started";
    private static final String TAG_WATER = "Water";

    private final ItemStackHandler inventory = new ItemStackHandler(this.type().usesWater() ? 4 : 2) {
        @Override
        public boolean isItemValid(final int slot, @Nonnull final ItemStack stack) {
            if (slot == WATER_INPUT_SLOT) {
                return isWaterContainer(stack);
            }
            return slot == INPUT_SLOT && ProcessingTileEntity.this.type().recipes().isInput(stack);
        }

        @Override
        protected void onContentsChanged(final int slot) {
            if (slot == INPUT_SLOT) {
                ProcessingTileEntity.this.recipeDirty = true;
            }
            ProcessingTileEntity.this.markDirty();
        }
    };
    // Hoppers and pipes, as seen facing the front: insert into the input on the left, extract the output on the right
    private final IItemHandler inputAccess = new SlotAccess(INPUT_SLOT, true, false);
    private final IItemHandler outputAccess = new SlotAccess(OUTPUT_SLOT, false, true);

    // Power from the rear, top or bottom
    private final MachineEnergy energy = MachineEnergy.create(this, ENERGY_CAPACITY, ENERGY_TIER,
            side -> side == EnumFacing.UP || side == EnumFacing.DOWN || side == this.facing().getOpposite());

    // Water, for machines that use it: only water goes in, and nothing outside the machine can take it back out
    @Nullable private final FluidTank water = this.type().usesWater() ? new WaterTank(this.type().waterCapacity()) : null;
    // Pipes fill the tank from above
    private final IFluidHandler waterAccess = new WaterAccess();

    private int progress;
    // Which of the input's recipes is made (the GUI's output selector)
    private int selected;
    // The selected recipe for the current input, recomputed only when the input or the selection changes
    @Nullable private ProcessingRecipe recipe;
    private boolean recipeDirty = true;
    // The input item the selection was made for: a different item starts again at its first output (and has to be started again)
    private ItemStack selectedFor = ItemStack.EMPTY;
    // Whether the player pressed Start (only needed when the input has a choice of outputs, see MachineType#startOnChoice)
    private boolean started;
    // Whether the current input needs Start before it's processed
    private boolean needsStart;
    // Whether the block currently shows its active textures; read from the block state on the first tick so it isn't toggled needlessly
    private boolean active;
    private boolean activeKnown;
    /** Client only: the angle of the machine's spinning part (if it has one), kept when it stops so the part doesn't snap back. */
    public float spinAngle;

    /**
     * Which machine this is.
     */
    public abstract MachineType type();

    public IItemHandler getInventory() {
        return this.inventory;
    }

    public MachineEnergy getEnergy() {
        return this.energy;
    }

    /**
     * @return the water in the tank, in mB (0 for machines that don't use water)
     */
    public int getWater() {
        return this.water == null ? 0 : this.water.getFluidAmount();
    }

    public int getProgress() {
        return this.progress;
    }

    public int getSelected() {
        return this.selected;
    }

    /**
     * Picks the previous ({@code -1}) or next ({@code 1}) output for the current input, starting the operation over.
     */
    public void cycleSelection(final int direction) {
        final List<ProcessingRecipe> recipes = this.type().recipes().selectable(this.inventory.getStackInSlot(INPUT_SLOT));
        if (recipes.size() < 2) {
            return;
        }
        this.selected = Math.floorMod(this.selected + direction, recipes.size());
        this.progress = 0;
        this.recipeDirty = true;
        this.markDirty();
    }

    public boolean isStarted() {
        return this.started;
    }

    /**
     * Start / Stop from the GUI. Stopping also throws away the current operation's progress.
     */
    public void toggleStarted() {
        this.started = !this.started;
        if (!this.started) {
            this.progress = 0;
        }
        this.markDirty();
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
            this.active = state.getBlock() instanceof ProcessingBlock && state.getValue(ProcessingBlock.ACTIVE);
            this.activeKnown = true;
        }

        this.energy.update();
        this.fillFromContainer();
        this.refreshRecipe();

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
            final SoundEvent sound = this.type().sound();
            if (sound != null && this.world.getTotalWorldTime() % SOUND_INTERVAL == 0) {
                this.world.playSound(null, this.pos, sound, SoundCategory.BLOCKS, 1f, 1f);
            }
            if (this.progress >= PROCESS_TICKS) {
                this.process();
                this.progress = 0;
                // The input just changed, so check against what's left
                this.refreshRecipe();
            }
            this.markDirty();
        }

        // Active while working and able to keep going. Checked after finishing an operation, so it stays active straight into the next one
        // instead of switching off for the tick progress resets (which caused a flicker).
        final boolean active = worked && this.canProcess();
        if (active != this.active) {
            this.active = active;
            ProcessingBlock.setActive(this.world, this.pos, active);
        }
    }

    private void refreshRecipe() {
        if (!this.recipeDirty) {
            return;
        }
        this.recipeDirty = false;

        final ItemStack input = this.inventory.getStackInSlot(INPUT_SLOT);
        // An empty slot keeps the selection (and Start), so refills of the same item carry on; a different item starts over.
        // Machines with modes keep the mode whatever goes in.
        if (!input.isEmpty() && this.type().recipes().modes().isEmpty()
                && !ItemHandlerHelper.canItemStacksStack(input, this.selectedFor)) {
            this.selected = 0;
            this.started = false;
            this.selectedFor = ItemHandlerHelper.copyStackWithSize(input, 1);
        }

        final List<ProcessingRecipe> recipes = this.type().recipes().recipesFor(input);
        this.needsStart = this.type().startOnChoice() && recipes.size() > 1;
        if (recipes.isEmpty()) {
            this.recipe = null;
            return;
        }
        if (this.selected >= recipes.size()) {
            this.selected = 0;
        }
        this.recipe = recipes.get(this.selected);
    }

    private boolean canProcess() {
        if (this.recipe == null || this.inventory.getStackInSlot(INPUT_SLOT).getCount() < this.recipe.inputCount()) {
            return false;
        }
        // A choice of outputs: wait until the player has picked one and pressed Start
        if (this.needsStart && !this.started) {
            return false;
        }
        // Machines that use water wait until there's enough for an operation
        if (this.water != null && this.water.getFluidAmount() < this.type().waterPerOperation()) {
            return false;
        }

        final ItemStack output = this.recipe.output();
        final ItemStack current = this.inventory.getStackInSlot(OUTPUT_SLOT);
        if (current.isEmpty()) {
            return true;
        }

        return ItemHandlerHelper.canItemStacksStack(current, output)
                && current.getCount() + output.getCount() <= Math.min(current.getMaxStackSize(), this.inventory.getSlotLimit(OUTPUT_SLOT));
    }

    private void process() {
        final ProcessingRecipe recipe = this.recipe;
        if (recipe == null) {
            return;
        }

        // Chance recipes (the wash plant's nuggets) use up the input whether or not they make anything
        if (recipe.chance() >= 1 || RANDOM.nextDouble() < recipe.chance()) {
            final ItemStack output = recipe.output();
            final ItemStack current = this.inventory.getStackInSlot(OUTPUT_SLOT);
            if (current.isEmpty()) {
                this.inventory.setStackInSlot(OUTPUT_SLOT, output);
            } else {
                final ItemStack grown = current.copy();
                grown.grow(output.getCount());
                this.inventory.setStackInSlot(OUTPUT_SLOT, grown);
            }
        }

        this.inventory.extractItem(INPUT_SLOT, recipe.inputCount(), false);
        if (this.water != null) {
            this.water.drain(this.type().waterPerOperation(), true);
        }
    }

    /**
     * Empties one water container (bucket, cell, ...) from the water slot into the tank, if it all fits and the emptied container has
     * somewhere to go.
     */
    private void fillFromContainer() {
        if (this.water == null) {
            return;
        }
        final ItemStack container = this.inventory.getStackInSlot(WATER_INPUT_SLOT);
        final int space = this.water.getCapacity() - this.water.getFluidAmount();
        if (container.isEmpty() || space <= 0) {
            return;
        }

        final FluidActionResult simulated = FluidUtil.tryEmptyContainer(container, this.water, space, null, false);
        if (!simulated.isSuccess() || !this.fitsWaterOutput(simulated.getResult())) {
            return;
        }

        final FluidActionResult result = FluidUtil.tryEmptyContainer(container, this.water, space, null, true);
        if (!result.isSuccess()) {
            return;
        }
        this.inventory.extractItem(WATER_INPUT_SLOT, 1, false);
        final ItemStack emptied = result.getResult();
        if (!emptied.isEmpty()) {
            final ItemStack current = this.inventory.getStackInSlot(WATER_OUTPUT_SLOT);
            if (current.isEmpty()) {
                this.inventory.setStackInSlot(WATER_OUTPUT_SLOT, emptied);
            } else {
                final ItemStack grown = current.copy();
                grown.grow(emptied.getCount());
                this.inventory.setStackInSlot(WATER_OUTPUT_SLOT, grown);
            }
        }
        this.markDirty();
    }

    private boolean fitsWaterOutput(final ItemStack emptied) {
        if (emptied.isEmpty()) {
            return true;
        }
        final ItemStack current = this.inventory.getStackInSlot(WATER_OUTPUT_SLOT);
        return current.isEmpty() || (ItemHandlerHelper.canItemStacksStack(current, emptied)
                && current.getCount() + emptied.getCount() <= current.getMaxStackSize());
    }

    static boolean isWaterContainer(final ItemStack stack) {
        final FluidStack contained = FluidUtil.getFluidContained(stack);
        return contained != null && contained.getFluid() == FluidRegistry.WATER;
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
        this.selected = compound.getInteger(TAG_SELECTED);
        this.started = compound.getBoolean(TAG_STARTED);
        // The saved selection belongs to the saved input
        final ItemStack input = this.inventory.getStackInSlot(INPUT_SLOT);
        this.selectedFor = input.isEmpty() ? ItemStack.EMPTY : ItemHandlerHelper.copyStackWithSize(input, 1);
        this.energy.readFromNBT(compound);
        if (this.water != null) {
            this.water.readFromNBT(compound.getCompoundTag(TAG_WATER));
        }
        this.recipeDirty = true;
    }

    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setTag(TAG_INVENTORY, this.inventory.serializeNBT());
        compound.setInteger(TAG_PROGRESS, this.progress);
        compound.setInteger(TAG_SELECTED, this.selected);
        compound.setBoolean(TAG_STARTED, this.started);
        this.energy.writeToNBT(compound);
        if (this.water != null) {
            compound.setTag(TAG_WATER, this.water.writeToNBT(new NBTTagCompound()));
        }
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
        return state.getBlock() instanceof ProcessingBlock ? state.getValue(BlockHorizontal.FACING) : EnumFacing.NORTH;
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
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && this.water != null && facing == EnumFacing.UP) {
            return true;
        }
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return this.itemAccess(facing) != null;
        }
        return super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull final Capability<T> capability, @Nullable final EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && this.water != null && facing == EnumFacing.UP) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(this.waterAccess);
        }
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
            return ProcessingTileEntity.this.inventory.getStackInSlot(this.slot);
        }

        @Nonnull
        @Override
        public ItemStack insertItem(final int slot, @Nonnull final ItemStack stack, final boolean simulate) {
            return this.insert ? ProcessingTileEntity.this.inventory.insertItem(this.slot, stack, simulate) : stack;
        }

        @Nonnull
        @Override
        public ItemStack extractItem(final int slot, final int amount, final boolean simulate) {
            return this.extract ? ProcessingTileEntity.this.inventory.extractItem(this.slot, amount, simulate) : ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(final int slot) {
            return ProcessingTileEntity.this.inventory.getSlotLimit(this.slot);
        }
    }

    /**
     * A tank that only holds water.
     */
    private static final class WaterTank extends FluidTank {

        WaterTank(final int capacity) {
            super(capacity);
        }

        @Override
        public boolean canFillFluidType(final FluidStack fluid) {
            return fluid != null && fluid.getFluid() == FluidRegistry.WATER;
        }
    }

    /**
     * The tank as pipes see it: water can be piped in, never out.
     */
    private final class WaterAccess implements IFluidHandler {

        @Override
        public IFluidTankProperties[] getTankProperties() {
            final FluidTank tank = ProcessingTileEntity.this.water;
            return tank == null ? EmptyFluidHandler.EMPTY_TANK_PROPERTIES_ARRAY : tank.getTankProperties();
        }

        @Override
        public int fill(final FluidStack resource, final boolean doFill) {
            final FluidTank tank = ProcessingTileEntity.this.water;
            return tank == null ? 0 : tank.fill(resource, doFill);
        }

        @Nullable
        @Override
        public FluidStack drain(final FluidStack resource, final boolean doDrain) {
            return null;
        }

        @Nullable
        @Override
        public FluidStack drain(final int maxDrain, final boolean doDrain) {
            return null;
        }
    }
}
