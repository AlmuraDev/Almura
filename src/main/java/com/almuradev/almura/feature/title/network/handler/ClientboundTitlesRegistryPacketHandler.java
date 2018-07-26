/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.title.network.handler;

import com.almuradev.almura.feature.title.ClientTitleManager;
import com.almuradev.almura.feature.title.client.gui.ManageTitlesGUI;
import com.almuradev.almura.feature.title.network.ClientboundTitlesRegistryPacket;
import com.almuradev.almura.shared.util.PacketUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;

import javax.inject.Inject;

public final class ClientboundTitlesRegistryPacketHandler implements MessageHandler<ClientboundTitlesRegistryPacket> {

    private final ClientTitleManager titleManager;

    @Inject
    public ClientboundTitlesRegistryPacketHandler(final ClientTitleManager titleManager) {
        this.titleManager = titleManager;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void handleMessage(final ClientboundTitlesRegistryPacket message, final RemoteConnection connection, final Platform.Type side) {
        if (side.isClient() && PacketUtil.checkThreadAndEnqueue(Minecraft.getMinecraft(), message, this, connection, side)) {

            this.titleManager.putTitles(message.titles);

            final GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
            if (currentScreen instanceof ManageTitlesGUI) {
                ((ManageTitlesGUI) currentScreen).refreshTitles();
            }
        }
    }
}
