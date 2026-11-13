/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.machine.processing;

import net.minecraft.item.ItemStack;

/**
 * One thing a processing machine can make from its input: {@link #inputCount} input items become a copy of {@link #output}, every time or
 * only by {@link #chance} (the input is used up either way).
 */
public final class ProcessingRecipe {

    private final int inputCount;
    private final ItemStack output;
    private final double chance;

    public ProcessingRecipe(final int inputCount, final ItemStack output) {
        this(inputCount, output, 1);
    }

    public ProcessingRecipe(final int inputCount, final ItemStack output, final double chance) {
        this.inputCount = inputCount;
        this.output = output;
        this.chance = chance;
    }

    public int inputCount() {
        return this.inputCount;
    }

    /**
     * @return the chance (0-1) that an operation makes the output; 1 when it always does
     */
    public double chance() {
        return this.chance;
    }

    /**
     * @return a copy of what's made
     */
    public ItemStack output() {
        return this.output.copy();
    }

    ItemStack outputView() {
        return this.output;
    }
}
