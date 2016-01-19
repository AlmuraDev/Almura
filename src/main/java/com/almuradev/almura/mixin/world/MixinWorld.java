/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.mixin.world;

import com.almuradev.almura.Configuration;
import net.minecraft.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.storage.ISaveHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(World.class)
public abstract class MixinWorld {

    @Shadow private int[] lightUpdateBlockList;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onWorldInit(ISaveHandler saveHandler, String value, WorldProvider worldProvider, WorldSettings worldSettings, Profiler profiler,
            CallbackInfo ci) {
        if (Configuration.USE_OPTIMIZED_LIGHTING) {
            this.lightUpdateBlockList = new int[256];
        }
        System.out.println(lightUpdateBlockList.length);
    }
}
