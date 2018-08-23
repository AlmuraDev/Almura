/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange;

import com.almuradev.almura.feature.exchange.client.ClientExchangeManager;
import com.almuradev.almura.feature.exchange.client.gui.ExchangeBuyQuantityScreen;
import com.almuradev.almura.feature.exchange.client.gui.ExchangeListPriceScreen;
import com.almuradev.almura.feature.exchange.client.gui.ExchangeManagementScreen;
import com.almuradev.almura.feature.exchange.client.gui.ExchangeOfferScreen;
import com.almuradev.almura.feature.exchange.client.gui.ExchangeScreen;
import com.almuradev.almura.feature.exchange.network.ClientboundExchangeGuiResponsePacket;
import com.almuradev.almura.feature.exchange.network.ClientboundExchangeRegistryPacket;
import com.almuradev.almura.feature.exchange.network.ClientboundForSaleFilterRequestPacket;
import com.almuradev.almura.feature.exchange.network.ClientboundForSaleItemsResponsePacket;
import com.almuradev.almura.feature.exchange.network.ClientboundListItemsResponsePacket;
import com.almuradev.almura.feature.exchange.network.ClientboundListItemsSaleStatusPacket;
import com.almuradev.almura.feature.exchange.network.ServerboundExchangeGuiRequestPacket;
import com.almuradev.almura.feature.exchange.network.ServerboundForSaleFilterResponsePacket;
import com.almuradev.almura.feature.exchange.network.ServerboundTransactionRequestPacket;
import com.almuradev.almura.feature.exchange.network.ServerboundListItemsRequestPacket;
import com.almuradev.almura.feature.exchange.network.ServerboundModifyExchangePacket;
import com.almuradev.almura.feature.exchange.network.ServerboundModifyForSaleItemListStatusRequestPacket;
import com.almuradev.almura.feature.exchange.network.handler.ClientboundExchangeGuiResponsePacketHandler;
import com.almuradev.almura.feature.exchange.network.handler.ClientboundExchangesRegistryPacketHandler;
import com.almuradev.almura.feature.exchange.network.handler.ClientboundForSaleFilterRequestPacketHandler;
import com.almuradev.almura.feature.exchange.network.handler.ClientboundForSaleItemsResponsePacketHandler;
import com.almuradev.almura.feature.exchange.network.handler.ClientboundListItemsResponsePacketHandler;
import com.almuradev.almura.feature.exchange.network.handler.ClientboundListItemsSaleStatusPacketHandler;
import com.almuradev.almura.feature.exchange.network.handler.ServerboundExchangeGuiRequestPacketHandler;
import com.almuradev.almura.feature.exchange.network.handler.ServerboundForSaleFilterResponsePacketHandler;
import com.almuradev.almura.feature.exchange.network.handler.ServerboundTransactionRequestPacketHandler;
import com.almuradev.almura.feature.exchange.network.handler.ServerboundListItemsRequestPacketHandler;
import com.almuradev.almura.feature.exchange.network.handler.ServerboundModifyExchangePacketHandler;
import com.almuradev.almura.feature.exchange.network.handler.ServerboundModifyForSaleItemListStatusRequestPacketHandler;
import com.almuradev.almura.shared.feature.store.filter.FilterRegistry;
import com.almuradev.almura.shared.feature.store.filter.ListItemFilter;
import com.almuradev.almura.shared.inject.ClientBinder;
import com.almuradev.almura.shared.inject.CommonBinder;
import net.kyori.violet.AbstractModule;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.Platform;

import javax.inject.Inject;

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

            .bind(ClientboundListItemsSaleStatusPacket.class,
                binder -> binder.handler(ClientboundListItemsSaleStatusPacketHandler.class, Platform.Type.CLIENT))

            .bind(ServerboundListItemsRequestPacket.class,
                binder -> binder.handler(ServerboundListItemsRequestPacketHandler.class, Platform.Type.SERVER))

            .bind(ClientboundForSaleFilterRequestPacket.class,
                binder -> binder.handler(ClientboundForSaleFilterRequestPacketHandler.class, Platform.Type.CLIENT))

            .bind(ServerboundForSaleFilterResponsePacket.class,
                binder -> binder.handler(ServerboundForSaleFilterResponsePacketHandler.class, Platform.Type.SERVER))

            .bind(ClientboundForSaleItemsResponsePacket.class,
                binder -> binder.handler(ClientboundForSaleItemsResponsePacketHandler.class, Platform.Type.CLIENT))

            .bind(ServerboundModifyForSaleItemListStatusRequestPacket.class,
                binder -> binder.handler(ServerboundModifyForSaleItemListStatusRequestPacketHandler.class, Platform.Type.SERVER))

            .bind(ServerboundTransactionRequestPacket.class,
                binder -> binder.handler(ServerboundTransactionRequestPacketHandler.class, Platform.Type.SERVER));

        this.facet().add(ServerExchangeManager.class);

        FilterRegistry.instance.register("display_name", (ListItemFilter) (target, value) -> target.asRealStack().getDisplayName().contains(value));
        FilterRegistry.instance.register("seller_name", (ListItemFilter) (target, value) -> target.getSellerName().isPresent() && target
            .getSellerName().get().contains(value));

        this.on(Platform.Type.CLIENT, () -> {

            @SideOnly(Side.CLIENT)
            final class ClientModule extends AbstractModule implements ClientBinder {

                @SideOnly(Side.CLIENT)
                @Override
                protected void configure() {
                    this.facet().add(ClientExchangeManager.class);
                    this.requestStaticInjection(ExchangeBuyQuantityScreen.class);
                    this.requestStaticInjection(ExchangeListPriceScreen.class);
                    this.requestStaticInjection(ExchangeOfferScreen.class);
                    this.requestStaticInjection(ExchangeManagementScreen.class);
                    this.requestStaticInjection(ExchangeScreen.class);
                }
            }
            this.install(new ClientModule());
        });
    }
}
