/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.nick;

import com.almuradev.almura.feature.hud.screen.origin.component.panel.UIPlayerListPanel;
import com.almuradev.almura.feature.hud.screen.origin.component.panel.UIUserPanel;
import com.almuradev.almura.feature.nick.network.ClientboundNucleusNameChangeMappingPacket;
import com.almuradev.almura.feature.nick.network.ClientboundNucleusNameChangeMappingPacketHandler;
import com.almuradev.almura.feature.nick.network.ClientboundNucleusNameMappingsPacket;
import com.almuradev.almura.feature.nick.network.ClientboundNucleusNameMappingsPacketHandler;
import com.almuradev.almura.shared.inject.CommonBinder;
import net.kyori.violet.AbstractModule;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;

public final class NickModule extends AbstractModule implements CommonBinder {

    @Override
    protected void configure() {
        this.packet()
                .bind(ClientboundNucleusNameChangeMappingPacket.class, binder -> {
                    binder.channel(3);
                    binder.handler(ClientboundNucleusNameChangeMappingPacketHandler.class, Platform.Type.CLIENT);
                })
                .bind(ClientboundNucleusNameMappingsPacket.class, binder -> {
                    binder.channel(4);
                    binder.handler(ClientboundNucleusNameMappingsPacketHandler.class, Platform.Type.CLIENT);
                });

        this.facet()
                .add(ServerNickManager.class);

        if (Sponge.getPlatform().getType().isClient()) {
            this.requestStaticInjection(UIPlayerListPanel.class);
        }
    }
}
