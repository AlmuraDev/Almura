/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.renderer.accessories.type;

import com.almuradev.almura.client.renderer.accessories.IAccessory;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

// TODO Math may not be correct anymore
public class Tail implements IAccessory<ModelBiped> {

    private ModelRenderer tail;

    @Override
    public void onAttached(EntityPlayer player, ResourceLocation textureLocation, ModelBiped root) {
        tail = new ModelRenderer(root, 0, 0);
        tail.addBox(0.0F, 0.0F, 0.0F, 1, 10, 1);
        tail.setRotationPoint(-0.5F, 0F, -1F);
    }

    @Override
    public void onRender(EntityPlayer player, ResourceLocation textureLocation, ModelBiped root, float scale, float partialTicks) {
        GL11.glPushMatrix();
        GL11.glTranslatef(0.0F, 0.75F, 0.125F);
        double d = (player.field_71091_bM + (player.field_71094_bP - player.field_71091_bM) * (double) partialTicks) - (player.prevPosX + (player.posX - player.prevPosX) * (double) partialTicks);
        double d1 = (player.field_71096_bN + (player.field_71095_bQ - player.field_71096_bN) * (double) partialTicks) - (player.prevPosY + (player.posY - player.prevPosY) * (double) partialTicks);
        double d2 = (player.field_71097_bO + (player.field_71085_bR - player.field_71097_bO) * (double) partialTicks) - (player.prevPosZ + (player.posZ - player.prevPosZ) * (double) partialTicks);
        float f8 = player.prevRenderYawOffset + (player.renderYawOffset - player.prevRenderYawOffset) * partialTicks;
        double d3 = MathHelper.sin((f8 * 3.141593F) / 180F);
        double d4 = -MathHelper.cos((f8 * 3.141593F) / 180F);
        float f9 = (float) d1 * 10F;
        if (f9 < -6F) {
            f9 = -6F;
        }
        if (f9 > 32F) {
            f9 = 32F;
        }
        float f10 = (float) (d * d3 + d2 * d4) * 100F;
        float f11 = (float) (d * d4 - d2 * d3) * 100F;
        if (f10 < 0.0F) {
            f10 = 0.0F;
        }
        float f12 = player.prevCameraYaw + (player.cameraYaw - player.prevCameraYaw) * partialTicks;
        f9 += MathHelper.sin((player.prevDistanceWalkedModified + (player.distanceWalkedModified - player.prevDistanceWalkedModified) * partialTicks) * 6F) * 32F * f12;
        if (player.isSneaking()) {
            f9 += 25F;
        }
        GL11.glRotatef(6F + f10 / 2.0F + f9, 1.0F, 0.0F, 0.0F);
        GL11.glRotatef(f11 / 2.0F, 0.0F, 0.0F, 1.0F);
        GL11.glRotatef(-f11 / 2.0F, 0.0F, 0.0F, 0.0F);
        GL11.glRotatef(180F, 0.0F, 1.0F, 0.0F);
        tail.render(scale);
        GL11.glPopMatrix();
    }

    @Override
    public String getName() {
        return "tail";
    }
}
