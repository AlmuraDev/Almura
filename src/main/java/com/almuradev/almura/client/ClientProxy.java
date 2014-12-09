/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client;

import com.almuradev.almura.Almura;
import com.almuradev.almura.CommonProxy;
import com.almuradev.almura.client.gui.AlmuraMainMenu;
import com.almuradev.almura.client.gui.ingame.IngameConfig;
import com.almuradev.almura.client.gui.ingame.IngameDebugHUD;
import com.almuradev.almura.client.gui.ingame.IngameHUD;
import com.almuradev.almura.lang.LanguageRegistry;
import com.almuradev.almura.lang.Languages;
import com.almuradev.almura.pack.IBlockShapeContainer;
import com.almuradev.almura.pack.IShapeContainer;
import com.almuradev.almura.pack.Pack;
import com.almuradev.almura.pack.block.PackBlock;
import com.almuradev.almura.pack.renderer.BlockRenderer;
import com.almuradev.almura.pack.renderer.ItemRenderer;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;

public class ClientProxy extends CommonProxy {

    public static final String CLASSPATH = "com.almuradev.almura.client.ClientProxy";
    public static final BlockRenderer PACK_BLOCK_RENDERER = new BlockRenderer();
    public static final ItemRenderer PACK_ITEM_RENDERER = new ItemRenderer();

    public static final KeyBinding
            BINDING_CONFIG_GUI =
            new AlmuraBinding("key.almura.config", "Config", Keyboard.KEY_F4, "key.categories.almura", "Almura");

    @Override
    public void onPreInitialization(FMLPreInitializationEvent event) {
        super.onPreInitialization(event);
        PACK_BLOCK_RENDERER.registerFor(PackBlock.class);
        ClientRegistry.registerKeyBinding(BINDING_CONFIG_GUI);
        FMLCommonHandler.instance().bus().register(this);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void onPostInitialization(FMLPostInitializationEvent event) {
        super.onPostInitialization(event);
        MinecraftForge.EVENT_BUS.register(this);
        final IngameHUD almuraHud = new IngameHUD();
        final IngameDebugHUD almuraDebugHud = new IngameDebugHUD();
        MinecraftForge.EVENT_BUS.register(almuraHud);
        MinecraftForge.EVENT_BUS.register(almuraDebugHud);
        FMLCommonHandler.instance().bus().register(almuraHud);
        FMLCommonHandler.instance().bus().register(almuraDebugHud);
    }

    @Override
    public void onCreate(Pack pack) {
        pack.injectShapes();
        super.onCreate(pack);
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (event.gui instanceof GuiMainMenu) {
            event.gui = new AlmuraMainMenu();
        }
    }

    @SubscribeEvent
    public void onKeyInput(KeyInputEvent event) {
        if (BINDING_CONFIG_GUI.isPressed()) {
            final IngameConfig form = new IngameConfig();
            form.display();
        } else if (Keyboard.isKeyDown(Keyboard.KEY_F3)) {
            IngameDebugHUD.UPDATES_ENABLED = !IngameDebugHUD.UPDATES_ENABLED;
        }
    }

    @SubscribeEvent
    public void onTextureStitchEventPre(TextureStitchEvent.Pre event) {
        Almura.LOGGER
                .info("This computer can handle a maximum stitched texture size of width [" + Minecraft.getGLMaximumTextureSize() + "] and length ["
                      + Minecraft.getGLMaximumTextureSize() + "].");
    }
}

final class AlmuraBinding extends KeyBinding {

    public AlmuraBinding(String unlocalizedIdentifier, String name, int keycode, String unlocalizedCategory, String category) {
        super(unlocalizedIdentifier, keycode, unlocalizedCategory);

        LanguageRegistry.put(Languages.ENGLISH_AMERICAN, unlocalizedIdentifier, name);
        LanguageRegistry.put(Languages.ENGLISH_AMERICAN, unlocalizedCategory, category);
    }
}