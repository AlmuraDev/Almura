/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.util;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

public final class SerializationUtil {

    private static final BigDecimal FRACTIONAL_OP = BigDecimal.valueOf((1 / (Math.pow(2, 256))));
    private static final MathContext PRECISE_HALF_EVEN = new MathContext(0, RoundingMode.HALF_UP);

    private SerializationUtil() {
    }

    public static byte[] toBytes(final UUID uuid) {
        checkNotNull(uuid);

        final byte[] bytes = new byte[16];
        ByteBuffer.wrap(bytes)
            .order(ByteOrder.BIG_ENDIAN)
            .putLong(uuid.getMostSignificantBits())
            .putLong(uuid.getLeastSignificantBits());
        return bytes;
    }

    public static UUID uniqueIdFromBytes(final byte[] data) {
        checkNotNull(data);

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

    public static ResourceLocation fromString(String value) {
        checkNotNull(value);

        if (!value.contains(":")) {
            return null;
        }

        final int index = value.indexOf(":");

        return new ResourceLocation(value.substring(0, index), value.substring(index + 1, value.length()));
    }

    public static byte[] objectToBytes(final Serializable value) throws IOException {
        checkNotNull(value);

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            final ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(value);
            out.flush();
            return bos.toByteArray();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T bytesToObject(final byte[] bytes) throws IOException, ClassNotFoundException {
        checkNotNull(bytes);

        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        try (final ObjectInput in = new ObjectInputStream(bis)) {
            return (T) in.readObject();
        }
    }

    public static byte[] toBytes(final BigDecimal value) {
        checkNotNull(value);

        return value.divideToIntegralValue(FRACTIONAL_OP, PRECISE_HALF_EVEN).unscaledValue().toByteArray();
    }

    public static BigDecimal fromBytes(final byte[] data) {
        checkNotNull(data);

        final BigInteger intValue = new BigInteger(data);
        final BigDecimal decimalValue = new BigDecimal(intValue);
        return decimalValue.multiply(FRACTIONAL_OP);
    }
}
