/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.guide;

import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.almura.asm.ClientStaticAccess;
import com.almuradev.almura.feature.guide.network.ServerboundGuideOpenRequestPacket;
import com.almuradev.almura.feature.guide.network.ServerboundPageChangeRequestPacket;
import com.almuradev.almura.feature.guide.network.ServerboundPageMoveRequestPacket;
import com.almuradev.almura.feature.guide.network.ServerboundPageOpenRequestPacket;
import com.almuradev.almura.feature.notification.ClientNotificationManager;
import com.almuradev.almura.feature.notification.type.PopupNotification;
import com.almuradev.almura.shared.client.keyboard.binder.KeyBindingEntry;
import com.almuradev.almura.shared.network.NetworkConfig;
import com.almuradev.core.event.Witness;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

@SideOnly(Side.CLIENT)
@Singleton
public final class ClientPageManager implements Witness {

    public final ChannelBinding.IndexedMessageChannel network;
    private final ClientNotificationManager clientNotificationManager;

    private List<PageListEntry> pageEntries = new ArrayList<>();
    private Page page;
    public String preSnapshot, postSnapshot;

    @Inject
    public ClientPageManager(final @ChannelId(NetworkConfig.CHANNEL) ChannelBinding.IndexedMessageChannel network, final Set<KeyBindingEntry>
        keybindings, final ClientNotificationManager manager) {
        this.network = network;
        this.clientNotificationManager = manager;
    }

    public void requestGuideGUI() {
        this.network.sendToServer(new ServerboundGuideOpenRequestPacket());
    }

    /**
     * @return The pages to list: everything the server sent, without other players' guides unless "Show Player Guides" is on (admins)
     */
    public List<PageListEntry> getPageEntries() {
        if (ClientStaticAccess.configAdapter.get().general.guideShowPlayerGuides) {
            return Collections.unmodifiableList(this.pageEntries);
        }
        return Collections.unmodifiableList(this.pageEntries.stream()
            .filter(entry -> !entry.isPlayerGuide() || entry.isMine())
            .collect(Collectors.toList()));
    }

    /**
     * @return The list entry of the open page, null if none is open
     */
    @Nullable
    public PageListEntry getPageEntry() {
        if (this.page == null) {
            return null;
        }
        return this.pageEntries.stream().filter(entry -> entry.getId().equalsIgnoreCase(this.page.getId())).findFirst().orElse(null);
    }

    /**
     * The same pages in a new order: the open page stays open (with any unsaved edits).
     */
    public void reorderPageEntries(List<PageListEntry> pageEntries) {
        this.pageEntries.clear();
        this.pageEntries.addAll(pageEntries);
    }

    public void movePage(String pageId, int direction) {
        this.network.sendToServer(new ServerboundPageMoveRequestPacket(pageId, direction));
    }

    /**
     * Re-applies the "Show Player Guides" filter: stays on the open page if it is still listed, otherwise opens the first.
     */
    public void refreshVisiblePages() {
        this.setPageEntries(new ArrayList<>(this.pageEntries), null);
    }

    public void showError(final String message) {
        this.clientNotificationManager.handlePopup(new PopupNotification("Guide", message, 5));
    }

    public void setPageEntries(List<PageListEntry> pageEntries, String switchToPage) {
        final Page oldPage = this.page;
        if (this.page != null) {
            this.page = null;
        }

        this.pageEntries.clear();
        this.pageEntries.addAll(pageEntries);

        final List<PageListEntry> visible = this.getPageEntries();
        if (!visible.isEmpty()) {
            if (switchToPage != null) {
                this.requestPage(switchToPage);
            } else {
                // Stay on the open page if it is still listed, otherwise go to the first
                final PageListEntry stay = oldPage == null ? null : visible.stream()
                    .filter(p -> p.getId().equalsIgnoreCase(oldPage.getId()))
                    .findFirst()
                    .orElse(null);
                this.requestPage(stay != null ? stay.getId() : visible.get(0).getId());
            }
        }
    }

    @Nullable
    public Page getPage() {
        return this.page;
    }

    public void setPage(Page page) {
        this.page = page;
    }

    public void requestPage(String pageId) {
        checkNotNull(pageId);

        this.network.sendToServer(new ServerboundPageOpenRequestPacket(pageId));
    }

    public void requestNewPage(String id, int index, String title) {
        this.network.sendToServer(new ServerboundPageChangeRequestPacket(id, index, title, false));
    }

    /**
     * Creates a player guide of this player (the server picks its id).
     */
    public void requestNewPlayerPage(String title) {
        this.network.sendToServer(new ServerboundPageChangeRequestPacket("", 0, title, true));
    }

    public void requestSavePage() {
        if (this.page.getContent().length() > ServerboundPageChangeRequestPacket.MAX_CONTENT_LENGTH) {
            this.clientNotificationManager.handlePopup(new PopupNotification("Error", "Guide too long to save!",5));
        } else {
            this.network.sendToServer(new ServerboundPageChangeRequestPacket(this.page));
        }
    }

    public void requestRemovePage(String id) {
        this.network.sendToServer(new ServerboundPageChangeRequestPacket(id));
    }
}
