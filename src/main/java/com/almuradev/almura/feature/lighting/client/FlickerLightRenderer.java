/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.lighting.client;

import com.almuradev.almura.content.registry.FlickerLights;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Draws a flickering light's fast blink, as 3.5's renderer did: a black, see-through copy of its model over it, as dark as the light is dim
 * right now. Nothing is drawn while the light is steady, and the light's section is never rebuilt for it. Client only.
 */
public final class FlickerLightRenderer implements BlockEntityRenderer<FlickerLights.FlickerLightBlockEntity, FlickerLightRenderer.State> {

    // Below this much darkening the copy isn't worth drawing
    private static final float MIN_DARKNESS = 0.02f;
    // A hair bigger, so the copy sits on the model instead of fighting it
    private static final float OVERLAY_SCALE = 1.004f;
    private static final Direction[] FACES = {null, Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST,
        Direction.EAST};

    public FlickerLightRenderer(final BlockEntityRendererProvider.Context context) {
    }

    public static final class State extends BlockEntityRenderState {

        float darkness;
        @Nullable BlockState block;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(final FlickerLights.FlickerLightBlockEntity light, final State state, final float partialTicks,
        final Vec3 cameraPosition, final ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(light, state, partialTicks, cameraPosition, breakProgress);
        state.darkness = 1f - ClientFlickerLights.brightness(light.getBlockPos(), partialTicks);
        state.block = light.getBlockState();
    }

    @Override
    public void submit(final State state, final PoseStack poseStack, final SubmitNodeCollector collector, final CameraRenderState camera) {
        if (state.darkness < MIN_DARKNESS || state.block == null) {
            return;
        }
        final List<BlockStateModelPart> parts = new ArrayList<>();
        Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state.block).collectParts(RandomSource.create(42L), parts);
        final int alpha = Math.round(Math.min(1f, state.darkness) * 255f);
        poseStack.pushPose();
        poseStack.translate(0.5f, 0.5f, 0.5f);
        poseStack.scale(OVERLAY_SCALE, OVERLAY_SCALE, OVERLAY_SCALE);
        poseStack.translate(-0.5f, -0.5f, -0.5f);
        collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucent(TextureAtlas.LOCATION_BLOCKS), (pose, buffer) -> {
            for (final BlockStateModelPart part : parts) {
                for (final Direction face : FACES) {
                    for (final BakedQuad quad : part.getQuads(face)) {
                        quad(pose, buffer, quad, alpha);
                    }
                }
            }
        });
        poseStack.popPose();
    }

    private static void quad(final PoseStack.Pose pose, final VertexConsumer buffer, final BakedQuad quad, final int alpha) {
        final Direction direction = quad.direction();
        for (int vertex = 0; vertex < 4; vertex++) {
            final Vector3fc position = quad.position(vertex);
            final long uv = quad.packedUV(vertex);
            buffer.addVertex(pose, position.x(), position.y(), position.z())
                // Black, blended by how dark the light is
                .setColor(0, 0, 0, alpha)
                .setUv(UVPair.unpackU(uv), UVPair.unpackV(uv))
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightCoordsUtil.FULL_BRIGHT)
                .setNormal(pose, direction.getStepX(), direction.getStepY(), direction.getStepZ());
        }
    }
}
