/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2017 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.special.block;

import com.almuradev.almura.Configuration;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.opengl.GL11;

import java.text.NumberFormat;
import java.util.Locale;

public class CachesTileEntitySpecialRenderer extends TileEntitySpecialRenderer {

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTick) {

        
        if (!(te instanceof CachesTileEntity) || te.getWorld() == null) {
            return;
        }
        
        boolean empty = false;
        
        if (((CachesTileEntity) te).getCache() == null) {
            empty = true;
        }

        final Block block = te.getWorld().getBlock(te.xCoord, te.yCoord, te.zCoord);

        // Ensure the block at this position is our block
        if (!(block instanceof CachesBlock)) {
            return;
        }

        EntityClientPlayerMP viewer = (EntityClientPlayerMP) Minecraft.getMinecraft().renderViewEntity;
        if (viewer == null) {
            viewer = Minecraft.getMinecraft().thePlayer;
        }

        if (viewer != null && te.getDistanceSq(viewer.posX, viewer.posY, viewer.posZ) > (Configuration.DISTANCE_RENDER_CHEST * 16)) {
            return;
        }

        final CachesTileEntity cte = (CachesTileEntity) te;
        final int metadata = cte.getBlockMetadata();
        ItemStack cache = null;
        if (!empty) {
            cache = cte.getCache();
        }

        float angle = 0f;
        double translatedX, translatedZ;
        translatedX = x;
        translatedZ = z;

        int brightness = 0;
        if (metadata == 0) { // South
            translatedX += 0.5;
            translatedZ += 1.015;
            brightness = te.getWorld().getLightBrightnessForSkyBlocks(te.xCoord, te.yCoord, te.zCoord + 1, 0);
        } else if (metadata == 1) { // West
            angle = -90f;
            translatedX -= 0.015;
            translatedZ += 0.5;
            brightness = te.getWorld().getLightBrightnessForSkyBlocks(te.xCoord - 1, te.yCoord, te.zCoord, 0);
        } else if (metadata == 2) { // North
            angle = 180f;
            translatedX += 0.5;
            translatedZ -= 0.015;
            brightness = te.getWorld().getLightBrightnessForSkyBlocks(te.xCoord, te.yCoord, te.zCoord - 1, 0);
        } else if (metadata == 3) { //East
            translatedX += 1.015;
            translatedZ += 0.5;
            angle = 90f;
            brightness = te.getWorld().getLightBrightnessForSkyBlocks(te.xCoord + 1, te.yCoord, te.zCoord, 0);
        }

        // Draw ItemType 2D visual in front
        GL11.glPushMatrix();
        ItemStack visualStack = null;
        EntityItem visualItem = null;
        
        if (!empty) {
            visualStack = ItemStack.copyItemStack(cte.getCache());
            visualStack.stackSize = 1;

            visualItem = new EntityItem(te.getWorld(), 0, 0, 0, visualStack);
            visualItem.hoverStart = 0.0f;
        }

        GL11.glTranslated(translatedX, y + 0.35, translatedZ);
        // Rotate
        GL11.glRotatef(angle, 0, 1f, 0);
        int j = brightness % 65536;
        int k = brightness / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) j / 1.0F, (float) k / 1.0F);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        RenderItem.renderInFrame = true;
        if (!empty) {
            RenderManager.instance.renderEntityWithPosYaw(visualItem, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F);
        }
        RenderItem.renderInFrame = false;

        GL11.glPopMatrix();

        // Draw Quantity/Limit text visual
        GL11.glPushMatrix();

        GL11.glTranslated(translatedX, y, translatedZ);
        GL11.glRotatef(angle, 0f, 1f, 0f);

        float scaleFactor = 0.6666667F * 0.016666668F;
        GL11.glScalef(scaleFactor, -scaleFactor, scaleFactor);

        final FontRenderer renderer = Minecraft.getMinecraft().fontRendererObj;

        String displayName = "";

        if (empty) {
            displayName = ((CachesBlock) block).getDisplayName();
        } else {
            displayName = cache.getDisplayName();
        }

        String cacheQuantity = "0";

        if (!empty) {
            cacheQuantity = NumberFormat.getNumberInstance(Locale.US).format(cache.stackSize);
        }

        final String cacheMaxQuantity = NumberFormat.getNumberInstance(Locale.US).format(cte.getServerMaxStackSize());
        if (displayName.length() > 20) {
            renderer.drawString(displayName.substring(0, 14), -renderer.getStringWidth(displayName.substring(1,14)) / 2, (int) y - 85, 0);
            renderer.drawString(displayName.substring(14, displayName.length()), -renderer.getStringWidth(displayName.substring(15,displayName.length())) / 2, (int) y - 75, 0);
        } else {
            renderer.drawString(displayName, -renderer.getStringWidth(displayName) / 2, (int) y - 85, 0);
        }
        renderer.drawString(cacheQuantity, -renderer.getStringWidth(cacheQuantity) / 2, (int) y - 20, 0);
        renderer.drawString("-------", -renderer.getStringWidth("-------") / 2, (int) y - 15, 0);
        renderer.drawString(cacheMaxQuantity, -renderer.getStringWidth(cacheMaxQuantity) / 2, (int) y - 10, 0);
        GL11.glPopMatrix();
    }
}
