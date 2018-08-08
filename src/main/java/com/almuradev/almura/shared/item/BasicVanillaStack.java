/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;

public class BasicVanillaStack extends BasicDynamicCompoundStack implements VanillaStack {

    public BasicVanillaStack(final Item item, final int quantity, final int metadata, @Nullable final NBTTagCompound compound) {
        super(item, quantity, metadata, compound);
    }

    public BasicVanillaStack(final ItemStack stack) {
        this(stack.getItem(), stack.getCount(), stack.getMetadata(), stack.getTagCompound());
    }

    @Override
    public void setQuantity(int quantity) {
        this.quantity = quantity;

        this.stack = null;
    }

    @Override
    public VanillaStack copy() {
        return new BasicVanillaStack(this.getItem(), this.quantity, this.getMetadata(), this.compound == null ? null : this.compound.copy());
    }
}
