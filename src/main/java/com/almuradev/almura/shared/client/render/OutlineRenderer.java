/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.function.Consumer;

/**
 * Draws world-space line outlines (claim borders, WorldEdit selections): grid lines on the faces of a box or prism, with thicker edges.
 */
@SideOnly(Side.CLIENT)
public final class OutlineRenderer {

    // How strongly fillBox and fillPrism tint the faces.
    public static final float FILL_ALPHA = 0.25f;
    // Lifts the lines off block faces so the ground doesn't hide them.
    private static final double INSET = 0.01;

    private OutlineRenderer() {
    }

    /**
     * Sets up world-space drawing for a {@code RenderWorldLastEvent}: {@code fill} tints the faces, then {@code draw} adds the lines with the
     * alpha to use. Both are depth-tested, so terrain hides whatever is behind or under it, as WorldEditCUI did.
     */
    public static void render(final float partialTicks, final Runnable fill, final Consumer<Float> draw) {
        final Entity view = Minecraft.getMinecraft().getRenderViewEntity();
        if (view == null) {
            return;
        }
        final double x = view.lastTickPosX + (view.posX - view.lastTickPosX) * partialTicks;
        final double y = view.lastTickPosY + (view.posY - view.lastTickPosY) * partialTicks;
        final double z = view.lastTickPosZ + (view.posZ - view.lastTickPosZ) * partialTicks;

        GlStateManager.pushMatrix();
        GlStateManager.translate(-x, -y, -z);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.depthMask(false);

        GlStateManager.enableDepth();
        fill.run();
        draw.accept(1.0f);

        GlStateManager.glLineWidth(1.0f);
        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }

    public static BufferBuilder begin(final float lineWidth) {
        GlStateManager.glLineWidth(lineWidth);
        final BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        return buffer;
    }

    public static void end() {
        Tessellator.getInstance().draw();
    }

    public static BufferBuilder beginFill() {
        final BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        return buffer;
    }

    /**
     * Tints the walls of the box from (x0, y0, z0) to (x1, y1, z1), in block-edge coordinates, and its top and bottom when {@code caps} is set.
     */
    public static void fillBox(final BufferBuilder buffer, final double x0, final double y0, final double z0, final double x1, final double y1,
        final double z1, final boolean caps, final int color, final float alpha) {
        final double nx = x0 - INSET, px = x1 + INSET, ny = y0 - INSET, py = y1 + INSET, nz = z0 - INSET, pz = z1 + INSET;
        quad(buffer, nx, ny, nz, px, ny, nz, px, py, nz, nx, py, nz, color, alpha);
        quad(buffer, nx, ny, pz, px, ny, pz, px, py, pz, nx, py, pz, color, alpha);
        quad(buffer, nx, ny, nz, nx, ny, pz, nx, py, pz, nx, py, nz, color, alpha);
        quad(buffer, px, ny, nz, px, ny, pz, px, py, pz, px, py, nz, color, alpha);
        if (caps) {
            quad(buffer, nx, ny, nz, px, ny, nz, px, ny, pz, nx, ny, pz, color, alpha);
            quad(buffer, nx, py, nz, px, py, nz, px, py, pz, nx, py, pz, color, alpha);
        }
    }

    /**
     * Tints the walls of the vertical prism over the block columns {@code points} (each {x, z}), from y0 to y1.
     */
    public static void fillPrism(final BufferBuilder buffer, final List<int[]> points, final int y0, final int y1, final int color, final float alpha) {
        final int count = points.size();
        for (int i = 0; i < count; i++) {
            final int[] a = points.get(i);
            final int[] b = points.get((i + 1) % count);
            final double ax = a[0] + 0.5, az = a[1] + 0.5, bx = b[0] + 0.5, bz = b[1] + 0.5;
            quad(buffer, ax, y0, az, bx, y0, bz, bx, y1, bz, ax, y1, az, color, alpha);
        }
    }

    private static void quad(final BufferBuilder buffer, final double ax, final double ay, final double az, final double bx, final double by,
        final double bz, final double cx, final double cy, final double cz, final double dx, final double dy, final double dz, final int color,
        final float alpha) {
        final float r = (color >> 16 & 255) / 255f, g = (color >> 8 & 255) / 255f, b = (color & 255) / 255f;
        buffer.pos(ax, ay, az).color(r, g, b, alpha).endVertex();
        buffer.pos(bx, by, bz).color(r, g, b, alpha).endVertex();
        buffer.pos(cx, cy, cz).color(r, g, b, alpha).endVertex();
        buffer.pos(dx, dy, dz).color(r, g, b, alpha).endVertex();
    }

