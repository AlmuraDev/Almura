package com.almuradev.almura.core.mixin.impl;

import com.almuradev.almura.core.mixin.Mixin;
import com.almuradev.almura.core.mixin.Overwrite;
import com.almuradev.almura.core.mixin.Shadow;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.LoaderState;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.ITickable;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.util.ResourceLocation;

import java.util.Iterator;
import java.util.Map;

@Mixin(TextureManager.class)
public abstract class MixinTextureManager implements ITickable, IResourceManagerReloadListener {
    private boolean reload = false;

    @Shadow
    private Map mapTextureObjects;

    @Shadow
    abstract boolean loadTexture(ResourceLocation p_110579_1_, final ITextureObject p_110579_2_);

    @Overwrite
    public void onResourceManagerReload(IResourceManager p_110549_1_)
    {
        if (Loader.instance().activeModContainer() != null) {
            if (Loader.instance().isInState(LoaderState.AVAILABLE) && !reload) {
                reload = true;
                return;
            }
        }

        Iterator iterator = this.mapTextureObjects.entrySet().iterator();

        while (iterator.hasNext())
        {
            Map.Entry entry = (Map.Entry)iterator.next();
            this.loadTexture((ResourceLocation)entry.getKey(), (ITextureObject)entry.getValue());
        }
    }
}
