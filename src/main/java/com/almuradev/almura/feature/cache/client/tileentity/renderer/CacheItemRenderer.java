/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.cache.client.tileentity.renderer;

import com.almuradev.almura.feature.cache.CacheFeature;
import com.almuradev.almura.feature.cache.block.CacheBlock;
import com.almuradev.almura.shared.tileentity.SingleSlotTileEntity;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import static com.almuradev.almura.feature.cache.client.tileentity.renderer.CacheTileEntityRenderer.MAX_LINE_WIDTH;
import static com.almuradev.almura.feature.cache.client.tileentity.renderer.CacheTileEntityRenderer.ZERO_QUANTITY;

@SideOnly(Side.CLIENT)
public final class CacheItemRenderer extends TileEntityItemStackRenderer {

    public static final CacheItemRenderer INSTANCE = new CacheItemRenderer();
    private final SingleSlotTileEntity tile = new SingleSlotTileEntity();
    /*
     * Prevents recursive drawing.
     */
    private boolean inUse = false;

    private CacheItemRenderer() {
    }

    @Override
    public void renderByItem(ItemStack stack, float partialTicks) {
        final CacheBlock block = (CacheBlock) ((ItemBlock) stack.getItem()).getBlock();

        GlStateManager.translate(0D, 0D, 1.0D);
        Minecraft.getMinecraft().getBlockRendererDispatcher().renderBlockBrightness(block.getDefaultState().withProperty(BlockHorizontal.FACING, EnumFacing.EAST), 1.0F);

        if (this.inUse) {
            return;
        }
        CacheBlock.addStackToCache(stack, this.tile);
        final IItemHandler handler = this.tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        if (handler == null) {
            return;
        }
        this.inUse = true;
        render(handler.getStackInSlot(0), block, handler.getSlotLimit(0));
        this.inUse = false;
    }

    private void render(final ItemStack cache, final CacheBlock block, int max) {
        GlStateManager.pushMatrix();

        double translatedX = 0;
        double translatedZ = 0;

        final float angle = 90f;
        translatedX += 1.025;
        translatedZ += 0.5;

        // Move the matrix to the front face, in the center
        GlStateManager.translate(translatedX, 0.525f, translatedZ);
        GlStateManager.rotate(angle, 0f, 1f, 0f);

        // Set lightmap coordinates to the skylight value of the block in front of the cache item model
        int light = 15 << 20 | 15 << 4;
        int j = light % 65536;
        int k = light / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, j / 1.0F, k / 1.0F);
        GlStateManager.color(1f, 1f, 1f, 1f);

        // Rotate cache item model to fit default Vanilla cache icon look. We do not support rotating the item model
        GlStateManager.rotate(180f, 0.0F, 1f, 0f);
        GlStateManager.scale(0.5F, 0.5F, 0.5F);

        // FIXED is meant for ItemFrame rendering which we are mimicing
        Minecraft.getMinecraft().getRenderItem().renderItem(cache, ItemCameraTransforms.TransformType.FIXED);

        // Revert lightmap texture coordinates to world values pre-render tick
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, OpenGlHelper.lastBrightnessX, OpenGlHelper.lastBrightnessY);

        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();

        // Translate to face the cache item model is on but at the bottom edge
        GlStateManager.translate(translatedX, 0, translatedZ);
        // Rotate on the y-axis by the angle
        GlStateManager.rotate(angle, 0f, 1f, 0f);

        // Scale the matrix by a scale factor (to make writing legible but not too big on block)
        final float scaleFactor = 0.6666667F * 0.016666668F;
        GlStateManager.scale(scaleFactor, -scaleFactor, scaleFactor);

        final FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;

        String displayName;
        String cacheQuantity = ZERO_QUANTITY;

        boolean empty = cache.isEmpty();

        if (empty) {
            displayName = block.getLocalizedName();
        } else {
            cacheQuantity = CacheFeature.format.format(cache.getCount());
            displayName = cache.getDisplayName();
        }

        final String cacheMaxQuantity = CacheFeature.format.format(max);

        // Check if we can fit the displayname on the first line, if not then split it to two lines at most
        if (fontRenderer.getStringWidth(displayName) > MAX_LINE_WIDTH) {
            int firstLineLength = displayName.length();
            while (fontRenderer.getStringWidth(displayName.substring(0, firstLineLength)) > MAX_LINE_WIDTH) {
                firstLineLength--;
            }
            final String firstLine = displayName.substring(0, firstLineLength);
            final String secondLine = displayName.substring(firstLineLength);
            fontRenderer.drawString(firstLine, -fontRenderer.getStringWidth(firstLine) / 2, -80, 0);
            fontRenderer.drawString(secondLine, -fontRenderer.getStringWidth(secondLine) / 2, -70, 0);
        } else {
            fontRenderer.drawString(displayName, -fontRenderer.getStringWidth(displayName) / 2, -80, 0);
        }

        final int lineWidth = fontRenderer.getStringWidth(cacheMaxQuantity) / 2;
        fontRenderer.drawString(cacheQuantity, -fontRenderer.getStringWidth(cacheQuantity) / 2, -24, 0);
        Gui.drawRect(-lineWidth, -16, lineWidth, -15, 0xFF000000);
        fontRenderer.drawString(cacheMaxQuantity, -fontRenderer.getStringWidth(cacheMaxQuantity) / 2, -14, 0);

        GlStateManager.popMatrix();
    }
}
