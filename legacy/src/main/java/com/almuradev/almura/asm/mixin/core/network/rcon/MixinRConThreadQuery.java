/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.asm.mixin.core.network.rcon;

import net.minecraft.network.rcon.RConOutputStream;
import net.minecraft.network.rcon.RConThreadQuery;
import org.spongepowered.api.Sponge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.net.DatagramPacket;

/**
 * Adds "whitelist" (true/false) to the full query answer, after hostip, so Almura's multiplayer screen can show a whitelisted
 * server as locked. Sponge's QueryServerEvent would be the place for this, but Sponge for 1.12 never fires it.
 * Dedicated servers only (the query thread doesn't exist anywhere else).
 */
@Mixin(RConThreadQuery.class)
public abstract class MixinRConThreadQuery {

    @Shadow @Final private RConOutputStream output;

    @Inject(method = "createQueryResponse",
            slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/network/rcon/RConThreadQuery;queryHostname:Ljava/lang/String;")),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/network/rcon/RConOutputStream;writeString(Ljava/lang/String;)V", ordinal = 0,
                    shift = At.Shift.AFTER))
    private void almura$writeWhitelist(final DatagramPacket requestPacket, final CallbackInfoReturnable<byte[]> cir) throws IOException {
        this.output.writeString("whitelist");
        this.output.writeString(String.valueOf(Sponge.getServer().hasWhitelist()));
    }
}
