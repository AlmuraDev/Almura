/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange;

import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.almura.feature.exchange.network.ServerboundModifyExchangePacket;
import com.almuradev.almura.feature.exchange.network.ServerboundExchangeGuiRequestPacket;
import com.almuradev.almura.shared.network.NetworkConfig;
import com.almuradev.core.event.Witness;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@SideOnly(Side.CLIENT)
public final class ClientExchangeManager implements Witness {

    private final ChannelBinding.IndexedMessageChannel network;

    private final List<Exchange> exchanges = new ArrayList<>();
    private final List<Exchange> availableExchanges = new ArrayList<>();

    @Inject
    public ClientExchangeManager(final @ChannelId(NetworkConfig.CHANNEL) ChannelBinding.IndexedMessageChannel network) {
        this.network = network;
    }

    @SubscribeEvent
    public void onClientConnectedToServerEvent(final FMLNetworkEvent.ClientConnectedToServerEvent event) {
        this.exchanges.clear();
        this.availableExchanges.clear();
    }

    @Nullable
    public Exchange getExchange(final String id) {
        checkNotNull(id);

        return this.exchanges.stream().filter(axs -> axs.getId().equalsIgnoreCase(id)).findAny().orElse(null);
    }

    public List<Exchange> getExchanges() {
        return this.exchanges;
    }

    public void putExchanges(@Nullable final Set<Exchange> exchanges) {
        this.exchanges.clear();

        if (exchanges != null) {
            this.exchanges.addAll(exchanges);
        }
    }

    @Nullable
    public Exchange getAvailableExchange(final String id) {
        checkNotNull(id);

        return this.availableExchanges.stream().filter(axs -> axs.getId().equalsIgnoreCase(id)).findAny().orElse(null);
    }

    public List<Exchange> getAvailableExchanges() {
        return this.availableExchanges;
    }

    public void addAvailableExchanges(@Nullable final Set<Exchange> exchanges) {
        this.availableExchanges.clear();

        if (exchanges != null) {
            this.availableExchanges.addAll(exchanges);
        }
    }

    public void requestExchangeGui(final String id) {
        checkNotNull(id);

        this.network.sendToServer(new ServerboundExchangeGuiRequestPacket(id));
    }

    public void addExchange(final String id, final String name, final String permission, final boolean isHidden) {
        checkNotNull(id);
        checkNotNull(name);
        checkNotNull(permission);

        this.network.sendToServer(new ServerboundModifyExchangePacket(ExchangeModifyType.ADD, id, name, permission, isHidden));
    }

    public void modifyExchange(final String id, final String name, final String permission, final boolean isHidden) {
        checkNotNull(id);
        checkNotNull(name);
        checkNotNull(permission);

        this.network.sendToServer(new ServerboundModifyExchangePacket(ExchangeModifyType.MODIFY, id, name, permission, isHidden));
    }

    public void deleteExchange(final String id) {
        checkNotNull(id);

        this.network.sendToServer(new ServerboundModifyExchangePacket(id));
    }
}
