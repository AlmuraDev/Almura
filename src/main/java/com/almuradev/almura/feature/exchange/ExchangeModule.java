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
import com.almuradev.almura.feature.exchange.network.ClientboundExchangeGuiResponsePacket;
import com.almuradev.almura.feature.exchange.network.ClientboundExchangeListItemsResponsePacket;
import com.almuradev.almura.feature.exchange.network.ClientboundExchangeRegistryPacket;
import com.almuradev.almura.feature.exchange.network.ServerboundExchangeGuiRequestPacket;
import com.almuradev.almura.feature.exchange.network.ServerboundModifyExchangePacket;
import com.almuradev.almura.feature.exchange.network.ServerboundModifyListItemsPacket;
import com.almuradev.almura.feature.exchange.network.handler.ClientboundExchangeGuiResponsePacketHandler;
import com.almuradev.almura.feature.exchange.network.handler.ClientboundExchangeListItemsResponsePacketHandler;
import com.almuradev.almura.feature.exchange.network.handler.ClientboundExchangesRegistryPacketHandler;
import com.almuradev.almura.feature.exchange.network.handler.ServerboundExchangeGuiRequestPacketHandler;
import com.almuradev.almura.feature.exchange.network.handler.ServerboundModifyExchangePacketHandler;
import com.almuradev.almura.feature.exchange.network.handler.ServerboundModifyListItemsPacketHandler;
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

            .bind(ClientboundExchangeListItemsResponsePacket.class,
                binder -> binder.handler(ClientboundExchangeListItemsResponsePacketHandler.class, Platform.Type.CLIENT))

            .bind(ServerboundModifyListItemsPacket.class,
                binder -> binder.handler(ServerboundModifyListItemsPacketHandler.class, Platform.Type.CLIENT));
        
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
