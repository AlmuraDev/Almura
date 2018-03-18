/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange;

import com.almuradev.almura.feature.exchange.network.ClientboundExchangeOpenResponsePacket;
import com.almuradev.almura.feature.exchange.network.ServerboundExchangeOpenRequestPacket;
import com.almuradev.almura.feature.exchange.network.handler.ClientboundExchangeOpenResponsePacketHandler;
import com.almuradev.almura.feature.exchange.network.handler.ServerboundExchangeOpenRequestPacketHandler;
import com.almuradev.almura.shared.inject.ClientBinder;
import com.almuradev.almura.shared.inject.CommonBinder;
import net.kyori.violet.AbstractModule;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.spongepowered.api.Platform;

public final class ExchangeModule extends AbstractModule implements CommonBinder {

    @Override
    protected void configure() {
        this.packet()
                .bind(ServerboundExchangeOpenRequestPacket.class, binder -> binder.handler(ServerboundExchangeOpenRequestPacketHandler.class, Platform
                        .Type.SERVER))
                .bind(ClientboundExchangeOpenResponsePacket.class, binder -> binder.handler(ClientboundExchangeOpenResponsePacketHandler.class, Platform
                        .Type.CLIENT));

        this.on(Platform.Type.CLIENT, () -> {
            final class ClientModule extends AbstractModule implements ClientBinder {

                @SideOnly(Side.CLIENT)
                @Override
                protected void configure() {

                    this.facet().add(ClientExchangeManager.class);

                    this.keybinding().key(Keyboard.KEY_H, "key.almura.exchange.open", "key.categories.almura.exchange");
                }
            }
            this.install(new ClientModule());
        });
    }
}
