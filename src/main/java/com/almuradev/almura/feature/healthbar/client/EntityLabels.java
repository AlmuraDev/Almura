/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.healthbar.client;

import com.almuradev.almura.Almura;
import com.almuradev.almura.core.client.config.AlmuraClientConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.TriState;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jspecify.annotations.Nullable;

import java.util.function.BiConsumer;

/**
 * Names and health bars over players, monsters and animals, as 3.5's {@code MixinRenderLivingBase}: each kind shows its label within its own
 * distance (Options: Player, Enemy and Animal Name Distance; 0 hides that kind's), the name when Display Entity Names is on (unnamed mobs too,
 * as 3.5), and a health bar under it when Display Entity Healthbars is on. Other entities keep vanilla's name tags.
 * <p>
 * Works with the nickname hook (which sets a player's name tag and shows the player's own in third person) and the title line, which is drawn
 * over the name: with names off only the bar is drawn, without the name or the title.
 */
public final class EntityLabels {

    // 3.5's bar: under the name, 4 pixels tall, in the name tag's own pixels
    private static final float BAR_TOP = 10.0F, BAR_HEIGHT = 3.0F;
    // The bar's half width with no name to match
    private static final int BARE_HALF_WIDTH = 20;
    private static final int HEALTH = 0xC0E02020, LOST = 0x60000000;
    // Set on a labelled entity's render state when its label shows: how much health it has left, 0 to 1
    private static final ContextKey<Float> HEALTH_LEFT = new ContextKey<>(Identifier.fromNamespaceAndPath(Almura.ID, "health_left"));

    /**
     * After the nickname hook: whether the entity's label shows at all.
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onCanRender(final RenderNameTagEvent.CanRender event) {
        final Entity entity = event.getEntity();
        final ModConfigSpec.IntValue distance = distanceFor(entity);
        if (distance == null) {
            return;
        }
        final Minecraft minecraft = Minecraft.getInstance();
        final Entity camera = minecraft.getCameraEntity();
        // The player's own label in first person stays hidden, as vanilla's
        if (entity == camera && minecraft.options.getCameraType().isFirstPerson()) {
            return;
        }
        final int blocks = AlmuraClientConfig.get(distance);
        final boolean within = blocks > 0 && camera != null && entity.distanceToSqr(camera) <= (double) blocks * blocks;
        final boolean shown = AlmuraClientConfig.get(AlmuraClientConfig.DISPLAY_NAMES) || AlmuraClientConfig.get(AlmuraClientConfig.DISPLAY_HEALTHBARS);
        final boolean invisible = minecraft.player != null && entity != minecraft.player && entity.isInvisibleTo(minecraft.player);
        final boolean labelled = within && shown && !invisible;
        event.setCanRender(labelled ? TriState.TRUE : TriState.FALSE);
    }

    /**
     * Registers the health a bar shows on the render states of the labelled kinds. NeoForge resets a render state's extra data once the
     * renderer has extracted it and applies only registered modifiers, so it can't be set while the name tag is decided.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void onRegisterRenderStateModifiers(final RegisterRenderStateModifiersEvent event) {
        event.registerEntityModifier((Class) LivingEntityRenderer.class, (BiConsumer<LivingEntity, LivingEntityRenderState>) (entity, state) ->
            state.setRenderData(HEALTH_LEFT, distanceFor(entity) == null ? null
                : Math.min(1.0F, Math.max(0.0F, entity.getHealth() / Math.max(1.0F, entity.getMaxHealth())))));
    }

    /**
     * Before the title line: the health bar under the name, and the name (and title) left out when names are off.
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onDoRender(final RenderNameTagEvent.DoRender event) {
        final EntityRenderState state = event.getEntityRenderState();
        if (state.nameTag == null || state.nameTagAttachment == null || event.getContent() != state.nameTag) {
            return;
        }
        final Float health = state.getRenderData(HEALTH_LEFT);
        if (health == null) {
            return; // Not one of the labelled kinds
        }
        final boolean names = AlmuraClientConfig.get(AlmuraClientConfig.DISPLAY_NAMES);
        if (AlmuraClientConfig.get(AlmuraClientConfig.DISPLAY_HEALTHBARS) && !state.isDiscrete && health > 0.0F) {
            final Font font = Minecraft.getInstance().font;
            final int half = names ? font.width(event.getContent()) / 2 : BARE_HALF_WIDTH;
            bar(event, state, half, health, names ? BAR_TOP : 0.0F);
        }
        if (!names) {
            event.setCanceled(true);
        }
    }

    /**
     * A colour at a third of its opacity, as vanilla's see-through name text is dimmed.
     */
    private static int faint(final int argb) {
        return ((argb >>> 24) / 3) << 24 | argb & 0xFFFFFF;
    }

    /**
     * The bar, placed as vanilla places a name tag: over the entity, facing the camera, in pixels of 0.025 of a block.
     */
    private static void bar(final RenderNameTagEvent.DoRender event, final EntityRenderState state, final int half, final float health,
        final float top) {
        final PoseStack pose = event.getPoseStack();
        final SubmitNodeCollector collector = event.getSubmitNodeCollector();
        final Vec3 attachment = state.nameTagAttachment;
        pose.pushPose();
        pose.translate(attachment.x, attachment.y + 0.5, attachment.z);
        pose.rotate(event.getCameraRenderState().orientation);
        pose.scale(0.025F, -0.025F, 0.025F);
        final float left = -half - 1, right = half + 1;
        final float split = left + (right - left) * health;
        // The health left in red, the health lost dark: side by side, so neither covers the other. As vanilla draws a name tag: faintly
        // through whatever is in front (tall grass, a fence), then fully where nothing is
        for (final Font.DisplayMode mode : new Font.DisplayMode[] {Font.DisplayMode.SEE_THROUGH, Font.DisplayMode.NORMAL}) {
            final boolean through = mode == Font.DisplayMode.SEE_THROUGH;
            collector.submitTextBackground(pose, left, top, split, top + BAR_HEIGHT, through ? faint(HEALTH) : HEALTH, mode, state.lightCoords);
            if (split < right) {
                collector.submitTextBackground(pose, split, top, right, top + BAR_HEIGHT, through ? faint(LOST) : LOST, mode, state.lightCoords);
            }
        }
        pose.popPose();
    }

    /**
     * @return the distance setting for the entity's kind, or null for entities left to vanilla
     */
    private static ModConfigSpec.@Nullable IntValue distanceFor(final Entity entity) {
        if (!(entity instanceof LivingEntity)) {
            return null;
        }
        if (entity instanceof Player) {
            return AlmuraClientConfig.PLAYER_NAME_RENDER_DISTANCE;
        }
        if (entity instanceof Enemy) {
            return AlmuraClientConfig.ENEMY_NAME_RENDER_DISTANCE;
        }
        if (entity instanceof Animal) {
            return AlmuraClientConfig.ANIMAL_NAME_RENDER_DISTANCE;
        }
        return null;
    }
}
