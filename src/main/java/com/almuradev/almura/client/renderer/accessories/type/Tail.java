/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2017 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.renderer.accessories.type;

import com.almuradev.almura.client.renderer.accessories.IAccessory;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

public class Tail implements IAccessory<ModelBiped> {

    private ModelRenderer tail;

    @Override
    public void onAttached(EntityLivingBase base, ResourceLocation textureLocation, ModelBiped root) {
        tail = new ModelRenderer(root, 0, 0);
        tail.addBox(0.0F, 0.0F, 0.0F, 1, 10, 1);
        tail.setRotationPoint(-0.5F, 0F, -1F);
    }

    @Override
    public void onRender(EntityLivingBase base, ResourceLocation textureLocation, ModelBiped root, float scale, float partialTicks) {
        //TODO Re-write Tail rendering
    }

    @Override
    public String getName() {
        return "tail";
    }
}
