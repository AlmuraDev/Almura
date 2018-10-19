/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.store;

import com.almuradev.almura.feature.store.client.ClientStoreManager;
import com.almuradev.almura.feature.store.client.gui.StoreListScreen;
import com.almuradev.almura.feature.store.client.gui.StoreManagementScreen;
import com.almuradev.almura.feature.store.client.gui.StoreScreen;
import com.almuradev.almura.feature.store.network.ClientboundListItemsResponsePacket;
import com.almuradev.almura.feature.store.network.ServerboundItemTransactionPacket;
import com.almuradev.almura.feature.store.network.ServerboundListItemsRequestPacket;
import com.almuradev.almura.feature.store.network.ClientboundStoreGuiResponsePacket;
import com.almuradev.almura.feature.store.network.ClientboundStoresRegistryPacket;
import com.almuradev.almura.feature.store.network.ServerboundModifyItemsPacket;
import com.almuradev.almura.feature.store.network.ServerboundModifyStorePacket;
import com.almuradev.almura.feature.store.network.handler.ClientboundStoreGuiResponsePacketHandler;
import com.almuradev.almura.feature.store.network.handler.ClientboundListItemsResponsePacketHandler;
import com.almuradev.almura.feature.store.network.handler.ClientboundStoresRegistryPacketHandler;
import com.almuradev.almura.feature.store.network.handler.ServerboundItemTransactionPacketHandler;
import com.almuradev.almura.feature.store.network.handler.ServerboundModifyItemsPacketHandler;
import com.almuradev.almura.feature.store.network.handler.ServerboundModifyStorePacketHandler;
import com.almuradev.almura.feature.store.network.handler.ServerboundListItemsRequestPacketHandler;
import com.almuradev.almura.shared.inject.ClientBinder;
import com.almuradev.almura.shared.inject.CommonBinder;
import net.kyori.violet.AbstractModule;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.Platform;

public final class StoreModule extends AbstractModule implements CommonBinder {

    @Override
    protected void configure() {
        this.requestStaticInjection(StoreCommandsCreator.class);

        this.command().child(StoreCommandsCreator.createCommand(), "store");
        
        this.facet().add(ServerStoreManager.class);

        this.packet()
            .bind(ClientboundStoresRegistryPacket.class,
                binder -> binder.handler(ClientboundStoresRegistryPacketHandler.class, Platform.Type.CLIENT))

            .bind(ClientboundStoreGuiResponsePacket.class,
                binder -> binder.handler(ClientboundStoreGuiResponsePacketHandler.class, Platform.Type.CLIENT))

            .bind(ServerboundModifyStorePacket.class,
                binder -> binder.handler(ServerboundModifyStorePacketHandler.class, Platform.Type.SERVER))

            .bind(ClientboundListItemsResponsePacket.class,
                binder -> binder.handler(ClientboundListItemsResponsePacketHandler.class, Platform.Type.CLIENT))

            .bind(ServerboundListItemsRequestPacket.class,
                binder -> binder.handler(ServerboundListItemsRequestPacketHandler.class, Platform.Type.SERVER))

            .bind(ServerboundModifyItemsPacket.class,
                binder -> binder.handler(ServerboundModifyItemsPacketHandler.class, Platform.Type.SERVER))

            .bind(ServerboundItemTransactionPacket.class,
                binder -> binder.handler(ServerboundItemTransactionPacketHandler.class, Platform.Type.SERVER));

        this.on(Platform.Type.CLIENT, () -> {
            final class ClientModule extends AbstractModule implements ClientBinder {

                @SideOnly(Side.CLIENT)
                @Override
                protected void configure() {
                    this.facet().add(ClientStoreManager.class);
                    this.requestStaticInjection(StoreScreen.class);
                    this.requestStaticInjection(StoreManagementScreen.class);
                    this.requestStaticInjection(StoreListScreen.class);
                }
            }
            this.install(new ClientModule());
        });
    }
}
