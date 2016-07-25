/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura;

import com.almuradev.almura.api.CreativeTab;
import com.almuradev.almura.api.block.BuildableBlockType;
import com.almuradev.almura.client.ClientProxy;
import com.almuradev.almura.server.ServerProxy;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraftforge.fml.common.SidedProxy;
import org.slf4j.Logger;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameConstructionEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;

import javax.inject.Inject;

@Plugin(id = Almura.PLUGIN_ID)
public class Almura {

    public static final String PLUGIN_ID = "almura", GUI_VERSION = "3.0", PACK_VERSION = "1.5";

    public static Almura instance;

    @SidedProxy(clientSide = ClientProxy.CLASSPATH, serverSide = ServerProxy.CLASSPATH)
    public static CommonProxy proxy;

    @Inject public Logger logger;
    @Inject public PluginContainer container;

    @Listener
    public void onGameConstruction(GameConstructionEvent event) {
        instance = this;
    }

    @Listener
    public void onGamePreInitialization(GamePreInitializationEvent event) {

        proxy.onGamePreInitialization(event);

        // TEST CODE
        BuildableBlockType.builder()
                .unlocalizedName("USA")
                .creativeTab((CreativeTab) net.minecraft.creativetab.CreativeTabs.FOOD)
                .material(Material.ANVIL)
                .mapColor(MapColor.ADOBE)
                .build("USA");
    }
}
