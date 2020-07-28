/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.util;

import br.net.fabiozumbi12.UltimateChat.Sponge.UChat;
import com.almuradev.core.event.Witness;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.advancement.AdvancementEvent;

public final class UchatUtil implements Witness {
    public static void relayMessageToDiscord(String icon, String message, boolean bold) {
        if (Sponge.getPluginManager().isLoaded("ultimatechat") && UChat.get().getUCJDA() != null) {
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

    @Listener
    public void onAchievementGranted(AdvancementEvent.Grant event) {
        System.out.println("Event: " + event);
        System.out.println("Message: " + event.getMessage());
        System.out.println("Name " + event.getAdvancement().getName());
        System.out.println("Player: " + event.getTargetEntity().getDisplayNameData().displayName());
    }
}


