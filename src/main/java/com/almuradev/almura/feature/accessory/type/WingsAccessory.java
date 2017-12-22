package com.almuradev.almura.feature.accessory.type;

import com.almuradev.almura.feature.accessory.AccessoryType;
import com.almuradev.almura.feature.accessory.model.WingsModel;
import net.minecraft.client.model.ModelBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.UUID;

public final class WingsAccessory extends Accessory {

    public WingsAccessory(UUID holderUniqueId, AccessoryType type) {
        super(holderUniqueId, type);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ModelBase getModel(ModelBase rootModel) {
        return new WingsModel(this.getType(), rootModel);
    }
}
