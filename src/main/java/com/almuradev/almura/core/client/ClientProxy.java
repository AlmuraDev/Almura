/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.core.client;

import com.almuradev.almura.CommonProxy;
import com.almuradev.almura.Constants;
import com.almuradev.shared.client.resource.CustomFolderResourcePack;
import com.google.inject.Injector;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourcePack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;

import java.util.List;

/**
 * The client platform of Almura. All code not meant to run on a dedicated server should go here.
 */
@SideOnly(Side.CLIENT)
public final class ClientProxy extends CommonProxy {

    @Override
    protected Injector createInjector(final Injector parent) {
        return parent.createChildInjector(new ClientModule());
    }

    @Override
    public void onGamePreInitialization(GamePreInitializationEvent event) {
        super.onGamePreInitialization(event);

        // Must be a better way to go about this...
        final List<IResourcePack> resourcePacks = Minecraft.getMinecraft().defaultResourcePacks;
        resourcePacks.add(new CustomFolderResourcePack("CustomFolderResourcePack: " + Constants.Plugin.NAME, Constants.FileSystem
                .PATH_ASSETS_ALMURA_30, Constants.Plugin.ID));
        Minecraft.getMinecraft().refreshResources();

        MinecraftForge.EVENT_BUS.register(this);
    }
}
