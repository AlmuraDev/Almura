/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.item;

import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;

public class BasicDynamicCompoundStack extends BasicVirtualStack implements DynamicCompoundStack {

    public BasicDynamicCompoundStack(final Item item, final int quantity, final int metadata, @Nullable final NBTTagCompound compound) {
        super(item, quantity, metadata, compound);
    }

    @Override
    public void setCompound(@Nullable final NBTTagCompound compound) {
        if (compound == null) {
            this.compound = null;
        } else {
            this.compound = compound.copy();
        }

        this.stack = null;
    }

    @Override
    public DynamicCompoundStack copy() {
        return new BasicDynamicCompoundStack(this.getItem(), this.quantity, this.getMetadata(), this.compound == null ? null : this.compound.copy());
    }
}
