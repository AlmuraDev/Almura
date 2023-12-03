/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.animal.asm.mixin.entity.passive;

import net.minecraft.client.model.ModelCow;
import net.minecraft.client.model.ModelQuadruped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ModelCow.class)
public class MixinModelCow extends ModelQuadruped {

    private float headRotationAngleX;

    public MixinModelCow(int height, float scale) {
        super(height, scale);
    }

    public void setLivingAnimations(EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTickTime) {
        super.setLivingAnimations(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTickTime);
        // Todo:  This doesn't work because I'm injecting getHeadTotationPointY and the proceding method in MixinEntityCow and you cant reference it directly.

        //this.head.rotationPointY = 6.0F + ((EntityCow)entitylivingbaseIn).getHeadRotationPointY(partialTickTime) * 9.0F;
        //this.headRotationAngleX = ((EntityCow)entitylivingbaseIn).getHeadRotationAngleX(partialTickTime);
    }

    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
        super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);
        this.head.rotateAngleX = this.headRotationAngleX;
    }
}
