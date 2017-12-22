/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.accessory.type;

import com.almuradev.almura.feature.accessory.AccessoryType;
import com.almuradev.almura.feature.accessory.model.HaloModel;
import net.minecraft.client.model.ModelBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.UUID;

public final class HaloAccessory extends Accessory {

    @SideOnly(Side.CLIENT)
    private HaloModel model;

    public HaloAccessory(UUID holderUniqueId, AccessoryType type) {
        super(holderUniqueId, type);

    }

    @SideOnly(Side.CLIENT)
    public HaloModel getModel(ModelBase rootModel) {
        if (this.model == null) {
            this.model = new HaloModel(this.getType(), rootModel);
        }
        return this.model;
    }
}
