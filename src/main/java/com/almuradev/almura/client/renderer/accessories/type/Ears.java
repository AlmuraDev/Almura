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

public class Ears implements IAccessory<ModelBiped> {

    private ModelRenderer ears;

    @Override
    public void onAttached(EntityLivingBase base, ResourceLocation textureLocation, ModelBiped root) {
        ears = new ModelRenderer(root, 0, 0);
        ears.addBox(-3F, -6F, -1F, 6, 6, 1);
    }

    @Override
    public void onRender(EntityLivingBase base, ResourceLocation textureLocation, ModelBiped root, float scale, float partialTicks) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(textureLocation);

        ears.rotateAngleY = root.bipedHead.rotateAngleY;
        ears.rotateAngleX = root.bipedHead.rotateAngleX;
        ears.render(scale);
    }

    @Override
    public String getName() {
        return "ears";
    }
}
