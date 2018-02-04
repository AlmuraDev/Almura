/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.action.component.drop;

import com.almuradev.content.type.item.definition.ItemDefinition;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.util.weighted.VariableAmount;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@Immutable
public final class ItemDrop extends Drop {

    private final ItemDefinition item;
    private final VariableAmount amount;
    private final Random random = new Random();

    public ItemDrop(final VariableAmount amount, @Nullable final VariableAmount bonusAmount, @Nullable final VariableAmount bonusChance, final ItemDefinition item) {
        super(amount, bonusAmount, bonusChance);
        this.amount = amount;
        this.item = item;
    }

    public void fill(final List<ItemStack> items) {
        final ItemStack drop = this.item.create();
        drop.setCount(amount.getFlooredAmount(random));
        items.add(drop);
    }
}
