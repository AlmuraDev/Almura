/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.asm.mixin.core.client;

import com.almuradev.almura.asm.ClientStaticAccess;
import com.almuradev.almura.core.client.config.ClientConfiguration;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// This should help the client cache chunks.  Not sure how this will interact with Optifine since I cannot
// see its codebase.
@SideOnly(Side.CLIENT)
@Mixin(ChunkProviderClient.class)
public abstract class MixinChunkProviderClient {
    // Maintained List of chunks to keep loaded so that the client can continue to render them.
    private final Long2ObjectMap<ChunkPos> clientChunkQueue = new Long2ObjectOpenHashMap<>(8192);
    @Shadow @Final private Long2ObjectMap<Chunk> loadedChunks;
    @Shadow public abstract Chunk provideChunk(final int x, final int z);

    /**
     * @author Dockter
     * @reason Overwrite the unloadChunk method so that it adds the chunk to a maintained list instead of unloading them
     */
    @Overwrite
    public void unloadChunk(int x, int z) {
        final ClientConfiguration configuration = ClientStaticAccess.configAdapter.get();
        if (configuration.general.extendedView) {
            // Client option for Extended View is enabled, so proceed.
            clientChunkQueue.put(ChunkPos.asLong(x, z), new ChunkPos(x, z));
        } else {
            Chunk chunk = this.provideChunk(x, z);
            if (!chunk.isEmpty()) {
                chunk.onUnload();
            }
            this.loadedChunks.remove(ChunkPos.asLong(x, z));
        }
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void onTick(final CallbackInfoReturnable<Boolean> callback) {
        // This could be terribly slow, need to profile
        final ClientConfiguration configuration = ClientStaticAccess.configAdapter.get();
        if (configuration.general.extendedView) {
            final int clientViewDistance = Minecraft.getMinecraft().gameSettings.renderDistanceChunks;
            ObjectIterator<ChunkPos> clientChunkUnloadList = this.clientChunkQueue.values().iterator();

            while (clientChunkUnloadList.hasNext()) {
                ChunkPos chunkPos = clientChunkUnloadList.next();
                if (chunkPos.getDistanceSq(Minecraft.getMinecraft().player) > (clientViewDistance * 16 * clientViewDistance * 16)) {
                    final Chunk chunk = this.provideChunk(chunkPos.x, chunkPos.z);

                    // Make sure the chunk isn't null and isn't garbage.
                    if (chunk != null && !chunk.isEmpty()) {
                        chunk.onUnload();
                    }

                    // Unload the chunk from the client provider
                    this.loadedChunks.remove(ChunkPos.asLong(chunkPos.x, chunkPos.z));

                    // Remove the previously cached chunk from the main cache list
                    clientChunkUnloadList.remove();
                }
            }
        }
    }
}
