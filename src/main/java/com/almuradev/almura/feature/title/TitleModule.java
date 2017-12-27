/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.title;

import com.almuradev.almura.feature.title.network.ClientboundPlayerSelectedTitlePacket;
import com.almuradev.almura.feature.title.network.ClientboundPlayerSelectedTitlePacketHandler;
import com.almuradev.almura.feature.title.network.ClientboundPlayerSelectedTitlesPacket;
import com.almuradev.almura.feature.title.network.ClientboundPlayerSelectedTitlesPacketHandler;
import com.almuradev.almura.shared.inject.CommonBinder;
import net.kyori.violet.AbstractModule;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;

public final class TitleModule extends AbstractModule implements CommonBinder {

    @Override
    protected void configure() {
        this.command()
                .child(TitleCommands.generateTitleCommand(), "title");
        this.packet()
                .bind(ClientboundPlayerSelectedTitlePacket.class, binder -> {
                    binder.channel(5);
                    binder.handler(ClientboundPlayerSelectedTitlePacketHandler.class, Platform.Type.CLIENT);
                })
                .bind(ClientboundPlayerSelectedTitlesPacket.class, binder -> {
                    binder.channel(6);
                    binder.handler(ClientboundPlayerSelectedTitlesPacketHandler.class, Platform.Type.CLIENT);
                });
        this.facet()
                .add(ServerTitleManager.class);
        this.requestStaticInjection(TitleCommands.class);
        if (Sponge.getPlatform().getType().isClient()) {
            this.requestMixinInjection();
        }
    }

    @SideOnly(Side.CLIENT)
    // HACK: inject into required mixin target classes
    @SuppressWarnings("UnnecessaryStaticInjection")
    private void requestMixinInjection() {
        this.requestStaticInjection(RenderPlayer.class);
    }
}
