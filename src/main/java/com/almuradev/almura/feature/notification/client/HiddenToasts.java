/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.notification.client;

import net.minecraft.client.gui.components.toasts.SystemToast;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ToastAddEvent;

/**
 * Vanilla toasts Almura doesn't show: "Chat messages can't be verified", which a server that can't verify chat (one in offline mode, or not
 * enforcing secure profiles) brings up on every join.
 */
public final class HiddenToasts {

    @SubscribeEvent
    public void onToastAdd(final ToastAddEvent event) {
        if (event.getToast() instanceof final SystemToast toast && toast.getToken() == SystemToast.SystemToastId.UNSECURE_SERVER_WARNING) {
            event.setCanceled(true);
        }
    }
}
