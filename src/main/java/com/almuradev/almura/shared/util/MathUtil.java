/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.util;

public class MathUtil {

    /**
     * Scales a value from one range to another
     *
     * @param originalValue The original value
     * @param originalMin The original minimum
     * @param originalMax The original maximum
     * @param newMin The new minimum
     * @param newMax The new maximum
     * @return The scaled value
     */
    public static int scalei(final int originalValue,
            final int originalMin, final int originalMax, final int newMin, final int newMax) {
        final int originalRange = (originalMax - originalMin);
        if (originalRange == 0) {
            return newMin;
        } else {
            final int newRange = (newMax - newMin);
            return (((originalValue - originalMin) * newRange) / originalRange) + newMin;
        }
    }

    /**
     * Scales a value from one range to another
     *
     * @param originalValue The original value
     * @param originalMin The original minimum
     * @param originalMax The original maximum
     * @param newMin The new minimum
     * @param newMax The new maximum
     * @return The scaled value
     */
    public static float scalef(final float originalValue,
            final float originalMin, final float originalMax, final float newMin, final float newMax) {
        final float originalRange = (originalMax - originalMin);
        if (originalRange == 0) {
            return newMin;
        } else {
            final float newRange = (newMax - newMin);
            return (((originalValue - originalMin) * newRange) / originalRange) + newMin;
        }
    }

    /**
     * Scales a value from one range to another
     *
     * @param originalValue The original value
     * @param originalMin The original minimum
     * @param originalMax The original maximum
     * @param newMin The new minimum
     * @param newMax The new maximum
     * @return The scaled value
     */
    public static double scaled(final double originalValue,
            final double originalMin, final double originalMax, final double newMin, final double newMax) {
        final double originalRange = (originalMax - originalMin);
        if (originalRange == 0) {
            return newMin;
        } else {
            final double newRange = (newMax - newMin);
            return (((originalValue - originalMin) * newRange) / originalRange) + newMin;
        }
    }

    /**
     * Squashes the passed value to fit within the minimum and maximum specified.
     *
     * @param value The value to squash
     * @param min The minimum value to return
     * @param max The maximum value to return
     * @return The value between the minimum and maximum values
     */
    public static int squashi(final int value, final int min, final int max) {
        if (min > max) {
            throw new IllegalArgumentException("Minimum value [" + min + "] cannot be greater than the maximum value [" + max + "]!");
        } else if (max < min) {
            throw new IllegalArgumentException("Maximum value [" + max + "] cannot be lesser than the minimum value [" + min + "]!");
        }
        return Math.min(Math.max(value, min), max);
    }


    /**
     * Squashes the passed value to fit within the minimum and maximum specified.
     *
     * @param value The value to squash
     * @param min The minimum value to return
     * @param max The maximum value to return
     * @return The value between the minimum and maximum values
     */
    public static float squashf(final float value, final float min, final float max) {
        if (min > max) {
            throw new IllegalArgumentException("Minimum value [" + min + "] cannot be greater than the maximum value [" + max + "]!");
        } else if (max < min) {
            throw new IllegalArgumentException("Maximum value [" + max + "] cannot be lesser than the minimum value [" + min + "]!");
        }
        return Math.min(Math.max(value, min), max);
    }

    /**
     * Squashes the passed value to fit within the minimum and maximum specified.
     *
     * @param value The value to squash
     * @param min The minimum value to return
     * @param max The maximum value to return
     * @return The value between the minimum and maximum values
     */
    public static double squashd(final double value, final double min, final double max) {
        if (min > max) {
            throw new IllegalArgumentException("Minimum value [" + min + "] cannot be greater than the maximum value [" + max + "]!");
        } else if (max < min) {
            throw new IllegalArgumentException("Maximum value [" + max + "] cannot be lesser than the minimum value [" + min + "]!");
        }
        return Math.min(Math.max(value, min), max);
    }

    /**
     * Check if a value is within a range
     *
     * @param value The value to check
     * @param start The start of the range
     * @param end The end of the range
     * @return True if within the range
     */
    public static boolean withinRange(final double value, final double start, final double end) {
        return value >= start && value <= end;
    }
}
