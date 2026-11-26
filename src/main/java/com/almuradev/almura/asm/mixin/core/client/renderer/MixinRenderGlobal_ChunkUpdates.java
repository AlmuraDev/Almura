/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.asm.mixin.core.client.renderer;

import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Builds chunk sections changed by the player's own clicks in the background, like every other nearby change.
 *
 * <p>Only applies with OptiFine (HD U G5). Its setupTerrain builds a dirty section near the player on the main thread when
 * {@code RenderChunk.isPlayerUpdate()} says the player's own click changed it (breaking a crop, planting a seed), and puts
 * every other nearby section in its forced queue, which is built by the render worker threads. Farming changes a section
 * with every click, so those main thread builds stutter. Answering false sends them to the forced queue too: the change
 * shows a frame or two later instead of holding up the frame.</p>
 *
 * <p>isPlayerUpdate only exists in OptiFine's RenderChunk, so without OptiFine (or with a version that dropped it) this
 * finds nothing to redirect and does nothing (require = 0).</p>
 */
@SideOnly(Side.CLIENT)
@Mixin(RenderGlobal.class)
public abstract class MixinRenderGlobal_ChunkUpdates {

    @Redirect(
        method = "setupTerrain",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/RenderChunk;isPlayerUpdate()Z", remap = false),
        require = 0,
        expect = 0
    )
    private boolean almura$buildPlayerUpdatesInBackground(final RenderChunk renderChunk) {
        return false;
    }
}
