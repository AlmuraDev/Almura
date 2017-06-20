/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.asm.mixin.core.client.gui;

import com.almuradev.almura.Almura;
import com.almuradev.almura.client.ClientProxy;
import com.almuradev.almura.client.gui.screen.ingame.hud.AbstractHUD;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiBossOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Optional;

/**
 * This Mixin's objective is to have the Mojang BossBar's respect a Y offset provided by the HUD in-use
 */
@Mixin(GuiBossOverlay.class)
public abstract class MixinGuiBossOverlay extends Gui {

    @ModifyVariable(method = "Lnet/minecraft/client/gui/GuiBossOverlay;renderBossHealth()V", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/client/gui/ScaledResolution;getScaledWidth()I", shift = At.Shift.BY, by = 6), ordinal = 1)
    private int adjustYRenderCoordinate(int j) {
        final ClientProxy proxy = (ClientProxy) Almura.proxy;

        final Optional<AbstractHUD> customHud = proxy.getCustomIngameHud();

        // Check hud instance here, adjust 6 value as needed
        return customHud.map(AbstractHUD::getOriginBossBarOffsetY).orElse(12);
    }
}
