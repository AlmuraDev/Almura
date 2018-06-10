/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.util;

public class MathUtil {

    public static float convertToRange(float originalValue, float originalMin, float originalMax, float newMin, float newMax) {
        float originalDiff = originalMax - originalMin;
        float newDiff = newMax - newMin;
        float ratio = newDiff / originalDiff;
        return Math.round(((originalValue * ratio) + newMin) * 100.0f) / 100.0f;
    }

    /**
     * Squashes the passed value to fit within the minimum and maximum specified.
     *
     * @param value The value to squash
     * @param min The minimum value to return
     * @param max The maximum value to return
     * @return The value between the minimum and maximum values
     */
    public static int squashi(int value, int min, int max) {
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
    public static float squashf(float value, float min, float max) {
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
    public static boolean withinRange(double value, double start, double end) {
        return value >= start && value <= end;
    }
}
