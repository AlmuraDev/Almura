/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.core.mixin.client.settings;

import com.almuradev.almura.Configuration;
import com.flowpowered.cerealization.config.ConfigurationException;

import net.minecraft.client.settings.GameSettings;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(GameSettings.class)
public abstract class MixinGameSettings {
    @Shadow
    public List resourcePacks;

    @Inject(method = "loadOptions", at = @At(value = "RETURN"))
    public void onLoadOptions(CallbackInfo ci) {
        try {
			Configuration.load();
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
    	if (Configuration.FIRST_LAUNCH) {
            resourcePacks.add("Almura Preferred Font.zip");
        }

    }
}
