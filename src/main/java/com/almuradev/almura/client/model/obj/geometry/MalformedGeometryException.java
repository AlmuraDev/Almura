/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.model.obj.geometry;

import net.minecraftforge.client.model.ModelLoaderRegistry;

public class MalformedGeometryException extends ModelLoaderRegistry.LoaderException {

    public MalformedGeometryException(String message) {
        super(message);
    }

    public MalformedGeometryException(String message, Throwable cause) {
        super(message, cause);
    }
}
