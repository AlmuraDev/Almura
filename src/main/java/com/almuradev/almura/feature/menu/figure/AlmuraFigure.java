/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.menu.figure;

import com.almuradev.almura.Almura;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

/**
 * Dockter_Almura in 3D, holding a diamond sword, playing once: standing as they are, raising the sword, a star burst from its tip at the
 * top, slowly lowering it, then turning to face the viewer and holding there. The timeline, the pose and the transforms, shared by
 * {@link AlmuraFigureRenderer}, which draws the figure, and {@link FigureSlot}, which finds the sword's tip on screen for the burst. As 1.12's
 * UIPlayerFigure did it.
 */
public final class AlmuraFigure {

    public static final Identifier SKIN = Identifier.fromNamespaceAndPath(Almura.ID, "textures/entity/player/dockter_almura.png");
    public static final Identifier BURST = Identifier.fromNamespaceAndPath(Almura.ID, "figure_burst");

    // The sequence, in seconds from when the figure first shows: standing, raising, held up (the burst), lowering, turning to the front
    private static final float STAND_END = 1.5F, RAISE_END = 2.3F, HOLD_END = 3.3F, LOWER_END = 5.3F, TURN_END = 6.3F;
    // The burst: how long it lasts from the top of the raise, how fast it flares, and its size in blocks
    private static final float BURST_TIME = 1.0F, BURST_FLARE = 0.15F;
    public static final float BURST_SIZE = 0.55F;
    // Turned three quarters before facing the viewer
    private static final float BASE_YAW = 30.0F;
    // The figure's height in blocks: the player and the raised sword above their head
    public static final float HEIGHT_BLOCKS = 3.0F;
    // Width for its height: room for the arms and the raised sword out to the side
    public static final float WIDTH_RATIO = 0.62F;
    // The diamond sword's tip in its item model (0 to 1 each way, from the bottom left of its sprite)
    private static final float TIP_X = 0.94F, TIP_Y = 0.94F, TIP_Z = 0.5F;
    // Where the feet stand: vanilla's skin preview leaves this much below them
    private static final float FEET = 1.601F;

    private static @Nullable PlayerModel model;
    private static Model.@Nullable Simple drawn;

    private AlmuraFigure() {
    }

