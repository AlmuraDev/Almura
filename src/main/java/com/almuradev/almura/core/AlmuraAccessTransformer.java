package com.almuradev.almura.core;

import cpw.mods.fml.common.asm.transformers.AccessTransformer;

import java.io.IOException;

public class AlmuraAccessTransformer extends AccessTransformer {
    public AlmuraAccessTransformer() throws IOException {
        super("almura_at.cfg");
    }
}
