/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client;

import com.almuradev.almura.CommonProxy;
import com.almuradev.almura.Configuration;
import com.almuradev.almura.client.gui.SimpleGui;
import com.almuradev.almura.client.gui.ingame.hud.IngameAlmuraHUD;
import com.almuradev.almura.client.gui.menu.DynamicMainMenu;
import com.almuradev.almura.network.NetworkHandlers;
import com.almuradev.almura.network.play.SWorldInformationMessage;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.spongepowered.api.Platform;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;

/**
 * The client platform of Almura. All code not meant to run on a dedicated server should go here.
 */
public final class ClientProxy extends CommonProxy {
    public static final String CLASSPATH = "com.almuradev.almura.client.ClientProxy";

    public static SimpleGui HUD_INGAME;

    @Override
    protected void onGamePreInitialization(GamePreInitializationEvent event) {
        super.onGamePreInitialization(event);

        if (Configuration.FIRST_LAUNCH) {
            Configuration.setOptimizedConfig();
        }

        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onGuiScreen(GuiOpenEvent event) {
        if (event.gui != null && event.gui.getClass().equals(GuiMainMenu.class)) {
            event.setCanceled(true);
            new DynamicMainMenu(null).display();
        }
    }

    @Override
    protected void registerMessages() {
        super.registerMessages();
        this.network.addHandler(SWorldInformationMessage.class, Platform.Type.CLIENT, new NetworkHandlers.S00WorldInformationHandler());
    }

    @SubscribeEvent
    public void onRenderGameOverlayEventPost(RenderGameOverlayEvent.Pre event) {
        // Use proper HUD based on configuration
        if (Configuration.HUD_TYPE.equalsIgnoreCase("almura")) {
            if (!(HUD_INGAME instanceof IngameAlmuraHUD)) {
                if (HUD_INGAME != null) {
                    HUD_INGAME.closeOverlay();
                }
                HUD_INGAME = new IngameAlmuraHUD();
                HUD_INGAME.displayOverlay();
            }
            switch (event.type) {
                case HEALTH:
                case ARMOR:
                case FOOD:
                case EXPERIENCE:
                    event.setCanceled(true);
                    break;
                default:
            }
        }
//        } else if (Configuration.HUD_TYPE.equalsIgnoreCase("less")) {
//            if (!(HUD_INGAME instanceof IngameLessHUD)) {
//                if (HUD_INGAME != null) {
//                    HUD_INGAME.closeOverlay();
//                }
//                HUD_INGAME = new IngameLessHUD();
//                HUD_INGAME.displayOverlay();
//            }
//        } else if (Configuration.HUD_TYPE.equalsIgnoreCase("vanilla") && HUD_INGAME != null) {
//            HUD_INGAME.closeOverlay();
//            HUD_INGAME = null;
//        }
//
//        // Toggle residence off/on based on config. Creation of this HUD happens in the packet handler from Bukkit
//        if (Configuration.DISPLAY_RESIDENCE_HUD && HUDData.WITHIN_RESIDENCE) {
//            if (HUD_RESIDENCE == null) {
//                HUD_RESIDENCE = new IngameResidenceHUD();
//            }
//
//            HUD_RESIDENCE.displayOverlay();
//            HUD_RESIDENCE.updateWidgets();
//        } else if (HUD_RESIDENCE != null) {
//            HUD_RESIDENCE.closeOverlay();
//            HUD_RESIDENCE = null;
//        }
//
//        // Toggle enhanced debug off/on based on config.
//        if (event.type == ElementType.DEBUG) {
//            if (Configuration.DISPLAY_ENHANCED_DEBUG) {
//                event.setCanceled(true);
//                if (HUD_DEBUG == null) {
//                    HUD_DEBUG = new IngameDebugHUD();
//                }
//
//                HUD_DEBUG.displayOverlay();
//            } else if (HUD_DEBUG != null) {
//                HUD_DEBUG.closeOverlay();
//                HUD_DEBUG = null;
//            }
//        }
//
//        // User turned off debug mode, handle it.
//        if (!Minecraft.getMinecraft().gameSettings.showDebugInfo && HUD_DEBUG != null) {
//            HUD_DEBUG.closeOverlay();
//            HUD_DEBUG = null;
//        }
    }
}
