/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.store.client;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.store.Store;
import com.almuradev.almura.feature.store.StoreItemSegmentType;
import com.almuradev.almura.feature.store.StoreModifyType;
import com.almuradev.almura.feature.store.client.gui.StoreManagementScreen;
import com.almuradev.almura.feature.store.client.gui.StoreScreen;
import com.almuradev.almura.feature.store.listing.BuyingItem;
import com.almuradev.almura.feature.store.listing.SellingItem;
import com.almuradev.almura.feature.store.network.ServerboundStoreItemsListRequestPacket;
import com.almuradev.almura.feature.store.network.ServerboundModifyStorePacket;
import com.almuradev.almura.shared.item.VanillaStack;
import com.almuradev.almura.shared.network.NetworkConfig;
import com.almuradev.core.event.Witness;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;

import java.math.BigDecimal;
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

    @Nullable
    private Store getStore(final String id) {
        checkNotNull(id);

        return this.stores.stream().filter(axs -> axs.getId().equalsIgnoreCase(id)).findAny().orElse(null);
    }

    public List<Store> getStores() {
        return this.stores;
    }

    private void putStores(@Nullable final Set<Store> stores) {
        this.stores.clear();

        if (stores != null) {
            this.stores.addAll(stores);
        }
    }

    public void requestStoreManage() {
        Minecraft.getMinecraft().player.sendChatMessage("/" + Almura.ID + " store manage");
    }

    public void requestStoreSpecificGui(final String id) {
        checkNotNull(id);

        Minecraft.getMinecraft().player.sendChatMessage("/" + Almura.ID + " store open " + id);
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

    public void requestListSellingItem(final String id, final VanillaStack stack, final int index, final BigDecimal price) {
        checkNotNull(id);
        checkNotNull(stack);
        checkState(index >= 0);
        checkNotNull(price);
        checkState(price.doubleValue() >= 0);

        this.network.sendToServer(new ServerboundStoreItemsListRequestPacket(id, StoreItemSegmentType.SELLING, Lists.newArrayList(
            new ServerboundStoreItemsListRequestPacket.StoreItemCandidate(stack, index, price))));
    }

    public void requestListBuyingItem(final String id, final VanillaStack stack, final int index, final BigDecimal price) {
        checkNotNull(id);
        checkNotNull(stack);
        checkState(index >= 0);
        checkNotNull(price);
        checkState(price.doubleValue() >= 0);

        this.network.sendToServer(new ServerboundStoreItemsListRequestPacket(id, StoreItemSegmentType.BUYING, Lists.newArrayList(
            new ServerboundStoreItemsListRequestPacket.StoreItemCandidate(stack, index, price))));
    }

    public void handleStoreRegistry(@Nullable final Set<Store> stores) {
        this.putStores(stores);

        final GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;

        if (currentScreen instanceof StoreManagementScreen) {
            ((StoreManagementScreen) currentScreen).refresh();
        }
    }

    public void handleStoreManage() {
        new StoreManagementScreen().display();
    }

    public void handleStoreSpecific(final String id) {
        final Store store = this.getStore(id);

        if (store != null) {
            new StoreScreen(store, true).display();
        }
    }

    public void handleSellingItems(final String id, final List<SellingItem> items) {
        checkNotNull(id);
    }

    public void handleBuyingItems(final String id, final List<BuyingItem> items) {
        checkNotNull(id);
    }
}
