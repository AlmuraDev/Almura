/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.machine.processing.client;

import com.almuradev.almura.feature.machine.processing.ProcessingBlock;
import com.almuradev.almura.feature.machine.processing.ProcessingTileEntity;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

/**
 * Draws a machine's moving parts (see {@link SpinningPart}) every frame, each spinning around its own axle while the machine is running.
 * When it stops the parts stay where they were.
 */
@SideOnly(Side.CLIENT)
public final class SpinningPartRenderer<T extends ProcessingTileEntity> extends TileEntitySpecialRenderer<T> {

    private final List<SpinningPart> parts;

    SpinningPartRenderer(final SpinningPart... parts) {
        this.parts = ImmutableList.copyOf(parts);
    }

    List<SpinningPart> parts() {
        return this.parts;
    }

    @Override
    public void render(final T te, final double x, final double y, final double z, final float partialTicks, final int destroyStage,
            final float alpha) {
        if (!te.hasWorld()) {
            return;
        }

        final IBlockState state = te.getWorld().getBlockState(te.getPos());
        if (!(state.getBlock() instanceof ProcessingBlock)) {
            return;
        }

        // Spin while running; when stopped, stay where they were rather than snapping back
        if (state.getValue(ProcessingBlock.ACTIVE)) {
            te.spinTicks = te.getWorld().getTotalWorldTime() + partialTicks;
        }

        this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);

        // Face the same way as the block model (blockstate y rotations turn clockwise seen from above)
        GlStateManager.translate(0.5F, 0.0F, 0.5F);
        GlStateManager.rotate(-facingDegrees(state.getValue(BlockHorizontal.FACING)), 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(-0.5F, 0.0F, -0.5F);

        // Lit like the block they're in
        RenderHelper.disableStandardItemLighting();
        final int light = te.getWorld().getCombinedLight(te.getPos(), 0);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, light % 65536, light / 65536);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        final BlockModelRenderer renderer = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer();
        for (final SpinningPart part : this.parts) {
            final IBakedModel model = part.model;
            if (model == null) {
                continue;
            }
            // Spin around the part's axle
            GlStateManager.pushMatrix();
            GlStateManager.translate(part.axleX, part.axleY, part.axleZ);
            GlStateManager.rotate((te.spinTicks * part.degreesPerTick) % 360.0F, part.axisX, part.axisY, part.axisZ);
            GlStateManager.translate(-part.axleX, -part.axleY, -part.axleZ);
            renderer.renderModelBrightnessColor(model, 1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.popMatrix();
        }

        RenderHelper.enableStandardItemLighting();
        GlStateManager.popMatrix();
    }

    private static float facingDegrees(final EnumFacing facing) {
        switch (facing) {
            case EAST:
                return 90.0F;
            case SOUTH:
                return 180.0F;
            case WEST:
                return 270.0F;
            default:
                return 0.0F;
        }
    }
}
