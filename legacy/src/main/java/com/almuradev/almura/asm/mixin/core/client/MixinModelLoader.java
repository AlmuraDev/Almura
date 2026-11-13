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

import java.io.FileNotFoundException;

/**
 * Logs model loading errors as one line naming the cause (e.g. the missing model file) instead of a full stack trace per variant.
 */
@Mixin(value = ModelLoader.class, remap = false)
public abstract class MixinModelLoader {

    // "Exception loading model for variant X for blockstate "Y"", exception
    @Redirect(
            method = "onPostBakeEvent",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/apache/logging/log4j/Logger;error(Ljava/lang/String;Ljava/lang/Throwable;)V"
            )
    )
    private void almura$logModelError(final Logger logger, final String message, final Throwable exception) {
        logger.error("{} ({})", message, almura$describe(exception));
    }

    // "{}, normal location exception: " / "{}, blockstate location exception: " / "Exception loading blockstate for the variant {}: ",
    // location, exception
    @Redirect(
            method = "onPostBakeEvent",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/apache/logging/log4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V"
            )
    )
    private void almura$logLocationError(final Logger logger, final String message, final Object location, final Object exception) {
        final String cause = exception instanceof Throwable ? almura$describe((Throwable) exception) : String.valueOf(exception);
        logger.error(message + "{}", location, cause);
    }

    private static String almura$describe(final Throwable exception) {
        Throwable root = exception;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }

        if (root instanceof FileNotFoundException) {
            return "missing file " + root.getMessage();
        }
        return root.getClass().getSimpleName() + (root.getMessage() == null ? "" : ": " + root.getMessage());
    }
}
