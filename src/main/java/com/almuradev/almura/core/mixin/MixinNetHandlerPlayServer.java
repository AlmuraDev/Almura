/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.core.mixin;

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
    int counter = 0;

    @Inject(method = "processUpdateSign", at = @At(value = "JUMP", opcode = Opcodes.IF_ICMPGE, ordinal = 0, shift = At.Shift.BEFORE, by = -3))
    public void onProcessUpdateSignStep1(C12PacketUpdateSign packet, CallbackInfo ci) {
        counter++;
        if (counter >= 6) {
            counter = 0;
            initialSignLines = null;
        }

        if (initialSignLines == null) {
            System.out.println("Inject back 3 steps");
            initialSignLines = Arrays.copyOf(packet.func_149589_f(), 4);
        }
    }

    @Inject(method = "processUpdateSign", at = @At(value = "JUMP", opcode = Opcodes.IF_ICMPGE, ordinal = 0, shift = At.Shift.AFTER))
    public void onProcessUpdateSignPreLoop(C12PacketUpdateSign packet, CallbackInfo ci) {
        System.out.println("Inject back 1 step");

        for (int i = 0; i < packet.func_149589_f().length; i++) {
            String line = packet.func_149589_f()[i];
            if ("!?".equals(line)) {
                //Swap out to pre-parsed line
                packet.func_149589_f()[i] = initialSignLines[i];

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
                    packet.func_149589_f()[i] = "Error!? :D";
                }
            }
        }
    }
}
