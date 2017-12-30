/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.model.obj.material;

import net.minecraftforge.client.model.ModelLoaderRegistry;

public class MalformedMaterialLibraryException extends ModelLoaderRegistry.LoaderException {

    public MalformedMaterialLibraryException(final String message) {
        super(message);
    }

    public MalformedMaterialLibraryException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
