/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.core.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.tileentity.RenderItemFrame;
import net.minecraft.entity.item.EntityItemFrame;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.almuradev.almura.Configuration;

@Mixin(RenderItemFrame.class)
public abstract class MixinRenderItemFrame extends Render {
    @Inject(method = "doRender", at = @At(value = "HEAD"), cancellable = true)
    public void onDoRender(EntityItemFrame entity, double x, double y, double z, float f1, float f2, CallbackInfo ci) {
        EntityClientPlayerMP viewer = (EntityClientPlayerMP) Minecraft.getMinecraft().renderViewEntity;
        if (viewer == null) {
            viewer = Minecraft.getMinecraft().thePlayer;
        }

        if (viewer != null && entity.getDistanceSqToEntity(viewer) > (Configuration.ITEM_FRAME_RENDER_DISTANCE *16)) {
            ci.cancel();
        }
    }
}
