/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.material;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;

public final class MaterialImpl extends Material {

    private boolean isLiquid = false, isSolid = false;

    public MaterialImpl(final MapColor color) {
        super(color);
    }

    @Override
    public boolean isLiquid() {
        return this.isLiquid;
    }

    public void setLiquid(final boolean value) {
        this.isLiquid = value;
    }

    @Override
    public boolean isSolid() {
        return this.isSolid;
    }

    public void setSolid(final boolean value) {
        this.isSolid = value;
    }

    @Override
    public Material setRequiresTool() {
        return super.setRequiresTool();
    }

    @Override
    public Material setNoPushMobility() {
        return super.setNoPushMobility();
    }

    @Override
    public Material setImmovableMobility() {
        return super.setImmovableMobility();
    }
}
