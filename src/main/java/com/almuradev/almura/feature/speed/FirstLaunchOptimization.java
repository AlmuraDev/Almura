/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.speed;

import com.almuradev.almura.core.client.config.ClientConfiguration;
import com.almuradev.almura.core.client.config.ClientCategory;
import com.almuradev.almura.shared.config.ConfigLoadEvent;
import com.almuradev.core.event.Witness;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraft.client.settings.GameSettings;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Iterator;

@SideOnly(Side.CLIENT)
public final class FirstLaunchOptimization implements Witness {

    @SubscribeEvent
    public void onConfigLoad(final ConfigLoadEvent<ClientConfiguration> event) {
        final ClientCategory category = event.config().client;
        if (category.firstLaunch) {
            category.firstLaunch = false;
            optimizeGame(true);
            event.adapter().save();
        }
    }

    /**
     * Called on first launch to optimize the client's GUI settings. Addresses many users lack of knowledge of
     * the various settings that can lead to better FPS. Improves overall experience with Almura.
     */
    public static void optimizeGame(boolean firstLaunch) {
        final GameSettings settings = Minecraft.getMinecraft().gameSettings;
        settings.autoJump = false;
        settings.ambientOcclusion = 1;
        settings.fancyGraphics = false;
        settings.useVbo = true;
        settings.mipmapLevels = 0;
        settings.guiScale = 2;
        settings.limitFramerate = 120;
        settings.enableVsync = false;
        settings.clouds = 0;
        settings.snooperEnabled = false;
        settings.renderDistanceChunks = 12;
        settings.viewBobbing = false;
        settings.resourcePacks.clear();

        if (!settings.resourcePacks.contains("Almura Font.zip")) {
            settings.resourcePacks.add("Almura Font.zip");
        }

        final ResourcePackRepository resourcepackrepository = Minecraft.getMinecraft().getResourcePackRepository();
        final Iterator<String> iterator = settings.resourcePacks.iterator();

        while (iterator.hasNext()) {
            String name = iterator.next();
            for (ResourcePackRepository.Entry resourcepackrepository$entry : resourcepackrepository.repositoryEntriesAll) {
                if (resourcepackrepository$entry.getResourcePackName().equals(name)) {
                    if (resourcepackrepository$entry.getPackFormat() == 3 || settings.incompatibleResourcePacks.contains(resourcepackrepository$entry.getResourcePackName())) {
                        resourcepackrepository.repositoryEntries.add(resourcepackrepository$entry);
                        break;
                    }
                    iterator.remove();
                }
            }
        }

        settings.saveOptions();

        if (!firstLaunch) {
            Minecraft.getMinecraft().refreshResources();
        }
    }
}
