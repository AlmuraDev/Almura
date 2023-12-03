/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.animal.asm.mixin.entity.passive;

import com.almuradev.almura.asm.mixin.interfaces.IMixinEntityCow;
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
        this.head.rotationPointY = 2.0F + ((IMixinEntityCow)entitylivingbaseIn).getHeadRotationPointY(partialTickTime) * 5.0F;
        this.headRotationAngleX = ((IMixinEntityCow)entitylivingbaseIn).getHeadRotationAngleX(partialTickTime);
    }

    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
        super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);
        this.head.rotateAngleX = this.headRotationAngleX;
    }
}
