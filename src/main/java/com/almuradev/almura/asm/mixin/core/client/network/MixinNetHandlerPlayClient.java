/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.asm.mixin.core.client.network;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Entity packets that arrive just before the player leaves a server wait for the main thread, which runs them after the world
 * is gone: each looked its entity up in a world that no longer exists, and the log filled with "Error executing task"
 * NullPointerExceptions on every disconnect. Without a world there is nothing for them to update, so they are dropped.
 */
@Mixin(NetHandlerPlayClient.class)
public abstract class MixinNetHandlerPlayClient {

    @Shadow private WorldClient world;

    @Inject(method = {
            "handleEntityMovement",
            "handleEntityTeleport",
            "handleEntityVelocity",
            "handleEntityHeadLook",
            "handleEntityMetadata",
            "handleEntityStatus",
            "handleEntityEquipment",
            "handleEntityEffect",
            "handleRemoveEntityEffect",
            "handleEntityProperties",
            "handleEntityAttach",
            "handleSetPassengers",
            "handleDestroyEntities"
    }, at = @At("HEAD"), cancellable = true)
    private void almura$dropWithoutWorld(final CallbackInfo ci) {
        if (this.world == null) {
            ci.cancel();
        }
    }
}
