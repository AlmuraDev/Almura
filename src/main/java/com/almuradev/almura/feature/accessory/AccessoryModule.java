/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.accessory;

import com.almuradev.almura.feature.accessory.client.ClientAccessoryManager;
import com.almuradev.almura.feature.accessory.network.ClientboundPlayerSelectedAccessoriesPacket;
import com.almuradev.almura.feature.accessory.network.handler.ClientboundPlayerSelectedAccessoriesPacketHandler;
import com.almuradev.almura.feature.accessory.registry.AccessoryTypeRegistryModule;
import com.almuradev.almura.shared.inject.ClientBinder;
import com.almuradev.almura.shared.inject.CommonBinder;
import net.kyori.violet.AbstractModule;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.Platform;

public final class AccessoryModule extends AbstractModule implements CommonBinder {

    @Override
    protected void configure() {
        this.registry().module(AccessoryType.class, AccessoryTypeRegistryModule.class);

        this.packet()
                .bind(ClientboundPlayerSelectedAccessoriesPacket.class, binder ->
                  binder.handler(ClientboundPlayerSelectedAccessoriesPacketHandler.class, Platform.Type.CLIENT));

        this.facet().add(ServerAccessoryManager.class);
        this.on(Platform.Type.CLIENT, () -> {
            @SideOnly(Side.CLIENT)
            final class ClientModule extends AbstractModule implements ClientBinder {

                @SideOnly(Side.CLIENT)
                @Override
                protected void configure() {
                    this.facet().add(ClientAccessoryManager.class);
                }
            }

            this.install(new ClientModule());
        });
    }
}
