/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.title.client;

import com.almuradev.almura.feature.accessory.client.ClientAccessories;
import com.almuradev.almura.feature.menu.StrataGraphics;
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
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.event.input.InputEvents;
import org.inspirenxe.strata.api.geometry.Rect;
import org.inspirenxe.strata.api.input.MouseButton;
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

    // Where vanilla puts the name over its attachment, and a little room over the title, in blocks
    private static final double NAME_ABOVE = 0.5, LABEL_PAD = 0.05;
    // How far a drag of one pixel turns the figure
    private static final float DEGREES_PER_PIXEL = 1.5F;
    // Spaces painted since the screen last drew
    private static final List<PlayerPreview> PAINTED = new ArrayList<>();

    private final Spacer space;
    private final int size;
    private final Supplier<Component> name;
    // How far the figure is turned, in degrees: dragged with the left button, 0 facing the viewer
    private float yaw;

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
        // Drawn as Strata paints its space, so what Strata paints after (tooltips) goes over it; else over the screen, as before
        space.onEvent(org.inspirenxe.strata.api.component.Component.PAINTED, event -> {
            final GuiGraphicsExtractor graphics = StrataGraphics.of(event.painter());
            if (graphics != null) {
                preview.draw(graphics);
            } else {
                PAINTED.add(preview);
            }
        });
        // Dragged left or right to turn the figure all the way round, as 3.5's accessory preview
        space.setHitTestVisible(true);
        space.onEvent(InputEvents.DRAG, event -> {
            if (event.button() == MouseButton.LEFT) {
                preview.yaw = (preview.yaw - event.deltaX() * DEGREES_PER_PIXEL) % 360.0F;
            }
        });
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
                preview.draw(graphics);
            }
            PAINTED.clear();
        }
    }

    private void draw(final GuiGraphicsExtractor graphics) {
        final LocalPlayer player = Minecraft.getInstance().player;
        final Rect bounds = this.space.bounds();
        if (player == null || bounds.width() <= 0f || bounds.height() <= 0f) {
            return;
        }
        final int x0 = Math.round(bounds.x()), y0 = Math.round(bounds.y());
        final int x1 = Math.round(bounds.x() + bounds.width()), y1 = Math.round(bounds.y() + bounds.height());
        // Turned as far as it has been dragged; level, so it can be seen from all round
        final Quaternionf rotation = new Quaternionf().rotateZ((float) Math.PI);
        final Quaternionf xRotation = new Quaternionf();
        final EntityRenderState state = renderState(player);
        if (state instanceof final LivingEntityRenderState living) {
            living.bodyRot = 180.0F + this.yaw;
            living.yRot = 0.0F;
            living.xRot = 0.0F;
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
        // The figure and its labels centred together: the labels reach the top of the title line, over the name (raised over a tall
        // accessory); a larger offset draws the figure lower in its space
        final double labelsTop = state.nameTagAttachment.y + ClientAccessories.labelLift(state) + NAME_ABOVE + TitleTags.LINE + LABEL_PAD;
        final float above = (float) Math.max(0.0, labelsTop - state.boundingBoxHeight);
        final Vector3f translation = new Vector3f(0.0F, (state.boundingBoxHeight + above) / 2.0F, 0.0F);
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
