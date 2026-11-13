/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.machine.kitchen.client;

import com.almuradev.almura.feature.machine.processing.ProcessingBlock;
import com.almuradev.almura.feature.machine.processing.ProcessingTileEntity;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

/**
 * Draws a machine's moving part (e.g. the slicer's blade or the grinder's cutting plate, kept out of the block model) every frame, spinning
 * around its axle while the machine is running. When it stops the part stays where it was.
 */
@SideOnly(Side.CLIENT)
public final class SpinningPartRenderer<T extends ProcessingTileEntity> extends TileEntitySpecialRenderer<T> {

    private final ResourceLocation modelLocation;
    // A point on the axle and its direction, in block units (matching the models, which face north)
    private final float axleX;
    private final float axleY;
    private final float axleZ;
    private final float axisX;
    private final float axisY;
    private final float axisZ;
    private final float degreesPerTick;

    /** Baked when models are (see KitchenMachinesClient); null until then. */
    @Nullable IBakedModel model;

    /**
     * @param modelLocation the part's OBJ model
     * @param axle a point on the axle (x, y, z)
     * @param axis the axle's direction (x, y, z)
     * @param degreesPerTick spin speed while running
     */
    SpinningPartRenderer(final ResourceLocation modelLocation, final float[] axle, final float[] axis, final float degreesPerTick) {
        this.modelLocation = modelLocation;
        this.axleX = axle[0];
        this.axleY = axle[1];
        this.axleZ = axle[2];
        this.axisX = axis[0];
        this.axisY = axis[1];
        this.axisZ = axis[2];
        this.degreesPerTick = degreesPerTick;
    }

    ResourceLocation modelLocation() {
        return this.modelLocation;
    }

    @Override
    public void render(final T te, final double x, final double y, final double z, final float partialTicks, final int destroyStage,
            final float alpha) {
        final IBakedModel model = this.model;
        if (model == null || !te.hasWorld()) {
            return;
        }

        final IBlockState state = te.getWorld().getBlockState(te.getPos());
        if (!(state.getBlock() instanceof ProcessingBlock)) {
            return;
        }

        // Spin while running; when stopped, stay at the last angle rather than snapping back
        if (state.getValue(ProcessingBlock.ACTIVE)) {
            te.spinAngle = ((te.getWorld().getTotalWorldTime() + partialTicks) * this.degreesPerTick) % 360.0F;
        }

        this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);

        // Face the same way as the block model (blockstate y rotations turn clockwise seen from above)
        GlStateManager.translate(0.5F, 0.0F, 0.5F);
        GlStateManager.rotate(-facingDegrees(state.getValue(BlockHorizontal.FACING)), 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(-0.5F, 0.0F, -0.5F);

        // Spin around the axle
        GlStateManager.translate(this.axleX, this.axleY, this.axleZ);
        GlStateManager.rotate(te.spinAngle, this.axisX, this.axisY, this.axisZ);
        GlStateManager.translate(-this.axleX, -this.axleY, -this.axleZ);

        // Lit like the block it's in
        RenderHelper.disableStandardItemLighting();
        final int light = te.getWorld().getCombinedLight(te.getPos(), 0);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, light % 65536, light / 65536);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer().renderModelBrightnessColor(model, 1.0F, 1.0F, 1.0F, 1.0F);

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
