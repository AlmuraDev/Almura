/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.claim;

import com.almuradev.almura.feature.claim.gui.ClaimManageScreen;
import com.almuradev.almura.feature.claim.network.ClientboundClaimDataPacket;
import com.almuradev.almura.feature.claim.network.ClientboundClaimGuiResponsePacket;
import com.almuradev.almura.feature.claim.network.ServerboundClaimGuiAbandonRequestPacket;
import com.almuradev.almura.feature.claim.network.ServerboundClaimGuiForSaleRequestPacket;
import com.almuradev.almura.feature.claim.network.ServerboundClaimGuiRequestPacket;
import com.almuradev.almura.feature.claim.network.ServerboundClaimGuiSaveRequestPacket;
import com.almuradev.almura.feature.claim.network.ServerboundClaimGuiSetSpawnRequestPacket;
import com.almuradev.almura.feature.claim.network.ServerboundClaimGuiToggleDenyMessagesRequestPacket;
import com.almuradev.almura.feature.claim.network.ServerboundClaimGuiToggleVisualsRequestPacket;
import com.almuradev.almura.feature.claim.network.handler.ClientboundClaimDataPacketHandler;
import com.almuradev.almura.feature.claim.network.handler.ClientboundClaimGuiResponsePacketHandler;
import com.almuradev.almura.feature.claim.network.handler.ServerboundClaimGuiAbandonRequestPacketHandler;
import com.almuradev.almura.feature.claim.network.handler.ServerboundClaimGuiForSaleRequestPacketHandler;
import com.almuradev.almura.feature.claim.network.handler.ServerboundClaimGuiRequestPacketHandler;
import com.almuradev.almura.feature.claim.network.handler.ServerboundClaimGuiSaveRequestPacketHandler;
import com.almuradev.almura.feature.claim.network.handler.ServerboundClaimGuiSetSpawnRequestPacketHandler;
import com.almuradev.almura.feature.claim.network.handler.ServerboundClaimGuiToggleDenyMessagesRequestPacketHandler;
import com.almuradev.almura.feature.claim.network.handler.ServerboundClaimGuiToggleVisualsRequestPacketHandler;
import com.almuradev.almura.shared.inject.ClientBinder;
import com.almuradev.almura.shared.inject.CommonBinder;
import com.almuradev.almura.shared.plugin.Plugin;
import net.kyori.violet.AbstractModule;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.common.SpongeImplHooks;

public final class ClaimModule extends AbstractModule implements CommonBinder {

    @Override
    protected void configure() {
        // Safe to register both during Common
        // Note: the order in which the packets are registered will affect everything, register client/server packets in the same order.
        this.packet().bind(ClientboundClaimDataPacket.class, binder -> binder.handler(ClientboundClaimDataPacketHandler.class, Platform.Type.CLIENT));
        this.packet().bind(ClientboundClaimGuiResponsePacket.class, binder -> binder.handler(ClientboundClaimGuiResponsePacketHandler.class, Platform.Type.CLIENT));

        if (SpongeImplHooks.isDeobfuscatedEnvironment()) {
            loadServerModules();
        } else {
            if (Sponge.getPluginManager().isLoaded("griefdefender")) {
                loadServerModules();
            }
        }
        this.on(Platform.Type.CLIENT, () -> {
            final class ClientModule extends AbstractModule implements ClientBinder {
                @Override
                protected void configure() {

                    this.facet().add(ClientClaimManager.class);

                    if (!SpongeImplHooks.isDeobfuscatedEnvironment()) { //Only register these packets this way IF running production mode
                        System.out.println("Registering Client Packets!!!");
                        this.packet().bind(ServerboundClaimGuiRequestPacket.class);
                        this.packet().bind(ServerboundClaimGuiSaveRequestPacket.class);
                        this.packet().bind(ServerboundClaimGuiAbandonRequestPacket.class);
                        this.packet().bind(ServerboundClaimGuiToggleVisualsRequestPacket.class);
                        this.packet().bind(ServerboundClaimGuiToggleDenyMessagesRequestPacket.class);
                        this.packet().bind(ServerboundClaimGuiForSaleRequestPacket.class);
                        this.packet().bind(ServerboundClaimGuiSetSpawnRequestPacket.class);
                    }

                    this.requestStaticInjection(ClaimManageScreen.class);
                }
            }
            this.install(new ClientModule());
        });
    }

    private void loadServerModules() {
        // Note:  Can't register these during a normal load because the packet handler registration causes the class load of serverClaimManager.
        System.out.println("Registering Server Packets");
        this.packet().bind(ServerboundClaimGuiRequestPacket.class, binder -> binder.handler(ServerboundClaimGuiRequestPacketHandler.class, Platform.Type.SERVER));
        this.packet().bind(ServerboundClaimGuiSaveRequestPacket.class, binder -> binder.handler(ServerboundClaimGuiSaveRequestPacketHandler.class, Platform.Type.SERVER));
        this.packet().bind(ServerboundClaimGuiAbandonRequestPacket.class, binder -> binder.handler(ServerboundClaimGuiAbandonRequestPacketHandler.class, Platform.Type.SERVER));
        this.packet().bind(ServerboundClaimGuiToggleVisualsRequestPacket.class, binder -> binder.handler(ServerboundClaimGuiToggleVisualsRequestPacketHandler.class, Platform.Type.SERVER));
        this.packet().bind(ServerboundClaimGuiToggleDenyMessagesRequestPacket.class, binder -> binder.handler(ServerboundClaimGuiToggleDenyMessagesRequestPacketHandler.class, Platform.Type.SERVER));
        this.packet().bind(ServerboundClaimGuiForSaleRequestPacket.class, binder -> binder.handler(ServerboundClaimGuiForSaleRequestPacketHandler.class, Platform.Type.SERVER));
        this.packet().bind(ServerboundClaimGuiSetSpawnRequestPacket.class, binder -> binder.handler(ServerboundClaimGuiSetSpawnRequestPacketHandler.class, Platform.Type.SERVER));
        this.facet().add(ServerClaimManager.class);
    }
}
