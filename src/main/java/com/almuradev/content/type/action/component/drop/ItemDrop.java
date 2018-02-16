/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.action.component.drop;

import com.almuradev.content.type.item.definition.ItemDefinition;
import com.almuradev.toolbox.util.math.DoubleRange;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@Immutable
public final class ItemDrop extends Drop {
    private final ItemDefinition item;

    public ItemDrop(final DoubleRange amount, @Nullable final DoubleRange bonusAmount, @Nullable final DoubleRange bonusChance, final ItemDefinition item) {
        super(amount, bonusAmount, bonusChance);
        this.item = item;
    }

    public void fill(final List<ItemStack> items, final Random random) {
        final ItemStack drop = this.item.create();
        drop.setCount(this.flooredAmount(random));
        items.add(drop);
    }
}
