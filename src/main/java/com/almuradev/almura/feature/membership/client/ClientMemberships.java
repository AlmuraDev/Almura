/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.membership.client;

import com.almuradev.almura.feature.membership.network.MembershipMessages.ClientboundApplied;
import com.almuradev.almura.feature.membership.network.MembershipMessages.ClientboundOpen;
import com.almuradev.almura.feature.membership.network.MembershipMessages.ServerboundOpen;
import com.almuradev.almura.feature.membership.network.MembershipMessages.ServerboundUpgrade;
import com.almuradev.almura.feature.menu.InGameScreens;
import com.almuradev.almura.feature.notification.PopupNotification;
import com.almuradev.almura.feature.notification.client.ClientNotifications;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jspecify.annotations.Nullable;

/**
 * Memberships on the client, as 3.5's {@code ClientMembershipManager}: the window opened with what the server sent, and the player's
 * choices sent back.
 */
public final class ClientMemberships {

    private static @Nullable MembershipScreen open;

    private ClientMemberships() {
    }

    /**
     * Asks the server for the memberships window (the Features menu's Memberships button).
     */
    public static void requestOpen() {
        ClientPacketDistributor.sendToServer(new ServerboundOpen());
    }

    static void requestUpgrade(final int level, final boolean bySkills) {
        ClientPacketDistributor.sendToServer(new ServerboundUpgrade(level, bySkills));
    }

    public static void onOpen(final ClientboundOpen message) {
        final Minecraft minecraft = Minecraft.getInstance();
        open = new MembershipScreen(message.admin(), message.skillsTotal(), message.funds(), message.current());
        InGameScreens.open(open, minecraft.gui.screen());
    }

    /**
     * A membership was bought: 3.5's Congratulations, over the window (or as a popup if it was closed).
     */
    public static void onApplied(final ClientboundApplied message) {
        if (open != null && InGameScreens.isShowing(open)) {
            open.applied(message.membership());
        } else {
            ClientNotifications.show(new PopupNotification("Almura Memberships", "Congratulations! " + message.membership()
                + " successfully applied!", 5));
        }
    }
}
