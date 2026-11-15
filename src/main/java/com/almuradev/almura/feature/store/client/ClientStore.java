/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.store.client;

import com.almuradev.almura.feature.menu.InGameScreens;
import com.almuradev.almura.feature.store.StoreSide;
import com.almuradev.almura.feature.store.network.StoreMessages.ClientboundItems;
import com.almuradev.almura.feature.store.network.StoreMessages.ClientboundOpen;
import com.almuradev.almura.feature.store.network.StoreMessages.ClientboundRegistry;
import com.almuradev.almura.feature.store.network.StoreMessages.ItemView;
import com.almuradev.almura.feature.store.network.StoreMessages.ServerboundOpen;
import com.almuradev.almura.feature.store.network.StoreMessages.StoreInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Stores on the client, as 3.5's {@code ClientStoreManager}: the stores the player may see, each store's listings as the server last sent
 * them, the store and management screens opened and refreshed with them, and the player's requests sent to the server.
 */
public final class ClientStore {

    private static List<StoreInfo> stores = List.of();
    private static final Map<String, List<ItemView>> buying = new HashMap<>(), selling = new HashMap<>();
    // Edit mode, for store admins: on shows the admin pane; kept while the game runs
    private static boolean editMode = true;
    private static @Nullable StoreScreen openStore;
    private static @Nullable StoreManagementScreen openManagement;

    private ClientStore() {
    }

    static List<StoreInfo> stores() {
        return stores;
    }

    static Optional<StoreInfo> store(final String id) {
        return stores.stream().filter(store -> store.id().equals(id)).findFirst();
    }

    static List<ItemView> items(final String id, final StoreSide side) {
        return (side == StoreSide.BUY ? buying : selling).getOrDefault(id, List.of());
    }

    static boolean editMode() {
        return editMode;
    }

    static void setEditMode(final boolean on) {
        editMode = on;
    }

    /**
     * Asks the server to open the store management screen (the Features menu's Manage Stores).
     */
    public static void requestManage() {
        send(new ServerboundOpen(true, ""));
    }

    static void requestOpen(final String id) {
        send(new ServerboundOpen(false, id));
    }

    static void send(final CustomPacketPayload payload) {
        ClientPacketDistributor.sendToServer(payload);
    }

    public static void onRegistry(final ClientboundRegistry registry) {
        stores = List.copyOf(registry.stores());
        final StoreManagementScreen management = shownManagement();
        if (management != null) {
            management.refresh();
        }
        final StoreScreen store = shownStore();
        if (store != null) {
            store.registryChanged();
        }
    }

    public static void onOpen(final ClientboundOpen open) {
        final Minecraft minecraft = Minecraft.getInstance();
        if (open.manage()) {
            final StoreManagementScreen management = shownManagement();
            if (management != null) {
                management.refresh();
                return;
            }
            openManagement = new StoreManagementScreen();
            InGameScreens.open(openManagement, minecraft.gui.screen());
            return;
        }
        final StoreInfo store = store(open.id()).orElse(null);
        if (store == null) {
            return;
        }
        openStore = new StoreScreen(store, open.admin());
        InGameScreens.open(openStore, minecraft.gui.screen());
    }

    public static void onItems(final ClientboundItems items) {
        (items.side() == StoreSide.BUY ? buying : selling).put(items.id(), List.copyOf(items.items()));
        final StoreScreen screen = shownStore();
        if (screen != null && screen.storeId().equals(items.id())) {
            screen.refresh();
        }
    }

    private static @Nullable StoreScreen shownStore() {
        if (openStore != null && !InGameScreens.isShowing(openStore)) {
            openStore = null;
        }
        return openStore;
    }

    private static @Nullable StoreManagementScreen shownManagement() {
        if (openManagement != null && !InGameScreens.isShowing(openManagement)) {
            openManagement = null;
        }
        return openManagement;
    }
}
