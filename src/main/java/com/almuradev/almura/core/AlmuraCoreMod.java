/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.core;

import com.almuradev.almura.Configuration;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.filter.RegexFilter;
import org.spongepowered.asm.mixin.MixinEnvironment;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

public class AlmuraCoreMod implements IFMLLoadingPlugin {

    static {
        //Filter those pesky log entries
        //TODO Non-functional yet
        final Logger launchWrapperLogger = (Logger) LogManager.getLogger("LaunchWrapper");
        launchWrapperLogger.addFilter(
                RegexFilter.createFilter(".*has a security seal for path org.lwjgl.*", "true", Filter.Result.DENY.name(), Filter.Result.DENY.name()));
    }

    @SuppressWarnings("unchecked")
    public AlmuraCoreMod() {
        Launch.classLoader.addClassLoaderExclusion("org.spongepowered.asm.mixin.");
        try {
            Field f = LaunchClassLoader.class.getDeclaredField("classLoaderExceptions");
            f.setAccessible(true);
            Set<String> classLoaderExclusions = (Set<String>) f.get(Launch.classLoader);
            classLoaderExclusions.remove("org.lwjgl.");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        //MixinEnvironment.getCurrentEnvironment().addConfiguration("mixins.almura.json");
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[]{
                MixinEnvironment.MIXIN_TRANSFORMER_CLASS
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
