/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.worldload.asm.mixin;

import com.almuradev.almura.feature.worldload.WorldLoadHooks;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.world.chunk.storage.AnvilSaveHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;

/**
 * Sponge's world loading (used by plugins, and by Forge's DimensionManager.initDimension that mods like SGCraft call) creates the
 * world's save handler before anything slow happens: building the chunk generator, which can take a long time for TerrainControl
 * worlds, and preparing the spawn area. Sponge's LoadWorldEvent only fires after the generator is built, too late to warn players.
 */
@Mixin(AnvilSaveHandler.class)
public abstract class MixinAnvilSaveHandler {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void almura$onCreated(final File savesDirectory, final String directoryName, final boolean storePlayerData,
            final DataFixer dataFixer, final CallbackInfo ci) {
        WorldLoadHooks.onSaveHandlerCreated(directoryName);
    }
}
