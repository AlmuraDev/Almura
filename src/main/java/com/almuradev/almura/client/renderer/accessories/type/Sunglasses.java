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

public class Sunglasses implements IAccessory<ModelBiped> {

    private ModelRenderer sunglassesFront;
    private ModelRenderer sunglassesFront2;
    private ModelRenderer sunglassesBridge;
    private ModelRenderer rightSunglasses;
    private ModelRenderer leftSunglasses;
    private ModelRenderer rightSunglassesBridge;
    private ModelRenderer leftSunglassesBridge;

    @Override
    public void onAttached(EntityPlayer player, ResourceLocation textureLocation, ModelBiped root) {
        sunglassesFront = new ModelRenderer(root, 0, 0);
        sunglassesFront.addBox(-3F, -4F, -5F, 2, 2, 1);
        sunglassesFront.setTextureSize(32, 32);

        sunglassesFront2 = new ModelRenderer(root, 6, 0);
        sunglassesFront2.addBox(1F, -4F, -5F, 2, 2, 1);
        sunglassesFront2.setTextureSize(32, 32);

        sunglassesBridge = new ModelRenderer(root, 0, 4);
        sunglassesBridge.addBox(-1F, -4F, -5F, 2, 1, 1);
        sunglassesBridge.setTextureSize(32, 32);

        rightSunglasses = new ModelRenderer(root, 0, 6);
        rightSunglasses.addBox(4.0F, -4.0F, -4.0F, 1, 1, 4);
        rightSunglasses.setTextureSize(32, 32);

        rightSunglassesBridge = new ModelRenderer(root, 6, 7);
        rightSunglassesBridge.addBox(3.0F, -4.0F, -5.0F, 2, 1, 1);
        rightSunglassesBridge.setTextureSize(32, 32);

        leftSunglasses = new ModelRenderer(root, 12, 0);
        leftSunglasses.addBox(-5.0F, -4.0F, -4.0F, 1, 1, 4);
        leftSunglasses.setTextureSize(32, 32);

        leftSunglassesBridge = new ModelRenderer(root, 6, 5);
        leftSunglassesBridge.addBox(-5.0F, -4.0F, -5.0F, 2, 1, 1);
        leftSunglassesBridge.setTextureSize(32, 32);
    }

    @Override
    public void onRender(EntityPlayer player, ResourceLocation textureLocation, ModelBiped root, float scale, float partialTicks) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(textureLocation);
        sunglassesFront.rotateAngleY = root.bipedHead.rotateAngleY;
        sunglassesFront.rotateAngleX = root.bipedHead.rotateAngleX;
        sunglassesFront.render(scale);
        sunglassesFront2.rotateAngleY = root.bipedHead.rotateAngleY;
        sunglassesFront2.rotateAngleX = root.bipedHead.rotateAngleX;
        sunglassesFront2.render(scale);
        sunglassesBridge.rotateAngleY = root.bipedHead.rotateAngleY;
        sunglassesBridge.rotateAngleX = root.bipedHead.rotateAngleX;
        sunglassesBridge.render(scale);
        rightSunglasses.rotateAngleY = root.bipedHead.rotateAngleY;
        rightSunglasses.rotateAngleX = root.bipedHead.rotateAngleX;
        rightSunglasses.render(scale);
        leftSunglasses.rotateAngleY = root.bipedHead.rotateAngleY;
        leftSunglasses.rotateAngleX = root.bipedHead.rotateAngleX;
        leftSunglasses.render(scale);
        leftSunglassesBridge.rotateAngleY = root.bipedHead.rotateAngleY;
        leftSunglassesBridge.rotateAngleX = root.bipedHead.rotateAngleX;
        leftSunglassesBridge.render(scale);
        rightSunglassesBridge.rotateAngleY = root.bipedHead.rotateAngleY;
        rightSunglassesBridge.rotateAngleX = root.bipedHead.rotateAngleX;
        rightSunglassesBridge.render(scale);
    }

    @Override
    public String getName() {
        return "sunglasses";
    }
}
