/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.claim;

import com.almuradev.almura.feature.claim.network.ClientboundClaimNamePacket;
import com.almuradev.almura.feature.claim.network.handler.ClientboundClaimNamePacketHandler;
import com.almuradev.almura.shared.inject.ClientBinder;
import com.almuradev.almura.shared.inject.CommonBinder;
import net.kyori.violet.AbstractModule;
import org.spongepowered.api.Platform;

public final class ClaimModule extends AbstractModule implements CommonBinder {

    @Override
    protected void configure() {
        this.packet()
                .bind(ClientboundClaimNamePacket.class, binder -> binder.handler(ClientboundClaimNamePacketHandler.class, Platform.Type.CLIENT));
        this.facet().add(ClaimManager.class);
    }
}
