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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public final class PacketUtil {

    public static <M extends Message> boolean checkThreadAndEnqueue(IThreadListener scheduler, final M message, final MessageHandler<M> handler,
            RemoteConnection connection, Platform.Type side) throws ThreadQuickExitException {
        if (!scheduler.isCallingFromMinecraftThread()) {
            scheduler.addScheduledTask(() -> handler.handleMessage(message, connection, side));
            return false;
        }

        return true;
    }

    public static byte[] asBytes(final Serializable object) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            final ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(object);
            out.flush();
            return bos.toByteArray();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T fromBytes(final byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        try (final ObjectInput in = new ObjectInputStream(bis)) {
            return (T) in.readObject();
        }
    }

    private PacketUtil() {}
}
