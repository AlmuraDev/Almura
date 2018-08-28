/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.item;

import net.minecraft.item.ItemStack;

/**
 * A close representation of the blueprint of an {@link ItemStack}.
 */
public interface VanillaStack extends DynamicCompoundStack {

    /**
     * Sets the quantity.
     *
     * @param quantity
     */
    void setQuantity(final int quantity);

    @Override
    VanillaStack copy();
}
