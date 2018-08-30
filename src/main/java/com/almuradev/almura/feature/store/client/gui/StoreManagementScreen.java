package com.almuradev.almura.feature.store.client.gui;

import com.almuradev.almura.feature.store.Store;
import com.almuradev.almura.feature.store.client.ClientStoreManager;
import com.almuradev.almura.shared.client.ui.screen.IngameFeatureManagementScreen;
import net.minecraft.client.resources.I18n;

import java.util.Collections;

import javax.inject.Inject;

public class StoreManagementScreen extends IngameFeatureManagementScreen<Store> {

    @Inject private static ClientStoreManager storeManager;

    public StoreManagementScreen() {
        super(I18n.format("almura.feature.common.title.management", I18n.format("almura.feature.store.name")),
                I18n.format("almura.feature.store.name"),
                // onRefresh
                screen -> {
                    screen.setItems(Collections.unmodifiableList(storeManager.getStores()));
                },
                // onDelete
                (screen, feature) -> {
                    feature.ifPresent(f -> storeManager.deleteStore(f.getId()));
                },
                // onOpen
                (screen, feature) -> {
                    feature.ifPresent(f -> storeManager.requestStoreSpecificGui(f.getId()));
                },
                // onSave
                (screen, feature) -> {
                    if (feature.isPresent()) { // It's a new listing if true
                        storeManager.addStore(screen.getPendingId(), screen.getPendingTitle(), screen.getPendingPermission(),
                                screen.getPendingHiddenState());
                    } else {
                        storeManager.modifyStore(screen.getPendingId(), screen.getPendingTitle(), screen.getPendingPermission(),
                                screen.getPendingHiddenState());
                    }
                });
    }
}
