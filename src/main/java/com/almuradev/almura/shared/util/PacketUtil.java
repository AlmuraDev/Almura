/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.util;

import net.minecraft.network.ThreadQuickExitException;
import net.minecraft.util.IThreadListener;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.Message;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;

public final class PacketUtil {

    public static <M extends Message> boolean checkThreadAndEnqueue(IThreadListener scheduler, final M message, final MessageHandler<M> handler,
            RemoteConnection connection, Platform.Type side) throws ThreadQuickExitException {
        if (!scheduler.isCallingFromMinecraftThread()) {
            scheduler.addScheduledTask(() -> handler.handleMessage(message, connection, side));
            return false;
        }

        return true;
    }

    private PacketUtil() {}
}
