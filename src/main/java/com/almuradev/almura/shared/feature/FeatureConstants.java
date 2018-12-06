/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.feature;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.UUID;

public interface FeatureConstants {

    UUID UNKNOWN_OWNER = new UUID(0, 0);

    String DELIMITER = ";";

    String EQUALITY = "=";

    int UNLIMITED = -1;

    double MILLION = 1000000.0;

    double BILLION = 1000000000.0;

    double TRILLION = 1000000000000.0;

    DecimalFormat CURRENCY_DECIMAL_FORMAT = new DecimalFormat("#,###.##");

    static String withSuffix(final double value) {
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

    static String filterToNumber(final String input, final int maxTrailingDigits, final boolean allowNegative) {
        // TODO: Maybe make a fancy regex for some of this

        // Get the current decimal separator
        final String decimalSeparator = String.valueOf(DecimalFormatSymbols.getInstance().getDecimalSeparator());

        // Filter out characters that are not a digit, the decimal separator, or (if allowed) a minus sign for negatives
        final String filteredValue = input.replaceAll("[^" + (allowNegative ? "-" : "") + decimalSeparator + "\\d]", "");

        // Return if empty
        if (filteredValue.isEmpty()) {
            return filteredValue;
        }


        // Get the last char and see if it's a decimal separator, if it is and we don't allow trailing digits then strip it
        if (String.valueOf(filteredValue.charAt(filteredValue.length() - 1)).equals(decimalSeparator) && maxTrailingDigits == 0) {
            return filteredValue.substring(0, filteredValue.length() - 1);
        }

        // See if we're attempting multiple decimal separators
        if (filteredValue.indexOf(decimalSeparator) != filteredValue.lastIndexOf(decimalSeparator)) {
            return filteredValue.substring(0, filteredValue.length() - 1);
        }

        // Split against the decimal separator
        final String[] values = Iterables.toArray(Splitter.on(CharMatcher.anyOf(decimalSeparator)).split(filteredValue), String.class);

        // If we have more than 2 sets of values after the split or the length of the 2nd block is bigger than 2
        // then return a value we expect to see.
        if (values.length > 2 || values.length > 1 && values[1].length() > maxTrailingDigits) {
            return values[0] + decimalSeparator + values[1].substring(0, maxTrailingDigits);
        }

        return filteredValue;
    }
}
