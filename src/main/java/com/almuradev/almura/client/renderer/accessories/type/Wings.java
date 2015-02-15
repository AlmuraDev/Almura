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

public class Wings implements IAccessory<ModelBiped> {

    private ModelRenderer leftWingPart0;
    private ModelRenderer leftWingPart1;
    private ModelRenderer leftWingPart2;
    private ModelRenderer leftWingPart3;
    private ModelRenderer leftWingPart4;
    private ModelRenderer leftWingPart5;
    private ModelRenderer leftWingPart6;
    private ModelRenderer leftWingPart7;
    private ModelRenderer leftWingPart8;
    private ModelRenderer rightWingPart0;
    private ModelRenderer rightWingPart1;
    private ModelRenderer rightWingPart2;
    private ModelRenderer rightWingPart3;
    private ModelRenderer rightWingPart4;
    private ModelRenderer rightWingPart5;
    private ModelRenderer rightWingPart6;
    private ModelRenderer rightWingPart7;
    private ModelRenderer rightWingPart8;

    @Override
    public void onAttached(EntityPlayer player, ResourceLocation textureLocation, ModelBiped root) {
        leftWingPart1 = new ModelRenderer(root, 56, 0);
        leftWingPart1.addBox(-1F, 1F, 3F, 1, 10, 1);
        leftWingPart1.setRotationPoint(0F, 0F, 0F);
        leftWingPart1.setTextureSize(64, 32);
        leftWingPart1.mirror = true;
        setRotation(leftWingPart1, 0F, 0.5007752F, 0.0174533F);
        leftWingPart2 = new ModelRenderer(root, 50, 0);
        leftWingPart2.addBox(-1F, 0F, 4F, 1, 10, 2);
        leftWingPart2.setRotationPoint(0F, 0F, 0F);
        leftWingPart2.setTextureSize(64, 32);
        leftWingPart2.mirror = true;
        setRotation(leftWingPart2, 0F, 0.5182285F, 0.0349066F);
        leftWingPart3 = new ModelRenderer(root, 46, 0);
        leftWingPart3.addBox(-1F, -1F, 6F, 1, 10, 1);
        leftWingPart3.setRotationPoint(0F, 0F, 0F);
        leftWingPart3.setTextureSize(64, 32);
        leftWingPart3.mirror = true;
        setRotation(leftWingPart3, 0F, 0.5356818F, 0.0523599F);
        leftWingPart4 = new ModelRenderer(root, 38, 0);
        leftWingPart4.addBox(-1F, -2F, 7F, 1, 10, 3);
        leftWingPart4.setRotationPoint(0F, 0F, 0F);
        leftWingPart4.setTextureSize(64, 32);
        leftWingPart4.mirror = true;
        setRotation(leftWingPart4, 0F, 0.5531351F, 0.0698132F);
        leftWingPart5 = new ModelRenderer(root, 34, 0);
        leftWingPart5.addBox(-1F, -1F, 10F, 1, 10, 1);
        leftWingPart5.setRotationPoint(0F, 0F, 0F);
        leftWingPart5.setTextureSize(64, 32);
        leftWingPart5.mirror = true;
        setRotation(leftWingPart5, 0F, 0.5531351F, 0.0523599F);
        leftWingPart6 = new ModelRenderer(root, 30, 0);
        leftWingPart6.addBox(-1F, 0F, 11F, 1, 10, 1);
        leftWingPart6.setRotationPoint(0F, 0F, 0F);
        leftWingPart6.setTextureSize(64, 32);
        leftWingPart6.mirror = true;
        setRotation(leftWingPart6, 0F, 0.5705884F, 0.0349066F);
        leftWingPart7 = new ModelRenderer(root, 26, 0);
        leftWingPart7.addBox(-1F, 1F, 12F, 1, 10, 1);
        leftWingPart7.setRotationPoint(0F, 0F, 0F);
        leftWingPart7.setTextureSize(64, 32);
        leftWingPart7.mirror = true;
        setRotation(leftWingPart7, 0F, 0.5880417F, 0.0174533F);
        leftWingPart8 = new ModelRenderer(root, 22, 0);
        leftWingPart8.addBox(-1F, 3F, 13F, 1, 10, 1);
        leftWingPart8.setRotationPoint(0F, 0F, 0F);
        leftWingPart8.setTextureSize(64, 32);
        leftWingPart8.mirror = true;
        setRotation(leftWingPart8, 0F, 0.5880417F, 0F);
        leftWingPart0 = new ModelRenderer(root, 60, 0);
        leftWingPart0.addBox(-1F, 2F, 2F, 1, 10, 1);
        leftWingPart0.setRotationPoint(0F, 0F, 0F);
        leftWingPart0.setTextureSize(64, 32);
        leftWingPart0.mirror = true;
        setRotation(leftWingPart0, 0F, 0.4833219F, 0F);
        rightWingPart0 = new ModelRenderer(root, 60, 21);
        rightWingPart0.addBox(0F, 2F, 2F, 1, 10, 1);
        rightWingPart0.setRotationPoint(0F, 0F, 0F);
        rightWingPart0.setTextureSize(64, 32);
        rightWingPart0.mirror = true;
        setRotation(rightWingPart0, 0F, -0.4833166F, 0F);
        rightWingPart1 = new ModelRenderer(root, 56, 21);
        rightWingPart1.addBox(0F, 1F, 3F, 1, 10, 1);
        rightWingPart1.setRotationPoint(0F, 0F, 0F);
        rightWingPart1.setTextureSize(64, 32);
        rightWingPart1.mirror = true;
        setRotation(rightWingPart1, 0F, -0.5007699F, -0.0174533F);
        rightWingPart2 = new ModelRenderer(root, 50, 20);
        rightWingPart2.addBox(0F, 0F, 4F, 1, 10, 2);
        rightWingPart2.setRotationPoint(0F, 0F, 0F);
        rightWingPart2.setTextureSize(64, 32);
        rightWingPart2.mirror = true;
        setRotation(rightWingPart2, 0F, -0.5182232F, -0.0349066F);
        rightWingPart3 = new ModelRenderer(root, 46, 21);
        rightWingPart3.addBox(0F, -1F, 6F, 1, 10, 1);
        rightWingPart3.setRotationPoint(0F, 0F, 0F);
        rightWingPart3.setTextureSize(64, 32);
        rightWingPart3.mirror = true;
        setRotation(rightWingPart3, 0.0174533F, -0.5356765F, -0.0523599F);
        rightWingPart4 = new ModelRenderer(root, 38, 19);
        rightWingPart4.addBox(0F, -2F, 7F, 1, 10, 3);
        rightWingPart4.setRotationPoint(0F, 0F, 0F);
        rightWingPart4.setTextureSize(64, 32);
        rightWingPart4.mirror = true;
        setRotation(rightWingPart4, 0.0174533F, -0.5531297F, -0.0698132F);
        rightWingPart5 = new ModelRenderer(root, 34, 21);
        rightWingPart5.addBox(0F, -1F, 10F, 1, 10, 1);
        rightWingPart5.setRotationPoint(0F, 0F, 0F);
        rightWingPart5.setTextureSize(64, 32);
        rightWingPart5.mirror = true;
        setRotation(rightWingPart5, 0.0174533F, -0.570583F, -0.0523599F);
        rightWingPart6 = new ModelRenderer(root, 30, 21);
        rightWingPart6.addBox(0F, 0F, 11F, 1, 10, 1);
        rightWingPart6.setRotationPoint(0F, 0F, 0F);
        rightWingPart6.setTextureSize(64, 32);
        rightWingPart6.mirror = true;
        setRotation(rightWingPart6, 0.0174533F, -0.5880363F, -0.0349066F);
        rightWingPart7 = new ModelRenderer(root, 26, 21);
        rightWingPart7.addBox(0F, 1F, 12F, 1, 10, 1);
        rightWingPart7.setRotationPoint(0F, 0F, 0F);
        rightWingPart7.setTextureSize(64, 32);
        rightWingPart7.mirror = true;
        setRotation(rightWingPart7, 0.0174533F, -0.6054896F, -0.0174533F);
        rightWingPart8 = new ModelRenderer(root, 22, 21);
        rightWingPart8.addBox(0F, 3F, 13F, 1, 10, 1);
        rightWingPart8.setRotationPoint(0F, 0F, 0F);
        rightWingPart8.setTextureSize(64, 32);
        rightWingPart8.mirror = true;
        setRotation(rightWingPart8, 0.0174533F, -0.6229429F, 0F);
    }

    @Override
    public void onRender(EntityPlayer player, ResourceLocation textureLocation, ModelBiped root, float scale, float partialTicks) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(textureLocation);
        leftWingPart1.render(scale);
        leftWingPart2.render(scale);
        leftWingPart3.render(scale);
        leftWingPart4.render(scale);
        leftWingPart5.render(scale);
        leftWingPart6.render(scale);
        leftWingPart7.render(scale);
        leftWingPart8.render(scale);
        leftWingPart0.render(scale);
        rightWingPart0.render(scale);
        rightWingPart1.render(scale);
        rightWingPart2.render(scale);
        rightWingPart3.render(scale);
        rightWingPart4.render(scale);
        rightWingPart5.render(scale);
        rightWingPart6.render(scale);
        rightWingPart7.render(scale);
        rightWingPart8.render(scale);
    }

    @Override
    public String getName() {
        return "wings";
    }

    private void setRotation(ModelRenderer model, float x, float y, float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
}
