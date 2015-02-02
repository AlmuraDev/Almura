/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.core.mixin.client.resources;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.LoaderState;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(SimpleReloadableResourceManager.class)
public abstract class MixinSimpleReloadableResourceManager implements IReloadableResourceManager {

    @Shadow
    public List reloadListeners;

    @Inject(method = "registerReloadListener", at = @At("HEAD"), cancellable = true)
    public void onRegisterReloadListener(IResourceManagerReloadListener listener, CallbackInfo ci) {
        this.reloadListeners.add(listener);

        if (Loader.instance().hasReachedState(LoaderState.AVAILABLE)) {
            listener.onResourceManagerReload(this);
        }
        ci.cancel();
    }
}
