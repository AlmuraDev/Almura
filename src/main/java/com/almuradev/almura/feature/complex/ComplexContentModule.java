/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.complex;

import com.almuradev.almura.feature.complex.block.coinexchange.CoinExchange;
import com.almuradev.almura.feature.complex.block.membershipexchange.MembershipExchange;
import com.almuradev.almura.feature.complex.item.almanac.item.FarmersAlmanacItem;
import com.almuradev.almura.feature.complex.item.almanac.network.ClientboundWorldPositionInformationPacket;
import com.almuradev.almura.feature.complex.item.almanac.network.ClientboundWorldPositionInformationPacketHandler;
import com.almuradev.almura.shared.inject.CommonBinder;
import net.kyori.violet.AbstractModule;
import org.spongepowered.api.Platform;

public final class ComplexContentModule extends AbstractModule implements CommonBinder {

    @Override
    protected void configure() {
        this.packet().bind(ClientboundWorldPositionInformationPacket.class, binder -> binder.handler
                (ClientboundWorldPositionInformationPacketHandler.class, Platform.Type.CLIENT));
        this.facet().add(ComplexContentFeature.class);

        this.requestStaticInjection(FarmersAlmanacItem.class);
        this.requestStaticInjection(CoinExchange.class);
        this.requestStaticInjection(MembershipExchange.class);
    }
}
