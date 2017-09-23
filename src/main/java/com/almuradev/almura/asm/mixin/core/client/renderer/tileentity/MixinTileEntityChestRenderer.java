/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.asm.mixin.core.client.renderer.tileentity;

import com.almuradev.almura.asm.StaticAccess;
import com.almuradev.almura.core.client.config.ClientConfiguration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntityChestRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntityChest;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TileEntityChestRenderer.class)
public abstract class MixinTileEntityChestRenderer extends TileEntitySpecialRenderer {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void onRender(TileEntityChest te, double x, double y, double z, float partialTicks, int destroyStage, float val,
            CallbackInfo ci) {
        // 0 means perform Minecraft logic only, we do not interfere
        final ClientConfiguration config = StaticAccess.config.getConfig();

        if (config.client.chestRenderDistance == 0) {
            return;
        }

        EntityLivingBase viewer = (EntityLivingBase) Minecraft.getMinecraft().getRenderViewEntity();
        if (viewer == null) {
            viewer = Minecraft.getMinecraft().player;
        }

        if (viewer != null && te.getDistanceSq(viewer.posX, viewer.posY, viewer.posZ) > (config.client.chestRenderDistance * 16) && te.hasWorld()) {
            ci.cancel();
        }
    }
}