    /**
     * Grid lines every {@code step} blocks on the walls of the box from (x0, y0, z0) to (x1, y1, z1), in block-edge coordinates, and on its top
     * and bottom when {@code caps} is set.
     */
    public static void gridBox(final BufferBuilder buffer, final int x0, final int y0, final int z0, final int x1, final int y1, final int z1,
        final int step, final boolean caps, final int color, final float alpha) {
        final double nx = x0 - INSET, px = x1 + INSET, ny = y0 - INSET, py = y1 + INSET, nz = z0 - INSET, pz = z1 + INSET;
        for (int gx = x0 + step; gx < x1; gx += step) {
            line(buffer, gx, ny, nz, gx, py, nz, color, alpha);
            line(buffer, gx, ny, pz, gx, py, pz, color, alpha);
            if (caps) {
                line(buffer, gx, ny, nz, gx, ny, pz, color, alpha);
                line(buffer, gx, py, nz, gx, py, pz, color, alpha);
            }
        }
        for (int gz = z0 + step; gz < z1; gz += step) {
            line(buffer, nx, ny, gz, nx, py, gz, color, alpha);
            line(buffer, px, ny, gz, px, py, gz, color, alpha);
            if (caps) {
                line(buffer, nx, ny, gz, px, ny, gz, color, alpha);
                line(buffer, nx, py, gz, px, py, gz, color, alpha);
            }
        }
        for (int gy = y0 + step; gy < y1; gy += step) {
            ring(buffer, nx, nz, px, pz, gy, color, alpha);
        }
    }

    /**
     * The twelve edges of the box from (x0, y0, z0) to (x1, y1, z1), in block-edge coordinates.
     */
    public static void edgeBox(final BufferBuilder buffer, final double x0, final double y0, final double z0, final double x1, final double y1,
        final double z1, final int color, final float alpha) {
        final double nx = x0 - INSET, px = x1 + INSET, ny = y0 - INSET, py = y1 + INSET, nz = z0 - INSET, pz = z1 + INSET;
        line(buffer, nx, ny, nz, nx, py, nz, color, alpha);
        line(buffer, px, ny, nz, px, py, nz, color, alpha);
        line(buffer, nx, ny, pz, nx, py, pz, color, alpha);
        line(buffer, px, ny, pz, px, py, pz, color, alpha);
        ring(buffer, nx, nz, px, pz, ny, color, alpha);
        ring(buffer, nx, nz, px, pz, py, color, alpha);
    }

    /**
     * A vertical prism over the block columns {@code points} (each {x, z}), from y0 to y1 in block-edge coordinates: horizontal rings every
     * {@code step} blocks, or with {@code edges} set, the top and bottom rings and a vertical line at each corner.
     */
    public static void prism(final BufferBuilder buffer, final List<int[]> points, final int y0, final int y1, final int step, final boolean edges,
        final int color, final float alpha) {
        final int count = points.size();
        if (count == 0) {
            return;
        }
        // Each point is a block column; draw through its centre so the outline follows the blocks WorldEdit selected.
        for (int i = 0; i < count; i++) {
            final int[] a = points.get(i);
            final int[] b = points.get((i + 1) % count);
            final double ax = a[0] + 0.5, az = a[1] + 0.5, bx = b[0] + 0.5, bz = b[1] + 0.5;
            if (edges) {
                line(buffer, ax, y0 - INSET, az, ax, y1 + INSET, az, color, alpha);
                line(buffer, ax, y0 - INSET, az, bx, y0 - INSET, bz, color, alpha);
                line(buffer, ax, y1 + INSET, az, bx, y1 + INSET, bz, color, alpha);
            } else {
                for (int gy = y0 + step; gy < y1; gy += step) {
                    line(buffer, ax, gy, az, bx, gy, bz, color, alpha);
                }
            }
        }
    }

    public static void line(final BufferBuilder buffer, final double ax, final double ay, final double az, final double bx, final double by,
        final double bz, final int color, final float alpha) {
        final float r = (color >> 16 & 255) / 255f, g = (color >> 8 & 255) / 255f, b = (color & 255) / 255f;
        buffer.pos(ax, ay, az).color(r, g, b, alpha).endVertex();
        buffer.pos(bx, by, bz).color(r, g, b, alpha).endVertex();
    }

    private static void ring(final BufferBuilder buffer, final double nx, final double nz, final double px, final double pz, final double y,
        final int color, final float alpha) {
        line(buffer, nx, y, nz, px, y, nz, color, alpha);
        line(buffer, nx, y, pz, px, y, pz, color, alpha);
        line(buffer, nx, y, nz, nx, y, pz, color, alpha);
        line(buffer, px, y, nz, px, y, pz, color, alpha);
    }
}
