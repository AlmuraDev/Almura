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

public class Ears implements IAccessory<ModelBiped> {

    private ModelRenderer bipedEars2;

    @Override
    public void onAttached(EntityPlayer player, ResourceLocation textureLocation, ModelBiped root) {
        bipedEars2 = new ModelRenderer(root, 0, 0);
        bipedEars2.addBox(-3F, -6F, -1F, 6, 6, 1);
    }

    @Override
    public void onRender(EntityPlayer player, ResourceLocation textureLocation, ModelBiped root, float scale, float partialTicks) {
        bipedEars2.rotateAngleY = root.bipedHead.rotateAngleY;
        bipedEars2.rotateAngleX = root.bipedHead.rotateAngleX;
        bipedEars2.rotationPointX = 0.0F;
        bipedEars2.rotationPointY = 0.0F;
        bipedEars2.render(scale);
    }

    @Override
    public String getName() {
        return "ears";
    }
}
