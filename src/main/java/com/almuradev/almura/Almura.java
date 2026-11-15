/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura;

import com.almuradev.almura.content.AlmuraContent;
import com.almuradev.almura.core.database.AlmuraServerConfig;
import com.almuradev.almura.feature.bank.server.ServerBank;
import com.almuradev.almura.feature.economy.Economy;
import com.almuradev.almura.feature.guide.server.ServerGuide;
import com.almuradev.almura.feature.hud.server.ServerHud;
import com.almuradev.almura.feature.notification.server.ServerNotifications;
import com.almuradev.almura.network.AlmuraNetwork;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
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
        // The server's economy (Sponge's, such as EconomyLite's) for the HUD's money, when there is one
        Economy.install();
        NeoForge.EVENT_BUS.register(new ServerNotifications());
        NeoForge.EVENT_BUS.register(new ServerGuide());
        // The bank, on Almura's database (config/almura/server.toml) and the server's economy
        container.registerConfig(ModConfig.Type.LOCAL, AlmuraServerConfig.SPEC, AlmuraServerConfig.FILE);
        NeoForge.EVENT_BUS.register(new ServerBank());
    }
}
