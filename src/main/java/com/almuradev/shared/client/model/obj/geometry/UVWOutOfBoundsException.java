/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
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
