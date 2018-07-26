/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.title;

import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.almura.feature.title.network.ServerboundModifyTitlePacket;
import com.almuradev.almura.feature.title.network.ServerboundTitleGuiRequestPacket;
import com.almuradev.almura.shared.network.NetworkConfig;
import com.almuradev.core.event.Witness;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

@SideOnly(Side.CLIENT)
@Singleton
public final class ClientTitleManager implements Witness {

    private final ChannelBinding.IndexedMessageChannel network;

    private final List<Title> titles = new ArrayList<>();
    private final List<Title> availableTitles = new ArrayList<>();
    private final Map<UUID, Title> selectedTitles = new HashMap<>();

    @Nullable private Title titleContentForDisplay;

    @Inject
    public ClientTitleManager(@ChannelId(NetworkConfig.CHANNEL) final ChannelBinding.IndexedMessageChannel network) {
        this.network = network;
    }

    @SubscribeEvent
    public void onClientConnectedToServerEvent(final FMLNetworkEvent.ClientConnectedToServerEvent event) {
        this.titles.clear();
        this.selectedTitles.clear();
    }

    public void requestManageTitlesGUI() {
        this.network.sendToServer(new ServerboundTitleGuiRequestPacket());
    }

    @Nullable
    public Title getTitle(final String id) {
        return this.titles.stream().filter(title -> title.getId().equalsIgnoreCase(id)).findAny().orElse(null);
    }

    public List<Title> getTitles() {
        return this.titles;
    }

    public List<Title> getAvailableTitles() {
        return this.availableTitles;
    }

    @Nullable
    public Title getSelectedTitleFor(final UUID uniqueId) {
        checkNotNull(uniqueId);

        return this.selectedTitles.get(uniqueId);
    }

    public void putTitles(@Nullable final Set<Title> titles) {
        this.titles.clear();

        if (titles != null) {
            this.titles.addAll(titles);
        }
    }

    public void addAvailableTitles(@Nullable final Set<Title> titles) {
        this.availableTitles.clear();

        if (titles != null) {
            this.availableTitles.addAll(titles);
        }
    }

    public void putSelectedTitles(@Nullable final Map<UUID, String> titles) {
        this.selectedTitles.clear();

        if (titles != null) {
            for (Map.Entry<UUID, String> titleCandidate : titles.entrySet()) {
                final Title title = this.getTitle(titleCandidate.getValue());

                if (title != null) {
                    this.selectedTitles.put(titleCandidate.getKey(), title);
                }
            }
        }
    }

    public void putSelectedTitleFor(final UUID uniqueId, @Nullable final String titleId) {
        checkNotNull(uniqueId);

        if (titleId == null) {
            this.selectedTitles.remove(uniqueId);
        } else {
            final Title title = this.getTitle(titleId);
            if (title == null) {
                return;
            }

            this.selectedTitles.put(uniqueId, title);
        }
    }

    @Nullable
    public Title getTitleContentForDisplay() {
        return this.titleContentForDisplay;
    }

    public void setTitleContentForDisplay(@Nullable final Title titleContentForDisplay) {
        this.titleContentForDisplay = titleContentForDisplay;
    }

    public void addTitle(final String id, final String permission, final String content, final boolean isHidden) {
        checkNotNull(id);
        checkNotNull(permission);
        checkNotNull(content);

        this.network.sendToServer(new ServerboundModifyTitlePacket(TitleModifyType.ADD, id, permission, content, isHidden));
    }

    public void modifyTitle(final String id, final String permission, final String content, final boolean isHidden) {
        checkNotNull(id);
        checkNotNull(permission);
        checkNotNull(content);

        this.network.sendToServer(new ServerboundModifyTitlePacket(TitleModifyType.MODIFY, id, permission, content, isHidden));
    }

    public void deleteTitle(final String id) {
        checkNotNull(id);

        this.network.sendToServer(new ServerboundModifyTitlePacket(id));
    }
}
