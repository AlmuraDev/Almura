/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.core.mixin.impl;

import com.almuradev.almura.core.mixin.Mixin;
import com.almuradev.almura.core.mixin.Overwrite;
import com.almuradev.almura.core.mixin.Shadow;
import com.google.common.base.Strings;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import cpw.mods.fml.client.CustomModLoadingErrorDisplayException;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.IModGuiFactory;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.DuplicateModsFoundException;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.IFMLSidedHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.LoaderException;
import cpw.mods.fml.common.MissingModsException;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.WrongMinecraftVersionException;
import cpw.mods.fml.common.toposort.ModSortingException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import org.apache.logging.log4j.Level;

import java.util.Map;

@Mixin(FMLClientHandler.class)
public abstract class MixinFMLClientHandler implements IFMLSidedHandler {
    @Shadow
    private MissingModsException modsMissing;

    @Shadow
    private WrongMinecraftVersionException wrongMC;

    @Shadow
    private CustomModLoadingErrorDisplayException customError;

    @Shadow
    private DuplicateModsFoundException dupesFound;

    @Shadow
    private ModSortingException modSorting;

    @Shadow
    private Minecraft client;

    @Shadow
    private BiMap<ModContainer, IModGuiFactory> guiFactories;

    @Shadow
    private boolean loading = true;

    @Overwrite
    @SuppressWarnings({ "deprecation", "unchecked" })
    public void finishMinecraftLoading()
    {
        if (modsMissing != null || wrongMC != null || customError!=null || dupesFound!=null || modSorting!=null)
        {
            return;
        }
        try
        {
            Loader.instance().initializeMods();
        }
        catch (CustomModLoadingErrorDisplayException custom)
        {
            FMLLog.log(Level.ERROR, custom, "A custom exception was thrown by a mod, the game will now halt");
            customError = custom;
            return;
        }
        catch (LoaderException le)
        {
            haltGame("There was a severe problem during mod loading that has caused the game to fail", le);
            return;
        }

        //TODO Almura Start - Stop Double Load
        // Reload resources
        //client.refreshResources();

        RenderingRegistry.instance().loadEntityRenderers((Map<Class<? extends Entity>, Render>) RenderManager.instance.entityRenderMap);
        guiFactories = HashBiMap.create();
        for (ModContainer mc : Loader.instance().getActiveModList())
        {
            String className = mc.getGuiClassName();
            if (Strings.isNullOrEmpty(className))
            {
                continue;
            }
            try
            {
                Class<?> clazz = Class.forName(className, true, Loader.instance().getModClassLoader());
                Class<? extends IModGuiFactory> guiClassFactory = clazz.asSubclass(IModGuiFactory.class);
                IModGuiFactory guiFactory = guiClassFactory.newInstance();
                guiFactory.initialize(client);
                guiFactories.put(mc, guiFactory);
            } catch (Exception e)
            {
                FMLLog.log(Level.ERROR, e, "A critical error occurred instantiating the gui factory for mod %s", mc.getModId());
            }
        }
        loading = false;
        client.gameSettings.loadOptions(); //Reload options to load any mod added keybindings.
    }
}
