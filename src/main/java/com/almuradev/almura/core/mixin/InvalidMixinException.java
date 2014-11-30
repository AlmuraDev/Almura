/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.core.mixin;

public class InvalidMixinException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidMixinException(String message) {
        super(message);
    }

    public InvalidMixinException(Throwable message) {
        super(message);
    }

    public InvalidMixinException(String message, Throwable cause) {
        super(message, cause);
    }
}
