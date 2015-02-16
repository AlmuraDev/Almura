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
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

public class Halo implements IAccessory<ModelBiped> {
    private ModelRenderer right;
    private ModelRenderer front;
    private ModelRenderer left;
    private ModelRenderer back;

    @Override
    public void onAttached(EntityLivingBase base, ResourceLocation textureLocation, ModelBiped root) {
        right = new ModelRenderer(root, 0, 2);
        right.addBox(-3F, -14F, -2F, 1, 1, 4);

        front = new ModelRenderer(root, 0, 0);
        front.addBox(-2F, -14F, -3F, 4, 1, 1);

        left = new ModelRenderer(root, 0, 9);
        left.addBox(2F, -14F, -2F, 1, 1, 4);

        back = new ModelRenderer(root, 0, 7);
        back.addBox(-2F, -14F, 2F, 4, 1, 1);
    }

    @Override
    public void onRender(EntityLivingBase base, ResourceLocation textureLocation, ModelBiped root, float scale, float partialTicks) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(textureLocation);

        right.rotateAngleY = root.bipedHead.rotateAngleY;
        right.rotateAngleX = root.bipedHead.rotateAngleX;
        right.render(scale);

        front.rotateAngleY = root.bipedHead.rotateAngleY;
        front.rotateAngleX = root.bipedHead.rotateAngleX;
        front.render(scale);

        left.rotateAngleY = root.bipedHead.rotateAngleY;
        left.rotateAngleX = root.bipedHead.rotateAngleX;
        left.render(scale);

        back.rotateAngleY = root.bipedHead.rotateAngleY;
        back.rotateAngleX = root.bipedHead.rotateAngleX;
        back.render(scale);
    }

    @Override
    public String getName() {
        return "halo";
    }
}
