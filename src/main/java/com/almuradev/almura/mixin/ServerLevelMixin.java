/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.mixin;

import com.almuradev.almura.feature.worldload.server.ServerWorldLoad;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.concurrent.Executor;

/**
 * Sees a world being built before its chunk source is, where its chunk generator (TerrainControl's loads its configs and objects) is set up
 * and the server is held up longest, so players are told before the pause rather than after.
 */
@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {

    @Inject(method = "<init>", at = @At(value = "NEW", target = "net/minecraft/server/level/ServerChunkCache"))
    private void almura$building(final MinecraftServer server, final Executor executor, final LevelStorageSource.LevelStorageAccess storage,
        final ServerLevelData data, final ResourceKey<Level> dimension, final LevelStem stem, final boolean debug, final long seed,
        final List<CustomSpawner> spawners, final boolean tickTime, final CallbackInfo ci) {
        ServerWorldLoad.onBuilding(server, dimension);
    }
}
