/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.mixin.client.renderer.tileentity;

import com.almuradev.almura.Configuration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.renderer.tileentity.TileEntityChestRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntityChest;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TileEntityChestRenderer.class)
public abstract class MixinTileEntityChestRenderer extends TileEntitySpecialRenderer {

    @Inject(method = "renderTileEntityAt", at = @At("HEAD"), cancellable = true)
    public void onRenderTileEntityAt(TileEntityChest te, double x, double y, double z, float delta, CallbackInfo ci) {
        EntityClientPlayerMP viewer = (EntityClientPlayerMP) Minecraft.getMinecraft().renderViewEntity;
        if (viewer == null) {
            viewer = Minecraft.getMinecraft().thePlayer;
        }

        if (viewer != null && te.getDistanceSq(viewer.posX, viewer.posY, viewer.posZ) > (Configuration.DISTANCE_RENDER_CHEST * 16) && te
                .hasWorldObj()) {
            ci.cancel();
        }
    }
}
