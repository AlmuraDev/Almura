/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.asm.mixin.core.tileentity;

import net.minecraft.tileentity.TileEntity;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TileEntity.class)
public class MixinTileEntity {

    /**
     * Be less noisy when a block entity cannot be created because a type mapping cannot be found.
     */
    @Redirect(
            method = "create",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/apache/logging/log4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;)V"
            )
    )
    private static void silentNotFound(final Logger logger, final String message, final Object arg) {
    }
}
