/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.mixin.network;

import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.client.C12PacketUpdateSign;
import net.minecraft.util.ChatAllowedCharacters;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Mixin(NetHandlerPlayServer.class)
public abstract class MixinNetHandlerPlayServer {

    String[] initialSignLines;

    @Inject(method = "processUpdateSign", at = @At(value = "JUMP", opcode = Opcodes.IF_ICMPGE, ordinal = 0, shift = At.Shift.BY, by = -4))
    public void onProcessUpdateSignPreLoop(C12PacketUpdateSign packet, CallbackInfo ci) {
        initialSignLines = Arrays.copyOf(packet.getLines(), 4);
    }

    @Inject(method = "processUpdateSign", at = @At(value = "JUMP", opcode = Opcodes.IFEQ, ordinal = 3, shift = At.Shift.BY, by = -2))
    public void onProcessUpdateSignPostLoop(C12PacketUpdateSign packet, CallbackInfo ci) {
        for (int i = 0; i < packet.getLines().length; i++) {
            String line = packet.getLines()[i];
            if ("!?".equals(line)) {
                //Swap out to pre-parsed line
                packet.getLines()[i] = initialSignLines[i];

                boolean illegalLine = false;

                // Perform Almura parsing
                if (line.length() > 30) {
                    illegalLine = true;
                }

                if (!illegalLine) {
                    for (int j = 0; j < line.length(); ++j) {
                        final char c = line.charAt(j);
                        if (c != 167) {
                            if (!ChatAllowedCharacters.isAllowedCharacter(c)) {
                                illegalLine = true;
                            }
                        }
                    }
                }

                if (illegalLine) {
                    packet.getLines()[i] = "Error!? :D";
                }
            }
        }
    }
}
