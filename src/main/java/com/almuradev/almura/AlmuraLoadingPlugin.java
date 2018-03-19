/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

import java.util.Map;

@IFMLLoadingPlugin.MCVersion("1.12.2")
public final class AlmuraLoadingPlugin implements IFMLLoadingPlugin {

    public AlmuraLoadingPlugin() {
        MixinBootstrap.init();
        Mixins.addConfigurations(
                "mixins.bloom.content.json",
                "mixins.bloom.content.block.json",
                "mixins.bloom.content.block.horizontal.json",
                "mixins.bloom.content.blocksoundgroup.json",
                "mixins.bloom.content.item.json",
                "mixins.bloom.content.item.food.json",
                "mixins.bloom.content.item.seed.json",
                "mixins.bloom.content.itemgroup.json",
                "mixins.bloom.content.mapcolor.json",
                "mixins.bloom.content.material.json",
                "mixins.almura.core.json",
                "mixins.almura.feature.cache.json",
                "mixins.almura.feature.healthbar.json",
                "mixins.almura.feature.almanac.json",
                "mixins.almura.feature.sign.json",
                "mixins.almura.feature.title.json",
                "mixins.almura.feature.biome.json"
        );
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
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
    public void injectData(final Map<String, Object> data) {
    }

    @Override
    public String getAccessTransformerClass() {
        return "com.almuradev.almura.asm.transformer.AlmuraAccessTransformer";
    }
}
