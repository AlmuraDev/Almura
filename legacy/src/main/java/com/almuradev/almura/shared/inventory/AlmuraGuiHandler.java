/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * Forge allows only one {@link IGuiHandler} per mod, so every Almura container GUI registers here under its own id and this handler dispatches
 * on the id passed to {@code player.openGui}.
 */
public final class AlmuraGuiHandler implements IGuiHandler {

    /** Storage crates and chests. */
    public static final int STORAGE = 0;
    /** The seed processor machine. */
    public static final int SEED_PROCESSOR = 1;
    /** The kitchen slicer machine. */
    public static final int KITCHEN_SLICER = 2;
    /** The kitchen grinder machine. */
    public static final int KITCHEN_GRINDER = 3;
    /** The wash plant machine (only when IndustrialCraft 2 is installed). */
    public static final int WASHPLANT = 4;
    /** A harvesting sack held by the player (x = the hand). */
    public static final int HARVEST_SACK = 5;

    private static final Map<Integer, IGuiHandler> HANDLERS = new HashMap<>();
    private static boolean registered;

    private AlmuraGuiHandler() {
    }

    /**
     * Registers a handler for a GUI id, registering this dispatcher with Forge the first time.
     */
    public static synchronized void register(final Object mod, final int id, final IGuiHandler handler) {
        if (HANDLERS.putIfAbsent(id, handler) != null) {
            throw new IllegalStateException("A GUI handler is already registered for id " + id);
        }

        if (!registered) {
            NetworkRegistry.INSTANCE.registerGuiHandler(mod, new AlmuraGuiHandler());
            registered = true;
        }
    }

    @Nullable
    @Override
    public Object getServerGuiElement(final int ID, final EntityPlayer player, final World world, final int x, final int y, final int z) {
        final IGuiHandler handler = HANDLERS.get(ID);
        return handler == null ? null : handler.getServerGuiElement(ID, player, world, x, y, z);
    }

    @Nullable
    @Override
    public Object getClientGuiElement(final int ID, final EntityPlayer player, final World world, final int x, final int y, final int z) {
        final IGuiHandler handler = HANDLERS.get(ID);
        return handler == null ? null : handler.getClientGuiElement(ID, player, world, x, y, z);
    }
}
