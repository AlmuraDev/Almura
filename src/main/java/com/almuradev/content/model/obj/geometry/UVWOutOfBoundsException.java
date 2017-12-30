/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.model.obj.geometry;

public class UVWOutOfBoundsException extends MalformedGeometryException {

    public UVWOutOfBoundsException(final String message) {
        super(message);
    }

    public UVWOutOfBoundsException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
