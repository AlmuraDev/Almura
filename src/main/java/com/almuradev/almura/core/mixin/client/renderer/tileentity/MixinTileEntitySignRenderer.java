/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.core.mixin.client.renderer.tileentity;

import com.almuradev.almura.Configuration;
import com.almuradev.almura.extension.sign.IExtendedTileEntitySign;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.model.ModelSign;
import net.minecraft.client.renderer.tileentity.TileEntitySignRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(TileEntitySignRenderer.class)
public abstract class MixinTileEntitySignRenderer extends TileEntitySpecialRenderer {

    @Shadow
    private static ResourceLocation field_147513_b;

    @Shadow
    private ModelSign model;

    @Overwrite
    public void renderTileEntityAt(TileEntitySign par1TileEntitySign, double par2, double par4, double par6, float p_147500_8_) {
        Block var9 = par1TileEntitySign.getBlockType();
        GL11.glPushMatrix();
        float var10 = 0.6666667F;
        float var12;

        if (var9 == Blocks.standing_sign) {
            GL11.glTranslatef((float) par2 + 0.5F, (float) par4 + 0.75F * var10, (float) par6 + 0.5F);
            float var11 = (float) (par1TileEntitySign.getBlockMetadata() * 360) / 16.0F;
            GL11.glRotatef(-var11, 0.0F, 1.0F, 0.0F);
            this.model.signStick.showModel = true;
        } else {
            int var16 = par1TileEntitySign.getBlockMetadata();
            var12 = 0.0F;

            if (var16 == 2) {
                var12 = 180.0F;
            }

            if (var16 == 4) {
                var12 = 90.0F;
            }

            if (var16 == 5) {
                var12 = -90.0F;
            }

            GL11.glTranslatef((float) par2 + 0.5F, (float) par4 + 0.75F * var10, (float) par6 + 0.5F);
            GL11.glRotatef(-var12, 0.0F, 1.0F, 0.0F);
            GL11.glTranslatef(0.0F, -0.3125F, -0.4375F);
            this.model.signStick.showModel = false;
        }

        Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("textures/entity/sign.png"));
        GL11.glPushMatrix();
        GL11.glScalef(var10, -var10, -var10);
        this.model.renderSign();
        GL11.glPopMatrix();
        // Almura Start
        if (((IExtendedTileEntitySign) par1TileEntitySign).hasText()) {
            EntityLivingBase viewer = Minecraft.getMinecraft().renderViewEntity;
            if (viewer == null) {
                viewer = Minecraft.getMinecraft().thePlayer;
            }
            if (viewer != null && par1TileEntitySign.getDistanceSq(viewer.posX, viewer.posY, viewer.posZ) < (
                    Configuration.DISTANCE_RENDER_ITEM_FRAME
                            * 16) && par1TileEntitySign
                    .hasWorldObj()) {
                // Almura End
                FontRenderer var17 = this.func_147498_b();
                var12 = 0.016666668F * var10;
                GL11.glTranslatef(0.0F, 0.5F * var10, 0.07F * var10);
                GL11.glScalef(var12, -var12, var12);
                GL11.glNormal3f(0.0F, 0.0F, -1.0F * var12);
                GL11.glDepthMask(false);
                // Almura Start
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                byte var13 = 0;
                // Almura End

                for (int var14 = 0; var14 < par1TileEntitySign.signText.length; ++var14) {
                    String var15 = par1TileEntitySign.signText[var14];

                    if (var14 == par1TileEntitySign.lineBeingEdited) {
                        // Almura Start
                        int endColumnStart = Math.min(((IExtendedTileEntitySign) par1TileEntitySign).getColumnBeingEdited(), var15.length());
                        String before = "";
                        if (endColumnStart > 0) {
                            before = var15.substring(0, endColumnStart);
                        }
                        String after = "";
                        if (var15.length() - ((IExtendedTileEntitySign) par1TileEntitySign).getColumnBeingEdited() > 0) {
                            after = var15.substring(((IExtendedTileEntitySign) par1TileEntitySign).getColumnBeingEdited(), var15.length());
                        }
                        var15 = before + "_" + after;
                        // Almura End
                        var17.drawString(var15, -var17.getStringWidth(var15) / 2, var14 * 10 - par1TileEntitySign.signText.length * 5, var13);
                    } else {
                        var17.drawString(var15, -var17.getStringWidth(var15) / 2, var14 * 10 - par1TileEntitySign.signText.length * 5, var13);
                    }
                }

                GL11.glDepthMask(true);
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                // Almura Start
            }
        }
        // Almura End
        GL11.glPopMatrix();
    }
}
