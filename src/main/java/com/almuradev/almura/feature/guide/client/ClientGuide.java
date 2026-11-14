/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.guide.client;

import com.almuradev.almura.core.client.config.AlmuraClientConfig;
import com.almuradev.almura.feature.guide.network.GuideMessages.ClientboundListing;
import com.almuradev.almura.feature.guide.network.GuideMessages.ClientboundPage;
import com.almuradev.almura.feature.guide.network.GuideMessages.Entry;
import com.almuradev.almura.feature.guide.network.GuideMessages.OpenType;
import com.almuradev.almura.feature.guide.network.GuideMessages.ServerboundOpen;
import com.almuradev.almura.feature.menu.InGameScreens;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * The Guide on the client: asks the server to open it, and opens or updates it from the server's messages. Client only.
 */
public final class ClientGuide {

    private static List<Entry> entries = List.of();
    private static @Nullable String wanted;
    private static @Nullable GuideScreen open;

    private ClientGuide() {
    }

    /**
     * Asks the server to open the guide (the Features menu's Guide button).
     */
    public static void requestOpen() {
        ClientPacketDistributor.sendToServer(new ServerboundOpen());
    }

    static List<Entry> entries() {
        return entries;
    }

    static @Nullable String wanted() {
        return wanted;
    }

    /**
     * The pages the player may see: opens the guide on them, or updates the open one.
     */
    public static void onListing(final ClientboundListing listing) {
        entries = List.copyOf(listing.entries());
        wanted = listing.show().isEmpty() ? null : listing.show();
        final Minecraft minecraft = Minecraft.getInstance();
        final GuideScreen screen = shownScreen();
        if (listing.open() == OpenType.REFRESH) {
            if (screen != null) {
                screen.showEntries(entries, wanted);
            }
            return;
        }
        if (listing.open() == OpenType.LOGGED_IN) {
            // As 3.5: only when the player wants it, and not on the game's own (single-player) server
            if (!AlmuraClientConfig.get(AlmuraClientConfig.DISPLAY_GUIDE_ON_LOGIN) || minecraft.isLocalServer()
                || minecraft.gui.screen() != null) {
                return;
            }
        }
        if (screen != null) {
            screen.showEntries(entries, wanted);
            return;
        }
        open = new GuideScreen();
        InGameScreens.open(open, minecraft.gui.screen());
    }

    /**
     * A page: shown in the open guide.
     */
    public static void onPage(final ClientboundPage page) {
        final GuideScreen screen = shownScreen();
        if (screen != null) {
            screen.showPage(page);
        }
    }

    /**
     * The guide, while it's the screen shown.
     */
    private static @Nullable GuideScreen shownScreen() {
        if (open != null && !InGameScreens.isShowing(open)) {
            open = null;
        }
        return open;
    }
}
