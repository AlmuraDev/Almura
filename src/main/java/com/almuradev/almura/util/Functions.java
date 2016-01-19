/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.util;

import com.google.common.base.Function;

import javax.annotation.Nullable;

public final class Functions {

    public static Function<Object, String> FUNCTION_STRING_TRANSFORMER = new Function<Object, String>() {
        @Nullable
        @Override
        public String apply(Object input) {
            if (input == null) {
                return "";
            } else if (input.getClass() == String.class) {
                return (String) input;
            } else {
                return input.toString();
            }
        }
    };

    private Functions() {
    }
}
