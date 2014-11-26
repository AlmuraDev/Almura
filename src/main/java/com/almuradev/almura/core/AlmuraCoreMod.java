package com.almuradev.almura.core;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import net.minecraft.launchwrapper.Launch;

import java.util.Map;

public class AlmuraCoreMod implements IFMLLoadingPlugin {
    public AlmuraCoreMod() {
        Launch.classLoader.addClassLoaderExclusion("com.almuradev.almura.core.mixin.transformer.");
    }
    @Override
    public String[] getASMTransformerClass() {
        return new String[]{
                "com.almuradev.almura.core.mixin.transformer.MixinTransformer"
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
        return "com.almuradev.almura.core.AlmuraAccessTransformer";
    }
}
