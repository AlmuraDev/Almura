/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.cache.client.tileentity.renderer;

import com.almuradev.almura.feature.cache.block.CacheBlock;
import com.almuradev.almura.shared.tileentity.SingleSlotTileEntity;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class CacheItemRenderer extends TileEntityItemStackRenderer {

    public static final CacheItemRenderer INSTANCE = new CacheItemRenderer();
    private final SingleSlotTileEntity tile = new SingleSlotTileEntity();

    private CacheItemRenderer() {
    }

    @Override
    public void renderByItem(final ItemStack stack, final float partialTicks) {
        final CacheBlock block = (CacheBlock) ((ItemBlock) stack.getItem()).getBlock();
        final IBlockState state = block.getDefaultState().withProperty(BlockHorizontal.FACING, EnumFacing.EAST);

        GlStateManager.translate(0D, 0D, 1.0D);
        Minecraft.getMinecraft().getBlockRendererDispatcher().renderBlockBrightness(state, 1.0F);

        final ItemStack previous = CacheBlock.getFromCache(this.tile);
        CacheBlock.addStackToCache(stack, this.tile);
        CacheTileEntityRenderer.INSTANCE.render(this.tile, 0, 0, 0, state);
        CacheBlock.addStackToCache(previous, this.tile);
    }
}
