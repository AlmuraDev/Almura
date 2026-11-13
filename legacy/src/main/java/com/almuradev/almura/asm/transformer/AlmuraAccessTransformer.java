/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.asm.transformer;

import net.minecraftforge.fml.common.asm.transformers.AccessTransformer;

import java.io.IOException;

public class AlmuraAccessTransformer extends AccessTransformer {

    public AlmuraAccessTransformer() throws IOException {
        super("META-INF/almura_at.cfg");
    }
}
