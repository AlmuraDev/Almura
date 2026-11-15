/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.title.client;

import com.almuradev.almura.feature.menu.InGameScreens;
import com.almuradev.almura.feature.title.Title;
import com.almuradev.almura.feature.title.network.TitleMessages.ClientboundAvailable;
import com.almuradev.almura.feature.title.network.TitleMessages.ClientboundOpen;
import com.almuradev.almura.feature.title.network.TitleMessages.ClientboundRegistry;
import com.almuradev.almura.feature.title.network.TitleMessages.ClientboundSelected;
import com.almuradev.almura.feature.title.network.TitleMessages.ModifyType;
import com.almuradev.almura.feature.title.network.TitleMessages.ServerboundModify;
import com.almuradev.almura.feature.title.network.TitleMessages.ServerboundOpen;
import com.almuradev.almura.feature.title.network.TitleMessages.ServerboundSelect;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Titles on the client, as 3.5's {@code ClientTitleManager}: the titles the server sent, the ones the player may pick, each player's pick,
 * and the title being tried on in the Titles window (shown above the player's own name until they apply or close it).
 */
public final class ClientTitles {

    private static final Map<String, Title> titles = new LinkedHashMap<>();
    private static final List<String> available = new ArrayList<>();
    private static final Map<UUID, Title> selected = new HashMap<>();
    // Tried on in the Titles window, shown above the player's own name
    private static @Nullable Title preview;
    private static @Nullable TitlesScreen open;

    private ClientTitles() {
    }

    static List<Title> titles() {
        return List.copyOf(titles.values());
    }

    static List<Title> available() {
        return available.stream().map(titles::get).filter(title -> title != null).toList();
    }

    /**
     * @return the title shown above a player's name: the one being tried on for the player themselves, else their pick
     */
    public static @Nullable Title shownFor(final UUID player) {
        final Minecraft minecraft = Minecraft.getInstance();
        if (preview != null && minecraft.player != null && minecraft.player.getUUID().equals(player)) {
            return preview;
        }
        return selected.get(player);
    }

    static @Nullable Title selectedFor(final UUID player) {
        return selected.get(player);
    }

    static void setPreview(final @Nullable Title title) {
        preview = title;
    }

    // ---- From the server

    public static void onRegistry(final ClientboundRegistry message) {
        titles.clear();
        message.titles().forEach(title -> titles.put(title.id(), title));
        // Picks of titles that are gone
        selected.replaceAll((player, title) -> titles.getOrDefault(title.id(), title));
        refreshOpen();
    }

    public static void onAvailable(final ClientboundAvailable message) {
        available.clear();
        available.addAll(message.ids());
        refreshOpen();
    }

    public static void onSelected(final ClientboundSelected message) {
        if (message.replace()) {
            selected.clear();
        }
        message.selected().forEach((player, id) -> {
            final Title title = id.isEmpty() ? null : titles.get(id);
            if (title == null) {
                selected.remove(player);
            } else {
                selected.put(player, title);
            }
        });
    }

    public static void onOpen(final ClientboundOpen message) {
        open = new TitlesScreen(message.canChange(), message.admin());
        InGameScreens.open(open, Minecraft.getInstance().gui.screen());
    }

    private static void refreshOpen() {
        if (open != null && !InGameScreens.isShowing(open)) {
            open = null;
        }
        if (open != null) {
            open.refresh();
        }
    }

    // ---- To the server

    /**
     * Asks the server to open the Titles window (the Features menu's Titles).
     */
    public static void requestOpen() {
        ClientPacketDistributor.sendToServer(new ServerboundOpen());
    }

    static void select(final @Nullable Title title) {
        ClientPacketDistributor.sendToServer(new ServerboundSelect(title == null ? "" : title.id()));
    }

    static void modify(final ModifyType type, final String id, final String permission, final String content, final boolean hidden) {
        ClientPacketDistributor.sendToServer(new ServerboundModify(type, id, permission, content, hidden));
    }

    /**
     * Forgets the last server's titles when joining another, as 3.5.
     */
    public static final class Resetter {

        @SubscribeEvent
        public void onLoggingIn(final ClientPlayerNetworkEvent.LoggingIn event) {
            titles.clear();
            available.clear();
            selected.clear();
            preview = null;
        }
    }
}
