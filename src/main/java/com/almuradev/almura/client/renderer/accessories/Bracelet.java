/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.renderer.accessories;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.player.EntityPlayer;

public class Bracelet extends Accessory {
    public ModelRenderer bipedFrontBracelet;
    public ModelRenderer bipedLeftBracelet;
    public ModelRenderer bipedBackBracelet;
    public ModelRenderer bipedRightBracelet;

    public Bracelet(ModelBiped mb) {
        super(mb);
        bipedFrontBracelet = new ModelRenderer(mb, 0, 0);
        bipedFrontBracelet.addBox(4F, 9F, -3F, 4, 1, 1);
        bipedLeftBracelet = new ModelRenderer(mb, 10, 0);
        bipedLeftBracelet.addBox(3F, 9F, -3F, 1, 1, 6);
        bipedBackBracelet = new ModelRenderer(mb, 0, 2);
        bipedBackBracelet.addBox(4F, 9F, 2F, 4, 1, 1);
        bipedBackBracelet.setRotationPoint(0F, 0F, 0F);
        bipedRightBracelet = new ModelRenderer(mb, 0, 4);
        bipedRightBracelet.addBox(8F, 9F, -3F, 1, 1, 6);
        bipedRightBracelet.setRotationPoint(0F, 0F, 0F);
    }

    @Override
    public void render(EntityPlayer plr, float f) {
        bipedFrontBracelet.rotateAngleY = getModel().bipedLeftArm.rotateAngleY;
        bipedFrontBracelet.rotateAngleX = getModel().bipedLeftArm.rotateAngleX;
        bipedFrontBracelet.rotationPointX = 0.0F;
        bipedFrontBracelet.rotationPointY = 0.0F;
        bipedFrontBracelet.render(f);
        bipedLeftBracelet.rotateAngleY = getModel().bipedLeftArm.rotateAngleY;
        bipedLeftBracelet.rotateAngleX = getModel().bipedLeftArm.rotateAngleX;
        bipedLeftBracelet.rotationPointX = 0.0F;
        bipedLeftBracelet.rotationPointY = 0.0F;
        bipedLeftBracelet.render(f);
        bipedBackBracelet.rotateAngleY = getModel().bipedLeftArm.rotateAngleY;
        bipedBackBracelet.rotateAngleX = getModel().bipedLeftArm.rotateAngleX;
        bipedBackBracelet.rotationPointX = 0.0F;
        bipedBackBracelet.rotationPointY = 0.0F;
        bipedBackBracelet.render(f);
        bipedRightBracelet.rotateAngleY = getModel().bipedLeftArm.rotateAngleY;
        bipedRightBracelet.rotateAngleX = getModel().bipedLeftArm.rotateAngleX;
        bipedRightBracelet.rotationPointX = 0.0F;
        bipedRightBracelet.rotationPointY = 0.0F;
        bipedRightBracelet.render(f);
    }

    @Override
    public AccessoryType getType() {
        return AccessoryType.BRACELET;
    }
}
