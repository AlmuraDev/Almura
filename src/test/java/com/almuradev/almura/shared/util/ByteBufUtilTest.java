/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.util;

import static com.google.common.base.Preconditions.checkState;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import java.math.BigDecimal;

public final class ByteBufUtilTest {

    @Test
    public void testBigDecimalSerialization() {
        ByteBuf buf = Unpooled.buffer();
        BigDecimal value = BigDecimal.valueOf(5);
        checkState(ByteBufUtil.readBigDecimal(ByteBufUtil.writeBigDecimal(buf, value)).doubleValue() == value.doubleValue());

        buf = Unpooled.buffer();
        value = BigDecimal.valueOf(5.5);
        checkState(ByteBufUtil.readBigDecimal(ByteBufUtil.writeBigDecimal(buf, value)).doubleValue() == value.doubleValue());

        buf = Unpooled.buffer();
        value = BigDecimal.valueOf(5);
        checkState(ByteBufUtil.readBigDecimal(ByteBufUtil.writeBigDecimal(buf, value)).doubleValue() == value.doubleValue());

        buf = Unpooled.buffer();
        value = BigDecimal.valueOf(5.5);
        checkState(ByteBufUtil.readBigDecimal(ByteBufUtil.writeBigDecimal(buf, value)).doubleValue() == value.doubleValue());

        buf = Unpooled.buffer();
        value = BigDecimal.valueOf(5.23598745268957);
        checkState(ByteBufUtil.readBigDecimal(ByteBufUtil.writeBigDecimal(buf, value)).doubleValue() == value.doubleValue());

        buf = Unpooled.buffer();
        value = BigDecimal.valueOf(Double.MAX_VALUE);
        checkState(ByteBufUtil.readBigDecimal(ByteBufUtil.writeBigDecimal(buf, value)).doubleValue() == value.doubleValue());

        buf = Unpooled.buffer();
        value = BigDecimal.valueOf(9.05);
        checkState(ByteBufUtil.readBigDecimal(ByteBufUtil.writeBigDecimal(buf, value)).doubleValue() == value.doubleValue());
    }
}
