/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.complex.item.almanac.gui;

import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.element.position.Position.IPosition;
import net.malisis.core.client.gui.render.GuiRenderer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Draws a block state's model, turned the way blocks are shown in the inventory. The view is fitted to a {@link View}, so several states
 * (such as a crop's stages) can share one scale and line up.
 */
@SideOnly(Side.CLIENT)
public final class UIBlockStateModel extends UIComponent {

    // Same turn as a block in the inventory: tipped 30 degrees, turned 225
    private static final float TILT = 30F;
    private static final float TURN = 225F;

    private final IBlockState state;
    private final View view;
    private boolean failed;

    public UIBlockStateModel(final IBlockState state, final int size, final View view) {
        this.state = state;
        this.view = view;
        this.setSize(size, size);
        this.setForeground(this::renderModel);
    }

    private void renderModel(final GuiRenderer renderer) {
        if (this.failed) {
            return;
        }

        final Minecraft client = Minecraft.getMinecraft();
        // Draw what the GUI has batched so far, as drawItemStack does, before drawing with Minecraft's own renderer
        if (renderer.isDrawing()) {
            Tessellator.getInstance().draw();
        }

        final IPosition position = this.screenPosition();
        final float size = this.getWidth();
        final float scale = size * this.view.scale;

        GlStateManager.pushMatrix();
        // The GUI doesn't use the depth buffer; clear it so the model's own faces sort against each other only
        GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT);
        GlStateManager.enableDepth();
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        client.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        // The view's centre goes to the middle of the component
        GlStateManager.translate(position.x() + size / 2F, position.y() + size / 2F, 150F + this.getZIndex());
        GlStateManager.scale(scale, -scale, scale);
        GlStateManager.rotate(TILT, 1F, 0F, 0F);
        GlStateManager.rotate(TURN, 0F, 1F, 0F);
        GlStateManager.translate(-this.view.centerX, -this.view.centerY, -this.view.centerZ);
        // renderBlockBrightness turns the model 90 degrees about its corner first (for blocks held by endermen and minecarts); undo it
        GlStateManager.rotate(-90F, 0F, 1F, 0F);

        RenderHelper.enableGUIStandardItemLighting();
        try {
            client.getBlockRendererDispatcher().renderBlockBrightness(this.state, 1.0F);
        } catch (final RuntimeException e) {
            // A model or color handler that can't draw outside the world; show nothing rather than break the screen
            this.failed = true;
        }
        RenderHelper.disableStandardItemLighting();

        GlStateManager.disableRescaleNormal();
        GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT);
        GlStateManager.disableDepth();
        GlStateManager.popMatrix();

        // Put the GUI's texture back and carry on batching, as drawItemStack does
        try {
            renderer.forceRebind();
        } catch (final NullPointerException e) {
            // No texture was bound yet
            renderer.bindDefaultTexture();
        }
        renderer.startDrawing();
    }

    /**
     * Where to look and how far to zoom so every given state's model fits.
     */
    public static final class View {

        final float centerX, centerY, centerZ;
        /** Model units per pixel of the component's size */
        final float scale;

        private View(final float centerX, final float centerY, final float centerZ, final float scale) {
            this.centerX = centerX;
            this.centerY = centerY;
            this.centerZ = centerZ;
            this.scale = scale;
        }

        /**
         * @param states the states that will be shown with this view
         * @return a view that fits all of their models, turned as they're drawn, with a little margin
         */
        public static View fitting(final Collection<IBlockState> states) {
            float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, minZ = Float.MAX_VALUE;
            float maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE, maxZ = -Float.MAX_VALUE;
            boolean any = false;

            for (final IBlockState state : states) {
                final IBakedModel model;
                try {
                    model = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(state);
                } catch (final RuntimeException e) {
                    continue;
                }
                for (final BakedQuad quad : quads(model, state)) {
                    final VertexFormat format = quad.getFormat();
                    final int positionOffset = positionOffset(format);
                    if (positionOffset < 0) {
                        continue;
                    }
                    final int stride = format.getIntegerSize();
                    final int[] data = quad.getVertexData();
                    for (int vertex = 0; vertex + stride <= data.length; vertex += stride) {
                        final float x = Float.intBitsToFloat(data[vertex + positionOffset]);
                        final float y = Float.intBitsToFloat(data[vertex + positionOffset + 1]);
                        final float z = Float.intBitsToFloat(data[vertex + positionOffset + 2]);
                        minX = Math.min(minX, x); maxX = Math.max(maxX, x);
                        minY = Math.min(minY, y); maxY = Math.max(maxY, y);
                        minZ = Math.min(minZ, z); maxZ = Math.max(maxZ, z);
                        any = true;
                    }
                }
            }

            if (!any) {
                minX = minY = minZ = 0F;
                maxX = maxY = maxZ = 1F;
            }

            // Turn the corners of the box the way the model is drawn, and fit what's seen on screen
            final double turn = Math.toRadians(TURN);
            final double tilt = Math.toRadians(TILT);
            float screenMinX = Float.MAX_VALUE, screenMaxX = -Float.MAX_VALUE, screenMinY = Float.MAX_VALUE, screenMaxY = -Float.MAX_VALUE;
            final float centerX = (minX + maxX) / 2F, centerY = (minY + maxY) / 2F, centerZ = (minZ + maxZ) / 2F;
            for (int corner = 0; corner < 8; corner++) {
                final double x = ((corner & 1) == 0 ? minX : maxX) - centerX;
                final double y = ((corner & 2) == 0 ? minY : maxY) - centerY;
                final double z = ((corner & 4) == 0 ? minZ : maxZ) - centerZ;
                // Turned about Y, then tipped about X, as the GL calls apply them
                final double turnedX = x * Math.cos(turn) + z * Math.sin(turn);
                final double turnedZ = -x * Math.sin(turn) + z * Math.cos(turn);
                final double tippedY = y * Math.cos(tilt) - turnedZ * Math.sin(tilt);
                screenMinX = (float) Math.min(screenMinX, turnedX);
                screenMaxX = (float) Math.max(screenMaxX, turnedX);
                screenMinY = (float) Math.min(screenMinY, tippedY);
                screenMaxY = (float) Math.max(screenMaxY, tippedY);
            }

            final float extent = Math.max(screenMaxX - screenMinX, screenMaxY - screenMinY);
            final float scale = extent <= 0F ? 0.6F : 0.92F / extent;
            return new View(centerX, centerY, centerZ, scale);
        }

        private static List<BakedQuad> quads(final IBakedModel model, final IBlockState state) {
            final List<BakedQuad> quads = new ArrayList<>(model.getQuads(state, null, 0L));
            for (final EnumFacing side : EnumFacing.values()) {
                quads.addAll(model.getQuads(state, side, 0L));
            }
            return quads;
        }

        /**
         * @return the offset, in ints, of the vertex position in the format, or -1 if it has none
         */
        private static int positionOffset(final VertexFormat format) {
            for (int index = 0; index < format.getElementCount(); index++) {
                if (format.getElement(index).getUsage() == VertexFormatElement.EnumUsage.POSITION) {
                    return format.getOffset(index) / 4;
                }
            }
            return -1;
        }
    }
}
