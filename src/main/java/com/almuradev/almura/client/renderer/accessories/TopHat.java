/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.renderer.accessories;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.player.EntityPlayer;

public class TopHat extends Accessory {
    public ModelRenderer bipedBottomHat;
    public ModelRenderer bipedTopHat;

    public TopHat(ModelBiped model) {
        super(model);
        bipedBottomHat = new ModelRenderer(model, 0, 0);
        bipedBottomHat.addBox(-5.5F, -9F, -5.5F, 11, 2, 11);
        bipedTopHat = new ModelRenderer(model, 0, 13);
        bipedTopHat.addBox(-3.5F, -17F, -3.5F, 7, 8, 7);
    }

    @Override
    public void render(EntityPlayer plr, float f) {
        bipedBottomHat.rotateAngleY = getModel().bipedHead.rotateAngleY;
        bipedBottomHat.rotateAngleX = getModel().bipedHead.rotateAngleX;
        bipedBottomHat.rotationPointX = 0.0F;
        bipedBottomHat.rotationPointY = 0.0F;
        bipedBottomHat.render(f);
        bipedTopHat.rotateAngleY = getModel().bipedHead.rotateAngleY;
        bipedTopHat.rotateAngleX = getModel().bipedHead.rotateAngleX;
        bipedTopHat.rotationPointX = 0.0F;
        bipedTopHat.rotationPointY = 0.0F;
        bipedTopHat.render(f);
    }

    @Override
    public AccessoryType getType() {
        return AccessoryType.TOPHAT;
    }
}
