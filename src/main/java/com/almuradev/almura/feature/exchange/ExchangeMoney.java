/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * Money and amounts as the exchange shows them, as 3.5's {@code FeatureConstants}: {@code #,###.##}, with m, b and t past a million.
 */
public final class ExchangeMoney {

    public static final BigDecimal ONE_TRILLION = new BigDecimal("1000000000000");
    private static final double ONE_MILLION = 1_000_000d, ONE_BILLION = 1_000_000_000d, TRILLION = 1_000_000_000_000d;
    private static final ThreadLocal<DecimalFormat> FORMAT = ThreadLocal.withInitial(() -> new DecimalFormat("#,###.##"));

    private ExchangeMoney() {
    }

    public static String format(final double value) {
        return FORMAT.get().format(value);
    }

    public static String format(final BigDecimal value) {
        return FORMAT.get().format(value);
    }

    public static String withSuffix(final double value) {
        if (value < ONE_MILLION) {
            return format(value);
        } else if (value < ONE_BILLION) {
            return format(value / ONE_MILLION) + "m";
        } else if (value < TRILLION) {
            return format(value / ONE_BILLION) + "b";
        }
        return format(value / TRILLION) + "t";
    }

    /**
     * @return the amount sent as text, or zero if it isn't a number
     */
    public static BigDecimal parse(final String amount) {
        try {
            return new BigDecimal(amount);
        } catch (final NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
}
