/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.sign.mixin;

import net.minecraft.network.NetHandlerPlayServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(NetHandlerPlayServer.class)
public class MixinNetHandlerPlayServer {
    @Redirect(
        method = "processUpdateSign",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/text/TextFormatting;getTextWithoutFormattingCodes(Ljava/lang/String;)Ljava/lang/String;"
        )
    )
    private String colourful(final String text) {
        return text;
    }
}
