/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.title.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.geometry.Rect;
import org.inspirenxe.strata.api.layout.Spacer;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * The player as they stand, turning after the mouse, as 3.5's Titles and Nicknames windows showed them, with the name (and title) they would
 * have: a space in a Strata window, drawn over the screen after it (as the title menu's figure, {@code FigureSlot}).
 */
public final class PlayerPreview {

    // Spaces painted since the screen last drew
    private static final List<PlayerPreview> PAINTED = new ArrayList<>();

    private final Spacer space;
    private final int size;
    private final Supplier<Component> name;

    private PlayerPreview(final Spacer space, final int size, final Supplier<Component> name) {
        this.space = space;
        this.size = size;
        this.name = name;
    }

    /**
     * Makes the space for the player.
     *
     * @param size how many pixels a block is
     * @param name the name shown over them (their nickname being tried, or their name); the title shown is the one being tried on
     */
    public static Spacer create(final ComponentFactory factory, final float width, final float height, final int size,
        final Supplier<Component> name) {
        final Spacer space = factory.spacer(width, height);
        final PlayerPreview preview = new PlayerPreview(space, size, name);
        space.onEvent(org.inspirenxe.strata.api.component.Component.PAINTED, event -> PAINTED.add(preview));
        return space;
    }

    /**
     * Draws the players whose spaces the screen just painted. Registered on the game's event bus by the client's setup.
     */
    public static final class Drawer {

        @SubscribeEvent
        public void onScreenRender(final ScreenEvent.Render.Post event) {
            if (PAINTED.isEmpty()) {
                return;
            }
            final GuiGraphicsExtractor graphics = event.getGuiGraphics();
            graphics.nextStratum();
            for (final PlayerPreview preview : List.copyOf(PAINTED)) {
                preview.draw(graphics, event.getMouseX(), event.getMouseY());
            }
            PAINTED.clear();
        }
    }

    private void draw(final GuiGraphicsExtractor graphics, final float mouseX, final float mouseY) {
        final LocalPlayer player = Minecraft.getInstance().player;
        final Rect bounds = this.space.bounds();
        if (player == null || bounds.width() <= 0f || bounds.height() <= 0f) {
            return;
        }
        final int x0 = Math.round(bounds.x()), y0 = Math.round(bounds.y());
        final int x1 = Math.round(bounds.x() + bounds.width()), y1 = Math.round(bounds.y() + bounds.height());
        // As vanilla's inventory: turned towards the mouse
        final float xAngle = (float) Math.atan(((x0 + x1) / 2.0F - mouseX) / 40.0F);
        final float yAngle = (float) Math.atan(((y0 + y1) / 2.0F - mouseY) / 40.0F);
        final Quaternionf rotation = new Quaternionf().rotateZ((float) Math.PI);
        final Quaternionf xRotation = new Quaternionf().rotateX(yAngle * 20.0F * (float) (Math.PI / 180.0));
        rotation.mul(xRotation);
        final EntityRenderState state = renderState(player);
        if (state instanceof final LivingEntityRenderState living) {
            living.bodyRot = 180.0F + xAngle * 20.0F;
            living.yRot = xAngle * 20.0F;
            living.xRot = living.pose != Pose.FALL_FLYING ? -yAngle * 20.0F : 0.0F;
            living.boundingBoxWidth = living.boundingBoxWidth / living.scale;
            living.boundingBoxHeight = living.boundingBoxHeight / living.scale;
            living.scale = 1.0F;
        }
        // Their name (and so their title) over them, which vanilla never shows for the player themselves; the figure lowered to fit them
        state.nameTag = this.name.get();
        state.nameTagAttachment = player.getAttachments().getNullable(EntityAttachment.NAME_TAG, 0, player.getYRot());
        if (state.nameTagAttachment == null) {
            state.nameTagAttachment = new Vec3(0.0, player.getBbHeight() + 0.5, 0.0);
        }
        // A larger offset draws the figure lower in its box
        final Vector3f translation = new Vector3f(0.0F, state.boundingBoxHeight / 2.0F + 0.2F, 0.0F);
        graphics.entity(state, this.size, translation, rotation, xRotation, x0, y0, x1, y1);
    }

    private static EntityRenderState renderState(final LivingEntity entity) {
        final EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        final EntityRenderer<? super LivingEntity, ?> renderer = dispatcher.getRenderer(entity);
        final EntityRenderState state = renderer.createRenderState(entity, 1.0F);
        state.shadowPieces.clear();
        state.outlineColor = 0;
        return state;
    }
}
