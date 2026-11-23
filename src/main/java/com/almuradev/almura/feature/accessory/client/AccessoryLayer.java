/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.accessory.client;

import com.almuradev.almura.feature.accessory.AccessoryType;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import org.joml.Quaternionf;

import java.util.Map;

/**
 * Draws the accessories a player wears (from their render state, {@link ClientAccessories#WORN}), as 3.5's {@code AccessoryLayer}: hats,
 * the halo, sunglasses and ears on the head, the tail and wings on the body, the bracelet on the left arm, each following the posed player
 * model. The tail sways and the wings flap with the player's age and walking, as 3.5's did.
 */
final class AccessoryLayer extends RenderLayer<AvatarRenderState, PlayerModel> {

    private static final float EAR_SCALE = 4.0F / 3.0F;
    // Sunglasses are drawn slightly see-through, as 3.5's
    private static final int SUNGLASSES_TINT = 0xCCFFFFFF;
    // Worn armor is drawn a pixel out from the body all round: what sits on the head or the body moves out with it
    private static final float ARMOR = 1.0F / 16.0F;
    // The sunglasses (8 px, grown 0.6) are grown to clear a helmet (grown 1), about the head's middle
    private static final float SUNGLASSES_OVER_HELMET = 5.1F / 4.6F;
    // The bracelet's ring (3 px out from the arm's middle, 2.5 on a slim arm) is grown to clear a chestplate's sleeve
    private static final float BRACELET_OVER_SLEEVE_WIDE = 3.5F / 3.0F;
    private static final float BRACELET_OVER_SLEEVE_SLIM = 3.0F / 2.5F;

    AccessoryLayer(final RenderLayerParent<AvatarRenderState, PlayerModel> parent) {
        super(parent);
    }

    @Override
    public void submit(final PoseStack pose, final SubmitNodeCollector collector, final int light, final AvatarRenderState state, final float yRot,
        final float xRot) {
        final Map<AccessoryType, String> worn = state.getRenderData(ClientAccessories.WORN);
        if (worn == null || worn.isEmpty() || state.isInvisible) {
            return;
        }
        final PlayerModel model = this.getParentModel();
        final int overlay = LivingEntityRenderer.getOverlayCoords(state, 0.0F);
        final boolean helmet = isArmor(state.headEquipment, EquipmentSlot.HEAD);
        final boolean chestplate = isArmor(state.chestEquipment, EquipmentSlot.CHEST);
        final boolean backCovered = chestplate || isArmor(state.legsEquipment, EquipmentSlot.LEGS);
        for (final Map.Entry<AccessoryType, String> entry : worn.entrySet()) {
            final AccessoryType type = entry.getKey();
            final RenderType cutout = RenderTypes.entityCutout(type.texture(entry.getValue()));
            pose.pushPose();
            switch (type) {
                case TOP_HAT -> head(model, pose, collector, AccessoryModels.TOP_HAT, cutout, light, overlay, helmet);
                case NOTCH_HAT -> head(model, pose, collector, AccessoryModels.NOTCH_HAT, cutout, light, overlay, helmet);
                case CAT_HAT -> head(model, pose, collector, AccessoryModels.CAT_HAT, cutout, light, overlay, helmet);
                case HALO -> head(model, pose, collector, AccessoryModels.HALO, cutout, light, overlay, helmet);
                case SUNGLASSES -> {
                    model.head.translateAndRotate(pose);
                    if (helmet) {
                        pose.translate(0.0F, -5.0F / 16.0F, 0.0F);
                        pose.scale(SUNGLASSES_OVER_HELMET, SUNGLASSES_OVER_HELMET, SUNGLASSES_OVER_HELMET);
                        pose.translate(0.0F, 5.0F / 16.0F, 0.0F);
                    }
                    collector.submitModelPart(AccessoryModels.SUNGLASSES, pose, RenderTypes.entityTranslucent(type.texture(entry.getValue())), light,
                        overlay, null, SUNGLASSES_TINT);
                }
                case EARS -> {
                    model.head.translateAndRotate(pose);
                    for (int side = 0; side < 2; side++) {
                        pose.pushPose();
                        pose.translate((0.375F + (helmet ? ARMOR : 0.0F)) * (side * 2 - 1), -0.375F - (helmet ? ARMOR : 0.0F), 0.0F);
                        pose.scale(EAR_SCALE, EAR_SCALE, EAR_SCALE);
                        collector.submitModelPart(AccessoryModels.EAR, pose, cutout, light, overlay, null);
                        pose.popPose();
                    }
                }
                case TAIL -> {
                    model.body.translateAndRotate(pose);
                    // Hangs from the small of the back; +z is behind the player, so a positive x turn swings it backwards
                    pose.translate(0.0F, 11.0F / 16.0F, 2.0F / 16.0F + (backCovered ? ARMOR : 0.0F));
                    final float x = 0.6F + Mth.cos(state.ageInTicks * 0.05F) * 0.05F + state.walkAnimationSpeed * 0.3F;
                    final float z = Mth.sin(state.ageInTicks * 0.067F) * 0.12F
                        + Mth.sin(state.walkAnimationPos * 0.6662F) * state.walkAnimationSpeed * 0.4F;
                    pose.rotate(new Quaternionf().rotationZYX(z, 0.0F, x));
                    collector.submitModelPart(AccessoryModels.TAIL, pose, cutout, light, overlay, null);
                }
                case WINGS -> wings(model, pose, collector, state, cutout, light, overlay, chestplate);
                case BRACELET -> {
                    model.leftArm.translateAndRotate(pose);
                    final boolean slim = state.skin != null && state.skin.model() == PlayerModelType.SLIM;
                    if (chestplate) {
                        // Grown about the arm's middle (x 1 px, or 0.5 on a slim arm), level with the ring
                        final float middle = (slim ? 0.5F : 1.0F) / 16.0F;
                        final float grow = slim ? BRACELET_OVER_SLEEVE_SLIM : BRACELET_OVER_SLEEVE_WIDE;
                        pose.translate(middle, 0.0F, 0.0F);
                        pose.scale(grow, 1.0F, grow);
                        pose.translate(-middle, 0.0F, 0.0F);
                    }
                    collector.submitModelPart(slim ? AccessoryModels.BRACELET_SLIM : AccessoryModels.BRACELET_WIDE, pose, cutout, light, overlay, null);
                }
                default -> {
                }
            }
            pose.popPose();
        }
    }

