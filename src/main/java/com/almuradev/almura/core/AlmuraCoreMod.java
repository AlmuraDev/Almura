/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.core;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;

import java.util.Map;

@IFMLLoadingPlugin.SortingIndex(value = 1001)
public class AlmuraCoreMod implements IFMLLoadingPlugin {

    @SuppressWarnings("unchecked")
    public AlmuraCoreMod() {
        MixinBootstrap.init();
        MixinEnvironment.getCurrentEnvironment().addConfiguration("mixins.almura.json");
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[]{
                MixinBootstrap.TRANSFORMER_CLASS
        };
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
    }

    @Override
    public String getAccessTransformerClass() {
        return AlmuraAccessTransformer.CLASSPATH;
    }
}
