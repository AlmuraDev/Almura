/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.accessory.type;

import com.almuradev.almura.feature.accessory.AccessoryType;
import com.google.common.base.MoreObjects;
import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Objects;
import java.util.UUID;

public abstract class Accessory {

    private final UUID holderUniqueId;
    private final AccessoryType type;

    protected boolean isAttached = false;

    protected Accessory(UUID holderUniqueId, AccessoryType type) {
        this.holderUniqueId = holderUniqueId;
        this.type = type;
    }

    public UUID getHolderUniqueId() {
        return holderUniqueId;
    }

    public AccessoryType getType() {
        return this.type;
    }

    public boolean isAttached() {
        return this.isAttached;
    }

    @SideOnly(Side.CLIENT)
    public abstract ModelBase getModel(ModelBase rootModel);

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Accessory accessory = (Accessory) o;
        return Objects.equals(holderUniqueId, accessory.holderUniqueId) &&
                Objects.equals(type, accessory.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(holderUniqueId, type);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("holderUniqueId", this.holderUniqueId)
                .add("type", this.type)
                .toString();
    }
}
