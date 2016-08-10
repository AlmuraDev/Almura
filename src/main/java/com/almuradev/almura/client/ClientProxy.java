/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client;

import com.almuradev.almura.Almura;
import com.almuradev.almura.CommonProxy;
import com.almuradev.almura.FileSystem;
import com.almuradev.almura.client.gui.SimpleGui;
import com.almuradev.almura.client.gui.ingame.IngameOptionsMenu;
import com.almuradev.almura.client.gui.ingame.hud.AlmuraHUD;
import com.almuradev.almura.client.gui.ingame.hud.MinimalHUD;
import com.almuradev.almura.client.gui.menu.SimpleMainMenu;
import com.almuradev.almura.client.shape.ShapeLoader;
import com.almuradev.almura.configuration.ConfigurationAdapter;
import com.almuradev.almura.configuration.category.ClientCategory;
import com.almuradev.almura.configuration.type.ClientConfiguration;
import com.almuradev.almura.network.NetworkHandlers;
import com.almuradev.almura.network.play.SWorldInformationMessage;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Platform;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;

import java.io.IOException;
import java.util.Optional;

import javax.annotation.Nullable;

/**
 * The client platform of Almura. All code not meant to run on a dedicated server should go here.
 */
@SideOnly(Side.CLIENT)
public final class ClientProxy extends CommonProxy {

    public static final String CLASSPATH = "com.almuradev.almura.client.ClientProxy";

    private static final String HEADER = "1.0\nAlmura client configuration\n\nFor further assistance, join #almura on EsperNet.";
    @Nullable
    private ConfigurationAdapter<ClientConfiguration> configAdapter;
    @Nullable
    private SimpleGui customIngameHud;

    @Override
    public void onGamePreInitialization(GamePreInitializationEvent event) {
        super.onGamePreInitialization(event);

        this.configAdapter = new ConfigurationAdapter<>(ClientConfiguration.class, ConfigurationOptions.defaults().setHeader(HEADER), FileSystem
                .PATH_CONFIG_CLIENT);
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

        OBJLoader.INSTANCE.addDomain(Almura.PLUGIN_ID);
        ModelLoaderRegistry.registerLoader(new ShapeLoader());
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onGuiScreen(GuiOpenEvent event) {
        if (event.getGui() != null) {
            if (event.getGui().getClass().equals(GuiMainMenu.class)) {
                event.setCanceled(true);
                new SimpleMainMenu(null).display();
            } else if (event.getGui().getClass().equals(GuiIngameMenu.class)) {
                event.setCanceled(true);
                new IngameOptionsMenu().display();
            }
        }
    }

    @Override
    protected void registerMessages() {
        super.registerMessages();
        this.network.addHandler(SWorldInformationMessage.class, Platform.Type.CLIENT, new NetworkHandlers.S00WorldInformationHandler());
    }

    @Override
    public ConfigurationAdapter<ClientConfiguration> getConfigAdapter() {
        return this.configAdapter;
    }


    @SubscribeEvent
    public void onRenderGameOverlayEventPre(RenderGameOverlayEvent.Pre event) {
        final ClientCategory clientCategory = this.configAdapter.getConfig().client;

        // Use proper HUD based on configuration
        if (clientCategory.hud.equalsIgnoreCase("almura")) {
            if (!(this.customIngameHud instanceof AlmuraHUD)) {
                if (this.customIngameHud != null) {
                    this.customIngameHud.closeOverlay();
                }
                this.customIngameHud = new AlmuraHUD();
                this.customIngameHud.displayOverlay();
            }
            switch (event.getType()) {
                case HEALTH:
                case ARMOR:
                case FOOD:
                case EXPERIENCE:
                    event.setCanceled(true);
                    break;
                default:
            }
        } else if (clientCategory.hud.equalsIgnoreCase("minimal")) {
            if (!(this.customIngameHud instanceof MinimalHUD)) {
                if (this.customIngameHud != null) {
                    this.customIngameHud.closeOverlay();
                }
                this.customIngameHud = new MinimalHUD();
                this.customIngameHud.displayOverlay();
            }
        } else if (this.customIngameHud != null) {
            this.customIngameHud.closeOverlay();
            this.customIngameHud = null;
        }
    }

    public Optional<SimpleGui> getCustomIngameHud() {
        return Optional.ofNullable(this.customIngameHud);
    }
}
