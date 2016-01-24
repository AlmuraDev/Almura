/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.network;

import com.almuradev.almura.Almura;
import com.almuradev.almura.util.ThreadUtil;
import com.almuradev.almura.network.play.S00WorldInformation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;

/**
 * A unified location of all handlers for Almura's network messages. If you are adding a handler to this class,
 * keep in mind that no handler is thread-safe by default. If you are to modify any Minecraft game object, use
 * {@link ThreadUtil#executeOnClientThread(Runnable)} or {@link ThreadUtil#executeOnServerThread(String, Runnable)}.
 */
public final class NetworkHandlers {

    private NetworkHandlers() {}

    @SideOnly(Side.CLIENT)
    public static final class S00WorldInformationHandler implements MessageHandler<S00WorldInformation> {
        @Override
        public void handleMessage(S00WorldInformation message, RemoteConnection connection, Platform.Type side) {
            if (side.isClient()) {
                ThreadUtil.executeOnClientThread(() -> Almura.getInstance().logger.error(message.worldName));
            }
        }
    }
}
