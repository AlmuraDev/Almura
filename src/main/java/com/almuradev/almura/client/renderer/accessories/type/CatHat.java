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

public class CatHat implements IAccessory<ModelBiped> {
    private ModelRenderer catHatBase;
    private ModelRenderer catHatTop;

    @Override
    public void onAttached(EntityPlayer player, ResourceLocation textureLocation, ModelBiped root) {
        catHatBase = new ModelRenderer(root, 0, 0);
        catHatBase.addBox(-5F, -10F, -5F, 10, 2, 10);

        catHatTop = new ModelRenderer(root, 0, 12);
        catHatTop.addBox(-4F, -20F, -4F, 8, 10, 8);
    }

    @Override
    public void onRender(EntityPlayer player, ResourceLocation textureLocation, ModelBiped root, float scale, float partialTicks) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(textureLocation);
        catHatBase.rotateAngleY = root.bipedHead.rotateAngleY;
        catHatBase.rotateAngleX = root.bipedHead.rotateAngleX;
        catHatBase.rotationPointX = 0.0F;
        catHatBase.rotationPointY = 0.0F;
        catHatBase.render(scale);
        catHatTop.rotateAngleY = root.bipedHead.rotateAngleY;
        catHatTop.rotateAngleX = root.bipedHead.rotateAngleX;
        catHatTop.rotationPointX = 0.0F;
        catHatTop.rotationPointY = 0.0F;
        catHatTop.render(scale);
    }

    @Override
    public String getName() {
        return "cathat";
    }
}
