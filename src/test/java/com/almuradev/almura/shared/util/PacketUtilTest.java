/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.util;

import static com.google.common.base.Preconditions.checkState;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.math.BigDecimal;

public final class PacketUtilTest {

    @Test
    public void testBigDecimalSerialization() {
        BigDecimal value = BigDecimal.valueOf(5);
        assertEquals(PacketUtil.fromBytes(PacketUtil.toBytes(value)).doubleValue(), value.doubleValue(), 0);

        value = BigDecimal.valueOf(5.5);
        checkState(PacketUtil.fromBytes(PacketUtil.toBytes(value)).doubleValue() == value.doubleValue());

        value = BigDecimal.valueOf(5.23598745268957);
        checkState(PacketUtil.fromBytes(PacketUtil.toBytes(value)).doubleValue() == value.doubleValue());

        value = BigDecimal.valueOf(Double.MAX_VALUE);
        checkState(PacketUtil.fromBytes(PacketUtil.toBytes(value)).doubleValue() == value.doubleValue());
    }
}
