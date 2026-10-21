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
 * Wings on the back, each made of thin strips fanned out from the spine (the 1.7.10 model, whose textures paint the wing across the
 * strips). They flap slowly, a little faster and wider while walking.
 */
@SideOnly(Side.CLIENT)
public final class WingsModel extends AccessoryModel {

    private final ModelRenderer[] left = new ModelRenderer[9];
    private final ModelRenderer[] right = new ModelRenderer[9];
    private final float[] leftYaw = new float[9];
    private final float[] rightYaw = new float[9];

    public WingsModel() {
        this.left[0] = this.part(60, 0, -1F, 2F, 2F, 1, 0F, 0.4833219F, 0F);
        this.left[1] = this.part(56, 0, -1F, 1F, 3F, 1, 0F, 0.5007752F, 0.0174533F);
        this.left[2] = this.part(50, 0, -1F, 0F, 4F, 2, 0F, 0.5182285F, 0.0349066F);
        this.left[3] = this.part(46, 0, -1F, -1F, 6F, 1, 0F, 0.5356818F, 0.0523599F);
        this.left[4] = this.part(38, 0, -1F, -2F, 7F, 3, 0F, 0.5531351F, 0.0698132F);
        this.left[5] = this.part(34, 0, -1F, -1F, 10F, 1, 0F, 0.5531351F, 0.0523599F);
        this.left[6] = this.part(30, 0, -1F, 0F, 11F, 1, 0F, 0.5705884F, 0.0349066F);
        this.left[7] = this.part(26, 0, -1F, 1F, 12F, 1, 0F, 0.5880417F, 0.0174533F);
        this.left[8] = this.part(22, 0, -1F, 3F, 13F, 1, 0F, 0.5880417F, 0F);

        this.right[0] = this.part(60, 21, 0F, 2F, 2F, 1, 0F, -0.4833166F, 0F);
        this.right[1] = this.part(56, 21, 0F, 1F, 3F, 1, 0F, -0.5007699F, -0.0174533F);
        this.right[2] = this.part(50, 20, 0F, 0F, 4F, 2, 0F, -0.5182232F, -0.0349066F);
        this.right[3] = this.part(46, 21, 0F, -1F, 6F, 1, 0.0174533F, -0.5356765F, -0.0523599F);
        this.right[4] = this.part(38, 19, 0F, -2F, 7F, 3, 0.0174533F, -0.5531297F, -0.0698132F);
        this.right[5] = this.part(34, 21, 0F, -1F, 10F, 1, 0.0174533F, -0.570583F, -0.0523599F);
        this.right[6] = this.part(30, 21, 0F, 0F, 11F, 1, 0.0174533F, -0.5880363F, -0.0349066F);
        this.right[7] = this.part(26, 21, 0F, 1F, 12F, 1, 0.0174533F, -0.6054896F, -0.0174533F);
        this.right[8] = this.part(22, 21, 0F, 3F, 13F, 1, 0.0174533F, -0.6229429F, 0F);

        for (int i = 0; i < 9; i++) {
            this.leftYaw[i] = this.left[i].rotateAngleY;
            this.rightYaw[i] = this.right[i].rotateAngleY;
        }
    }

    private ModelRenderer part(final int u, final int v, final float x, final float y, final float z, final int depth, final float angleX,
            final float angleY, final float angleZ) {
        final ModelRenderer part = this.box(u, v, x, y, z, 1, 10, depth);
        part.rotateAngleX = angleX;
        part.rotateAngleY = angleY;
        part.rotateAngleZ = angleZ;
        return part;
    }

    @Override
    public void render(final ModelPlayer model, final AbstractClientPlayer player, final float limbSwing, final float limbSwingAmount,
            final float ageInTicks, final float scale) {
        model.bipedBody.postRender(scale);

        // Positive opens the wings wider (they fan out behind the back around the spine). A steady idle flap plus a beat per step: the
        // speed must not depend on the walking amount, as ageInTicks is large and any change to its multiplier jumps the phase
        final float flap = MathHelper.sin(ageInTicks * 0.1F) * 0.08F + MathHelper.sin(limbSwing * 0.6662F) * limbSwingAmount * 0.1F;
        for (int i = 0; i < 9; i++) {
            this.left[i].rotateAngleY = this.leftYaw[i] + flap;
            this.left[i].render(scale);
            this.right[i].rotateAngleY = this.rightYaw[i] - flap;
            this.right[i].render(scale);
        }
    }
}
