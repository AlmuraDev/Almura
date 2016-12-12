/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.util;

public class MathUtil {

    public static float ConvertToRange(float originalValue, float originalMin, float originalMax, float newMin, float newMax) {
        float originalDiff = originalMax - originalMin;
        float newDiff = newMax - newMin;
        float ratio = newDiff / originalDiff;
        return Math.round(((originalValue * ratio) + newMin) * 100.0f) / 100.0f;
    }
}
