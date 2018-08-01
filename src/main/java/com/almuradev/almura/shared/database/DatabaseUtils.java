/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.database;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

public final class DatabaseUtils {

    private DatabaseUtils() {
    }

    public static byte[] toBytes(final UUID uuid) {
        final byte[] bytes = new byte[16];
        ByteBuffer.wrap(bytes)
            .order(ByteOrder.BIG_ENDIAN)
            .putLong(uuid.getMostSignificantBits())
            .putLong(uuid.getLeastSignificantBits());
        return bytes;
    }

    public static UUID uniqueIdFromBytes(final byte[] data) {
        final ByteBuffer buf = ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN);
        return new UUID(buf.getLong(), buf.getLong());
    }

    public static NBTTagCompound compoundFromBytes(final byte[] data) throws IOException {
        checkNotNull(data);

        return CompressedStreamTools.read(ByteStreams.newDataInput(data), NBTSizeTracker.INFINITE);
    }

    public static byte[] toBytes(final NBTTagCompound compound) throws IOException {
        checkNotNull(compound);

        final ByteArrayDataOutput outputStream = ByteStreams.newDataOutput();
        CompressedStreamTools.write(compound, outputStream);

        return outputStream.toByteArray();
    }

    public static String toString(final ResourceLocation location) {
        checkNotNull(location);

        final String domain = location.getResourceDomain();
        final String path = location.getResourcePath();

        return domain + ":" + path;
    }

    public static ResourceLocation fromString(final String value) {
        checkNotNull(value);

        if (!value.contains(":")) {
            return null;
        }

        final int index = value.indexOf(":");

        return new ResourceLocation(value.substring(0, index), value.substring(index + 1, value.length()));
    }
}
