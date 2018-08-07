/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.item;

import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;

public final class BasicVirtualStack implements VirtualStack {

    private final Item item;
    private final int quantity, metadata;
    private final NBTTagCompound compound;

    private final ItemStack stack;

    public BasicVirtualStack(final Item item, final int quantity, final int metadata, @Nullable final NBTTagCompound compound) {
        checkNotNull(item);

        this.item = item;
        this.quantity = quantity;
        this.metadata = metadata;
        this.compound = compound;

        if (quantity == 0) {
            this.stack = new ItemStack(item, 1, metadata, compound);
        } else {
            this.stack = new ItemStack(item, quantity, metadata, compound);
        }
    }

    @Override
    public Item getItem() {
        return this.item;
    }

    @Override
    public int getQuantity() {
        return this.quantity;
    }

    @Override
    public int getMetadata() {
        return this.metadata;
    }

    @Nullable
    @Override
    public NBTTagCompound getCompound() {
        if (this.compound == null) {
            return null;
        }

        return this.compound.copy();
    }

    @Override
    public void setCompound(@Nullable NBTTagCompound compound) {
        throw new UnsupportedOperationException("This virtual stack does not support modifying NBT data!");
    }

    @Override
    public VirtualStack copy() {
        return new BasicVirtualStack(this.item, this.quantity, this.metadata, this.compound == null ? null : this.compound.copy());
    }

    @Override
    public ItemStack asRealStack() {
        return this.stack;
    }
}
