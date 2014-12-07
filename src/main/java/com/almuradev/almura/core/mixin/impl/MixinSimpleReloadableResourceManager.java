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
    private boolean reload = false;

    @Shadow
    public List reloadListeners;

    @Overwrite
    private void notifyReloadListeners()
    {
        Iterator iterator = reloadListeners.iterator();

        while (iterator.hasNext())
        {
            IResourceManagerReloadListener iresourcemanagerreloadlistener = (IResourceManagerReloadListener)iterator.next();
            if (iresourcemanagerreloadlistener.getClass() == TextureManager.class && Loader.instance().hasReachedState(LoaderState.AVAILABLE) && !reload) {
                reload = true;
                continue;
            }
            iresourcemanagerreloadlistener.onResourceManagerReload(this);
        }
    }
}
