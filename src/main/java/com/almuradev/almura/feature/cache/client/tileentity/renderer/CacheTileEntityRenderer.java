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
import com.almuradev.almura.shared.capability.ISingleSlotItemHandler;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;

public final class CacheTileEntityRenderer extends TileEntitySpecialRenderer<SingleSlotTileEntity> {


    private static final String ZERO_QUANTITY = "0";
    private static final String FOOTER = "-------";

    private final Minecraft client = Minecraft.getMinecraft();


    @Override
    public void render(SingleSlotTileEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {

        final ISingleSlotItemHandler
                itemHandler = (ISingleSlotItemHandler) te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

        if (itemHandler == null) {
            return;
        }

        final ItemStack cache = itemHandler.getStackInSlot(0);

        final BlockPos pos = te.getPos();
        final World world = te.getWorld();
        final IBlockState blockState = world.getBlockState(pos);

        if (!(blockState.getBlock() instanceof CacheBlock)) {
            return;
        }

        final CacheBlock block = (CacheBlock) blockState.getBlock();
        final int blockX = pos.getX();
        final int blockY = pos.getY();
        final int blockZ = pos.getZ();

        GlStateManager.pushMatrix();

        float angle = 0f;
        double translatedX = x;
        double translatedZ = z;
        float brightness = 0f;

        switch (blockState.getValue(BlockHorizontal.FACING)) {
            case SOUTH:
                angle = 0f;
                translatedX += 0.5;
                translatedZ += 1.025;
                brightness = blockState.getPackedLightmapCoords(world, new BlockPos(blockX, blockY, blockZ + 1));
                break;
            case WEST:
                angle = -90f;
                translatedX -= 0.025;
                translatedZ += 0.5;
                brightness = blockState.getPackedLightmapCoords(world, new BlockPos(blockX - 1, blockY, blockZ));
                break;
            case NORTH:
                angle = 180f;
                translatedX += 0.5;
                translatedZ -= 0.025;
                brightness = blockState.getPackedLightmapCoords(world, new BlockPos(blockX, blockY, blockZ - 1));
                break;
            case EAST:
                angle = 90f;
                translatedX += 1.025;
                translatedZ += 0.5;
                brightness = blockState.getPackedLightmapCoords(world, new BlockPos(blockX + 1, blockY, blockZ));
                break;
        }

        // Move the matrix to the front face, in the center
        GlStateManager.translate(translatedX, y + 0.525f, translatedZ);
        GlStateManager.rotate(angle, 0f, 1f, 0f);

        // Set lightmap coordinates to the skylight value of the block in front of the cache item model
        float j = brightness % 65536;
        float k = brightness / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, j / 1.0F, k / 1.0F);
        GlStateManager.color(1f, 1f, 1f, 1f);
        
        // Rotate cache item model to fit default Vanilla cache icon look. We do not support rotating the item model
        GlStateManager.rotate(180f, 0.0F, 1f, 0f);
        GlStateManager.scale(0.5F, 0.5F, 0.5F);

        // FIXED is meant for ItemFrame rendering which we are mimicing
        this.client.getRenderItem().renderItem(cache, ItemCameraTransforms.TransformType.FIXED);

        // Revert lightmap texture coordinates to world values pre-render tick
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, OpenGlHelper.lastBrightnessX, OpenGlHelper.lastBrightnessY);

        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();

        // Translate to face the cache item model is on but at the bottom edge
        GlStateManager.translate(translatedX, y, translatedZ);
        // Rotate on the y-axis by the angle
        GlStateManager.rotate(angle, 0f, 1f, 0f);

        // Scale the matrix by a scale factor (to make writing legible but not too big on block)
        final float scaleFactor = 0.6666667F * 0.016666668F;
        GlStateManager.scale(scaleFactor, -scaleFactor, scaleFactor);

        final FontRenderer fontRenderer = this.client.fontRenderer;

        String displayName;
        String cacheQuantity = ZERO_QUANTITY;

        boolean empty = cache.isEmpty();

        if (empty) {
            displayName = block.getLocalizedName();
        } else {
            cacheQuantity = CacheFeature.format.format(cache.getCount());
            displayName = cache.getDisplayName();
        }

        final String cacheMaxQuantity = CacheFeature.format.format(itemHandler.getSlotLimit(0));
        if (displayName.length() > 20) {
            fontRenderer.drawString(displayName.substring(0, 14), -fontRenderer.getStringWidth(displayName.substring(1, 14)) / 2 - 3, (int) y - 83,
                    0);
            fontRenderer.drawString(displayName.substring(14, displayName.length()), -fontRenderer.getStringWidth(displayName.substring(15,
                    displayName.length())) / 2, (int) y - 73, 0);
        } else {
            fontRenderer.drawString(displayName, -fontRenderer.getStringWidth(displayName) / 2, (int) y - 83, 0);
        }

        fontRenderer.drawString(cacheQuantity, -fontRenderer.getStringWidth(cacheQuantity) / 2, (int) y - 25, 0);
        fontRenderer.drawString(FOOTER, -fontRenderer.getStringWidth(FOOTER) / 2, (int) y - 20, 0);
        fontRenderer.drawString(cacheMaxQuantity, -fontRenderer.getStringWidth(cacheMaxQuantity) / 2, (int) y - 15, 0);

        GlStateManager.popMatrix();
    }
}
