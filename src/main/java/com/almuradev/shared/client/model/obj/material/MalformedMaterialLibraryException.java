/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.shared.client.model.obj.material;

import net.minecraftforge.client.model.ModelLoaderRegistry;

public class MalformedMaterialLibraryException extends ModelLoaderRegistry.LoaderException {

    public MalformedMaterialLibraryException(String message) {
        super(message);
    }

    public MalformedMaterialLibraryException(String message, Throwable cause) {
        super(message, cause);
    }
}
