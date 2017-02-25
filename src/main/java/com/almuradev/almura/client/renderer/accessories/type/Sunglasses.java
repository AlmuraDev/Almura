/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2017 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.renderer.accessories.type;

import com.almuradev.almura.client.renderer.accessories.IAccessory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

public class Sunglasses implements IAccessory<ModelBiped> {

    private ModelRenderer shadeRight;
    private ModelRenderer shadeLeft;
    private ModelRenderer bridge;
    private ModelRenderer leftTemple;
    private ModelRenderer rightTemple;
    private ModelRenderer leftTempleBridge;
    private ModelRenderer rightTempleBridge;

    @Override
    public void onAttached(EntityLivingBase base, ResourceLocation textureLocation, ModelBiped root) {
        shadeRight = new ModelRenderer(root, 0, 0);
        shadeRight.addBox(-3F, -4F, -5F, 2, 2, 1);

        shadeLeft = new ModelRenderer(root, 6, 0);
        shadeLeft.addBox(1F, -4F, -5F, 2, 2, 1);

        bridge = new ModelRenderer(root, 0, 4);
        bridge.addBox(-1F, -4F, -5F, 2, 1, 1);

        leftTemple = new ModelRenderer(root, 0, 6);
        leftTemple.addBox(4.0F, -4.0F, -4.0F, 1, 1, 4);

        leftTempleBridge = new ModelRenderer(root, 6, 7);
        leftTempleBridge.addBox(3.0F, -4.0F, -5.0F, 2, 1, 1);

        rightTemple = new ModelRenderer(root, 12, 0);
        rightTemple.addBox(-5.0F, -4.0F, -4.0F, 1, 1, 4);

        rightTempleBridge = new ModelRenderer(root, 6, 5);
        rightTempleBridge.addBox(-5.0F, -4.0F, -5.0F, 2, 1, 1);
    }

    @Override
    public void onRender(EntityLivingBase base, ResourceLocation textureLocation, ModelBiped root, float scale, float partialTicks) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(textureLocation);

        shadeRight.rotateAngleY = root.bipedHead.rotateAngleY;
        shadeRight.rotateAngleX = root.bipedHead.rotateAngleX;
        shadeRight.render(scale);

        shadeLeft.rotateAngleY = root.bipedHead.rotateAngleY;
        shadeLeft.rotateAngleX = root.bipedHead.rotateAngleX;
        shadeLeft.render(scale);

        bridge.rotateAngleY = root.bipedHead.rotateAngleY;
        bridge.rotateAngleX = root.bipedHead.rotateAngleX;
        bridge.render(scale);

        leftTemple.rotateAngleY = root.bipedHead.rotateAngleY;
        leftTemple.rotateAngleX = root.bipedHead.rotateAngleX;
        leftTemple.render(scale);

        rightTemple.rotateAngleY = root.bipedHead.rotateAngleY;
        rightTemple.rotateAngleX = root.bipedHead.rotateAngleX;
        rightTemple.render(scale);

        rightTempleBridge.rotateAngleY = root.bipedHead.rotateAngleY;
        rightTempleBridge.rotateAngleX = root.bipedHead.rotateAngleX;
        rightTempleBridge.render(scale);

        leftTempleBridge.rotateAngleY = root.bipedHead.rotateAngleY;
        leftTempleBridge.rotateAngleX = root.bipedHead.rotateAngleX;
        leftTempleBridge.render(scale);
    }

    @Override
    public String getName() {
        return "sunglasses";
    }
}
