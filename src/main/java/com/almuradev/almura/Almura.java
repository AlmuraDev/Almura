/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura;

import com.almuradev.almura.content.AlmuraContent;
import com.almuradev.almura.feature.hud.server.ServerHud;
import com.almuradev.almura.feature.notification.server.ServerNotifications;
import com.almuradev.almura.network.AlmuraNetwork;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Almura's NeoForge entry point, on both sides. The 1.12.2 features in legacy/ come back here as they are ported.
 */
@Mod(Almura.ID)
public final class Almura {

    public static final String ID = "almura";
    public static final Logger LOGGER = LoggerFactory.getLogger("Almura");

    public Almura(final IEventBus modBus, final ModContainer container) {
        LOGGER.info("Almura {} starting", container.getModInfo().getVersion());
        // Blocks, items and creative tabs from the content folders, as on 1.12
        AlmuraContent.register(modBus);
        AlmuraNetwork.register(modBus);
        // What the server does for Almura's players: the HUD's data, notifications
        NeoForge.EVENT_BUS.register(new ServerHud());
        NeoForge.EVENT_BUS.register(new ServerNotifications());
    }
}
