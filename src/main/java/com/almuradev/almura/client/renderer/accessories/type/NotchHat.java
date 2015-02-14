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

public class NotchHat implements IAccessory<ModelBiped> {

    private ModelRenderer NotchHatTop;
    private ModelRenderer NotchHatBottom;

    @Override
    public void onAttached(EntityPlayer player, ResourceLocation textureLocation, ModelBiped root) {
        NotchHatTop = new ModelRenderer(root, 0, 0);
        NotchHatTop.addBox(-5F, -9F, -5F, 10, 1, 10);
        NotchHatBottom = new ModelRenderer(root, 0, 11);
        NotchHatBottom.addBox(-4F, -13F, -4F, 8, 4, 8);
    }

    @Override
    public void onRender(EntityPlayer player, ResourceLocation textureLocation, ModelBiped root, float scale, float partialTicks) {
        NotchHatTop.rotateAngleY = root.bipedHead.rotateAngleY;
        NotchHatTop.rotateAngleX = root.bipedHead.rotateAngleX;
        NotchHatTop.rotationPointX = 0.0F;
        NotchHatTop.rotationPointY = 0.0F;
        NotchHatTop.render(scale);
        NotchHatBottom.rotateAngleY = root.bipedHead.rotateAngleY;
        NotchHatBottom.rotateAngleX = root.bipedHead.rotateAngleX;
        NotchHatBottom.rotationPointX = 0.0F;
        NotchHatBottom.rotationPointY = 0.0F;
        NotchHatBottom.render(scale);
    }

    @Override
    public String getName() {
        return "notchhat";
    }
}
