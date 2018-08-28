/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.guide;

import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.almura.feature.guide.network.ServerboundGuideOpenRequestPacket;
import com.almuradev.almura.feature.guide.network.ServerboundPageChangeRequestPacket;
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

    public List<PageListEntry> getPageEntries() {
        return Collections.unmodifiableList(this.pageEntries);
    }

    public void setPageEntries(List<PageListEntry> pageEntries, String switchToPage) {
        final Page oldPage = this.page;
        if (this.page != null) {
            this.page = null;
        }

        this.pageEntries.clear();
        this.pageEntries.addAll(pageEntries);

        if (!this.pageEntries.isEmpty()) {
            if (oldPage != null && switchToPage == null) {
                this.pageEntries
                    .stream()
                    .filter(p -> p.getId().equalsIgnoreCase(oldPage.getId()))
                    .findFirst()
                    .ifPresent(p -> this.requestPage(p.getId()));
            } else if (switchToPage != null) {
                this.requestPage(switchToPage);
            } else {
                this.pageEntries
                    .stream()
                    .findFirst()
                    .ifPresent(p -> this.requestPage(p.getId()));
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
        this.network.sendToServer(new ServerboundPageChangeRequestPacket(id, index, title));
    }

    public void requestSavePage() {
        if (this.page.getContent().length() > 8190) {
            this.clientNotificationManager.handlePopup(new PopupNotification("Error", "Guide too long to save!",5));
        } else {
            this.network.sendToServer(new ServerboundPageChangeRequestPacket(this.page));
        }
    }

    public void requestRemovePage(String id) {
        this.network.sendToServer(new ServerboundPageChangeRequestPacket(id));
    }
}
