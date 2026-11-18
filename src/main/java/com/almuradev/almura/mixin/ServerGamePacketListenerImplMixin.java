/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.mixin;

import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A sign keeps the {@code §} colour codes its lines are sent with, as 3.5's {@code MixinNetHandlerPlayServer}: vanilla strips them from every
 * line of an edit, so a coloured sign lost its colours when edited again. Players can't type a {@code §} themselves; Almura's sign editor sends
 * back the colours a line already had (see {@code AbstractSignEditScreenMixin}).
 */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {

    // The lines as sent, in place of vanilla's ChatFormatting::stripFormatting over them
    @Redirect(method = "handleSignUpdate", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;map(Ljava/util/function/Function;)Ljava/util/stream/Stream;"))
    private Stream<String> almura$keepColours(final Stream<String> lines, final Function<String, String> strip) {
        return lines;
    }
}
