/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.core.mixin.impl;

import com.almuradev.almura.core.mixin.Mixin;
import com.almuradev.almura.core.mixin.Overwrite;
import com.almuradev.almura.core.mixin.Shadow;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.LoaderState;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.resources.SimpleReloadableResourceManager;

import java.util.Iterator;
import java.util.List;

@Mixin(SimpleReloadableResourceManager.class)
public abstract class MixinSimpleReloadableResourceManager implements IReloadableResourceManager {

    @Shadow
    public List reloadListeners;
    private boolean reload = false;

    @Overwrite
    private void notifyReloadListeners() {

        for (Object reloadListener : reloadListeners) {
            IResourceManagerReloadListener iresourcemanagerreloadlistener = (IResourceManagerReloadListener) reloadListener;
            if (iresourcemanagerreloadlistener.getClass() == TextureManager.class && Loader.instance().hasReachedState(LoaderState.AVAILABLE)
                && !reload) {
                reload = true;
                continue;
            }
            iresourcemanagerreloadlistener.onResourceManagerReload(this);
        }
    }
}
