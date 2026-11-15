/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.mixin.client;

import com.almuradev.almura.core.client.config.AlmuraClientConfig;
import com.almuradev.almura.feature.renderdistance.RenderDistances;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Chests past Almura's chest render distance aren't drawn, as 3.5's {@code MixinTileEntityChestRenderer} did.
 */
@Mixin(BlockEntityRenderDispatcher.class)
public abstract class BlockEntityRenderDispatcherMixin {

    @Shadow private Vec3 cameraPos;

    @Inject(method = "tryExtractRenderState(Lnet/minecraft/world/level/block/entity/BlockEntity;FLnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;ZLnet/minecraft/client/renderer/culling/Frustum;)Lnet/minecraft/client/renderer/blockentity/state/BlockEntityRenderState;",
        at = @At("HEAD"), cancellable = true)
    private void almura$chestDistance(final BlockEntity blockEntity, final float partialTick, final ModelFeatureRenderer.CrumblingOverlay overlay,
        final boolean offScreen, final Frustum frustum, final CallbackInfoReturnable<BlockEntityRenderState> callback) {
        if ((blockEntity instanceof ChestBlockEntity || blockEntity instanceof EnderChestBlockEntity) && this.cameraPos != null
            && RenderDistances.beyond(AlmuraClientConfig.CHEST_RENDER_DISTANCE, Vec3.atCenterOf(blockEntity.getBlockPos()).distanceToSqr(this.cameraPos))) {
            callback.setReturnValue(null);
        }
    }
}
