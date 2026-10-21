/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.accessory.client.model;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * A pair of ears on top of the head, placed like vanilla's deadmau5 ears: the same 6x6x1 ear drawn on each side, a third larger.
 */
@SideOnly(Side.CLIENT)
public final class EarsModel extends AccessoryModel {

    private static final float EAR_SCALE = 4.0F / 3.0F;

    private final ModelRenderer ear = this.box(0, 0, -3F, -6F, -1F, 6, 6, 1);

    @Override
    public void render(final ModelPlayer model, final AbstractClientPlayer player, final float limbSwing, final float limbSwingAmount,
            final float ageInTicks, final float scale) {
        model.bipedHead.postRender(scale);
        for (int side = 0; side < 2; side++) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(0.375F * (side * 2 - 1), -0.375F, 0.0F);
            GlStateManager.scale(EAR_SCALE, EAR_SCALE, EAR_SCALE);
            this.ear.render(scale);
            GlStateManager.popMatrix();
        }
    }
}
