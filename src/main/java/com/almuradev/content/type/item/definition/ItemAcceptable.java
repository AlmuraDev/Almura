/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item.definition;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.item.ItemType;

import java.util.function.Predicate;

public interface ItemAcceptable extends Predicate<ItemStack> {

    @Override
    boolean test(ItemStack stack);

    @Deprecated
    default boolean test(ItemType item) {
        return test(new ItemStack((Item) item));
    }
}
