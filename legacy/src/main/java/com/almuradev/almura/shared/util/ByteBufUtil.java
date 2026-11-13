/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.util;

import io.netty.buffer.ByteBuf;

import java.math.BigDecimal;
import java.math.BigInteger;

public final class ByteBufUtil {

    private ByteBufUtil() {}

    public static BigDecimal readBigDecimal(final ByteBuf buf) {
        final byte[] data = new byte[buf.readInt()];
        buf.readBytes(data);
        return new BigDecimal(new BigInteger(data), buf.readInt());

    }

    public static ByteBuf writeBigDecimal(final ByteBuf buf, final BigDecimal value) {
        final BigInteger a = value.unscaledValue();
        final byte[] aData = a.toByteArray();

        buf.writeInt(aData.length);
        buf.writeBytes(aData);
        buf.writeInt(value.scale());

        return buf;
    }
}
