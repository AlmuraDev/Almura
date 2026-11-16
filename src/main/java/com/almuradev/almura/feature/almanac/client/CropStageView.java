/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.almanac.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Where to look and how far to zoom so every given block state's model fits, turned the way blocks are shown in the inventory, as 3.5's
 * {@code UIBlockStateModel.View}: a crop's stages share one, so they are drawn at the same size and the crop can be seen growing.
 *
 * @param scale how many times the drawn space's size one block is
 */
public record CropStageView(float centerX, float centerY, float centerZ, float scale) {

    // Same turn as a block in the inventory: tipped 30 degrees, turned 225
    static final float TILT = 30f, TURN = 225f;

    /**
     * @return a view that fits all of the states' models, turned as they're drawn, with a little margin
     */
    static CropStageView fitting(final Collection<BlockState> states) {
        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, minZ = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE, maxZ = -Float.MAX_VALUE;
        boolean any = false;

        final BlockStateModelSet models = Minecraft.getInstance().getModelManager().getBlockStateModelSet();
        for (final BlockState state : states) {
            final List<BlockStateModelPart> parts = new ArrayList<>();
            try {
                models.get(state).collectParts(RandomSource.create(42L), parts);
            } catch (final RuntimeException e) {
                continue;
            }
            for (final BlockStateModelPart part : parts) {
                for (final BakedQuad quad : quads(part)) {
                    for (int vertex = 0; vertex < 4; vertex++) {
                        final Vector3fc position = quad.position(vertex);
                        minX = Math.min(minX, position.x()); maxX = Math.max(maxX, position.x());
                        minY = Math.min(minY, position.y()); maxY = Math.max(maxY, position.y());
                        minZ = Math.min(minZ, position.z()); maxZ = Math.max(maxZ, position.z());
                        any = true;
                    }
                }
            }
        }
        if (!any) {
            minX = minY = minZ = 0f;
            maxX = maxY = maxZ = 1f;
        }

        // Turn the corners of the box the way the model is drawn, and fit what's seen on screen
        final double turn = Math.toRadians(TURN), tilt = Math.toRadians(TILT);
        final float centerX = (minX + maxX) / 2f, centerY = (minY + maxY) / 2f, centerZ = (minZ + maxZ) / 2f;
        float screenMinX = Float.MAX_VALUE, screenMaxX = -Float.MAX_VALUE, screenMinY = Float.MAX_VALUE, screenMaxY = -Float.MAX_VALUE;
        for (int corner = 0; corner < 8; corner++) {
            final double x = ((corner & 1) == 0 ? minX : maxX) - centerX;
            final double y = ((corner & 2) == 0 ? minY : maxY) - centerY;
            final double z = ((corner & 4) == 0 ? minZ : maxZ) - centerZ;
            // Turned about Y, then tipped about X, as the drawing applies them
            final double turnedX = x * Math.cos(turn) + z * Math.sin(turn);
            final double turnedZ = -x * Math.sin(turn) + z * Math.cos(turn);
            final double tippedY = y * Math.cos(tilt) - turnedZ * Math.sin(tilt);
            screenMinX = (float) Math.min(screenMinX, turnedX);
            screenMaxX = (float) Math.max(screenMaxX, turnedX);
            screenMinY = (float) Math.min(screenMinY, tippedY);
            screenMaxY = (float) Math.max(screenMaxY, tippedY);
        }
        final float extent = Math.max(screenMaxX - screenMinX, screenMaxY - screenMinY);
        return new CropStageView(centerX, centerY, centerZ, extent <= 0f ? 0.6f : 0.92f / extent);
    }

    private static List<BakedQuad> quads(final BlockStateModelPart part) {
        final List<BakedQuad> quads = new ArrayList<>(part.getQuads(null));
        for (final Direction side : Direction.values()) {
            quads.addAll(part.getQuads(side));
        }
        return quads;
    }
}
