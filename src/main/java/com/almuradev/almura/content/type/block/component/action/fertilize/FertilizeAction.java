/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.type.block.component.action.fertilize;

import com.almuradev.shared.registry.catalog.CatalogDelegate;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.item.ItemType;

import java.util.Set;
import java.util.function.Predicate;

public class FertilizeAction implements Predicate<ItemStack> {

    private final Set<CatalogDelegate<ItemType>> with;

    public FertilizeAction(final Set<CatalogDelegate<ItemType>> with) {
        this.with = with;
    }

    @Override
    public boolean test(final ItemStack stack) {
        return this.with.stream().anyMatch(input -> input.test((ItemType) stack.getItem()));
    }
}
