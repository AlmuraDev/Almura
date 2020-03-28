/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.membership;

import com.almuradev.almura.feature.complex.block.membershipexchange.MembershipExchange;
import com.almuradev.almura.feature.membership.client.gui.MembershipGui;
import com.almuradev.almura.feature.membership.client.gui.PurchaseConfirmGui;
import com.almuradev.almura.feature.membership.network.ClientboundMembershipGuiOpenPacket;
import com.almuradev.almura.feature.membership.network.ServerboundMembershipGuiOpenRequestPacket;
import com.almuradev.almura.feature.membership.network.ServerboundMembershipPurchaseRequestPacket;
import com.almuradev.almura.feature.membership.network.handler.ClientboundMembershipGuiOpenPacketHandler;
import com.almuradev.almura.feature.membership.network.handler.ServerboundMembershipGuiOpenRequestPacketHandler;
import com.almuradev.almura.feature.membership.network.handler.ServerboundMembershipPurchaseRequestPacketHandler;
import com.almuradev.almura.shared.inject.ClientBinder;
import com.almuradev.almura.shared.inject.CommonBinder;
import net.kyori.violet.AbstractModule;
import org.spongepowered.api.Platform;

public final class MembershipModule extends AbstractModule implements CommonBinder {

    @Override
    protected void configure() {

        this.packet().bind(ServerboundMembershipGuiOpenRequestPacket.class, binder -> binder.handler(ServerboundMembershipGuiOpenRequestPacketHandler.class, Platform.Type.SERVER));
        this.packet().bind(ServerboundMembershipPurchaseRequestPacket.class, binder -> binder.handler(ServerboundMembershipPurchaseRequestPacketHandler.class, Platform.Type.SERVER));

        this.packet().bind(ClientboundMembershipGuiOpenPacket.class, binder -> binder.handler(ClientboundMembershipGuiOpenPacketHandler.class, Platform.Type.CLIENT));

        this.requestStaticInjection(MembershipExchange.class);

        this.facet().add(MembershipHandler.class);

        this.on(Platform.Type.CLIENT, () -> {
            final class ClientModule extends AbstractModule implements ClientBinder {
                @Override
                @SuppressWarnings("UnnecessaryStaticInjection") // HACK: inject into required mixin target classes
                protected void configure() {
                    this.requestStaticInjection(MembershipGui.class);
                    this.requestStaticInjection(PurchaseConfirmGui.class);
                }
            }
            this.install(new ClientModule());
        });
    }
}
