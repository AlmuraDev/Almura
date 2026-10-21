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
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * A tail hanging from the lower back, angled back, swaying gently and swinging with each step. (The 1.7.10 tail never rendered.)
 */
@SideOnly(Side.CLIENT)
public final class TailModel extends AccessoryModel {

    private final ModelRenderer tail;

    public TailModel() {
        this.tail = this.box(0, 0, -0.5F, 0F, -0.5F, 1, 10, 1);
        this.tail.setRotationPoint(0F, 11F, 2F);
    }

    @Override
    public void render(final ModelPlayer model, final AbstractClientPlayer player, final float limbSwing, final float limbSwingAmount,
            final float ageInTicks, final float scale) {
        model.bipedBody.postRender(scale);
        // Body space: +z is behind the player, so a positive x rotation swings the hanging tail backwards
        this.tail.rotateAngleX = 0.6F + MathHelper.cos(ageInTicks * 0.05F) * 0.05F + limbSwingAmount * 0.3F;
        this.tail.rotateAngleZ = MathHelper.sin(ageInTicks * 0.067F) * 0.12F + MathHelper.sin(limbSwing * 0.6662F) * limbSwingAmount * 0.4F;
        this.tail.render(scale);
    }
}
