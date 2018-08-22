/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange;

import java.text.DecimalFormat;

public interface ExchangeConstants {

    int UNLIMITED = -1;
    double MILLION = 1000000.0;
    double BILLION = 1000000000.0;
    DecimalFormat CURRENCY_DECIMAL_FORMAT = new DecimalFormat("#,###.##");

    static String withSuffix(double value) {
        if (value < MILLION) {
            return ExchangeConstants.CURRENCY_DECIMAL_FORMAT.format(value);
        } else if (value < BILLION) {
            return ExchangeConstants.CURRENCY_DECIMAL_FORMAT.format(value / MILLION) + "m";
        }
        return ExchangeConstants.CURRENCY_DECIMAL_FORMAT.format(value / BILLION) + "t";
    }
}
