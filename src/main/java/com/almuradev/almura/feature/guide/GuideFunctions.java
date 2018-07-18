/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.guide;

import java.util.function.Function;

public class GuideFunctions {

    public static Function<String, String> IntegerFilterConsumer = (s) -> s = s.replaceAll("[^\\d]", "");

    public static Function<String, String> StringMaxLenthFilterConsumer = input -> {
        if (input.length() > 100) {
            input = input.substring(0, 100);
        }
        return input;
    };
}
