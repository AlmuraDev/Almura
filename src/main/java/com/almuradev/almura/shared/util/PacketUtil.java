/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.util;

import static com.google.common.base.Preconditions.checkNotNull;

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
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

public final class PacketUtil {

    private static final BigDecimal FRACTIONAL_OP = BigDecimal.valueOf((1 / (Math.pow(2, 256))));
    private static final MathContext PRECISE_HALF_EVEN = new MathContext(0, RoundingMode.HALF_UP);

    public static <M extends Message> boolean checkThreadAndEnqueue(IThreadListener scheduler, final M message, final MessageHandler<M> handler,
            RemoteConnection connection, Platform.Type side) throws ThreadQuickExitException {
        if (!scheduler.isCallingFromMinecraftThread()) {
            scheduler.addScheduledTask(() -> handler.handleMessage(message, connection, side));
            return false;
        }

        return true;
    }

    public static byte[] objectToBytes(final Serializable value) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            final ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(value);
            out.flush();
            return bos.toByteArray();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T bytesToObject(final byte[] bytes) throws IOException, ClassNotFoundException {
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
        final BigInteger intValue = new BigInteger(data);
        final BigDecimal decimalValue = new BigDecimal(intValue);
        return decimalValue.multiply(FRACTIONAL_OP);
    }

    private PacketUtil() {}
}
