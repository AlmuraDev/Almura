/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.worldedit.client;

import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Draws world-space outlines as 3.5's {@code OutlineRenderer} did: grid lines on the faces of a box or prism, thicker edges, and a light
 * tint on the faces. Drawn as the game's gizmos (its debug shapes), which are depth tested, so terrain hides whatever is behind or under the
 * outline, as WorldEditCUI did; the tint doesn't hide the lines behind it. Must be called while gizmos are being collected.
 * <p>
 * Coordinates are block edges in world space; colours are RGB.
 */
final class SelectionOutline {

    // How strongly the faces are tinted (3.5's 0.25)
    private static final int FILL_ALPHA = 0x40;
    // Lifts the lines off block faces so the ground doesn't hide them
    private static final double INSET = 0.01;

    private SelectionOutline() {
    }

    /**
     * Tints the six faces of the box.
     */
    static void fillBox(final double x0, final double y0, final double z0, final double x1, final double y1, final double z1, final int color) {
        Gizmos.cuboid(new AABB(x0 - INSET, y0 - INSET, z0 - INSET, x1 + INSET, y1 + INSET, z1 + INSET), GizmoStyle.fill(argb(FILL_ALPHA, color)));
    }

    /**
     * Tints the walls of the vertical prism over the block columns {@code points} (each {x, z}), from y0 to y1.
     */
    static void fillPrism(final List<int[]> points, final int y0, final int y1, final int color) {
        final GizmoStyle style = GizmoStyle.fill(argb(FILL_ALPHA, color));
        final int count = points.size();
        for (int i = 0; i < count; i++) {
            final int[] a = points.get(i);
            final int[] b = points.get((i + 1) % count);
            final double ax = a[0] + 0.5, az = a[1] + 0.5, bx = b[0] + 0.5, bz = b[1] + 0.5;
            Gizmos.rect(new Vec3(ax, y0, az), new Vec3(bx, y0, bz), new Vec3(bx, y1, bz), new Vec3(ax, y1, az), style);
        }
    }

    /**
     * Grid lines every {@code step} blocks on the walls of the box, and on its top and bottom when {@code caps} is set.
     */
    static void gridBox(final int x0, final int y0, final int z0, final int x1, final int y1, final int z1, final int step, final boolean caps,
        final int color, final float width) {
        final double nx = x0 - INSET, px = x1 + INSET, ny = y0 - INSET, py = y1 + INSET, nz = z0 - INSET, pz = z1 + INSET;
        for (int gx = x0 + step; gx < x1; gx += step) {
            line(gx, ny, nz, gx, py, nz, color, width);
            line(gx, ny, pz, gx, py, pz, color, width);
            if (caps) {
                line(gx, ny, nz, gx, ny, pz, color, width);
                line(gx, py, nz, gx, py, pz, color, width);
            }
        }
        for (int gz = z0 + step; gz < z1; gz += step) {
            line(nx, ny, gz, nx, py, gz, color, width);
            line(px, ny, gz, px, py, gz, color, width);
            if (caps) {
                line(nx, ny, gz, px, ny, gz, color, width);
                line(nx, py, gz, px, py, gz, color, width);
            }
        }
        for (int gy = y0 + step; gy < y1; gy += step) {
            ring(nx, nz, px, pz, gy, color, width);
        }
    }

    /**
     * The twelve edges of the box.
     */
    static void edgeBox(final double x0, final double y0, final double z0, final double x1, final double y1, final double z1, final int color,
        final float width) {
        final double nx = x0 - INSET, px = x1 + INSET, ny = y0 - INSET, py = y1 + INSET, nz = z0 - INSET, pz = z1 + INSET;
        line(nx, ny, nz, nx, py, nz, color, width);
        line(px, ny, nz, px, py, nz, color, width);
        line(nx, ny, pz, nx, py, pz, color, width);
        line(px, ny, pz, px, py, pz, color, width);
        ring(nx, nz, px, pz, ny, color, width);
        ring(nx, nz, px, pz, py, color, width);
    }

    /**
     * A vertical prism over the block columns {@code points} (each {x, z}), from y0 to y1: horizontal rings every {@code step} blocks, or with
     * {@code edges} set, the top and bottom rings and a vertical line at each corner.
     */
    static void prism(final List<int[]> points, final int y0, final int y1, final int step, final boolean edges, final int color, final float width) {
        final int count = points.size();
        // Each point is a block column; draw through its centre so the outline follows the blocks WorldEdit selected
        for (int i = 0; i < count; i++) {
            final int[] a = points.get(i);
            final int[] b = points.get((i + 1) % count);
            final double ax = a[0] + 0.5, az = a[1] + 0.5, bx = b[0] + 0.5, bz = b[1] + 0.5;
            if (edges) {
                line(ax, y0 - INSET, az, ax, y1 + INSET, az, color, width);
                line(ax, y0 - INSET, az, bx, y0 - INSET, bz, color, width);
                line(ax, y1 + INSET, az, bx, y1 + INSET, bz, color, width);
            } else {
                for (int gy = y0 + step; gy < y1; gy += step) {
                    line(ax, gy, az, bx, gy, bz, color, width);
                }
            }
        }
    }

    private static void line(final double ax, final double ay, final double az, final double bx, final double by, final double bz, final int color,
        final float width) {
        Gizmos.line(new Vec3(ax, ay, az), new Vec3(bx, by, bz), argb(0xFF, color), width);
    }

    private static void ring(final double nx, final double nz, final double px, final double pz, final double y, final int color, final float width) {
        line(nx, y, nz, px, y, nz, color, width);
        line(nx, y, pz, px, y, pz, color, width);
        line(nx, y, nz, nx, y, pz, color, width);
        line(px, y, nz, px, y, pz, color, width);
    }

    private static int argb(final int alpha, final int color) {
        return alpha << 24 | color & 0xFFFFFF;
    }
}
