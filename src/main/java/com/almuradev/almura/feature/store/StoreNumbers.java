/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.store;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Numbers as 3.5's stores showed and took them.
 */
public final class StoreNumbers {

    /** A listing's quantity when there is no end to it. */
    public static final int UNLIMITED = -1;
    /** The most a price or quantity may be, as 3.5's. */
    public static final long TEN_MILLION = 10_000_000L;

    private static final double ONE_MILLION = 1_000_000.0, ONE_BILLION = 1_000_000_000.0, ONE_TRILLION = 1_000_000_000_000.0;
    private static final ThreadLocal<DecimalFormat> CURRENCY = ThreadLocal.withInitial(() ->
        new DecimalFormat("#,###.##", DecimalFormatSymbols.getInstance(Locale.ROOT)));

    private StoreNumbers() {
    }

    /**
     * @return the amount as {@code #,###.##}
     */
    public static String currency(final double value) {
        return CURRENCY.get().format(value);
    }

    /**
     * @return the amount as {@code #,###.##}, from a million on in millions (m), billions (b) and trillions (t)
     */
    public static String withSuffix(final double value) {
        if (value < ONE_MILLION) {
            return currency(value);
        } else if (value < ONE_BILLION) {
            return currency(value / ONE_MILLION) + "m";
        } else if (value < ONE_TRILLION) {
            return currency(value / ONE_BILLION) + "b";
        }
        return currency(value / ONE_TRILLION) + "t";
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

    /**
     * Digits and one decimal point (at most {@code maxDecimals} after it), and a leading minus if allowed; nothing past ten million.
     */
    public static String filterNumber(final String text, final int maxDecimals, final boolean allowNegative) {
        final StringBuilder filtered = new StringBuilder();
        boolean point = false;
        int decimals = 0;
        for (final char c : text.toCharArray()) {
            if (c == '-' && allowNegative && filtered.isEmpty()) {
                filtered.append(c);
            } else if (c == '.' && !point && maxDecimals > 0) {
                point = true;
                filtered.append(c);
            } else if (Character.isDigit(c) && (!point || decimals < maxDecimals)) {
                filtered.append(c);
                if (point) {
                    decimals++;
                }
            }
        }
        final String result = filtered.toString();
        final BigDecimal value = parse(result.equals("-") ? "0" : result);
        if (value.abs().longValue() > TEN_MILLION) {
            return result.substring(0, result.length() - 1);
        }
        return result;
    }
}
