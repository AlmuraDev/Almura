/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.network;

import com.almuradev.almura.Almura;
import com.almuradev.almura.network.play.S00WorldInformation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;

public class NetworkHandlers {
    @SideOnly(Side.CLIENT)
    public static final class S00WorldInformationHandler implements MessageHandler<S00WorldInformation> {
        @Override
        public void handleMessage(S00WorldInformation message, RemoteConnection connection, Platform.Type side) {
            if (side.isClient()) {
                Almura.getInstance().logger.error(message.worldName);
            }
        }
    }
}
