/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.machine.client;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.machine.MachineType;
import com.almuradev.almura.feature.machine.ProcessingBlock;
import com.almuradev.almura.feature.machine.ProcessingBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * The kitchen machines' moving parts, kept out of their block models, as 3.5's: the slicer's blade and the grinder's cutting plate spin
 * around their axle while the machine works, and stay where they were when it stops. The seed processor has none.
 */
public final class SpinningPartRenderer implements BlockEntityRenderer<ProcessingBlockEntity, SpinningPartRenderer.State> {

    /**
     * A moving part: its model, a point on its axle and the axle's direction (in block units, as the models face north), and how fast it
     * spins.
     */
    private record Part(StandaloneModelKey<BlockStateModelPart> model, Identifier modelId, float[] axle, float[] axis, float degreesPerTick) {
    }

    /** The slicer's blade: axle along x, through y 0.48, z 0.50. */
    private static final Part SLICER_BLADE = part("block/machine/kitchen_slicer_blade", new float[] {0f, 0.48f, 0.5f},
        new float[] {1f, 0f, 0f}, 36f);
    /** The grinder's cutting plate: axle along z (the grinding tube), through x 0.50, y 0.44. */
    private static final Part GRINDER_PLATE = part("block/machine/kitchen_grinder_plate", new float[] {0.5f, 0.44f, 0f},
        new float[] {0f, 0f, 1f}, 24f);

    public SpinningPartRenderer(final BlockEntityRendererProvider.Context context) {
    }

    private static Part part(final String path, final float[] axle, final float[] axis, final float degreesPerTick) {
        final Identifier id = Identifier.fromNamespaceAndPath(Almura.ID, path);
        return new Part(new StandaloneModelKey<>(id::toString), id, axle, axis, degreesPerTick);
    }

    /**
     * Loads the parts' models, which no block state names. Registered on the mod bus by the client's setup.
     */
    public static void onRegisterStandalone(final ModelEvent.RegisterStandalone event) {
        for (final Part part : List.of(SLICER_BLADE, GRINDER_PLATE)) {
            event.register(part.model(), SimpleUnbakedStandaloneModel.simpleModelWrapper(part.modelId()));
        }
    }

    private static @Nullable Part partOf(final BlockState state) {
        if (!(state.getBlock() instanceof final ProcessingBlock block)) {
            return null;
        }
        return block.type() == MachineType.KITCHEN_SLICER ? SLICER_BLADE : block.type() == MachineType.KITCHEN_GRINDER ? GRINDER_PLATE : null;
    }

    public static final class State extends BlockEntityRenderState {

        @Nullable Part part;
        Direction facing = Direction.NORTH;
        float angle;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(final ProcessingBlockEntity machine, final State state, final float partialTicks, final Vec3 cameraPosition,
        final ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(machine, state, partialTicks, cameraPosition, breakProgress);
        final BlockState block = machine.getBlockState();
        state.part = partOf(block);
        if (state.part == null) {
            return;
        }
        state.facing = block.getValue(ProcessingBlock.FACING);
        // Spins while working; when stopped, stays at the last angle rather than snapping back
        if (block.getValue(ProcessingBlock.ACTIVE) && machine.getLevel() != null) {
            machine.spinAngle = ((machine.getLevel().getGameTime() + partialTicks) * state.part.degreesPerTick()) % 360f;
        }
        state.angle = machine.spinAngle;
    }

    @Override
    public void submit(final State state, final PoseStack poseStack, final SubmitNodeCollector collector, final CameraRenderState camera) {
        final Part part = state.part;
        if (part == null) {
            return;
        }
        final BlockStateModelPart model = Minecraft.getInstance().getModelManager().getStandaloneModel(part.model());
        if (model == null) {
            return;
        }
        poseStack.pushPose();
        // Facing as the block model does (block state y rotations turn clockwise seen from above)
        poseStack.translate(0.5f, 0f, 0.5f);
        poseStack.rotateDegrees(Axis.YP, -facingDegrees(state.facing));
        poseStack.translate(-0.5f, 0f, -0.5f);
        // Around the axle
        final float[] axle = part.axle(), axis = part.axis();
        poseStack.translate(axle[0], axle[1], axle[2]);
        poseStack.rotate(new Quaternionf().rotationAxis((float) Math.toRadians(state.angle), axis[0], axis[1], axis[2]));
        poseStack.translate(-axle[0], -axle[1], -axle[2]);
        collector.submitBlockModel(poseStack, Sheets.cutoutBlockItemSheet(), List.of(model), BlockModelRenderState.EMPTY_TINTS, state.lightCoords,
            OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }

    private static float facingDegrees(final Direction facing) {
        return switch (facing) {
            case EAST -> 90f;
            case SOUTH -> 180f;
            case WEST -> 270f;
            default -> 0f;
        };
    }
}
