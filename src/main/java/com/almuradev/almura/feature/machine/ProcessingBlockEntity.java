/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.machine;

import com.almuradev.almura.feature.machine.recipe.ProcessingRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * A processing machine, as 3.5's {@code ProcessingTileEntity}: turns its input into what its {@link MachineType}'s crafting recipes make,
 * over {@link #PROCESS_TICKS} ticks. Progress starts over when the input goes or nothing more fits. When the input has several recipes the
 * player picks the output on its screen (and the kitchen machines wait for Start). 3.5's machines ran on IndustrialCraft 2's power when it
 * was installed, free otherwise; 4.0 has no IC2, so they're free.
 * <p>
 * Hoppers, as seen facing the front: the input goes in on the left, the output comes out on the right.
 */
public final class ProcessingBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {

    public static final int INPUT_SLOT = 0, OUTPUT_SLOT = 1;
    // About 1.65 seconds per operation
    public static final int PROCESS_TICKS = 33;
    // What the screen is told: progress, the selected output, Start pressed, and how many outputs there are to choose from
    public static final int DATA_PROGRESS = 0, DATA_SELECTED = 1, DATA_STARTED = 2, DATA_CHOICES = 3, DATA_COUNT = 4;
    private static final int[] INPUT = {INPUT_SLOT}, OUTPUT = {OUTPUT_SLOT}, NONE = {};
    private static final String PROGRESS = "Progress", SELECTED = "Selected", STARTED = "Started";

    private NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);
    // What the selected recipe makes, shown on the screen (not kept)
    private final SimpleContainer preview = new SimpleContainer(1);
    private int progress;
    // Which of the input's recipes is made (the screen's output selector)
    private int selected;
    // Whether the player pressed Start (only needed when the input has a choice, see MachineType#startOnChoice)
    private boolean started;
    // The selected recipe and how many there were, worked out again when the input or the selection changes
    private @Nullable ProcessingRecipe recipe;
    private int choices;
    private boolean recipeDirty = true;
    // The input the selection was made for: a different item starts again at its first output (and has to be started again)
    private ItemStack selectedFor = ItemStack.EMPTY;
    /** Client only: the angle of the machine's spinning part, kept when it stops so the part doesn't snap back. */
    public float spinAngle;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(final int index) {
            final ProcessingBlockEntity machine = ProcessingBlockEntity.this;
            return switch (index) {
                case DATA_PROGRESS -> machine.progress;
                case DATA_SELECTED -> machine.selected;
                case DATA_STARTED -> machine.started ? 1 : 0;
                case DATA_CHOICES -> machine.choices;
                default -> 0;
            };
        }

        @Override
        public void set(final int index, final int value) {
            // The server's values only
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    public ProcessingBlockEntity(final BlockPos pos, final BlockState state) {
        super(Machines.blockEntityType(), pos, state);
    }

    public MachineType type() {
        return this.getBlockState().getBlock() instanceof final ProcessingBlock block ? block.type() : MachineType.SEED_PROCESSOR;
    }

    // ---- Working

    void serverTick(final ServerLevel level) {
        final RecipeManager recipes = level.recipeAccess();
        this.refreshRecipe(recipes);
        boolean worked = false;
        if (!this.canProcess()) {
            // Nothing to make (or nowhere to put it): start over next time, like a furnace
            if (this.progress != 0) {
                this.progress = 0;
                this.setChanged();
            }
        } else {
            worked = true;
            this.progress++;
            if (this.progress >= PROCESS_TICKS) {
                this.process();
                this.progress = 0;
                // The input just changed: check against what's left
                this.refreshRecipe(recipes);
            }
            this.setChanged();
        }
        // Working, and able to keep going: checked after an operation, so it stays on straight into the next instead of flickering
        final boolean active = worked && this.canProcess();
        final BlockState state = this.getBlockState();
        if (state.getBlock() instanceof ProcessingBlock && state.getValue(ProcessingBlock.ACTIVE) != active) {
            level.setBlock(this.worldPosition, state.setValue(ProcessingBlock.ACTIVE, active), Block.UPDATE_ALL);
        }
    }

    private void refreshRecipe(final RecipeManager recipes) {
        if (!this.recipeDirty) {
            return;
        }
        this.recipeDirty = false;
        final ItemStack input = this.items.get(INPUT_SLOT);
        // An empty slot keeps the selection (and Start), so refills of the same item carry on; a different item starts over
        if (!input.isEmpty() && !ItemStack.isSameItemSameComponents(input, this.selectedFor)) {
            this.selected = 0;
            this.started = false;
            this.selectedFor = input.copyWithCount(1);
        }
        final List<ProcessingRecipe> found = this.type().recipes().recipesFor(recipes, input);
        this.choices = found.size();
        if (found.isEmpty()) {
            this.recipe = null;
        } else {
            if (this.selected >= found.size()) {
                this.selected = 0;
            }
            this.recipe = found.get(this.selected);
        }
        this.preview.setItem(0, this.recipe == null ? ItemStack.EMPTY : this.recipe.output());
    }

    private boolean needsStart() {
        return this.type().startOnChoice() && this.choices > 1;
    }

    private boolean canProcess() {
        if (this.recipe == null || this.items.get(INPUT_SLOT).getCount() < this.recipe.inputCount()) {
            return false;
        }
        // A choice of outputs: wait until the player has picked one and pressed Start
        if (this.needsStart() && !this.started) {
            return false;
        }
        final ItemStack output = this.recipe.output();
        final ItemStack current = this.items.get(OUTPUT_SLOT);
        return current.isEmpty() || ItemStack.isSameItemSameComponents(current, output)
            && current.getCount() + output.getCount() <= Math.min(current.getMaxStackSize(), this.getMaxStackSize());
    }

    private void process() {
        final ProcessingRecipe recipe = this.recipe;
        if (recipe == null) {
            return;
        }
        final ItemStack output = recipe.output();
        final ItemStack current = this.items.get(OUTPUT_SLOT);
        if (current.isEmpty()) {
            this.items.set(OUTPUT_SLOT, output);
        } else {
            current.grow(output.getCount());
        }
        this.items.get(INPUT_SLOT).shrink(recipe.inputCount());
        this.recipeDirty = true;
    }

    /**
     * The previous ({@code -1}) or next ({@code 1}) output for the input, starting the operation over.
     */
    void cycleSelection(final int direction) {
        if (this.choices < 2) {
            return;
        }
        this.selected = Math.floorMod(this.selected + direction, this.choices);
        this.progress = 0;
        this.recipeDirty = true;
        this.setChanged();
    }

    /**
     * Start / Stop from the screen. Stopping throws away the operation's progress.
     */
    void toggleStarted() {
        this.started = !this.started;
        if (!this.started) {
            this.progress = 0;
        }
        this.setChanged();
    }

    // ---- Container

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(final NonNullList<ItemStack> items) {
        this.items = items;
        this.recipeDirty = true;
    }

    @Override
    public int getContainerSize() {
        return 2;
    }

    @Override
    public void setItem(final int slot, final ItemStack stack) {
        super.setItem(slot, stack);
        if (slot == INPUT_SLOT) {
            this.recipeDirty = true;
        }
    }

    @Override
    public ItemStack removeItem(final int slot, final int count) {
        final ItemStack removed = super.removeItem(slot, count);
        if (slot == INPUT_SLOT) {
            this.recipeDirty = true;
        }
        return removed;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        // Slots change their stack in place (a player adding to a stack); the recipe is checked again
        this.recipeDirty = true;
    }

    @Override
    public boolean canPlaceItem(final int slot, final ItemStack stack) {
        return slot == INPUT_SLOT && this.level instanceof final ServerLevel serverLevel
            && this.type().recipes().isInput(serverLevel.recipeAccess(), stack);
    }

    @Override
    public int[] getSlotsForFace(final Direction side) {
        final Direction front = this.getBlockState().hasProperty(ProcessingBlock.FACING)
            ? this.getBlockState().getValue(ProcessingBlock.FACING) : Direction.NORTH;
        if (side == front.getClockWise()) {
            return INPUT;
        }
        return side == front.getCounterClockWise() ? OUTPUT : NONE;
    }

    @Override
    public boolean canPlaceItemThroughFace(final int slot, final ItemStack stack, final @Nullable Direction side) {
        return slot == INPUT_SLOT && this.canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(final int slot, final ItemStack stack, final Direction side) {
        return slot == OUTPUT_SLOT;
    }

    @Override
    protected Component getDefaultName() {
        return this.getBlockState().getBlock().getName();
    }

    @Override
    protected AbstractContainerMenu createMenu(final int containerId, final Inventory inventory) {
        return new ProcessingMenu(Machines.menuType(), containerId, inventory, this, this.preview, this.data, this.type());
    }

    // ---- Saved

    @Override
    protected void saveAdditional(final ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.items);
        output.putInt(PROGRESS, this.progress);
        output.putInt(SELECTED, this.selected);
        output.putBoolean(STARTED, this.started);
    }

    @Override
    protected void loadAdditional(final ValueInput input) {
        super.loadAdditional(input);
        this.items = NonNullList.withSize(2, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, this.items);
        this.progress = input.getIntOr(PROGRESS, 0);
        this.selected = input.getIntOr(SELECTED, 0);
        this.started = input.getBooleanOr(STARTED, false);
        // The saved selection belongs to the saved input
        final ItemStack in = this.items.get(INPUT_SLOT);
        this.selectedFor = in.isEmpty() ? ItemStack.EMPTY : in.copyWithCount(1);
        this.recipeDirty = true;
    }
}
