/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange;

import com.almuradev.almura.feature.exchange.client.gui.ExchangeManagementScreen;
import com.almuradev.almura.feature.exchange.client.gui.ExchangeScreen;
import com.almuradev.almura.feature.exchange.network.ClientboundForSaleItemsResponsePacket;
import com.almuradev.almura.feature.exchange.network.ClientboundExchangeGuiResponsePacket;
import com.almuradev.almura.feature.exchange.network.ClientboundListItemsResponsePacket;
import com.almuradev.almura.feature.exchange.network.ClientboundExchangeRegistryPacket;
import com.almuradev.almura.feature.exchange.network.ServerboundForSaleItemRequestPacket;
import com.almuradev.almura.feature.exchange.network.ServerboundExchangeGuiRequestPacket;
import com.almuradev.almura.feature.exchange.network.ServerboundExchangeTransactionRequestPacket;
import com.almuradev.almura.feature.exchange.network.ServerboundModifyExchangePacket;
import com.almuradev.almura.feature.exchange.network.ServerboundListItemsRequestPacket;
import com.almuradev.almura.feature.exchange.network.handler.ClientboundForSaleItemsResponsePacketHandler;
import com.almuradev.almura.feature.exchange.network.handler.ClientboundExchangeGuiResponsePacketHandler;
import com.almuradev.almura.feature.exchange.network.handler.ClientboundListItemsResponsePacketHandler;
import com.almuradev.almura.feature.exchange.network.handler.ClientboundExchangesRegistryPacketHandler;
import com.almuradev.almura.feature.exchange.network.handler.ServerboundForSaleItemsRequestPacketHandler;
import com.almuradev.almura.feature.exchange.network.handler.ServerboundExchangeGuiRequestPacketHandler;
import com.almuradev.almura.feature.exchange.network.handler.ServerboundExchangeTransactionRequestPacketHandler;
import com.almuradev.almura.feature.exchange.network.handler.ServerboundModifyExchangePacketHandler;
import com.almuradev.almura.feature.exchange.network.handler.ServerboundListItemsRequestPacketHandler;
import com.almuradev.almura.shared.inject.ClientBinder;
import com.almuradev.almura.shared.inject.CommonBinder;
import net.kyori.violet.AbstractModule;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.Platform;

public final class ExchangeModule extends AbstractModule implements CommonBinder {

    @Override
    protected void configure() {
        this.packet()
            .bind(ClientboundExchangeRegistryPacket.class,
                binder -> binder.handler(ClientboundExchangesRegistryPacketHandler.class, Platform.Type.CLIENT))

            .bind(ServerboundExchangeGuiRequestPacket.class,
                binder -> binder.handler(ServerboundExchangeGuiRequestPacketHandler.class, Platform.Type.SERVER))

            .bind(ClientboundExchangeGuiResponsePacket.class,
                binder -> binder.handler(ClientboundExchangeGuiResponsePacketHandler.class, Platform.Type.CLIENT))

            .bind(ServerboundModifyExchangePacket.class,
                binder -> binder.handler(ServerboundModifyExchangePacketHandler.class, Platform.Type.SERVER))

            .bind(ClientboundListItemsResponsePacket.class,
                binder -> binder.handler(ClientboundListItemsResponsePacketHandler.class, Platform.Type.CLIENT))

            .bind(ServerboundListItemsRequestPacket.class,
                binder -> binder.handler(ServerboundListItemsRequestPacketHandler.class, Platform.Type.SERVER))

            .bind(ClientboundForSaleItemsResponsePacket.class,
                binder -> binder.handler(ClientboundForSaleItemsResponsePacketHandler.class, Platform.Type.CLIENT))

            .bind(ServerboundForSaleItemRequestPacket.class,
                binder -> binder.handler(ServerboundForSaleItemsRequestPacketHandler.class, Platform.Type.SERVER))

            .bind(ServerboundExchangeTransactionRequestPacket.class,
                binder -> binder.handler(ServerboundExchangeTransactionRequestPacketHandler.class, Platform.Type.SERVER));
        
        this.facet().add(ServerExchangeManager.class);

        this.on(Platform.Type.CLIENT, () -> {

            @SideOnly(Side.CLIENT)
            final class ClientModule extends AbstractModule implements ClientBinder {

                @SideOnly(Side.CLIENT)
                @Override
                protected void configure() {
                    this.facet().add(ClientExchangeManager.class);
                    this.requestStaticInjection(ExchangeScreen.class);
                    this.requestStaticInjection(ExchangeManagementScreen.class);
                }
            }
            this.install(new ClientModule());
        });
    }
}
