/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.asm.mixin.core.client;

import net.minecraftforge.client.model.ModelLoader;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ModelLoader.class)
public class MixinModelLoader {

    @Redirect(
            method = "loadItemModels",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/apache/logging/log4j/Logger;debug(Ljava/lang/String;Ljava/lang/Object;)V"
            )
    )
    private void foo(final Logger logger, final String message, final Object location) {
    }
}
