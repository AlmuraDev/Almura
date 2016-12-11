/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.network;

import com.almuradev.almura.client.gui.screen.ingame.hud.HUDData;
import com.almuradev.almura.network.play.SServerInformationMessage;
import com.almuradev.almura.network.play.SWorldInformationMessage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;

/**
 * A unified location of all handlers for Almura's network messages. If you are adding a handler to this class,
 * keep in mind that no handler is thread-safe by default and you *will* need to schedule it on either Server or Client thread
 */
public final class NetworkHandlers {

    private NetworkHandlers() {
    }

    @SideOnly(Side.CLIENT)
    public static final class SWorldInformationHandler implements MessageHandler<SWorldInformationMessage> {

        @Override
        public void handleMessage(SWorldInformationMessage message, RemoteConnection connection, Platform.Type side) {
            if (side.isClient()) {
                HUDData.WORLD_NAME = message.getWorldName();
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public static final class SServerInformationHandler implements MessageHandler<SServerInformationMessage> {

        @Override
        public void handleMessage(SServerInformationMessage message, RemoteConnection connection, Platform.Type side) {
            if (side.isClient()) {
                HUDData.SERVER_PLAYER_COUNT = message.playerCount;
                HUDData.SERVER_PLAYER_MAX_COUNT = message.maxPlayerCount;
            }
        }
    }
}
