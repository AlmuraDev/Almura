/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.core;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.spongepowered.asm.mixin.MixinEnvironment;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

public class AlmuraCoreMod implements IFMLLoadingPlugin {

    @SuppressWarnings("unchecked")
    public AlmuraCoreMod() {
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
        return new String[] {MixinEnvironment.MIXIN_TRANSFORMER_CLASS};
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
