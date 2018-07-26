/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.claim;

import com.almuradev.almura.shared.inject.ClientBinder;
import com.almuradev.almura.shared.inject.CommonBinder;
import io.github.nucleuspowered.nucleus.api.service.NucleusNicknameService;
import net.kyori.violet.AbstractModule;
import org.spongepowered.api.Platform;

public final class ClaimModule extends AbstractModule implements CommonBinder {

    @Override
    protected void configure() {
        //this.packet().bind(ServerboundClaimRequestPacket.class, binder -> binder.handler(ServerboundClaimRequestPacketHandler.class, Platform.Type.SERVER));
        //this.packet().bind(ClientboundClaimPacket.class, binder -> binder.handler(ClientboundClaimPacketHandler.class, Platform.Type.CLIENT));

        //this.facet().add(ClaimHandler.class);  // Don't load this yet, if SSP environment is missing the API at the moment this will cause crash.
        // Todo: can you suggest a lamba to hide this behind a:
        // this.game.getServiceManager().provide(GriefPreventionAPI.class).ifPresent((service) -> {}


        this.on(Platform.Type.CLIENT, () -> {
            final class ClientModule extends AbstractModule implements ClientBinder {
                @Override
                protected void configure() {
                    //this.requestStaticInjection(ClaimHUD.class);
                }
            }
            this.install(new ClientModule());
        });

    }
}
