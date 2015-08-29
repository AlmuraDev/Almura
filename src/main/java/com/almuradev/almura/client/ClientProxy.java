/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client;

import com.almuradev.almura.client.network.play.B01ResTokenConfirmation;

import com.almuradev.almura.server.network.play.bukkit.B05GuiController;
import com.almuradev.almura.Almura;
import com.almuradev.almura.CommonProxy;
import com.almuradev.almura.Configuration;
import com.almuradev.almura.client.gui.ingame.HUDData;
import com.almuradev.almura.client.gui.ingame.IngameDebugHUD;
import com.almuradev.almura.client.gui.ingame.IngameDied;
import com.almuradev.almura.client.gui.ingame.IngameHUD;
import com.almuradev.almura.client.gui.ingame.IngameOptions;
import com.almuradev.almura.client.gui.ingame.residence.IngameResidenceHUD;
import com.almuradev.almura.client.gui.menu.DynamicMainMenu;
import com.almuradev.almura.client.network.play.B00PlayerDeathConfirmation;
import com.almuradev.almura.client.renderer.accessories.AccessoryManager;
import com.almuradev.almura.pack.block.PackBlock;
import com.almuradev.almura.pack.container.PackContainerBlock;
import com.almuradev.almura.pack.crop.PackCrops;
import com.almuradev.almura.pack.renderer.BlockRenderer;
import com.almuradev.almura.pack.renderer.ItemRenderer;
import com.almuradev.almura.server.network.play.bukkit.B00PlayerDisplayName;
import com.almuradev.almura.server.network.play.bukkit.B01PlayerCurrency;
import com.almuradev.almura.server.network.play.bukkit.B02AdditionalWorldInformation;
import com.almuradev.almura.server.network.play.bukkit.B03ResidenceInformation;
import com.almuradev.almura.server.network.play.bukkit.B04PlayerAccessories;
import com.almuradev.almurasdk.lang.LanguageRegistry;
import com.almuradev.almurasdk.lang.Languages;
import com.google.common.base.Optional;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent.KeyInputEvent;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public class ClientProxy extends CommonProxy {

    public static final String CLASSPATH = "com.almuradev.almura.client.ClientProxy";
    public static final BlockRenderer PACK_BLOCK_RENDERER = new BlockRenderer();
    public static final ItemRenderer PACK_ITEM_RENDERER = new ItemRenderer();

    public static final KeyBinding BINDING_CONFIG_GUI =
            new AlmuraBinding("key.almura.config", "Config", Keyboard.KEY_F4, "key.categories.almura", "Almura");

    public static final KeyBinding BINDING_OPEN_BACKPACK =
            new AlmuraBinding("key.almura.backpack", "Backpack", Keyboard.KEY_B, "key.categories.almura", "Almura");

    public static IngameDebugHUD HUD_DEBUG;
    public static IngameHUD HUD_INGAME;
    public static IngameResidenceHUD HUD_RESIDENCE;
    public static final SimpleNetworkWrapper NETWORK_BUKKIT = new SimpleNetworkWrapper("AM|BUK");

    public static void setIngameHUD() {
        if (Configuration.DISPLAY_ENHANCED_GUI && HUD_INGAME == null) {
            HUD_INGAME = new IngameHUD();
            HUD_INGAME.displayOverlay();
        } else if (!Configuration.DISPLAY_ENHANCED_GUI && HUD_INGAME != null) {
            HUD_INGAME.closeOverlay();
            HUD_INGAME = null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onPreInitialization(FMLPreInitializationEvent event) {
        super.onPreInitialization(event);
        if (Configuration.FIRST_LAUNCH) {
            Configuration.setOptimizedConfig();
        }
        // Register handlers for Bukkit packets
        NETWORK_BUKKIT.registerMessage(B00PlayerDisplayName.class, B00PlayerDisplayName.class, 0, Side.CLIENT);
        NETWORK_BUKKIT.registerMessage(B01PlayerCurrency.class, B01PlayerCurrency.class, 1, Side.CLIENT);
        NETWORK_BUKKIT.registerMessage(B02AdditionalWorldInformation.class, B02AdditionalWorldInformation.class, 2, Side.CLIENT);
        NETWORK_BUKKIT.registerMessage(B03ResidenceInformation.class, B03ResidenceInformation.class, 3, Side.CLIENT);
        NETWORK_BUKKIT.registerMessage(B04PlayerAccessories.class, B04PlayerAccessories.class, 4, Side.CLIENT);
        NETWORK_BUKKIT.registerMessage(B05GuiController.class, B05GuiController.class, 5, Side.CLIENT);
        // Register outgoing Bukkit packets. While this also registers a handler, we ignore this (FML requires a handler...)
        NETWORK_BUKKIT.registerMessage(B00PlayerDeathConfirmation.class, B00PlayerDeathConfirmation.class, 0, Side.SERVER);
        NETWORK_BUKKIT.registerMessage(B01ResTokenConfirmation.class, B01ResTokenConfirmation.class, 1, Side.SERVER);
        PACK_BLOCK_RENDERER.registerFor(PackBlock.class);
        PACK_BLOCK_RENDERER.registerFor(PackCrops.class);
        PACK_BLOCK_RENDERER.registerFor(PackContainerBlock.class);
        ClientRegistry.registerKeyBinding(BINDING_CONFIG_GUI);
        ClientRegistry.registerKeyBinding(BINDING_OPEN_BACKPACK);
        FMLCommonHandler.instance().bus().register(this);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onGuiOpenEvent(GuiOpenEvent event) {
        if (event.gui instanceof GuiMainMenu) {
            if (Configuration.FIRST_LAUNCH) {
                Configuration.setOptimizedConfig();
                try {
                    Configuration.setFirstLaunch(false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            event.setCanceled(true);
            new DynamicMainMenu(null).display();

        }

        if (event.gui instanceof GuiIngameMenu) {
            event.setCanceled(true);
            new IngameOptions().display();
        }

        if (event.gui instanceof GuiGameOver) {
            event.setCanceled(true);
            new IngameDied().display();

        }
    }

    @SubscribeEvent
    public void onKeyInputEvent(KeyInputEvent event) {
        if (Keyboard.isKeyDown(BINDING_OPEN_BACKPACK.getKeyCode())) {
            Minecraft.getMinecraft().thePlayer.sendQueue.addToSendQueue(new C01PacketChatMessage("/backpack"));
        }
    }

    @SubscribeEvent
    public void onTextureStitchEventPre(TextureStitchEvent.Pre event) {
        Almura.LOGGER
                .info("This computer can handle a maximum stitched texture size of width [" + Minecraft.getGLMaximumTextureSize() + "] and length ["
                        + Minecraft.getGLMaximumTextureSize() + "].");
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onNameFormatEvent(net.minecraftforge.event.entity.player.PlayerEvent.NameFormat event) {
        final Optional<String> displayNameOpt = DisplayNameManager.getDisplayName(event.username);
        if (displayNameOpt.isPresent()) {
            event.displayname = displayNameOpt.get();
        }
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public void onRenderPlayerSpecialPostEvent(RenderPlayerEvent.Specials.Post event) {
        AccessoryManager.onRenderPlayerSpecialEventPost(event);
    }

    @SubscribeEvent
    public void onRenderGameOverlayEventPost(RenderGameOverlayEvent.Pre event) {
        // Toggle enhanced hud off/on.
        if (Configuration.DISPLAY_ENHANCED_GUI) {
            switch (event.type) {
                case HEALTH:
                case ARMOR:
                case FOOD:
                case EXPERIENCE:
                    event.setCanceled(true);
                    break;
                default:
            }
            if (HUD_INGAME == null) {
                HUD_INGAME = new IngameHUD();
            }

            HUD_INGAME.displayOverlay();
        } else if (HUD_INGAME != null) {
            HUD_INGAME.closeOverlay();
            HUD_INGAME = null;
        }

        // Toggle residence off/on based on config. Creation of this HUD happens in the packet handler from Bukkit
        if (Configuration.DISPLAY_RESIDENCE_HUD && HUDData.WITHIN_RESIDENCE) {
            if (HUD_RESIDENCE == null) {
                HUD_RESIDENCE = new IngameResidenceHUD();
            }

            HUD_RESIDENCE.displayOverlay();
            HUD_RESIDENCE.updateWidgets();
        } else if (HUD_RESIDENCE != null) {
            HUD_RESIDENCE.closeOverlay();
            HUD_RESIDENCE = null;
        }

        // Toggle enhanced debug off/on based on config.
        if (event.type == ElementType.DEBUG) {
            if (Configuration.DISPLAY_ENHANCED_DEBUG) {
                event.setCanceled(true);
                if (HUD_DEBUG == null) {
                    HUD_DEBUG = new IngameDebugHUD();
                }

                HUD_DEBUG.displayOverlay();
            } else if (HUD_DEBUG != null) {
                HUD_DEBUG.closeOverlay();
                HUD_DEBUG = null;
            }
        }

        // User turned off debug mode, handle it.
        if (!Minecraft.getMinecraft().gameSettings.showDebugInfo && HUD_DEBUG != null) {
            HUD_DEBUG.closeOverlay();
            HUD_DEBUG = null;
        }
    }
}

final class AlmuraBinding extends KeyBinding {

    public AlmuraBinding(String unlocalizedIdentifier, String name, int keycode, String unlocalizedCategory, String category) {
        super(unlocalizedIdentifier, keycode, unlocalizedCategory);

        LanguageRegistry.put(Languages.ENGLISH_AMERICAN, unlocalizedIdentifier, name);
        LanguageRegistry.put(Languages.ENGLISH_AMERICAN, unlocalizedCategory, category);
    }
}
