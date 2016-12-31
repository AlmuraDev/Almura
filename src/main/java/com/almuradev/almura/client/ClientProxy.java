/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client;

import com.almuradev.almura.CommonProxy;
import com.almuradev.almura.Constants;
import com.almuradev.almura.client.model.obj.OBJModelLoader;
import com.almuradev.almura.client.gui.screen.ingame.SimpleIngameMenu;
import com.almuradev.almura.client.gui.screen.ingame.hud.AbstractHUD;
import com.almuradev.almura.client.gui.screen.ingame.hud.OriginHUD;
import com.almuradev.almura.client.gui.screen.menu.AnimatedMainMenu;
import com.almuradev.almura.configuration.MappedConfigurationAdapter;
import com.almuradev.almura.configuration.category.ClientCategory;
import com.almuradev.almura.configuration.type.ClientConfiguration;
import com.almuradev.almura.network.NetworkHandlers;
import com.almuradev.almura.network.play.SServerInformationMessage;
import com.almuradev.almura.network.play.SWorldInformationMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.IResourcePack;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Platform;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

/**
 * The client platform of Almura. All code not meant to run on a dedicated server should go here.
 */
@SideOnly(Side.CLIENT)
public final class ClientProxy extends CommonProxy {

    @Nullable
    private MappedConfigurationAdapter<ClientConfiguration> configAdapter;
    @Nullable
    private AbstractHUD customIngameHud;

    @Override
    public void onGamePreInitialization(GamePreInitializationEvent event) {
        // Must be a better way to go about this...
        final List<IResourcePack> resourcePacks = ObfuscationReflectionHelper.getPrivateValue(Minecraft.class, Minecraft.getMinecraft(),
                "defaultResourcePacks");
        resourcePacks.add(new CustomFolderResourcePack("CustomFolderResourcePack: " + Constants.Plugin.NAME, Constants.FileSystem
                .PATH_ASSETS_ALMURA_30, Constants.Plugin.ID));
        Minecraft.getMinecraft().refreshResources();

        this.configAdapter = new MappedConfigurationAdapter<>(ClientConfiguration.class, ConfigurationOptions.defaults()
                .setHeader(Constants.Config.HEADER), Constants.FileSystem.PATH_CONFIG_ALMURA_CLIENT);
        try {
            this.configAdapter.load();
        } catch (IOException | ObjectMappingException e) {
            throw new RuntimeException("Failed to load config for class [" + this.configAdapter.getConfigClass() + "] from [" + this.configAdapter
                    .getConfigPath() + "]!", e);
        }

        final ClientCategory clientCategory = this.configAdapter.getConfig().client;
        if (clientCategory.firstLaunch) {
            clientCategory.optimizeGame();

            clientCategory.firstLaunch = false;

            try {
                this.configAdapter.save();
            } catch (IOException | ObjectMappingException e) {
                throw new RuntimeException("Failed to save config for class [" + this.configAdapter.getConfigClass() + "] in [" + this.configAdapter
                        .getConfigPath() + "]!", e);
            }
        }

        ModelLoaderRegistry.registerLoader(OBJModelLoader.instance);
        OBJModelLoader.instance.registerDomain(Constants.Plugin.ID);

        MinecraftForge.EVENT_BUS.register(this);
        super.onGamePreInitialization(event);
    }

    @SubscribeEvent
    public void onGuiScreen(GuiOpenEvent event) {
        final GuiScreen screen = event.getGui();
        if (event.getGui() != null) {
            if (event.getGui().getClass().equals(GuiMainMenu.class)) {
                event.setCanceled(true);
                new AnimatedMainMenu(null).display();
            } else if (event.getGui().getClass().equals(GuiIngameMenu.class)) {
                event.setCanceled(true);
                new SimpleIngameMenu().display();
            }
        }
    }

    @Override
    protected void registerMessages() {
        super.registerMessages();
        this.network.addHandler(SWorldInformationMessage.class, Platform.Type.CLIENT, new NetworkHandlers.SWorldInformationHandler());
        this.network.addHandler(SServerInformationMessage.class, Platform.Type.CLIENT, new NetworkHandlers.SServerInformationHandler());
    }

    @Override
    public MappedConfigurationAdapter<ClientConfiguration> getPlatformConfigAdapter() {
        return this.configAdapter;
    }


    @SubscribeEvent
    public void onRenderGameOverlayEventPre(RenderGameOverlayEvent.Pre event) {
        final ClientCategory clientCategory = this.configAdapter.getConfig().client;

        switch (clientCategory.hud.toLowerCase()) {
            case "origin":
                if (!(this.customIngameHud instanceof OriginHUD)) {
                    if (this.customIngameHud != null) {
                        this.customIngameHud.closeOverlay();
                    }
                    this.customIngameHud = new OriginHUD();
                    this.customIngameHud.displayOverlay();
                }
                switch (event.getType()) {
                    case HEALTH:
                    case ARMOR:
                    case FOOD:
                    case HEALTHMOUNT:
                    case EXPERIENCE:
                        event.setCanceled(true);
                        break;
                    default:
                }
                break;
            default:
                if (this.customIngameHud != null) {
                    this.customIngameHud.closeOverlay();
                    this.customIngameHud = null;
                }
                break;
        }
    }

    public Optional<AbstractHUD> getCustomIngameHud() {
        return Optional.ofNullable(this.customIngameHud);
    }
}
