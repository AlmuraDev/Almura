/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.shared.client.model.obj.geometry;

public class UVWOutOfBoundsException extends MalformedGeometryException {

    public UVWOutOfBoundsException(String message) {
        super(message);
    }

    public UVWOutOfBoundsException(String message, Throwable cause) {
        super(message, cause);
    }
}
