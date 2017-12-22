/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.accessory;

import com.almuradev.almura.feature.accessory.network.ClientboundPlayerSelectedAccessoriesPacket;
import com.almuradev.almura.feature.accessory.network.ClientboundPlayerSelectedAccessoriesPacketHandler;
import com.almuradev.almura.feature.accessory.registry.AccessoryTypeRegistryModule;
import com.almuradev.almura.shared.inject.CommonBinder;
import net.kyori.violet.AbstractModule;
import org.spongepowered.api.Platform;

public final class AccessoryModule extends AbstractModule implements CommonBinder {

    @Override
    protected void configure() {
        this.registry().module(AccessoryType.class, AccessoryTypeRegistryModule.class);

        this.packet()
                .bind(ClientboundPlayerSelectedAccessoriesPacket.class, binder -> {
                    binder.channel(10);
                    binder.handler(ClientboundPlayerSelectedAccessoriesPacketHandler.class, Platform.Type.CLIENT);
                });

        this.facet().add(AccessoryManager.class);
    }
}
