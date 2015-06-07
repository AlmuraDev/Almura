/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client;

import com.almuradev.almura.Almura;
import com.almuradev.almura.CommonProxy;
import com.almuradev.almura.Configuration;
import com.almuradev.almura.client.gui.ingame.IngameDebugHUD;
import com.almuradev.almura.client.gui.ingame.IngameHUD;
import com.almuradev.almura.client.gui.ingame.residence.IngameResidenceHUD;
import com.almuradev.almura.client.gui.menu.DynamicMainMenu;
import com.almuradev.almura.client.renderer.accessories.AccessoryManager;
import com.almuradev.almura.extension.entity.IExtendedEntityLivingBase;
import com.almuradev.almura.pack.block.PackBlock;
import com.almuradev.almura.pack.container.PackContainerBlock;
import com.almuradev.almura.pack.crop.PackCrops;
import com.almuradev.almura.pack.renderer.BlockRenderer;
import com.almuradev.almura.pack.renderer.ItemRenderer;
import com.almuradev.almurasdk.lang.LanguageRegistry;
import com.almuradev.almurasdk.lang.Languages;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
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
    private static boolean isInitialized = false;

    @Override
    @SuppressWarnings("unchecked")
    public void onPreInitialization(FMLPreInitializationEvent event) {
        super.onPreInitialization(event);
        if (Configuration.FIRST_LAUNCH) {
            Configuration.setOptimizedConfig();
        }
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
            event.gui = new DynamicMainMenu(null);
        }
    }

    @SubscribeEvent
    public void onKeyInputEvent(KeyInputEvent event) {
        if (Keyboard.isKeyDown(Keyboard.KEY_F3)) {
            IngameDebugHUD.UPDATES_ENABLED = !IngameDebugHUD.UPDATES_ENABLED;
        } else if (Keyboard.isKeyDown(BINDING_OPEN_BACKPACK.getKeyCode())) {
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
        // Race condition check
        if (Minecraft.getMinecraft().theWorld == null) {
            return;
        }
        final EntityPlayer player = Minecraft.getMinecraft().theWorld.getPlayerEntityByName(event.username);

        if (((IExtendedEntityLivingBase) player).getServerName() != null && !((IExtendedEntityLivingBase) player).getServerName().isEmpty()) {
            event.displayname = ((IExtendedEntityLivingBase) player).getServerName();
        }
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public void onRenderPlayerSpecialPostEvent(RenderPlayerEvent.Specials.Post event) {
        AccessoryManager.onRenderPlayerSpecialEventPost(event);
    }

    @SubscribeEvent
    public void onRenderGameOverlayEventPost(RenderGameOverlayEvent.Pre event) {

        // In game HUD
        if (Configuration.DISPLAY_ENHANCED_GUI) {
            switch (event.type) {
                case HEALTH:
                case ARMOR:
                case FOOD:
                case EXPERIENCE:
                    // event.setCanceled(true);
                    break;
                default:
            }
            if (!isInitialized) {
                HUD_INGAME = new IngameHUD();
                HUD_INGAME.displayOverlay();
            }
        }

//        if (!isInitialized) {
//
//            HUD_DEBUG = new IngameDebugHUD();
//            HUD_RESIDENCE = new IngameResidenceHUD();
//            isInitialized = true;
//        }

        isInitialized = true;
    }
}

final class AlmuraBinding extends KeyBinding {

    public AlmuraBinding(String unlocalizedIdentifier, String name, int keycode, String unlocalizedCategory, String category) {
        super(unlocalizedIdentifier, keycode, unlocalizedCategory);

        LanguageRegistry.put(Languages.ENGLISH_AMERICAN, unlocalizedIdentifier, name);
        LanguageRegistry.put(Languages.ENGLISH_AMERICAN, unlocalizedCategory, category);
    }
}
