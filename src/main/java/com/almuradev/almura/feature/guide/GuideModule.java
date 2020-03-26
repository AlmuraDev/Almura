/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.guide;

import com.almuradev.almura.feature.guide.client.gui.GuidePageCreateScreen;
import com.almuradev.almura.feature.guide.client.gui.GuidePageDetailsScreen;
import com.almuradev.almura.feature.guide.client.gui.GuidePageViewScreen;
import com.almuradev.almura.feature.guide.network.ClientboundGuideOpenResponsePacket;
import com.almuradev.almura.feature.guide.network.ClientboundPageChangeResponsePacket;
import com.almuradev.almura.feature.guide.network.ClientboundPageListingsPacket;
import com.almuradev.almura.feature.guide.network.ClientboundPageOpenResponsePacket;
import com.almuradev.almura.feature.guide.network.ServerboundGuideOpenRequestPacket;
import com.almuradev.almura.feature.guide.network.ServerboundPageChangeRequestPacket;
import com.almuradev.almura.feature.guide.network.ServerboundPageOpenRequestPacket;
import com.almuradev.almura.feature.guide.network.handler.ClientboundGuideOpenResponsePacketHandler;
import com.almuradev.almura.feature.guide.network.handler.ClientboundPageChangeResponsePacketHandler;
import com.almuradev.almura.feature.guide.network.handler.ClientboundPageListingsPacketHandler;
import com.almuradev.almura.feature.guide.network.handler.ClientboundPageOpenResponsePacketHandler;
import com.almuradev.almura.feature.guide.network.handler.ServerboundGuideOpenRequestPacketHandler;
import com.almuradev.almura.feature.guide.network.handler.ServerboundPageChangeRequestPacketHandler;
import com.almuradev.almura.feature.guide.network.handler.ServerboundPageOpenRequestPacketHandler;
import com.almuradev.almura.feature.menu.SimpleOptionsMenu;
import com.almuradev.almura.shared.inject.ClientBinder;
import com.almuradev.almura.shared.inject.CommonBinder;
import net.kyori.violet.AbstractModule;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.Platform;

public final class GuideModule extends AbstractModule implements CommonBinder {

    @Override
    protected void configure() {
        this.command().child(GuideCommands.generateGuideCommand(), "guide");
        this.packet()
                .bind(ServerboundGuideOpenRequestPacket.class,
                        binder -> binder.handler(ServerboundGuideOpenRequestPacketHandler.class, Platform.Type.SERVER))
                .bind(ClientboundGuideOpenResponsePacket.class,
                        binder -> binder.handler(ClientboundGuideOpenResponsePacketHandler.class, Platform.Type.CLIENT))
                .bind(ServerboundPageOpenRequestPacket.class,
                        binder -> binder.handler(ServerboundPageOpenRequestPacketHandler.class, Platform.Type.SERVER))
                .bind(ClientboundPageOpenResponsePacket.class,
                        binder -> binder.handler(ClientboundPageOpenResponsePacketHandler.class, Platform.Type.CLIENT))
                .bind(ClientboundPageListingsPacket.class,
                        binder -> binder.handler(ClientboundPageListingsPacketHandler.class, Platform.Type.CLIENT))
                .bind(ServerboundPageChangeRequestPacket.class,
                        binder -> binder.handler(ServerboundPageChangeRequestPacketHandler.class, Platform.Type.SERVER))
                .bind(ClientboundPageChangeResponsePacket.class,
                        binder -> binder.handler(ClientboundPageChangeResponsePacketHandler.class, Platform.Type.CLIENT));

        this.facet().add(ServerPageManager.class);
        this.requestStaticInjection(GuideCommands.class);
        this.requestStaticInjection(ServerPageManager.class);
        this.requestStaticInjection(ClientboundGuideOpenResponsePacketHandler.class);
        
        this.on(Platform.Type.CLIENT, () -> {
            @SideOnly(Side.CLIENT)
            final class ClientModule extends AbstractModule implements ClientBinder {

                @SideOnly(Side.CLIENT)
                @Override
                protected void configure() {
                    this.requestStaticInjection(GuidePageViewScreen.class);
                    this.requestStaticInjection(GuidePageCreateScreen.class);
                    this.requestStaticInjection(GuidePageDetailsScreen.class);

                    this.facet().add(ClientPageManager.class);
                }
            }
            this.install(new ClientModule());
        });
    }
}
