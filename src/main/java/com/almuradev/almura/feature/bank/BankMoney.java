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
 * How the bank shows money: always with cents (1,234.50, 0.00), like the HUD.
 */
public final class BankMoney {

    private static final ThreadLocal<DecimalFormat> FORMAT = ThreadLocal.withInitial(() -> new DecimalFormat("#,##0.00"));

    private BankMoney() {
    }

    public static String format(final BigDecimal amount) {
        return FORMAT.get().format(amount);
    }
}
