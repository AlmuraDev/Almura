/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.menu.ingame;

import com.almuradev.almura.feature.menu.ingame.network.ClientboundFeaturesOpenResponsePacket;
import com.almuradev.almura.feature.menu.ingame.network.ServerboundFeaturesOpenRequestPacket;
import com.almuradev.almura.feature.menu.ingame.network.handler.ClientboundFeaturesOpenResponsePacketHandler;
import com.almuradev.almura.feature.menu.ingame.network.handler.ServerboundFeaturesOpenRequestPacketHandler;
import com.almuradev.almura.shared.inject.ClientBinder;
import com.almuradev.almura.shared.inject.CommonBinder;
import net.kyori.violet.AbstractModule;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.spongepowered.api.Platform;

public final class FeaturesModule extends AbstractModule implements CommonBinder {

    @Override
    protected void configure() {
        this.packet()
                .bind(ServerboundFeaturesOpenRequestPacket.class, binder -> binder.handler(ServerboundFeaturesOpenRequestPacketHandler.class, Platform
                        .Type.SERVER))
                .bind(ClientboundFeaturesOpenResponsePacket.class, binder -> binder.handler(ClientboundFeaturesOpenResponsePacketHandler.class, Platform
                        .Type.CLIENT));

        this.on(Platform.Type.CLIENT, () -> {
            final class ClientModule extends AbstractModule implements ClientBinder {

                @SideOnly(Side.CLIENT)
                @Override
                protected void configure() {

                    this.facet().add(ClientFeaturesManager.class);
                    this.requestStaticInjection(FeatureScreen.class);
                    this.keybinding().key(Keyboard.KEY_F12, "key.almura.features.open", "key.categories.almura.features");
                }
            }
            this.install(new ClientModule());
        });
    }
}
