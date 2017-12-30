/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.model.obj.geometry;

import net.minecraftforge.client.model.ModelLoaderRegistry;

public class MalformedGeometryException extends ModelLoaderRegistry.LoaderException {

    public MalformedGeometryException(final String message) {
        super(message);
    }

    public MalformedGeometryException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
