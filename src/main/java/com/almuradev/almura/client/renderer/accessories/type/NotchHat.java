/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2015 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.renderer.accessories.type;

import com.almuradev.almura.client.renderer.accessories.IAccessory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

public class NotchHat implements IAccessory<ModelBiped> {

    private ModelRenderer bottom;
    private ModelRenderer top;

    @Override
    public void onAttached(EntityLivingBase base, ResourceLocation textureLocation, ModelBiped root) {
        bottom = new ModelRenderer(root, 0, 0);
        bottom.addBox(-5F, -9F, -5F, 10, 1, 10);

        top = new ModelRenderer(root, 0, 11);
        top.addBox(-4F, -13F, -4F, 8, 4, 8);
    }

    @Override
    public void onRender(EntityLivingBase base, ResourceLocation textureLocation, ModelBiped root, float scale, float partialTicks) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(textureLocation);

        bottom.rotateAngleY = root.bipedHead.rotateAngleY;
        bottom.rotateAngleX = root.bipedHead.rotateAngleX;
        bottom.render(scale);

        top.rotateAngleY = root.bipedHead.rotateAngleY;
        top.rotateAngleX = root.bipedHead.rotateAngleX;
        top.render(scale);
    }

    @Override
    public String getName() {
        return "notchhat";
    }
}
