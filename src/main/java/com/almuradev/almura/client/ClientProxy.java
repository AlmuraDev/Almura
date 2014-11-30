/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client;

import com.almuradev.almura.CommonProxy;
import com.almuradev.almura.client.gui.AlmuraMainMenu;
import com.almuradev.almura.client.gui.ingame.IngameDebugHUD;
import com.almuradev.almura.client.gui.ingame.IngameHUD;
import com.almuradev.almura.pack.IShapeContainer;
import com.almuradev.almura.pack.block.PackBlock;
import com.almuradev.almura.pack.block.PackModelBlock;
import com.almuradev.almura.pack.renderer.PackBlockRenderer;
import com.almuradev.almura.pack.renderer.PackItemRenderer;
import com.almuradev.almura.pack.renderer.PackModelRenderer;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;

public class ClientProxy extends CommonProxy {

    public static final String CLASSPATH = "com.almuradev.almura.client.ClientProxy";
    public static final PackBlockRenderer PACK_BLOCK_RENDERER = new PackBlockRenderer();
    public static final PackItemRenderer PACK_ITEM_RENDERER = new PackItemRenderer();
    public static final PackModelRenderer PACK_MODEL_RENDERER = new PackModelRenderer();

    @Override
    public void onPreInitialization(FMLPreInitializationEvent event) {
        super.onPreInitialization(event);
        PACK_BLOCK_RENDERER.registerFor(PackBlock.class);
        PACK_MODEL_RENDERER.registerFor(PackModelBlock.class);
    }

    @Override
    public void onPostInitialization(FMLPostInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        final IngameHUD almuraHud = new IngameHUD();
        final IngameDebugHUD almuraDebugHud = new IngameDebugHUD();
        MinecraftForge.EVENT_BUS.register(almuraHud);
        MinecraftForge.EVENT_BUS.register(almuraDebugHud);
        FMLCommonHandler.instance().bus().register(almuraHud);
        FMLCommonHandler.instance().bus().register(almuraDebugHud);
    }

    @Override
    public void onPostCreate(Block block) {
        super.onPostCreate(block);
        if (block instanceof IShapeContainer) {
            ((IShapeContainer) block).setShapeFromPack();
        }
    }

    @Override
    public void onPostCreate(Item item) {
        super.onPostCreate(item);
        if (item instanceof IShapeContainer) {
            ((IShapeContainer) item).setShapeFromPack();
            if (((IShapeContainer) item).getShape() != null) {
                PACK_ITEM_RENDERER.registerFor(item);
            }
        }
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (event.gui instanceof GuiMainMenu) {
            event.gui = new AlmuraMainMenu();
        }
    }
}
