/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.feature;

import java.text.DecimalFormat;

public interface FeatureConstants {

    String DELIMITER = ";";
    String EQUALITY = "=";

    int UNLIMITED = -1;
    double MILLION = 1000000.0;
    double BILLION = 1000000000.0;
    double TRILLION = 1000000000000.0;
    DecimalFormat CURRENCY_DECIMAL_FORMAT = new DecimalFormat("#,###.##");

    static String withSuffix(double value) {
        if (value < MILLION) {
            return FeatureConstants.CURRENCY_DECIMAL_FORMAT.format(value);
        } else if (value < BILLION) {
            return FeatureConstants.CURRENCY_DECIMAL_FORMAT.format(value / MILLION) + "m";
        } else if (value < TRILLION) {
            return FeatureConstants.CURRENCY_DECIMAL_FORMAT.format(value / BILLION) + "b";
        } else {
            return FeatureConstants.CURRENCY_DECIMAL_FORMAT.format(value / TRILLION) + "t";
        }
    }
}
