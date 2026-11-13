/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.menu.figure;

import com.almuradev.almura.Almura;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

import java.io.InputStream;

/**
 * The diamond sword the figure holds, built from the sword's texture the way vanilla builds a flat item's model: each solid pixel a little
 * block a pixel deep. The figure can't hold a real item: on the title screen items have no data yet (a server's data packs give it to them),
 * so no diamond sword item can be made there.
 */
final class FigureSword {

    private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/item/diamond_sword.png");
    // A pixel's depth, centred as a flat item's model is
    private static final float Z0 = 7.5F / 16.0F, Z1 = 8.5F / 16.0F;

    // Which pixels are solid, [x][y] from the top left; read once, an empty array if the texture couldn't be read
    private static boolean[][] solid;

    private FigureSword() {
    }

    /**
     * Draws the sword, with the item's own space (0 to 1 each way) set up on the pose stack.
     */
    static void submit(final PoseStack poseStack, final SubmitNodeCollector collector, final int light) {
        final boolean[][] solid = solid();
        if (solid.length == 0) {
            return;
        }
        final int width = solid.length, height = solid[0].length;
        collector.submitCustomGeometry(poseStack, RenderTypes.entityCutout(TEXTURE), (pose, buffer) -> {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if (!solid[x][y]) {
                        continue;
                    }
                    // The pixel's colour on every face: sampled at its middle
                    final float u = (x + 0.5F) / width, v = (y + 0.5F) / height;
                    final float x0 = (float) x / width, x1 = (float) (x + 1) / width;
                    // The texture's top is the item's top
                    final float y0 = (float) (height - 1 - y) / height, y1 = (float) (height - y) / height;
                    final Face face = new Face(pose, buffer, u, v, light);
                    face.quad(0, 0, 1, x0, y0, Z1, x1, y0, Z1, x1, y1, Z1, x0, y1, Z1);
                    face.quad(0, 0, -1, x1, y0, Z0, x0, y0, Z0, x0, y1, Z0, x1, y1, Z0);
                    // The edges, where no solid pixel is beside it
                    if (x == 0 || !solid[x - 1][y]) {
                        face.quad(-1, 0, 0, x0, y0, Z0, x0, y0, Z1, x0, y1, Z1, x0, y1, Z0);
                    }
                    if (x == width - 1 || !solid[x + 1][y]) {
                        face.quad(1, 0, 0, x1, y0, Z1, x1, y0, Z0, x1, y1, Z0, x1, y1, Z1);
                    }
                    if (y == 0 || !solid[x][y - 1]) {
                        face.quad(0, 1, 0, x0, y1, Z1, x1, y1, Z1, x1, y1, Z0, x0, y1, Z0);
                    }
                    if (y == height - 1 || !solid[x][y + 1]) {
                        face.quad(0, -1, 0, x0, y0, Z0, x1, y0, Z0, x1, y0, Z1, x0, y0, Z1);
                    }
                }
            }
        });
    }

    private static boolean[][] solid() {
        if (FigureSword.solid == null) {
            boolean[][] read = new boolean[0][0];
            try (InputStream in = Minecraft.getInstance().getResourceManager().open(TEXTURE); NativeImage image = NativeImage.read(in)) {
                read = new boolean[image.getWidth()][image.getHeight()];
                for (int x = 0; x < image.getWidth(); x++) {
                    for (int y = 0; y < image.getHeight(); y++) {
                        read[x][y] = (image.getPixel(x, y) >>> 24) > 0;
                    }
                }
            } catch (final Exception e) {
                Almura.LOGGER.warn("The title figure's sword texture {} couldn't be read; the figure holds no sword", TEXTURE, e);
            }
            FigureSword.solid = read;
        }
        return FigureSword.solid;
    }

    /**
     * Writes one pixel's faces, all in the pixel's colour, fully lit.
     */
    private record Face(PoseStack.Pose pose, VertexConsumer buffer, float u, float v, int light) {

        void quad(final float nx, final float ny, final float nz, final float... corners) {
            for (int i = 0; i < 12; i += 3) {
                this.buffer.addVertex(this.pose, corners[i], corners[i + 1], corners[i + 2])
                    .setColor(-1)
                    .setUv(this.u, this.v)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(this.light)
                    .setNormal(this.pose, nx, ny, nz);
            }
        }
    }
}
