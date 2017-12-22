/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.accessory.network;

import com.almuradev.almura.feature.accessory.AccessoryManager;
import com.almuradev.almura.feature.accessory.AccessoryType;
import com.almuradev.almura.feature.accessory.registry.AccessoryTypeRegistryModule;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.Platform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.PlayerConnection;
import org.spongepowered.api.network.RemoteConnection;

import javax.inject.Inject;

public final class ClientboundPlayerSelectedAccessoriesPacketHandler implements MessageHandler<ClientboundPlayerSelectedAccessoriesPacket> {

    private final AccessoryTypeRegistryModule store;
    private final AccessoryManager manager;

    @Inject
    public ClientboundPlayerSelectedAccessoriesPacketHandler(final AccessoryTypeRegistryModule store, final AccessoryManager manager) {
        this.store = store;
        this.manager = manager;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void handleMessage(ClientboundPlayerSelectedAccessoriesPacket message, RemoteConnection connection, Platform.Type side) {
        if (side.isClient()) {
            if (connection instanceof PlayerConnection) {

                for (String typeId : message.accessories) {
                    final AccessoryType type = this.store.getById(typeId).orElse(null);

                    if (type == null) {
                        // Server sent us an accessory id we do not have. Ignore it
                        continue;
                    }

                    try {
                        this.manager.putSelectedAccessory(message.uniqueId, typeId);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
