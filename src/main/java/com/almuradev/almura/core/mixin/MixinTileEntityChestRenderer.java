/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.core.mixin;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.model.ModelChest;
import net.minecraft.client.renderer.tileentity.TileEntityChestRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@SideOnly(Side.CLIENT)
@Mixin(value = TileEntityChestRenderer.class, remap = false)
public abstract class MixinTileEntityChestRenderer extends TileEntitySpecialRenderer {

    @Shadow
    private static ResourceLocation field_147507_b;

    @Shadow
    private static ResourceLocation field_147508_c;

    @Shadow
    private static ResourceLocation field_147505_d;

    @Shadow
    private static ResourceLocation field_147506_e;

    @Shadow
    private static ResourceLocation field_147503_f;

    @Shadow
    private static ResourceLocation field_147504_g;

    @Shadow
    private ModelChest field_147510_h;

    @Shadow
    private ModelChest field_147511_i;

    @Shadow
    private boolean field_147509_j;

    public void renderTileEntityAt(TileEntityChest p_147500_1_, double p_147500_2_, double p_147500_4_, double p_147500_6_, float p_147500_8_) {
        int i;

        EntityClientPlayerMP viewer = (EntityClientPlayerMP) Minecraft.getMinecraft().renderViewEntity;
        boolean drawModel;
        if (viewer == null) {
            viewer = Minecraft.getMinecraft().thePlayer;
        }
        //TODO Almura Start
        drawModel = viewer != null && p_147500_1_.getDistanceFrom(viewer.posX, viewer.posY, viewer.posZ) < 200 || !p_147500_1_.hasWorldObj();

        if (!p_147500_1_.hasWorldObj()) {
            i = 0;
        } else {
            Block block = p_147500_1_.getBlockType();
            i = p_147500_1_.getBlockMetadata();

            if (block instanceof BlockChest && i == 0) {
                try {
                    ((BlockChest) block).func_149954_e(p_147500_1_.getWorldObj(), p_147500_1_.xCoord, p_147500_1_.yCoord, p_147500_1_.zCoord);
                } catch (ClassCastException e) {
                    FMLLog.severe("Attempted to render a chest at %d,  %d, %d that was not a chest", p_147500_1_.xCoord, p_147500_1_.yCoord,
                                  p_147500_1_.zCoord);
                }
                i = p_147500_1_.getBlockMetadata();
            }

            p_147500_1_.checkForAdjacentChests();
        }

        if (p_147500_1_.adjacentChestZNeg == null && p_147500_1_.adjacentChestXNeg == null) {
            ModelChest modelchest;

            if (p_147500_1_.adjacentChestXPos == null && p_147500_1_.adjacentChestZPos == null) {
                modelchest = this.field_147510_h;

                if (p_147500_1_.func_145980_j() == 1) {
                    this.bindTexture(field_147506_e);
                } else if (this.field_147509_j) {
                    this.bindTexture(field_147503_f);
                } else {
                    this.bindTexture(field_147504_g);
                }
            } else {
                modelchest = this.field_147511_i;

                if (p_147500_1_.func_145980_j() == 1) {
                    this.bindTexture(field_147507_b);
                } else if (this.field_147509_j) {
                    this.bindTexture(field_147508_c);
                } else {
                    this.bindTexture(field_147505_d);
                }
            }

            GL11.glPushMatrix();
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glTranslatef((float) p_147500_2_, (float) p_147500_4_ + 1.0F, (float) p_147500_6_ + 1.0F);
            GL11.glScalef(1.0F, -1.0F, -1.0F);
            GL11.glTranslatef(0.5F, 0.5F, 0.5F);
            short short1 = 0;

            if (i == 2) {
                short1 = 180;
            }

            if (i == 3) {
                short1 = 0;
            }

            if (i == 4) {
                short1 = 90;
            }

            if (i == 5) {
                short1 = -90;
            }

            if (i == 2 && p_147500_1_.adjacentChestXPos != null) {
                GL11.glTranslatef(1.0F, 0.0F, 0.0F);
            }

            if (i == 5 && p_147500_1_.adjacentChestZPos != null) {
                GL11.glTranslatef(0.0F, 0.0F, -1.0F);
            }

            GL11.glRotatef((float) short1, 0.0F, 1.0F, 0.0F);
            GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
            float f1 = p_147500_1_.prevLidAngle + (p_147500_1_.lidAngle - p_147500_1_.prevLidAngle) * p_147500_8_;
            float f2;

            if (p_147500_1_.adjacentChestZNeg != null) {
                f2 =
                        p_147500_1_.adjacentChestZNeg.prevLidAngle
                        + (p_147500_1_.adjacentChestZNeg.lidAngle - p_147500_1_.adjacentChestZNeg.prevLidAngle) * p_147500_8_;

                if (f2 > f1) {
                    f1 = f2;
                }
            }

            if (p_147500_1_.adjacentChestXNeg != null) {
                f2 =
                        p_147500_1_.adjacentChestXNeg.prevLidAngle
                        + (p_147500_1_.adjacentChestXNeg.lidAngle - p_147500_1_.adjacentChestXNeg.prevLidAngle) * p_147500_8_;

                if (f2 > f1) {
                    f1 = f2;
                }
            }

            f1 = 1.0F - f1;
            f1 = 1.0F - f1 * f1 * f1;
            modelchest.chestLid.rotateAngleX = -(f1 * (float) Math.PI / 2.0F);
            if (drawModel) {
                modelchest.renderAll();
            }
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            GL11.glPopMatrix();
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    public void func_180535_a(TileEntityChest p_147500_1_, double p_147500_2_, double p_147500_4_, double p_147500_6_, float p_147500_8_) {
        int i;

        EntityClientPlayerMP viewer = (EntityClientPlayerMP) Minecraft.getMinecraft().renderViewEntity;
        boolean drawModel;
        if (viewer == null) {
            viewer = Minecraft.getMinecraft().thePlayer;
        }
        //TODO Almura Start
        drawModel = viewer != null && p_147500_1_.getDistanceFrom(viewer.posX, viewer.posY, viewer.posZ) < 200 || !p_147500_1_.hasWorldObj();

        if (!p_147500_1_.hasWorldObj()) {
            i = 0;
        } else {
            Block block = p_147500_1_.getBlockType();
            i = p_147500_1_.getBlockMetadata();

            if (block instanceof BlockChest && i == 0) {
                try {
                    ((BlockChest) block).func_149954_e(p_147500_1_.getWorldObj(), p_147500_1_.xCoord, p_147500_1_.yCoord, p_147500_1_.zCoord);
                } catch (ClassCastException e) {
                    FMLLog.severe("Attempted to render a chest at %d,  %d, %d that was not a chest", p_147500_1_.xCoord, p_147500_1_.yCoord,
                                  p_147500_1_.zCoord);
                }
                i = p_147500_1_.getBlockMetadata();
            }

            p_147500_1_.checkForAdjacentChests();
        }

        if (p_147500_1_.adjacentChestZNeg == null && p_147500_1_.adjacentChestXNeg == null) {
            ModelChest modelchest;

            if (p_147500_1_.adjacentChestXPos == null && p_147500_1_.adjacentChestZPos == null) {
                modelchest = this.field_147510_h;

                if (p_147500_1_.func_145980_j() == 1) {
                    this.bindTexture(field_147506_e);
                } else if (this.field_147509_j) {
                    this.bindTexture(field_147503_f);
                } else {
                    this.bindTexture(field_147504_g);
                }
            } else {
                modelchest = this.field_147511_i;

                if (p_147500_1_.func_145980_j() == 1) {
                    this.bindTexture(field_147507_b);
                } else if (this.field_147509_j) {
                    this.bindTexture(field_147508_c);
                } else {
                    this.bindTexture(field_147505_d);
                }
            }

            GL11.glPushMatrix();
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glTranslatef((float) p_147500_2_, (float) p_147500_4_ + 1.0F, (float) p_147500_6_ + 1.0F);
            GL11.glScalef(1.0F, -1.0F, -1.0F);
            GL11.glTranslatef(0.5F, 0.5F, 0.5F);
            short short1 = 0;

            if (i == 2) {
                short1 = 180;
            }

            if (i == 3) {
                short1 = 0;
            }

            if (i == 4) {
                short1 = 90;
            }

            if (i == 5) {
                short1 = -90;
            }

            if (i == 2 && p_147500_1_.adjacentChestXPos != null) {
                GL11.glTranslatef(1.0F, 0.0F, 0.0F);
            }

            if (i == 5 && p_147500_1_.adjacentChestZPos != null) {
                GL11.glTranslatef(0.0F, 0.0F, -1.0F);
            }

            GL11.glRotatef((float) short1, 0.0F, 1.0F, 0.0F);
            GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
            float f1 = p_147500_1_.prevLidAngle + (p_147500_1_.lidAngle - p_147500_1_.prevLidAngle) * p_147500_8_;
            float f2;

            if (p_147500_1_.adjacentChestZNeg != null) {
                f2 =
                        p_147500_1_.adjacentChestZNeg.prevLidAngle
                        + (p_147500_1_.adjacentChestZNeg.lidAngle - p_147500_1_.adjacentChestZNeg.prevLidAngle) * p_147500_8_;

                if (f2 > f1) {
                    f1 = f2;
                }
            }

            if (p_147500_1_.adjacentChestXNeg != null) {
                f2 =
                        p_147500_1_.adjacentChestXNeg.prevLidAngle
                        + (p_147500_1_.adjacentChestXNeg.lidAngle - p_147500_1_.adjacentChestXNeg.prevLidAngle) * p_147500_8_;

                if (f2 > f1) {
                    f1 = f2;
                }
            }

            f1 = 1.0F - f1;
            f1 = 1.0F - f1 * f1 * f1;
            modelchest.chestLid.rotateAngleX = -(f1 * (float) Math.PI / 2.0F);
            if (drawModel) {
                modelchest.renderAll();
            }
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            GL11.glPopMatrix();
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
}

