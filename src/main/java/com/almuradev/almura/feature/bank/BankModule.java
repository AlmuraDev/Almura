/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.bank;

import com.almuradev.almura.feature.bank.client.ClientBankManager;
import com.almuradev.almura.feature.bank.client.gui.BankMembersScreen;
import com.almuradev.almura.feature.bank.client.gui.BankScreen;
import com.almuradev.almura.feature.bank.network.ClientboundBankHistoryPacket;
import com.almuradev.almura.feature.bank.network.ClientboundBankStatePacket;
import com.almuradev.almura.feature.bank.network.ServerboundBankActionPacket;
import com.almuradev.almura.feature.bank.network.handler.ClientboundBankHistoryPacketHandler;
import com.almuradev.almura.feature.bank.network.handler.ClientboundBankStatePacketHandler;
import com.almuradev.almura.feature.bank.network.handler.ServerboundBankActionPacketHandler;
import com.almuradev.almura.shared.inject.ClientBinder;
import com.almuradev.almura.shared.inject.CommonBinder;
import net.kyori.violet.AbstractModule;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.Platform;

/**
 * Banking: player owned (and shared) bank accounts, opened with /bank or the Coin Exchange block.
 */
public final class BankModule extends AbstractModule implements CommonBinder {

    @Override
    protected void configure() {
        this.packet()
            .bind(ServerboundBankActionPacket.class, binder -> binder.handler(ServerboundBankActionPacketHandler.class, Platform.Type.SERVER))
            .bind(ClientboundBankStatePacket.class, binder -> binder.handler(ClientboundBankStatePacketHandler.class, Platform.Type.CLIENT))
            .bind(ClientboundBankHistoryPacket.class, binder -> binder.handler(ClientboundBankHistoryPacketHandler.class, Platform.Type.CLIENT));

        this.facet().add(ServerBankManager.class);
        this.requestStaticInjection(BankCommands.class);
        this.command().root(BankCommands.createCommand(), "bank");

        this.on(Platform.Type.CLIENT, () -> {
            @SideOnly(Side.CLIENT)
            final class ClientModule extends AbstractModule implements ClientBinder {

                @SideOnly(Side.CLIENT)
                @Override
                protected void configure() {
                    this.facet().add(ClientBankManager.class);
                    this.requestStaticInjection(BankScreen.class);
                    this.requestStaticInjection(BankMembersScreen.class);
                }
            }
            this.install(new ClientModule());
        });
    }
}