    private static void head(final PlayerModel model, final PoseStack pose, final SubmitNodeCollector collector, final ModelPart part,
        final RenderType type, final int light, final int overlay, final boolean helmet) {
        model.head.translateAndRotate(pose);
        if (helmet) {
            pose.translate(0.0F, -ARMOR, 0.0F);
        }
        collector.submitModelPart(part, pose, type, light, overlay, null);
    }

    /**
     * @return whether a stack is armor drawn on the body in that slot (a helmet, not a pumpkin or a head)
     */
    static boolean isArmor(final ItemStack stack, final EquipmentSlot slot) {
        final Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        return equippable != null && equippable.slot() == slot && equippable.assetId().isPresent();
    }

    /**
     * The wings fanned out round the spine, each feather turned as 3.5's, opening and closing with a steady flap plus a beat per step.
     */
    private static void wings(final PlayerModel model, final PoseStack pose, final SubmitNodeCollector collector, final AvatarRenderState state,
        final RenderType type, final int light, final int overlay, final boolean chestplate) {
        model.body.translateAndRotate(pose);
        if (chestplate) {
            pose.translate(0.0F, 0.0F, ARMOR);
        }
        final float flap = Mth.sin(state.ageInTicks * 0.1F) * 0.08F
            + Mth.sin(state.walkAnimationPos * 0.6662F) * state.walkAnimationSpeed * 0.1F;
        for (int i = 0; i < AccessoryModels.FEATHERS * 2; i++) {
            final boolean left = i < AccessoryModels.FEATHERS;
            final float[] angle = AccessoryModels.WING_ANGLES[i];
            pose.pushPose();
            pose.rotate(new Quaternionf().rotationZYX(angle[2], angle[1] + (left ? flap : -flap), angle[0]));
            collector.submitModelPart(AccessoryModels.WINGS.getChild((left ? "left" : "right") + (left ? i : i - AccessoryModels.FEATHERS)), pose,
                type, light, overlay, null);
            pose.popPose();
        }
    }
}
