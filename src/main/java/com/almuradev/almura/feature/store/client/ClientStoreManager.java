/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.store.client;

import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.store.Store;
import com.almuradev.almura.feature.store.StoreModifyType;
import com.almuradev.almura.feature.store.network.ServerboundModifyStorePacket;
import com.almuradev.almura.feature.store.network.ServerboundStoreSpecificOfferRequestPacket;
import com.almuradev.almura.shared.network.NetworkConfig;
import com.almuradev.core.event.Witness;
import com.google.inject.Inject;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Singleton;

@Singleton
public final class ClientStoreManager implements Witness {

    private final ChannelBinding.IndexedMessageChannel network;
    private final List<Store> stores = new ArrayList<>();

    @Inject
    public ClientStoreManager(@ChannelId(NetworkConfig.CHANNEL) final ChannelBinding.IndexedMessageChannel network) {
        this.network = network;
    }

    @SubscribeEvent
    public void onClientConnectedToServerEvent(final FMLNetworkEvent.ClientConnectedToServerEvent event) {
        this.stores.clear();
    }

    public void requestStoreManage() {
        Minecraft.getMinecraft().player.sendChatMessage("/" + Almura.ID + " store manage");
    }

    public void requestStoreSpecificGui(final String id) {
        checkNotNull(id);

        Minecraft.getMinecraft().player.sendChatMessage("/" + Almura.ID + " store open " + id);
    }

    public void requestStoreSpecificOfferGui(final String id) {
        checkNotNull(id);

        this.network.sendToServer(new ServerboundStoreSpecificOfferRequestPacket(id));
    }

    public void addStore(final String id, final String name, final String permission, final boolean isHidden) {
        checkNotNull(id);
        checkNotNull(name);
        checkNotNull(permission);

        this.network.sendToServer(new ServerboundModifyStorePacket(StoreModifyType.ADD, id, name, permission, isHidden));
    }

    public void modifyStore(final String id, final String name, final String permission, final boolean isHidden) {
        checkNotNull(id);
        checkNotNull(name);
        checkNotNull(permission);

        this.network.sendToServer(new ServerboundModifyStorePacket(StoreModifyType.MODIFY, id, name, permission, isHidden));
    }

    public void deleteStore(final String id) {
        checkNotNull(id);

        this.network.sendToServer(new ServerboundModifyStorePacket(id));
    }

    public void handleStoreRegistry(@Nullable final Set<Store> stores) {

    }

    public void handleStoreManage() {

    }

    public void handleStoreSpecific(final String id) {

    }

    public void handleStoreSpecificOffer(final String id) {

    }
}
