/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.menu.options;

import com.almuradev.almura.core.client.config.AlmuraClientConfig;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.GraphicsPreset;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.neoforged.fml.ModList;

/**
 * Load Optimized Defaults, as 3.5's {@code AlmuraSettings.optimizeGame}: vanilla's options set for speed. 3.5's VBO and snooper options are
 * gone from 26.3; the rest are set as 3.5 set them.
 */
final class OptimizedDefaults {

    private OptimizedDefaults() {
    }

    static void apply() {
        final Options options = Minecraft.getInstance().options;
        // Fast graphics first: the preset sets several options, which the lines after then set as 3.5 did
        options.graphicsPreset().set(GraphicsPreset.FAST);
        options.autoJump().set(false);
        options.ambientOcclusion().set(true);
        options.mipmapLevels().set(0);
        options.guiScale().set(3);
        options.framerateLimit().set(90);
        options.enableVsync().set(false);
        options.cloudStatus().set(CloudStatus.OFF);
        options.renderDistance().set(10);
        options.bobView().set(false);
        options.save();
        // JourneyMap's map sits where the location widget is, as 3.5 found
        if (ModList.get().isLoaded("journeymap")) {
            AlmuraClientConfig.set(AlmuraClientConfig.DISPLAY_LOCATION_WIDGET, false);
        }
    }
}
