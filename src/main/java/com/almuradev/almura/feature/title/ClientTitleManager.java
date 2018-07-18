/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.title;

import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.almura.feature.title.network.ServerboundPlayerTitlesRequestPacket;
import com.almuradev.almura.shared.network.NetworkConfig;
import com.almuradev.core.event.Witness;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

@SideOnly(Side.CLIENT)
@Singleton
public final class ClientTitleManager implements Witness {

    private final Map<UUID, String> selectedTitlesById = new HashMap<>();
    public String temporaryTitle = "";

    private final ChannelBinding.IndexedMessageChannel network;

    @Inject
    public ClientTitleManager(@ChannelId(NetworkConfig.CHANNEL) final ChannelBinding.IndexedMessageChannel network) {
        this.network = network;
    }

    @SubscribeEvent
    public void onClientConnectedToServerEvent(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        this.selectedTitlesById.clear();
    }

    @Nullable
    public String getTitle(UUID uniqueId) {
        checkNotNull(uniqueId);

        // Return temporary title set within TitleGUI

        if (temporaryTitle != null && !temporaryTitle.isEmpty() && uniqueId == Minecraft.getMinecraft().player.getUniqueID()) {
            return temporaryTitle;
        }

        final String title = this.selectedTitlesById.get(uniqueId);
        if (title == null) {
            return null;
        }

        return title;
    }

    public void putSelectedTitles(Map<UUID, String> titles) {
        checkNotNull(titles);

        this.selectedTitlesById.clear();

        this.selectedTitlesById.putAll(titles);
    }

    public void putSelectedTitle(UUID uniqueId, String title) {
        checkNotNull(uniqueId);
        checkNotNull(title);

        this.selectedTitlesById.put(uniqueId, title);
    }

    public void removeSelectedTitle(UUID uniqueId) {
        checkNotNull(uniqueId);

        this.selectedTitlesById.remove(uniqueId);
    }

    public void requestTitleGUI() {
        this.network.sendToServer(new ServerboundPlayerTitlesRequestPacket());
    }
}
