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
import com.almuradev.almura.shared.client.keyboard.binder.KeyBindingEntry;
import com.almuradev.almura.shared.event.Witness;
import com.almuradev.almura.shared.network.NetworkConfig;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

@SideOnly(Side.CLIENT)
@Singleton
public final class ClientPageManager implements Witness {

    private final ChannelBinding.IndexedMessageChannel network;
    private final KeyBinding guideOpenBinding;

    private Set<PageListEntry> pageEntries = new HashSet<>();
    private Page page;

    @Inject
    public ClientPageManager(final @ChannelId(NetworkConfig.CHANNEL) ChannelBinding.IndexedMessageChannel network, final Set<KeyBindingEntry>
            keybindings) {
        this.network = network;
        this.guideOpenBinding = keybindings.stream().map(KeyBindingEntry::getKeybinding).filter((keyBinding -> keyBinding
                .getKeyDescription().equalsIgnoreCase("key.almura.guide.open"))).findFirst().orElse(null);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (guideOpenBinding.isPressed()) {
            this.network.sendToServer(new ServerboundGuideOpenRequestPacket());
        }
    }

    public Set<PageListEntry> getPageEntries() {
        return Collections.unmodifiableSet(this.pageEntries);
    }

    public void setPageEntries(Set<PageListEntry> pageEntries, String switchToPage) {
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
        this.network.sendToServer(new ServerboundPageChangeRequestPacket(this.page));
    }

    public void requestRemovePage(String id) {
        this.network.sendToServer(new ServerboundPageChangeRequestPacket(id));
    }
}
