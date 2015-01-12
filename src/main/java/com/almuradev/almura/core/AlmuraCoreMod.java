/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.core;

import com.almuradev.almura.pack.Pack;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.launcher.FMLServerTweaker;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.Launch;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;

import java.util.List;
import java.util.Map;

public class AlmuraCoreMod implements IFMLLoadingPlugin {

    @SuppressWarnings("unchecked")
    public AlmuraCoreMod() {
        MixinBootstrap.init();
        boolean isServer = false;

        for (ITweaker tweak : (List<ITweaker>) Launch.blackboard.get("Tweaks")) {
            if (tweak instanceof FMLServerTweaker) {
                isServer = true;
                break;
            }
        }

        MixinEnvironment.getCurrentEnvironment().addConfiguration("mixins.server.almura.json");

        if (!isServer) {
            MixinEnvironment.getCurrentEnvironment().addConfiguration("mixins.client.almura.json");
        }
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
