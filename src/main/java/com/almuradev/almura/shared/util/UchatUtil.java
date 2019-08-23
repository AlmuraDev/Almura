/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.util;

import br.net.fabiozumbi12.UltimateChat.Sponge.UChat;
import org.spongepowered.api.Sponge;

public final class UchatUtil {
    public static void relayMessageToDiscord(String icon, String message, boolean bold) {
        if (Sponge.getPluginManager().isLoaded("ultimatechat")) {
            if (UChat.get().getUCJDA().JDAAvailable()) {
                if (bold) {
                    UChat.get().getUCJDA().sendRawToDiscord(icon + " " + "**" + message + "**");
                } else {
                    UChat.get().getUCJDA().sendRawToDiscord(icon + " " +  message);
                }
            } else {
                System.err.println("UChat JDA not available!");
            }
        } else {
            System.out.println("UltimateChat not loaded!");
        }
    }
}


