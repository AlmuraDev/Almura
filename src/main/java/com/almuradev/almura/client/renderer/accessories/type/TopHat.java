/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.renderer.accessories.type;

import com.almuradev.almura.client.renderer.accessories.IAccessory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class TopHat implements IAccessory<ModelBiped> {

    private ModelRenderer bipedBottomHat;
    private ModelRenderer bipedTopHat;
    
    @Override
    public void onAttached(EntityPlayer player, ResourceLocation textureLocation, ModelBiped root) {
        bipedBottomHat = new ModelRenderer(root, 0, 0);
        bipedBottomHat.addBox(-5.5F, -9F, -5.5F, 11, 2, 11);
        bipedTopHat = new ModelRenderer(root, 0, 13);
        bipedTopHat.addBox(-3.5F, -17F, -3.5F, 7, 8, 7);   
    }

    @Override
    public void onRender(EntityPlayer player, ResourceLocation textureLocation, ModelBiped root, float scale, float partialTicks) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(textureLocation);
        bipedBottomHat.rotateAngleY = root.bipedHead.rotateAngleY;
        bipedBottomHat.rotateAngleX = root.bipedHead.rotateAngleX;
        bipedBottomHat.rotationPointX = 0.0F;
        bipedBottomHat.rotationPointY = 0.0F;
        bipedBottomHat.render(scale);
        bipedTopHat.rotateAngleY = root.bipedHead.rotateAngleY;
        bipedTopHat.rotateAngleX = root.bipedHead.rotateAngleX;
        bipedTopHat.rotationPointX = 0.0F;
        bipedTopHat.rotationPointY = 0.0F;
        bipedTopHat.render(scale);
    }

    @Override
    public String getName() {
        return "tophat";
    }
}
