/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.mixin.client;

import com.almuradev.almura.core.client.config.AlmuraClientConfig;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.waypoints.ClientWaypointManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Almura's Locator Bar option: off, vanilla picks the bar above the hotbar as if no player were tracked, so it shows experience (or a
 * mount's jump) in place of the locator bar.
 */
@Mixin(Hud.class)
public abstract class HudMixin {

    @Redirect(method = "nextContextualInfoState", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/waypoints/ClientWaypointManager;hasWaypoints()Z"))
    private boolean almura$locatorBarOption(final ClientWaypointManager waypoints) {
        return AlmuraClientConfig.get(AlmuraClientConfig.DISPLAY_LOCATOR_BAR) && waypoints.hasWaypoints();
    }
}
