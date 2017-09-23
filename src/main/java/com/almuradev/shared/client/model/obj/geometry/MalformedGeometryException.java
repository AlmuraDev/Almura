/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.shared.client.model.obj.geometry;

import net.minecraftforge.client.model.ModelLoaderRegistry;

public class MalformedGeometryException extends ModelLoaderRegistry.LoaderException {

    public MalformedGeometryException(String message) {
        super(message);
    }

    public MalformedGeometryException(String message, Throwable cause) {
        super(message, cause);
    }
}
