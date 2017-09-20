/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.asm.mixin.core.client.renderer.tileentity;

import com.almuradev.almura.Almura;
import com.almuradev.almura.configuration.type.ClientConfiguration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderItemFrame;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItemFrame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderItemFrame.class)
public abstract class MixinRenderItemFrame extends Render {

    protected MixinRenderItemFrame(RenderManager renderManager) {
        super(renderManager);
    }

    @Inject(method = "doRender", at = @At(value = "HEAD"), cancellable = true)
    public void onDoRender(EntityItemFrame entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo ci) {
        // 0 means perform Minecraft logic only, we do not interfere
        final ClientConfiguration config = (ClientConfiguration) Almura.proxy.getPlatformConfigAdapter().getConfig();

        if (config.client.itemFrameRenderDistance == 0) {
            return;
        }

        EntityLivingBase viewer = (EntityLivingBase) Minecraft.getMinecraft().getRenderViewEntity();
        if (viewer == null) {
            viewer = Minecraft.getMinecraft().player;
        }

        if (viewer != null && entity.getDistanceToEntity(viewer) > config.client.itemFrameRenderDistance) {
            ci.cancel();
        }
    }
}
