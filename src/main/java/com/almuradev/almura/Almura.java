/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura;

import com.almuradev.almura.api.block.BuildableBlockType;
import com.almuradev.almura.block.builder.BlockTypeBuilder;
import com.almuradev.almura.client.ClientProxy;
import com.almuradev.almura.server.ServerProxy;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.fml.common.SidedProxy;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameConstructionEvent;
import org.spongepowered.api.event.game.state.GameLoadCompleteEvent;
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

        Sponge.getRegistry().registerBuilderSupplier(BuildableBlockType.Builder.class, BlockTypeBuilder::new);

        // TEST CODE
        BuildableBlockType.builder()
                .creativeTab(CreativeTabs.FOOD)
                .material(Material.ANVIL)
                .mapColor(MapColor.ADOBE)
                .build("test");
    }

    @Listener
    public void onGameLoadComplete(GameLoadCompleteEvent event) {
        // TEST CODE
        Sponge.getRegistry().getType(BlockType.class, "almura:test").ifPresent(System.err::println);
    }
}
