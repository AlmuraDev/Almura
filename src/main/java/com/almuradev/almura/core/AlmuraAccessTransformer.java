/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.core;

import cpw.mods.fml.common.asm.transformers.AccessTransformer;

import java.io.IOException;

public final class AlmuraAccessTransformer extends AccessTransformer {

    public AlmuraAccessTransformer() throws IOException {
        super("almurasdk_at.cfg");
    }
}
