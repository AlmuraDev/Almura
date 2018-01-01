/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.component.apply.context;

import net.minecraft.item.ItemStack;

import java.util.Random;

public final class ItemOnlyApplyContext implements ItemApplyContext {

    private final ItemStack itemStack;
    private final Random random;

    public ItemOnlyApplyContext(ItemStack itemStack, Random random) {
        this.itemStack = itemStack;
        this.random = random;
    }

    @Override
    public ItemStack item() {
        return this.itemStack;
    }

    @Override
    public Random random() {
        return this.random;
    }
}
