/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.accessory.network.handler;

import com.almuradev.almura.feature.accessory.client.ClientAccessoryManager;
import com.almuradev.almura.feature.accessory.client.gui.AccessoryScreen;
import com.almuradev.almura.feature.accessory.network.ClientboundAccessoryGuiPacket;
import com.almuradev.almura.shared.util.PacketUtil;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;

import javax.inject.Inject;

public final class ClientboundAccessoryGuiPacketHandler implements MessageHandler<ClientboundAccessoryGuiPacket> {

    private final ClientAccessoryManager manager;

    @Inject
    private ClientboundAccessoryGuiPacketHandler(final ClientAccessoryManager manager) {
        this.manager = manager;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void handleMessage(final ClientboundAccessoryGuiPacket message, final RemoteConnection connection, final Platform.Type side) {
        if (side.isClient() && PacketUtil.checkThreadAndEnqueue(Minecraft.getMinecraft(), message, this, connection, side)) {
            final Minecraft minecraft = Minecraft.getMinecraft();
            if (minecraft.player == null) {
                return;
            }
            this.manager.handleWorn(minecraft.player.getUniqueID(), message.worn);
            // Back to the screen it was opened from (the F12 menu) when closed
            new AccessoryScreen(minecraft.currentScreen, this.manager, message.allowed).display();
        }
    }
}
