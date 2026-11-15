/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.bank;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * Money as the bank shows it, as 3.5's: {@code #,##0.00}.
 */
public final class BankMoney {

    private static final ThreadLocal<DecimalFormat> FORMAT = ThreadLocal.withInitial(() -> new DecimalFormat("#,##0.00"));

    private BankMoney() {
    }

    public static String format(final BigDecimal amount) {
        return FORMAT.get().format(amount);
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
