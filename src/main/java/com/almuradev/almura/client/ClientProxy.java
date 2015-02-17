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
import com.almuradev.almura.client.gui.menu.AlmuraMainMenu;
import com.almuradev.almura.client.renderer.accessories.AccessoryManager;
import com.almuradev.almura.lang.LanguageRegistry;
import com.almuradev.almura.lang.Languages;
import com.almuradev.almura.pack.block.PackBlock;
import com.almuradev.almura.pack.container.PackContainerBlock;
import com.almuradev.almura.pack.crop.PackCrops;
import com.almuradev.almura.pack.renderer.BlockRenderer;
import com.almuradev.almura.pack.renderer.ItemRenderer;
import com.flowpowered.cerealization.config.ConfigurationException;
import com.flowpowered.cerealization.config.ConfigurationNode;
import com.google.common.collect.Maps;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;

import org.lwjgl.input.Keyboard;

import java.util.Map;

public class ClientProxy extends CommonProxy {

    public static final String CLASSPATH = "com.almuradev.almura.client.ClientProxy";
    public static final BlockRenderer PACK_BLOCK_RENDERER = new BlockRenderer();
    public static final ItemRenderer PACK_ITEM_RENDERER = new ItemRenderer();
    public static final Map<String, String> PLAYER_DISPLAY_NAME_MAP = Maps.newHashMap();

    public static final KeyBinding
            BINDING_CONFIG_GUI =
            new AlmuraBinding("key.almura.config", "Config", Keyboard.KEY_F4, "key.categories.almura", "Almura");

    public static final KeyBinding
            BINDING_OPEN_BACKPACK =
            new AlmuraBinding("key.almura.backpack", "Backpack", Keyboard.KEY_B, "key.categories.almura", "Almura");

    public static IngameDebugHUD HUD_DEBUG;
    public static IngameHUD HUD_INGAME;
    public static IngameResidenceHUD HUD_RESIDENCE;

    @Override
    @SuppressWarnings("unchecked")
    public void onPreInitialization(FMLPreInitializationEvent event) {
        super.onPreInitialization(event);
        PACK_BLOCK_RENDERER.registerFor(PackBlock.class);
        PACK_BLOCK_RENDERER.registerFor(PackCrops.class);
        PACK_BLOCK_RENDERER.registerFor(PackContainerBlock.class);
        ClientRegistry.registerKeyBinding(BINDING_CONFIG_GUI);
        ClientRegistry.registerKeyBinding(BINDING_OPEN_BACKPACK);
        FMLCommonHandler.instance().bus().register(this);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void onInitialization(FMLInitializationEvent event) {
        super.onInitialization(event);
        HUD_DEBUG = new IngameDebugHUD();
        HUD_INGAME = new IngameHUD();
        HUD_RESIDENCE = new IngameResidenceHUD();
    }

    @SubscribeEvent
    public void onGuiOpenEvent(GuiOpenEvent event) {
        if (event.gui instanceof GuiMainMenu) {
        	if (Configuration.FIRST_LAUNCH) {
        		Configuration.setOptimizedConfig();
        		Configuration.setFirstLaunched(false);
        	}
        	event.gui = new AlmuraMainMenu(null);
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
        final String displayName = PLAYER_DISPLAY_NAME_MAP.get(event.username);

        if (displayName != null && !displayName.isEmpty()) {
            event.displayname = displayName;

            if (event.entityPlayer == Minecraft.getMinecraft().thePlayer) {
                HUD_INGAME.playerTitle.setText(displayName);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public void onRenderPlayerSpecialPostEvent(RenderPlayerEvent.Specials.Post event) {
        AccessoryManager.onRenderPlayerSpecialEventPost(event);
    }
}

final class AlmuraBinding extends KeyBinding {

    public AlmuraBinding(String unlocalizedIdentifier, String name, int keycode, String unlocalizedCategory, String category) {
        super(unlocalizedIdentifier, keycode, unlocalizedCategory);

        LanguageRegistry.put(Languages.ENGLISH_AMERICAN, unlocalizedIdentifier, name);
        LanguageRegistry.put(Languages.ENGLISH_AMERICAN, unlocalizedCategory, category);
    }
}