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

public class NotchHat implements IAccessory<ModelBiped> {

    private ModelRenderer notchHatTop;
    private ModelRenderer notchHatBottom;

    @Override
    public void onAttached(EntityPlayer player, ResourceLocation textureLocation, ModelBiped root) {
        notchHatTop = new ModelRenderer(root, 0, 0);
        notchHatTop.addBox(-5F, -9F, -5F, 10, 1, 10);

        notchHatBottom = new ModelRenderer(root, 0, 11);
        notchHatBottom.addBox(-4F, -13F, -4F, 8, 4, 8);
    }

    @Override
    public void onRender(EntityPlayer player, ResourceLocation textureLocation, ModelBiped root, float scale, float partialTicks) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(textureLocation);
        notchHatTop.rotateAngleY = root.bipedHead.rotateAngleY;
        notchHatTop.rotateAngleX = root.bipedHead.rotateAngleX;
        notchHatTop.rotationPointX = 0.0F;
        notchHatTop.rotationPointY = 0.0F;
        notchHatTop.render(scale);
        notchHatBottom.rotateAngleY = root.bipedHead.rotateAngleY;
        notchHatBottom.rotateAngleX = root.bipedHead.rotateAngleX;
        notchHatBottom.rotationPointX = 0.0F;
        notchHatBottom.rotationPointY = 0.0F;
        notchHatBottom.render(scale);
    }

    @Override
    public String getName() {
        return "notchhat";
    }
}
