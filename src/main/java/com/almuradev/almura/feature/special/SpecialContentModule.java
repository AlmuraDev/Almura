/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.special;

import com.almuradev.almura.feature.special.almanac.item.FarmersAlmanacItem;
import com.almuradev.almura.feature.special.almanac.network.ClientboundWorldPositionInformationPacket;
import com.almuradev.almura.feature.special.almanac.network.ClientboundWorldPositionInformationPacketHandler;
import com.almuradev.almura.shared.inject.CommonBinder;
import net.kyori.violet.AbstractModule;
import org.spongepowered.api.Platform;

public final class SpecialContentModule extends AbstractModule implements CommonBinder {

    @Override
    protected void configure() {
        this.packet().bind(ClientboundWorldPositionInformationPacket.class, binder -> binder.handler
                (ClientboundWorldPositionInformationPacketHandler.class, Platform.Type.CLIENT));
        this.facet().add(SpecialContentFeature.class);

        this.requestStaticInjection(FarmersAlmanacItem.class);
    }
}
