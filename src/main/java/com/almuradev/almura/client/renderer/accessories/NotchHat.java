/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.renderer.accessories;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.player.EntityPlayer;

public class NotchHat extends Accessory {
    public ModelRenderer NotchHatTop;
    public ModelRenderer NotchHatBottom;

    public NotchHat(ModelBiped mb) {
        super(mb);
        NotchHatTop = new ModelRenderer(mb, 0, 0);
        NotchHatTop.addBox(-5F, -9F, -5F, 10, 1, 10);
        NotchHatBottom = new ModelRenderer(mb, 0, 11);
        NotchHatBottom.addBox(-4F, -13F, -4F, 8, 4, 8);
    }

    @Override
    public void render(EntityPlayer plr, float f) {
        NotchHatTop.rotateAngleY = getModel().bipedHead.rotateAngleY;
        NotchHatTop.rotateAngleX = getModel().bipedHead.rotateAngleX;
        NotchHatTop.rotationPointX = 0.0F;
        NotchHatTop.rotationPointY = 0.0F;
        NotchHatTop.render(f);
        NotchHatBottom.rotateAngleY = getModel().bipedHead.rotateAngleY;
        NotchHatBottom.rotateAngleX = getModel().bipedHead.rotateAngleX;
        NotchHatBottom.rotationPointX = 0.0F;
        NotchHatBottom.rotationPointY = 0.0F;
        NotchHatBottom.render(f);
    }

    @Override
    public AccessoryType getType() {
        return AccessoryType.NOTCHHAT;
    }
}
