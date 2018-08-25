/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.item;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.MoreObjects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;

public class BasicVirtualStack implements VirtualStack {

    private final Item item;
    private final int metadata;
    protected int quantity;
    protected NBTTagCompound compound;
    protected ItemStack stack;

    public BasicVirtualStack(final Item item, final int quantity, final int metadata, @Nullable final NBTTagCompound compound) {
        checkNotNull(item);
        checkState(metadata >= 0, "Metadata must be greater than or equal to 0!");

        this.item = item;
        this.quantity = quantity;
        this.metadata = metadata;
        this.compound = compound;

        if (quantity == 0) {
            this.stack = new ItemStack(item, 1, metadata);
        } else {
            this.stack = new ItemStack(item, quantity, metadata);
        }

        this.stack.setTagCompound(compound);
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
    public VirtualStack copy() {
        return new BasicVirtualStack(this.item, this.quantity, this.metadata, this.compound == null ? null : this.compound.copy());
    }

    @Override
    public ItemStack asRealStack() {
        if (this.stack == null) {
            this.stack = new ItemStack(this.item, !this.isEmpty() ? this.quantity : 1, this.metadata);
            this.stack.setTagCompound(this.compound);
        }

        return this.stack;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("item", this.item)
                .add("quantity", this.quantity)
                .add("metadata", this.metadata)
                .add("compound", this.compound)
                .toString();
    }
}
