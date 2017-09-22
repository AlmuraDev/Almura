/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.type.block.component.action.fertilize;

import net.minecraft.item.ItemStack;

import java.util.function.Predicate;

public class Fertilizer implements Predicate<ItemStack> {

    @Override
    public boolean test(final ItemStack stack) {
        return false;
    }
}
