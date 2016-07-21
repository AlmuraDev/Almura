/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.loader.transformer;

import net.minecraftforge.fml.common.asm.transformers.AccessTransformer;

import java.io.IOException;

public class AlmuraAccessTransformer extends AccessTransformer {

    public AlmuraAccessTransformer() throws IOException {
        super("META-INF/almura_at.cfg");
    }
}
