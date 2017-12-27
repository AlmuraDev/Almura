/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.animal.asm.mixin.client.renderer.entity;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.entity.RenderChicken;
import net.minecraft.client.renderer.entity.RenderCow;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPig;
import net.minecraft.client.renderer.entity.RenderSheep;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.util.text.TextFormatting;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = {RenderChicken.class, RenderCow.class, RenderPig.class, RenderSheep.class})
public abstract class MixinRenderFarmAnimalNameplate extends RenderLiving {

    public MixinRenderFarmAnimalNameplate(RenderManager renderManagerIn, ModelBase modelBaseIn, float shadowSizeIn) {
        super(renderManagerIn, modelBaseIn, shadowSizeIn);
    }

    @Override
    protected void renderLivingLabel(Entity entityIn, String str, double x, double y, double z, int maxDistance)
    {
        double d0 = entityIn.getDistanceSq(this.renderManager.renderViewEntity);

        if (d0 <= (double)(maxDistance * maxDistance))
        {
            boolean flag = entityIn.isSneaking();
            float f = this.renderManager.playerViewY;
            float f1 = this.renderManager.playerViewX;
            boolean flag1 = this.renderManager.options.thirdPersonView == 2;
            float f2 = entityIn.height + 0.5F - (flag ? 0.25F : 0.0F);

            // Almura Start - No need to check deadmau5 for a Cow lol
//          int i = "deadmau5".equals(str) ? -10 : 0;
//          EntityRenderer.drawNameplate(this.getFontRendererFromRenderManager(), str, (float)x, (float)y + f2, (float)z, i, f, f1, flag1, flag);
            // Almura End

            // Almura Start - Check nameplate color due to heat status
            // TODO Check ClientAnimalManager to see if we can display that status
//            EntityAnimal animal = (EntityAnimal) entityIn;
//            System.err.println("Child: " + animal.isChild() + ", love: " + animal.isInLove());
//            if (!animal.isChild() && animal.isInLove()) {
//                str = TextFormatting.DARK_AQUA + str;
//            }

            EntityRenderer.drawNameplate(this.getFontRendererFromRenderManager(), str, (float)x, (float)y + f2, (float)z, 0, f, f1, flag1, flag);
            // Almura End
        }
    }
}
