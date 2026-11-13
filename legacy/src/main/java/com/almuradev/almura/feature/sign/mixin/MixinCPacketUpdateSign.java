/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.sign.mixin;

import net.minecraft.network.play.client.CPacketUpdateSign;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CPacketUpdateSign.class)
public class MixinCPacketUpdateSign {
    @Shadow private String[] lines;

    @Inject(
        method = "<init>(Lnet/minecraft/util/math/BlockPos;[Lnet/minecraft/util/text/ITextComponent;)V",
        at = @At("RETURN")
    )
    private void init(final BlockPos pos, final ITextComponent[] lines, final CallbackInfo ci) {
        this.lines = new String[] {
            lines[0].getFormattedText(),
            lines[1].getFormattedText(),
            lines[2].getFormattedText(),
            lines[3].getFormattedText()
        };
    }
}
