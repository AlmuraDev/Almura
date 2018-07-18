/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.hud;

import com.almuradev.almura.feature.hud.network.ClientboundPlayerCountPacket;
import com.almuradev.almura.feature.hud.network.ClientboundPlayerCurrencyPacket;
import com.almuradev.almura.feature.hud.network.ClientboundWorldNamePacket;
import com.almuradev.almura.feature.hud.network.handler.ClientboundPlayerCountPacketHandler;
import com.almuradev.almura.feature.hud.network.handler.ClientboundPlayerCurrencyPacketHandler;
import com.almuradev.almura.feature.hud.network.handler.ClientboundWorldNamePacketHandler;
import com.almuradev.almura.feature.hud.screen.origin.component.panel.UIDetailsPanel;
import com.almuradev.almura.feature.hud.screen.origin.component.panel.UIUserPanel;
import com.almuradev.almura.feature.hud.screen.origin.component.panel.UIWorldPanel;
import com.almuradev.almura.shared.inject.ClientBinder;
import com.almuradev.almura.shared.inject.CommonBinder;
import net.kyori.violet.AbstractModule;
import net.minecraft.client.gui.GuiIngame;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.Platform;

public final class HeadUpDisplayModule extends AbstractModule implements CommonBinder {

    @Override
    protected void configure() {
        this.packet()
                .bind(ClientboundWorldNamePacket.class, binder -> binder.handler(ClientboundWorldNamePacketHandler.class, Platform.Type.CLIENT))
                .bind(ClientboundPlayerCountPacket.class, binder -> binder.handler(ClientboundPlayerCountPacketHandler.class, Platform.Type.CLIENT))
                .bind(ClientboundPlayerCurrencyPacket.class, binder -> binder.handler(ClientboundPlayerCurrencyPacketHandler.class, Platform.Type.CLIENT));
        this.facet().add(ServerHeadUpDisplayManager.class);

        this.on(Platform.Type.CLIENT, () -> {
            final class ClientModule extends AbstractModule implements ClientBinder {
                @Override
                @SideOnly(Side.CLIENT)
                @SuppressWarnings("UnnecessaryStaticInjection") // HACK: inject into required mixin target classes
                protected void configure() {
                    this.facet().add(ClientHeadUpDisplayManager.class);
                    this.requestStaticInjection(UIDetailsPanel.class);
                    this.requestStaticInjection(UIWorldPanel.class);
                    this.requestStaticInjection(UIUserPanel.class);
                    this.requestStaticInjection(GuiIngame.class);
                }
            }
            this.install(new ClientModule());
        });
    }
}
