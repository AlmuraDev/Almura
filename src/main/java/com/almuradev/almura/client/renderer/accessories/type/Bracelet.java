/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.renderer.accessories.type;

import com.almuradev.almura.client.renderer.accessories.IAccessory;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class Bracelet implements IAccessory<ModelBiped> {

    private ModelRenderer bipedFrontBracelet;
    private ModelRenderer bipedLeftBracelet;
    private ModelRenderer bipedBackBracelet;
    private ModelRenderer bipedRightBracelet;

    @Override
    public void onAttached(EntityPlayer player, ResourceLocation textureLocation, ModelBiped root) {
        bipedFrontBracelet = new ModelRenderer(root, 0, 0);
        bipedFrontBracelet.addBox(4F, 9F, -3F, 4, 1, 1);
        bipedLeftBracelet = new ModelRenderer(root, 10, 0);
        bipedLeftBracelet.addBox(3F, 9F, -3F, 1, 1, 6);
        bipedBackBracelet = new ModelRenderer(root, 0, 2);
        bipedBackBracelet.addBox(4F, 9F, 2F, 4, 1, 1);
        bipedBackBracelet.setRotationPoint(0F, 0F, 0F);
        bipedRightBracelet = new ModelRenderer(root, 0, 4);
        bipedRightBracelet.addBox(8F, 9F, -3F, 1, 1, 6);
        bipedRightBracelet.setRotationPoint(0F, 0F, 0F);
    }

    @Override
    public void onRender(EntityPlayer player, ResourceLocation textureLocation, ModelBiped root, float partialTicks) {
        bipedFrontBracelet.rotateAngleY = root.bipedLeftArm.rotateAngleY;
        bipedFrontBracelet.rotateAngleX = root.bipedLeftArm.rotateAngleX;
        bipedFrontBracelet.rotationPointX = 0.0F;
        bipedFrontBracelet.rotationPointY = 0.0F;
        bipedFrontBracelet.render(partialTicks);
        bipedLeftBracelet.rotateAngleY = root.bipedLeftArm.rotateAngleY;
        bipedLeftBracelet.rotateAngleX = root.bipedLeftArm.rotateAngleX;
        bipedLeftBracelet.rotationPointX = 0.0F;
        bipedLeftBracelet.rotationPointY = 0.0F;
        bipedLeftBracelet.render(partialTicks);
        bipedBackBracelet.rotateAngleY = root.bipedLeftArm.rotateAngleY;
        bipedBackBracelet.rotateAngleX = root.bipedLeftArm.rotateAngleX;
        bipedBackBracelet.rotationPointX = 0.0F;
        bipedBackBracelet.rotationPointY = 0.0F;
        bipedBackBracelet.render(partialTicks);
        bipedRightBracelet.rotateAngleY = root.bipedLeftArm.rotateAngleY;
        bipedRightBracelet.rotateAngleX = root.bipedLeftArm.rotateAngleX;
        bipedRightBracelet.rotationPointX = 0.0F;
        bipedRightBracelet.rotationPointY = 0.0F;
        bipedRightBracelet.render(partialTicks);
    }

    @Override
    public String getName() {
        return "bracelet";
    }
}
