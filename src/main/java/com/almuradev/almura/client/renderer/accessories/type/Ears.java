/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.renderer.accessories.type;

import com.almuradev.almura.client.renderer.accessories.Accessory;
import com.almuradev.almura.client.renderer.accessories.AccessoryType;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.player.EntityPlayer;

public class Ears extends Accessory {
    public ModelRenderer bipedEars2;

    public Ears(ModelBiped mb) {
        super(mb);
        bipedEars2 = new ModelRenderer(mb, 0, 0);
        bipedEars2.addBox(-3F, -6F, -1F, 6, 6, 1);
    }

    @Override
    public void render(EntityPlayer plr, float f) {
        bipedEars2.rotateAngleY = getModel().bipedHead.rotateAngleY;
        bipedEars2.rotateAngleX = getModel().bipedHead.rotateAngleX;
        bipedEars2.rotationPointX = 0.0F;
        bipedEars2.rotationPointY = 0.0F;
        bipedEars2.render(f);
    }

    @Override
    public AccessoryType getType() {
        return AccessoryType.EARS;
    }
}