    /**
     * @return The player model the figure is posed on, baked the first time
     */
    static PlayerModel model() {
        if (AlmuraFigure.model == null) {
            final ModelPart root = Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER);
            AlmuraFigure.model = new PlayerModel(root, false);
            // The same parts, drawn without vanilla's player animation (a plain model keeps the pose it is given)
            AlmuraFigure.drawn = new Model.Simple(root, RenderTypes::entityTranslucent);
        }
        return AlmuraFigure.model;
    }

    static Model.Simple drawn() {
        model();
        return AlmuraFigure.drawn;
    }

    /**
     * Poses the model for this moment: standing, the sword arm raised overhead as far as the timeline says, the head turned to the front at
     * the end. The hat, sleeves, jacket and trousers are children of their parts and follow them.
     */
    static void pose(final float seconds) {
        final PlayerModel model = model();
        model.resetPose();
        final float raise = raiseAmount(seconds);
        final float turn = turnAmount(seconds);
        final float breath = seconds * 1.8F;
        final float idle = 1.0F - turn;

        model.head.yRot = (float) Math.sin(seconds * 0.7F) * 0.25F * idle;
        model.head.xRot = -0.35F * raise + (float) Math.sin(seconds * 0.9F) * 0.04F * idle;

        // Holding the sword: forward a little; raised: up overhead and out to the side
        model.rightArm.xRot = lerp(-0.35F + (float) Math.sin(breath) * 0.03F, -2.85F, raise);
        model.rightArm.yRot = lerp(0.0F, -0.3F, raise);
        model.rightArm.zRot = lerp((float) Math.cos(breath) * 0.05F + 0.05F, 0.35F, raise);

        model.leftArm.xRot = (float) Math.sin(breath + 1.0F) * 0.04F;
        model.leftArm.yRot = 0.0F;
        model.leftArm.zRot = -(float) Math.cos(breath) * 0.05F - 0.05F - 0.1F * raise;
    }

    /**
     * From the feet at the bottom middle of the figure's area into the model's space: turned three quarters with a gentle sway, facing the
     * viewer at the end, as vanilla's skin preview turns its model.
     */
    static void toModel(final PoseStack poseStack, final float seconds) {
        final float sway = (float) Math.sin(seconds * 0.5F) * 12.0F;
        poseStack.rotateDegrees(Axis.YP, -lerp(BASE_YAW + sway, 0.0F, turnAmount(seconds)));
        poseStack.translate(0.0F, -FEET, 0.0F);
    }

    /**
     * From the model into the right hand, as vanilla's ItemInHandLayer places held items.
     */
    static void toHand(final PoseStack poseStack) {
        final PlayerModel model = model();
        model.root().translateAndRotate(poseStack);
        model.rightArm.translateAndRotate(poseStack);
        poseStack.rotateDegrees(Axis.XP, -90.0F);
        poseStack.rotateDegrees(Axis.YP, 180.0F);
        poseStack.translate(1.0F / 16.0F, 2.0F / 16.0F, -10.0F / 16.0F);
    }

    /**
     * From the hand into the sword's own space (0 to 1 each way): the held sword's third person placement (item/handheld) and the item's
     * centring, as vanilla's ItemTransform applies them.
     */
    static void toSword(final PoseStack poseStack) {
        poseStack.translate(0.0F, 4.0F / 16.0F, 0.5F / 16.0F);
        poseStack.rotate(new Quaternionf().rotationXYZ(0.0F, (float) Math.toRadians(-90.0), (float) Math.toRadians(55.0)));
        poseStack.scale(0.85F, 0.85F, 0.85F);
        poseStack.translate(-0.5F, -0.5F, -0.5F);
    }

    /**
     * Where the sword's tip is on screen, for a figure drawn in the given area.
     *
     * @param pixelsPerBlock the figure's scale
     * @return the tip's x and y, in GUI pixels
     */
    static Vector3f tip(final float seconds, final float x0, final float y0, final float width, final float height, final float pixelsPerBlock) {
        pose(seconds);
        final PoseStack poseStack = new PoseStack();
        // As the picture-in-picture renderer starts: feet at the bottom middle, its scale, depth flipped
        poseStack.translate(x0 + width / 2.0F, y0 + height, 0.0F);
        poseStack.scale(pixelsPerBlock, pixelsPerBlock, -pixelsPerBlock);
        toModel(poseStack, seconds);
        toHand(poseStack);
        toSword(poseStack);
        poseStack.translate(TIP_X, TIP_Y, TIP_Z);
        return poseStack.last().pose().transformPosition(0.0F, 0.0F, 0.0F, new Vector3f());
    }

    /**
     * @return How far the sword is raised: up from the end of standing, held, then lowered slowly
     */
    static float raiseAmount(final float seconds) {
        if (seconds < STAND_END || seconds >= LOWER_END) {
            return 0.0F;
        }
        if (seconds < RAISE_END) {
            return smooth((seconds - STAND_END) / (RAISE_END - STAND_END));
        }
        if (seconds < HOLD_END) {
            return 1.0F;
        }
        return 1.0F - smooth((seconds - HOLD_END) / (LOWER_END - HOLD_END));
    }

    /**
     * @return How bright the burst is: flaring at the top of the raise, then fading
     */
    static float burstAmount(final float seconds) {
        final float t = seconds - RAISE_END;
        if (t < 0.0F || t >= BURST_TIME) {
            return 0.0F;
        }
        return t < BURST_FLARE ? t / BURST_FLARE : 1.0F - (t - BURST_FLARE) / (BURST_TIME - BURST_FLARE);
    }

    private static float turnAmount(final float seconds) {
        return smooth(Math.max(0.0F, Math.min(1.0F, (seconds - LOWER_END) / (TURN_END - LOWER_END))));
    }

    private static float smooth(final float t) {
        return t * t * (3.0F - 2.0F * t);
    }

    private static float lerp(final float from, final float to, final float amount) {
        return from + (to - from) * amount;
    }
}
