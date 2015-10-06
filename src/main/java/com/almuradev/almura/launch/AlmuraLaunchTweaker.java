/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2015 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.launch;

import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.spongepowered.asm.launch.MixinTweaker;

import java.io.File;
import java.util.List;

public class AlmuraLaunchTweaker implements ITweaker {
    private static MixinTweaker mixinTweaker;

    @Override
    public void acceptOptions(List<String> list, File file, File file1, String s) {
        mixinTweaker = new MixinTweaker();
        mixinTweaker.acceptOptions(list, file, file1, s);
    }

    @Override
    public void injectIntoClassLoader(LaunchClassLoader launchClassLoader) {
        mixinTweaker.injectIntoClassLoader(launchClassLoader);
    }

    @Override
    public String getLaunchTarget() {
        return "";
    }

    @Override
    public String[] getLaunchArguments() {
        return new String[0];
    }
}
