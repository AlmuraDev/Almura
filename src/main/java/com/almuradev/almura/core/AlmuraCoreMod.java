package com.almuradev.almura.core;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

public class AlmuraCoreMod implements IFMLLoadingPlugin {

    @SuppressWarnings("unchecked")
    public AlmuraCoreMod() {
        Launch.classLoader.addClassLoaderExclusion("com.almuradev.almura.core.mixin.transformer.");
        try {
            Field f = LaunchClassLoader.class.getDeclaredField("classLoaderExceptions");
            f.setAccessible(true);
            Set<String> classLoaderExclusions = (Set<String>) f.get(Launch.classLoader);
            classLoaderExclusions.remove("org.lwjgl.");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
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
