/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.nick.client;

import com.almuradev.almura.feature.menu.InGameScreens;
import com.almuradev.almura.feature.nick.network.NickMessages.ClientboundNames;
import com.almuradev.almura.feature.nick.network.NickMessages.ClientboundOpen;
import com.almuradev.almura.feature.nick.network.NickMessages.ServerboundChange;
import com.almuradev.almura.feature.nick.network.NickMessages.ServerboundOpen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.util.TriState;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Nicknames on the client, as 3.5's {@code ClientNickManager}: each online player's nickname from the server, shown on their name tag, and
 * the one being tried in the Nicknames window (shown on the player's own until they apply or close it).
 */
public final class ClientNicks {

    private static final Map<UUID, String> nicknames = new HashMap<>();
    // Tried in the Nicknames window, for the player themselves
    private static @Nullable String preview;

    private ClientNicks() {
    }

    /**
     * @return the player's nickname as shown (with its {@code ~} and colour codes), or null for none
     */
    public static @Nullable String nicknameFor(final UUID player) {
        final Minecraft minecraft = Minecraft.getInstance();
        if (preview != null && minecraft.player != null && minecraft.player.getUUID().equals(player)) {
            return preview;
        }
        return nicknames.get(player);
    }

    static void setPreview(final @Nullable String nickname) {
        preview = nickname;
    }

    // ---- From the server

    public static void onNames(final ClientboundNames message) {
        if (message.replace()) {
            nicknames.clear();
        }
        message.nicknames().forEach((player, nickname) -> {
            if (nickname.isEmpty()) {
                nicknames.remove(player);
            } else {
                nicknames.put(player, nickname);
            }
        });
    }

    public static void onOpen(final ClientboundOpen message) {
        InGameScreens.open(new NicknameScreen(message.canChange(), message.admin()), Minecraft.getInstance().gui.screen());
    }

    // ---- To the server

    /**
     * Asks the server to open the Nicknames window (the Features menu's Nicknames).
     */
    public static void requestOpen() {
        ClientPacketDistributor.sendToServer(new ServerboundOpen());
    }

    static void change(final String nickname) {
        ClientPacketDistributor.sendToServer(new ServerboundChange(nickname));
    }

    /**
     * Name tags show nicknames, as 3.5's did, and the last server's are forgotten on joining another.
     */
    public static final class Hooks {

        @SubscribeEvent
        public void onCanRenderNameTag(final RenderNameTagEvent.CanRender event) {
            if (event.getEntity() instanceof final Player player) {
                final String nickname = nicknameFor(player.getUUID());
                if (nickname != null) {
                    event.setContent(Component.literal(nickname));
                }
                // The player's own name (and title) over them in third person, as 3.5's name plates showed it; vanilla never shows it
                final Minecraft minecraft = Minecraft.getInstance();
                if (player == minecraft.player && !minecraft.options.getCameraType().isFirstPerson() && !player.isInvisible()) {
                    event.setCanRender(TriState.TRUE);
                }
            }
        }

        @SubscribeEvent
        public void onLoggingIn(final ClientPlayerNetworkEvent.LoggingIn event) {
            nicknames.clear();
            preview = null;
        }
    }
}
