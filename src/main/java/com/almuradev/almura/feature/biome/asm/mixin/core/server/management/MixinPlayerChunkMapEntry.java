/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.biome.asm.mixin.core.server.management;

import com.almuradev.almura.feature.biome.asm.mixin.iface.IMixinChunk;
import com.almuradev.almura.feature.biome.network.ClientboundBiomeChunkDataPacket;
import com.almuradev.almura.shared.network.NetworkConfig;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

import javax.inject.Inject;

@Mixin(PlayerChunkMapEntry.class)
public abstract class MixinPlayerChunkMapEntry {

    @Inject @ChannelId(NetworkConfig.CHANNEL) private static ChannelBinding.IndexedMessageChannel network;
    @Shadow public Chunk chunk;
    @Shadow @Final public List<EntityPlayerMP> players;
    @Shadow @Final public ChunkPos pos;
    @Shadow public boolean sentToPlayers;

    @Redirect(method = "sendToPlayer*", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetHandlerPlayServer;sendPacket"
            + "(Lnet/minecraft/network/Packet;)V"))
    private void redirectSendPacket(NetHandlerPlayServer netHandlerPlayServer, Packet<?> packetIn) {
        this.sendBiomeChunkThenRealChunkTo(netHandlerPlayServer, packetIn);
    }

    @Redirect(method = "sendToPlayers", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetHandlerPlayServer;sendPacket"
            + "(Lnet/minecraft/network/Packet;)V"))
    private void redirectSendPacketToPlayers(NetHandlerPlayServer netHandlerPlayServer, Packet<?> packetIn) {
        this.sendBiomeChunkThenRealChunkTo(netHandlerPlayServer, packetIn);
    }

    @Redirect(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/management/PlayerChunkMapEntry;sendPacket"
            + "(Lnet/minecraft/network/Packet;)V", ordinal = 1))
    private void redirectSendPacketUpdate(PlayerChunkMapEntry playerChunkMapEntry, Packet<?> packetIn) {
        this.sendBiomeChunkThenRealChunkToAll(playerChunkMapEntry, packetIn);
    }

    @Redirect(method = "removePlayer",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetHandlerPlayServer;sendPacket(Lnet/minecraft/network/Packet;)V"))
    private void redirectSendPacketRemove(NetHandlerPlayServer netHandlerPlayServer, Packet<?> packetIn) {
        netHandlerPlayServer.sendPacket(packetIn);
        network.sendTo((Player) netHandlerPlayServer.player, new ClientboundBiomeChunkDataPacket(ChunkPos.asLong(this.pos.x, this.pos.z)));
    }

    private void sendBiomeChunkThenRealChunkTo(NetHandlerPlayServer netHandlerPlayServer, Packet<?> packetIn) {
        final SPacketChunkData chunkDataPacket = (SPacketChunkData) packetIn;
        if (chunkDataPacket.isFullChunk()) {
            final IMixinChunk mixinChunk = (IMixinChunk) this.chunk;
            int[] extendedBiomeArray = mixinChunk.getExtendedBiomeArray();
            if (extendedBiomeArray == null) {
                extendedBiomeArray = mixinChunk.cacheRealBiomeIds();
            }

            final Player spongePlayer = (Player) netHandlerPlayServer.player;
            network.sendTo(spongePlayer, new ClientboundBiomeChunkDataPacket(ChunkPos.asLong(this.chunk.x, this.chunk.z), extendedBiomeArray));
        }

        netHandlerPlayServer.sendPacket(packetIn);
    }

    private void sendBiomeChunkThenRealChunkToAll(PlayerChunkMapEntry playerChunkMapEntry, Packet<?> packetIn) {
        final SPacketChunkData chunkDataPacket = (SPacketChunkData) packetIn;
        if (chunkDataPacket.isFullChunk()) {
            final IMixinChunk mixinChunk = (IMixinChunk) this.chunk;
            int[] extendedBiomeArray = mixinChunk.getExtendedBiomeArray();
            if (extendedBiomeArray == null) {
                extendedBiomeArray = mixinChunk.cacheRealBiomeIds();
            }

            final ClientboundBiomeChunkDataPacket biomePacket =
                    new ClientboundBiomeChunkDataPacket(ChunkPos.asLong(this.chunk.x, this.chunk.z), extendedBiomeArray);

            if (this.sentToPlayers) {
                for (EntityPlayerMP player : this.players) {
                    network.sendTo((Player) player, biomePacket);
                }
            }
        }

        playerChunkMapEntry.sendPacket(packetIn);
    }
}
