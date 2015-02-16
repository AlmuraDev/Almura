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

public class Bracelet implements IAccessory<ModelBiped> {

    private ModelRenderer front;
    private ModelRenderer left;
    private ModelRenderer right;
    private ModelRenderer back;

    @Override
    public void onAttached(EntityLivingBase base, ResourceLocation textureLocation, ModelBiped root) {
        front = new ModelRenderer(root, 0, 0);
        front.addBox(4F, 9F, -3F, 4, 1, 1);

        left = new ModelRenderer(root, 10, 0);
        left.addBox(3F, 9F, -3F, 1, 1, 6);

        right = new ModelRenderer(root, 0, 2);
        right.addBox(4F, 9F, 2F, 4, 1, 1);

        back = new ModelRenderer(root, 0, 4);
        back.addBox(8F, 9F, -3F, 1, 1, 6);
    }

    @Override
    public void onRender(EntityLivingBase base, ResourceLocation textureLocation, ModelBiped root, float scale, float partialTicks) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(textureLocation);

        front.rotateAngleY = root.bipedLeftArm.rotateAngleY;
        front.rotateAngleX = root.bipedLeftArm.rotateAngleX;
        front.render(scale);

        left.rotateAngleY = root.bipedLeftArm.rotateAngleY;
        left.rotateAngleX = root.bipedLeftArm.rotateAngleX;
        left.render(scale);

        right.rotateAngleY = root.bipedLeftArm.rotateAngleY;
        right.rotateAngleX = root.bipedLeftArm.rotateAngleX;
        right.render(scale);

        back.rotateAngleY = root.bipedLeftArm.rotateAngleY;
        back.rotateAngleX = root.bipedLeftArm.rotateAngleX;
        back.render(scale);
    }

    @Override
    public String getName() {
        return "bracelet";
    }
}
