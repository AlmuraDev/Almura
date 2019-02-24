/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.cache.client.tileentity.renderer;

import com.almuradev.almura.asm.ClientStaticAccess;
import com.almuradev.almura.core.client.config.ClientConfiguration;
import com.almuradev.almura.feature.cache.CacheFeature;
import com.almuradev.almura.feature.cache.block.CacheBlock;
import com.almuradev.almura.shared.capability.ISingleSlotItemHandler;
import com.almuradev.almura.shared.tileentity.SingleSlotTileEntity;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;

public final class CacheTileEntityRenderer extends TileEntitySpecialRenderer<SingleSlotTileEntity> {

    public static final CacheTileEntityRenderer INSTANCE = new CacheTileEntityRenderer();
    static final int MAX_LINE_WIDTH = 84;
    static final String ZERO_QUANTITY = "0";

    private final Minecraft client = Minecraft.getMinecraft();

    private CacheTileEntityRenderer() {
    }

    @Override
    public void render(final SingleSlotTileEntity te, final double x, final double y, final double z, final float partialTicks, final int destroyStage, final float alpha) {
        final BlockPos pos = te.getPos();
        final World world = te.getWorld();
        final IBlockState blockState = world.getBlockState(pos);

        render(te, x, y, z, blockState);
    }

    void render(final SingleSlotTileEntity te, final double x, final double y, final double z, final IBlockState blockState) {
        final ClientConfiguration configuration = ClientStaticAccess.configAdapter.get();
        EntityLivingBase viewer = (EntityLivingBase) Minecraft.getMinecraft().getRenderViewEntity();
        if (viewer == null) {
            viewer = Minecraft.getMinecraft().player;
        }

        final ISingleSlotItemHandler
                itemHandler = (ISingleSlotItemHandler) te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

        if (itemHandler == null) {
            return;
        }

        final ItemStack cache = itemHandler.getStackInSlot(0);

        final BlockPos pos = te.getPos();

        if (!(blockState.getBlock() instanceof CacheBlock)) {
            return;
        }

        final CacheBlock block = (CacheBlock) blockState.getBlock();

        GlStateManager.pushMatrix();

        float angle = 0f;
        double translatedX = x;
        double translatedZ = z;
        final EnumFacing facing = blockState.getValue(BlockHorizontal.FACING);

        switch (facing) {
            case SOUTH:
                angle = 0f;
                translatedX += 0.5;
                translatedZ += 1.025;
                break;
            case WEST:
                angle = -90f;
                translatedX -= 0.025;
                translatedZ += 0.5;
                break;
            case NORTH:
                angle = 180f;
                translatedX += 0.5;
                translatedZ -= 0.025;
                break;
            case EAST:
                angle = 90f;
                translatedX += 1.025;
                translatedZ += 0.5;
                break;
        }

        final int brightness = te.hasWorld() ? blockState.getPackedLightmapCoords(te.getWorld(), pos.offset(facing)) : 15 << 20 | 15 << 4;

        // Move the matrix to the front face, in the center
        GlStateManager.translate(translatedX, y + 0.525f, translatedZ);
        GlStateManager.rotate(angle, 0f, 1f, 0f);

        // Set lightmap coordinates to the skylight value of the block in front of the cache item model
        int j = brightness % 65536;
        int k = brightness / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, j / 1.0F, k / 1.0F);
        GlStateManager.color(1f, 1f, 1f, 1f);

        // Rotate cache item model to fit default Vanilla cache icon look. We do not support rotating the item model
        GlStateManager.rotate(180f, 0.0F, 1f, 0f);
        GlStateManager.scale(0.5F, 0.5F, 0.5F);

        // FIXED is meant for ItemFrame rendering which we are mimicing
        boolean renderItem = true;
        if (configuration.general.itemFrameRenderDistance > 0) {
            if (viewer == null || !te.hasWorld() || te.getDistanceSq(viewer.posX, viewer.posY, viewer.posZ) >
                (configuration.general.itemFrameRenderDistance * 16)) {
                renderItem = false;
            }
        }
        if (renderItem) {
            this.client.getRenderItem().renderItem(cache, ItemCameraTransforms.TransformType.FIXED);
        }

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

        boolean renderText = true;

        if (configuration.general.signTextRenderDistance > 0) {
            if (viewer == null || !te.hasWorld() || te.getDistanceSq(viewer.posX, viewer.posY, viewer.posZ) >
                (configuration.general.signTextRenderDistance * 16)) {
                renderText = false;
            }
        }

        if (renderText) {
            boolean empty = cache.isEmpty();

            if (empty) {
                displayName = block.getLocalizedName();
            } else {
                cacheQuantity = CacheFeature.format.format(cache.getCount());
                displayName = cache.getDisplayName();
            }

            final String cacheMaxQuantity = CacheFeature.format.format(itemHandler.getSlotLimit(0));

            // Check if we can fit the displayname on the first line, if not then split it to two lines at most
            if (fontRenderer.getStringWidth(displayName) > MAX_LINE_WIDTH) {
                int firstLineLength = displayName.length();
                while (fontRenderer.getStringWidth(displayName.substring(0, firstLineLength)) > MAX_LINE_WIDTH) {
                    firstLineLength--;
                }
                final String firstLine = displayName.substring(0, firstLineLength);
                final String secondLine = displayName.substring(firstLineLength);
                fontRenderer.drawString(firstLine, -fontRenderer.getStringWidth(firstLine) / 2, (int) y - 80, 0);
                fontRenderer.drawString(secondLine, -fontRenderer.getStringWidth(secondLine) / 2, (int) y - 70, 0);
            } else {
                fontRenderer.drawString(displayName, -fontRenderer.getStringWidth(displayName) / 2, (int) y - 80, 0);
            }

            final int lineWidth = fontRenderer.getStringWidth(cacheMaxQuantity) / 2;
            fontRenderer.drawString(cacheQuantity, -fontRenderer.getStringWidth(cacheQuantity) / 2, (int) y - 24, 0);
            Gui.drawRect(-lineWidth, (int) y - 16, lineWidth, (int) y - 15, 0xFF000000);
            fontRenderer.drawString(cacheMaxQuantity, -fontRenderer.getStringWidth(cacheMaxQuantity) / 2, (int) y - 14, 0);
        }

        GlStateManager.popMatrix();
    }
}
