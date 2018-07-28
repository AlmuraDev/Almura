/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.claim;

import com.almuradev.almura.feature.claim.gui.ManageClaimGUI;
import com.almuradev.almura.feature.claim.network.ClientboundClaimGuiResponsePacket;
import com.almuradev.almura.feature.claim.network.ClientboundClaimDataPacket;
import com.almuradev.almura.feature.claim.network.ServerboundClaimGuiAbandonRequestPacket;
import com.almuradev.almura.feature.claim.network.ServerboundClaimGuiRequestPacket;
import com.almuradev.almura.feature.claim.network.ServerboundClaimGuiSaveRequestPacket;
import com.almuradev.almura.feature.claim.network.handler.ClientboundClaimGuiResponsePacketHandler;
import com.almuradev.almura.feature.claim.network.handler.ClientboundClaimDataPacketHandler;
import com.almuradev.almura.feature.claim.network.handler.ServerboundClaimGuiAbandonRequestPacketHandler;
import com.almuradev.almura.feature.claim.network.handler.ServerboundClaimGuiRequestPacketHandler;
import com.almuradev.almura.feature.claim.network.handler.ServerboundClaimGuiSaveRequestPacketHandler;
import com.almuradev.almura.shared.inject.ClientBinder;
import com.almuradev.almura.shared.inject.CommonBinder;
import net.kyori.violet.AbstractModule;
import org.spongepowered.api.Platform;

public final class ClaimModule extends AbstractModule implements CommonBinder {

    @Override
    protected void configure() {
        this.packet().bind(ClientboundClaimDataPacket.class, binder -> binder.handler(ClientboundClaimDataPacketHandler.class, Platform.Type.CLIENT));
        this.packet().bind(ClientboundClaimGuiResponsePacket.class, binder -> binder.handler(ClientboundClaimGuiResponsePacketHandler.class, Platform.Type.CLIENT));

        this.packet().bind(ServerboundClaimGuiRequestPacket.class, binder -> binder.handler(ServerboundClaimGuiRequestPacketHandler.class, Platform.Type.SERVER));
        this.packet().bind(ServerboundClaimGuiSaveRequestPacket.class, binder -> binder.handler(ServerboundClaimGuiSaveRequestPacketHandler.class, Platform.Type.SERVER));
        this.packet().bind(ServerboundClaimGuiAbandonRequestPacket.class, binder -> binder.handler(ServerboundClaimGuiAbandonRequestPacketHandler.class, Platform.Type.SERVER));

        this.facet().add(ServerClaimManager.class);

        this.on(Platform.Type.CLIENT, () -> {
            final class ClientModule extends AbstractModule implements ClientBinder {
                @Override
                protected void configure() {
                    //Todo: Zidane, I loaded this is FeatureModule as a ClientOnly since it won't contaminate the environment since it doesn't load  GP's API.
                    //this.facet().add(ClientClaimManager.class);
                    this.requestStaticInjection(ManageClaimGUI.class);
                }
            }
            this.install(new ClientModule());
        });
    }
}
