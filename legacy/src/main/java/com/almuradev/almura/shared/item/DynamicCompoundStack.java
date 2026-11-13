/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.item;

import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;

public interface DynamicCompoundStack extends VirtualStack {

    /**
     * Sets the {@link NBTTagCompound}. The given compound will be copied if not null.
     *
     * @param compound The compound
     */
    void setCompound(@Nullable final NBTTagCompound compound);

    @Override
    DynamicCompoundStack copy();
}
