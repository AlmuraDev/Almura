/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.cache.client;

import com.almuradev.almura.core.client.config.AlmuraClientConfig;
import com.almuradev.almura.feature.cache.CacheBlock;
import com.almuradev.almura.feature.cache.CacheBlockEntity;
import com.almuradev.almura.feature.cache.CacheStack;
import com.almuradev.almura.feature.renderdistance.RenderDistances;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * What a cache holds, on its front, as 3.5's {@code CacheTileEntityRenderer}: the item in the middle as an item frame shows it, its name
 * (the cache's own when empty) above it on up to two lines, and how many over its limit below it.
 */
public final class CacheRenderer implements BlockEntityRenderer<CacheBlockEntity, CacheRenderer.State> {

    private static final int MAX_LINE_WIDTH = 84;
    // 3.5's text scale: a sign's
    private static final float TEXT_SCALE = 0.6666667F * 0.016666668F;
    // Just off the front face, so neither flickers against it
    private static final float FRONT = 0.525F;

    private final ItemModelResolver itemModelResolver;
    private final Font font;

    public CacheRenderer(final BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
        this.font = context.font();
    }

    public static final class State extends BlockEntityRenderState {

        final ItemStackRenderState item = new ItemStackRenderState();
        Direction facing = Direction.NORTH;
        List<FormattedCharSequence> name = List.of();
        String quantity = "0", limit = "0";
        int frontLight;
    }

    /**
     * Past Almura's cache render distance (Options), the cache's item and text aren't drawn, as 3.5's.
     */
    @Override
    public boolean shouldRender(final CacheBlockEntity cache, final Vec3 cameraPosition) {
        return BlockEntityRenderer.super.shouldRender(cache, cameraPosition)
            && !RenderDistances.beyond(AlmuraClientConfig.CACHE_RENDER_DISTANCE, Vec3.atCenterOf(cache.getBlockPos()).distanceToSqr(cameraPosition));
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(final CacheBlockEntity cache, final State state, final float partialTicks, final Vec3 cameraPosition,
        final ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(cache, state, partialTicks, cameraPosition, breakProgress);
        state.facing = cache.getBlockState().hasProperty(HorizontalDirectionalBlock.FACING)
            ? cache.getBlockState().getValue(HorizontalDirectionalBlock.FACING) : Direction.NORTH;
        final CacheStack stack = cache.stack();
        state.item.clear();
        final Component name;
        if (stack.isEmpty()) {
            name = cache.getBlockState().getBlock().getName();
            state.quantity = "0";
        } else {
            this.itemModelResolver.updateForTopItem(state.item, stack.resource().toStack(), ItemDisplayContext.FIXED, cache.getLevel(), null, 0);
            name = stack.resource().getHoverName();
            state.quantity = CacheBlock.FORMAT.format(stack.amount());
        }
        state.name = this.font.split(name, MAX_LINE_WIDTH).stream().limit(2).toList();
        state.limit = CacheBlock.FORMAT.format(cache.limit());
        // Lit as the block in front of it is (the cache's own place is solid, so dark)
        state.frontLight = cache.getLevel() == null ? state.lightCoords
            : LightCoordsUtil.getLightCoords(cache.getLevel(), cache.getBlockPos().relative(state.facing));
    }

    @Override
    public void submit(final State state, final PoseStack poseStack, final SubmitNodeCollector collector, final CameraRenderState camera) {
        poseStack.pushPose();
        // The front face's middle, its outward side along +z
        poseStack.translate(0.5F, 0F, 0.5F);
        poseStack.rotateDegrees(Axis.YP, -state.facing.toYRot());
        poseStack.translate(0F, 0F, FRONT);

        if (!state.item.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0F, FRONT, 0F);
            poseStack.rotateDegrees(Axis.YP, 180F);
            poseStack.scale(0.5F, 0.5F, 0.5F);
            state.item.submit(poseStack, collector, state.frontLight, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }

        // The text, as a sign's: from the bottom edge up, 3.5's lines
        poseStack.scale(TEXT_SCALE, -TEXT_SCALE, TEXT_SCALE);
        if (state.name.size() == 1) {
            this.text(poseStack, collector, state.name.getFirst(), -80, state.frontLight);
        } else {
            for (int line = 0; line < state.name.size(); line++) {
                this.text(poseStack, collector, state.name.get(line), -80 + line * 10, state.frontLight);
            }
        }
        this.text(poseStack, collector, Component.literal(state.quantity).getVisualOrderText(), -24, state.frontLight);
        // The line between how many and the limit, as wide as the limit
        final int spaces = Math.max(1, Math.round(this.font.width(state.limit) / (float) this.font.width(" ")));
        this.text(poseStack, collector, Component.literal(" ".repeat(spaces)).withStyle(ChatFormatting.STRIKETHROUGH).getVisualOrderText(), -20,
            state.frontLight);
        this.text(poseStack, collector, Component.literal(state.limit).getVisualOrderText(), -14, state.frontLight);
        poseStack.popPose();
    }

    private void text(final PoseStack poseStack, final SubmitNodeCollector collector, final FormattedCharSequence text, final float y,
        final int light) {
        collector.submitText(poseStack, -this.font.width(text) / 2F, y, text, false, Font.DisplayMode.POLYGON_OFFSET, light, 0xFF000000, 0, 0);
    }
}
