package com.almuradev.almura.core;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

import java.util.Map;

public class AlmuraCoreMod implements IFMLLoadingPlugin {
    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return "com.almuradev.almura.Almura";
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
        return null;
    }
}
