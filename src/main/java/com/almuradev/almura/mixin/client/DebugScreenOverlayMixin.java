/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.mixin.client;

import com.almuradev.almura.feature.hud.Huds;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Leaves out vanilla's debug screen text while Almura's HUD shows, whose debug panels show it instead, as on 1.12; the charts (F3 with 1, 2, 3
 * or 4) still draw.
 */
@Mixin(DebugScreenOverlay.class)
public abstract class DebugScreenOverlayMixin {

    @Inject(method = "extractLines", at = @At("HEAD"), cancellable = true)
    private void almura$hideLines(final GuiGraphicsExtractor graphics, final List<String> lines, final boolean alignLeft, final int width,
        final CallbackInfo ci) {
        if (Huds.showing()) {
            ci.cancel();
        }
    }
}
