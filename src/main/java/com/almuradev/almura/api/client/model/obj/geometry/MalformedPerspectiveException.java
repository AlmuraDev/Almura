/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.api.client.model.obj.geometry;

import net.minecraftforge.client.model.ModelLoaderRegistry;

public class MalformedPerspectiveException extends ModelLoaderRegistry.LoaderException {

    public MalformedPerspectiveException(String message) {
        super(message);
    }

    public MalformedPerspectiveException(String message, Throwable cause) {
        super(message, cause);
    }
}
