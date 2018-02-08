/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura;

import com.google.inject.Injector;
import net.minecraftforge.fml.common.SidedProxy;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameConstructionEvent;
import org.spongepowered.api.event.game.state.GameStoppingEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;

import javax.inject.Inject;

@Plugin(id = Almura.ID, dependencies = {
        @Dependency(id = "nucleus", optional = true)
})
public class Almura {

    public static Almura instance;

    public static final String ID = "almura";
    public static final String NAME = "Almura";
    public static boolean debug = true;

    @SidedProxy(
            modId = Almura.ID,
            clientSide = "com.almuradev.almura.core.client.ClientProxy",
            serverSide = "com.almuradev.almura.core.server.ServerProxy"
    )
    private static CommonProxy proxy;
    @Inject private Injector injector;

    @Listener
    public void onGameConstruction(GameConstructionEvent event) {
        proxy.construct(this.injector);
    }

    @Listener
    public void gameStopping(final GameStoppingEvent event) {
        proxy.destruct();
    }
}
