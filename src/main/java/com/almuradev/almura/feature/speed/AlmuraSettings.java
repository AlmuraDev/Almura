/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.speed;

import com.almuradev.almura.asm.ClientStaticAccess;
import com.almuradev.almura.asm.mixin.accessors.client.resources.ResourcePackRepositoryAccessor;
import com.almuradev.almura.core.client.config.ClientConfiguration;
import com.almuradev.almura.feature.menu.main.PanoramicMainMenu;
import com.almuradev.almura.feature.speed.client.gui.ApplyTexturePackConfirmGui;
import com.almuradev.toolbox.config.map.MappedConfiguration;
import com.google.inject.Inject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraft.client.settings.GameSettings;
import net.minecraftforge.client.resource.VanillaResourceType;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Iterator;

@SideOnly(Side.CLIENT)
public class AlmuraSettings {
    @Inject private static MappedConfiguration<ClientConfiguration> configAdapter;

    final static String fontTexturePack = "Almura Font.zip";
    final static String preferredTexturePack = "Almura x128.zip";

    public static void checkFirstLaunched() {
        final GameSettings settings = Minecraft.getMinecraft().gameSettings;
        optimizeGame();
        if (!settings.resourcePacks.contains(AlmuraSettings.getFontTexturePack())) {
            AlmuraSettings.applyFontTexturePack();
        }
        if (!settings.resourcePacks.contains(AlmuraSettings.getPreferredTexturePack())) {
            new ApplyTexturePackConfirmGui(null).display();
        } else {
            new PanoramicMainMenu(null).display();
        }
        setFirstLaunched(false);
    }

    public static void optimizeGame() {
        final GameSettings settings = Minecraft.getMinecraft().gameSettings;
        final ClientConfiguration configuration = configAdapter.get();
        settings.autoJump = false;
        settings.ambientOcclusion = 1;
        settings.fancyGraphics = false;
        settings.useVbo = true;
        settings.mipmapLevels = 0;
        settings.guiScale = 3;
        settings.limitFramerate = 90;
        settings.enableVsync = false;
        settings.clouds = 0;
        settings.snooperEnabled = false;
        settings.renderDistanceChunks = 10;
        settings.viewBobbing = false;

        if (Loader.isModLoaded("journeymap")) {
            // Hide the x/y/z location because the map default uses top-right on screen.
            configuration.general.displayLocationWidget = false;
        }
        saveAlmuraConfigs();
    }

    protected static void setFirstLaunched(boolean value) {
        final ClientConfiguration configuration = configAdapter.get();
        configuration.general.firstLaunch = value;
        saveAlmuraConfigs();
    }

    protected static void saveAlmuraConfigs() {
        ClientStaticAccess.configAdapter.save();
        ClientStaticAccess.configAdapter.load();
    }

    protected static void applyFontTexturePack() {
        applyPack(fontTexturePack);
        FMLClientHandler.instance().scheduleResourcesRefresh(VanillaResourceType.TEXTURES);
        System.out.println("[Almura Settings] - Applied preferred font texture pack");
    }

    public static void applyPreferredTexturePack() {
        applyPack(preferredTexturePack);
        FMLClientHandler.instance().scheduleResourcesRefresh(VanillaResourceType.TEXTURES);
        FMLClientHandler.instance().scheduleResourcesRefresh(VanillaResourceType.MODELS);
        System.out.println("[Almura Settings] - Applied preferred texture pack");
    }

    protected static void applyPack(String pack) {
        final GameSettings settings = Minecraft.getMinecraft().gameSettings;
        final ClientConfiguration configuration = configAdapter.get();


        if (!settings.resourcePacks.contains(pack)) {
            settings.resourcePacks.add(pack);
        }

        settings.saveOptions();

        final ResourcePackRepository resourcepackrepository = Minecraft.getMinecraft().getResourcePackRepository();
        final Iterator<String> iterator = settings.resourcePacks.iterator();

        while (iterator.hasNext()) {
            String name = iterator.next();
            for (ResourcePackRepository.Entry resourcepackrepository$entry : ((ResourcePackRepositoryAccessor)resourcepackrepository).accessor$getRepositoryEntriesAll()) {
                if (resourcepackrepository$entry.getResourcePackName().equals(name)) {
                    if (resourcepackrepository$entry.getPackFormat() == 3 || settings.incompatibleResourcePacks.contains(resourcepackrepository$entry.getResourcePackName())) {
                        if (!((ResourcePackRepositoryAccessor)resourcepackrepository).accessor$getRepositoryEntries().contains(resourcepackrepository$entry)) {
                            ((ResourcePackRepositoryAccessor)resourcepackrepository).accessor$getRepositoryEntries().add(resourcepackrepository$entry);
                        }
                        break;
                    }
                    iterator.remove();
                }
            }
        }
    }

    public static String getFontTexturePack() {
        return fontTexturePack;
    }

    public static String getPreferredTexturePack() {
        return preferredTexturePack;
    }
}
