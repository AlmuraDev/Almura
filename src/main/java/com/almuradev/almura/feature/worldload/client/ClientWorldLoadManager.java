/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.worldload.client;

import com.almuradev.almura.feature.worldload.client.gui.WorldLoadingScreen;
import com.almuradev.core.event.Witness;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Tracks the worlds the server is loading and shows {@link WorldLoadingScreen} while it does.
 *
 * <p>The popup opens on top of whatever screen is up, which comes back when it closes, except over container screens (closing the
 * popup would close the container on the server): then it opens once the container is closed. Closing it hides it until the next
 * world starts loading. It closes itself once every world has finished loading.</p>
 */
@SideOnly(Side.CLIENT)
@Singleton
public final class ClientWorldLoadManager implements Witness {

    private final Logger logger;
    private final Set<String> loadingWorlds = new LinkedHashSet<>();
    private long loadStartedAt;
    private boolean dismissed;

    @Inject
    private ClientWorldLoadManager(final Logger logger) {
        this.logger = logger;
    }

    @SubscribeEvent
    public void onClientConnectedToServer(final FMLNetworkEvent.ClientConnectedToServerEvent event) {
        // Network thread
        Minecraft.getMinecraft().addScheduledTask(this::reset);
    }

    @SubscribeEvent
    public void onClientTick(final TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || this.loadingWorlds.isEmpty() || this.dismissed) {
            return;
        }

        final Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.player == null || minecraft.world == null || minecraft.currentScreen instanceof WorldLoadingScreen) {
            return;
        }

        // Closing the popup over a container screen would close the container on the server, so wait for it to be closed first
        if (minecraft.player.openContainer != minecraft.player.inventoryContainer) {
            return;
        }

        // Other screens (e.g. a dialing device) stay behind the popup and come back when it closes
        // display() (not displayGuiScreen) so the Malisis screen sets its resolution and constructs its components first
        new WorldLoadingScreen(minecraft.currentScreen, this).display();
    }

    public void onLoadStarted(final String worldName) {
        this.logger.info("Server is loading world '{}'.", worldName);
        if (this.loadingWorlds.isEmpty()) {
            this.loadStartedAt = System.currentTimeMillis();
        }
        this.loadingWorlds.add(worldName);
        this.dismissed = false;
        // Opened (or refreshed) by the tick handler
    }

    public void onLoadFinished(final String worldName) {
        this.logger.info("Server finished loading world '{}'.", worldName);
        this.loadingWorlds.remove(worldName);
        if (!this.loadingWorlds.isEmpty()) {
            return;
        }

        final Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.currentScreen instanceof WorldLoadingScreen) {
            ((WorldLoadingScreen) minecraft.currentScreen).close();
        }
        this.dismissed = false;
    }

    /**
     * Called when the player closes the popup themselves.
     */
    public void dismiss() {
        if (!this.loadingWorlds.isEmpty()) {
            this.dismissed = true;
        }
    }

    public List<String> getLoadingWorlds() {
        return new ArrayList<>(this.loadingWorlds);
    }

    public long getLoadStartedAt() {
        return this.loadStartedAt;
    }

    private void reset() {
        this.loadingWorlds.clear();
        this.dismissed = false;
    }
}
