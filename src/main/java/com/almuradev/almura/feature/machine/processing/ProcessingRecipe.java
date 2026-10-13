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
 * One thing a processing machine can make from its input: {@link #inputCount} input items become a copy of {@link #output}.
 */
public final class ProcessingRecipe {

    private final int inputCount;
    private final ItemStack output;

    public ProcessingRecipe(final int inputCount, final ItemStack output) {
        this.inputCount = inputCount;
        this.output = output;
    }

    public int inputCount() {
        return this.inputCount;
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
