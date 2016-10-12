/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2016 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.special.block;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.opengl.GL11;

import java.text.NumberFormat;
import java.util.Locale;

public class CachesTileEntitySpecialRenderer extends TileEntitySpecialRenderer {

    EntityItem visualItem;

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTick) {
        // Impossible
        if (!(te instanceof CachesTileEntity) || te.getWorld() == null) {
            return;
        }

        final CachesTileEntity cte = (CachesTileEntity) te;
        // Is empty, return
        if (cte.getServerStackSize() == -1) {
            visualItem = null;
            return;
        }
        final int metadata = cte.getBlockMetadata();

        float angle = 0f;
        double translatedX, translatedZ;
        translatedX = x;
        translatedZ = z;

        if (metadata == 0) {
            translatedX += 0.5;
            translatedZ += 1.015;
        } else if (metadata == 1) {
            angle = -90f;
            translatedX -= 0.015;
            translatedZ += 0.5;
        } else if (metadata == 2) {
            angle = 180f;
            translatedX += 0.5;
            translatedZ -= 0.015;
        } else if (metadata == 3) {
            translatedX += 1.015;
            translatedZ += 0.5;
            angle = 90f;
        }

        // Draw ItemType 2D visual in front
        GL11.glPushMatrix();

        if (visualItem == null) {
            visualItem = new EntityItem(te.getWorld(), 0, 0, 0, new ItemStack(Blocks.stone));
            visualItem.hoverStart = 0.0f;
        }

        GL11.glTranslated(translatedX, y + 0.35, translatedZ);

        RenderItem.renderInFrame = true;
        RenderManager.instance.renderEntityWithPosYaw(visualItem, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F);
        RenderItem.renderInFrame = false;

        GL11.glPopMatrix();

        // Draw Quantity/Limit text visual
        GL11.glPushMatrix();

        GL11.glTranslated(translatedX, y, translatedZ);
        GL11.glRotatef(angle, 0f, 1f, 0f);

        float scaleFactor = 0.6666667F * 0.016666668F;
        GL11.glScalef(scaleFactor, -scaleFactor, scaleFactor);
        final FontRenderer renderer = Minecraft.getMinecraft().fontRendererObj;
        final String cacheStatus = NumberFormat.getNumberInstance(Locale.US).format(((CachesTileEntity) te).getServerStackSize()) + "/" +
                NumberFormat.getNumberInstance(Locale.US).format(((CachesTileEntity) te).getServerMaxStackSize());
        renderer.drawString(cacheStatus, -renderer.getStringWidth(cacheStatus) / 2, (int) y - 10, 255);
        GL11.glPopMatrix();
    }
}
