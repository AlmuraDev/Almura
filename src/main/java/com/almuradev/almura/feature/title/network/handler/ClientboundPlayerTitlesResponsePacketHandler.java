/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.title.network.handler;

import com.almuradev.almura.feature.title.client.gui.TitleGUI;
import com.almuradev.almura.feature.title.network.ClientboundPlayerTitlesResponsePacket;
import com.almuradev.almura.shared.util.PacketUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("deprecation")
public final class ClientboundPlayerTitlesResponsePacketHandler implements MessageHandler<ClientboundPlayerTitlesResponsePacket> {

    @Override
    public void handleMessage(ClientboundPlayerTitlesResponsePacket message, RemoteConnection connection, Platform.Type side) {
        if (side.isClient()) {

            if (PacketUtil.checkThreadAndEnqueue(Minecraft.getMinecraft(), message, this, connection, side)) {
                final int selectedIndex = message.selectedIndex;
                final Set<Text> titles = message.titles;
                final Set<String> guiTitleDisplay = new HashSet<>();
                for (Text title : titles) {
                    guiTitleDisplay.add(TextSerializers.LEGACY_FORMATTING_CODE.serialize(title));
                }
                final Minecraft client = Minecraft.getMinecraft();
                final EntityPlayerSP player = client.player;

                new TitleGUI(player, guiTitleDisplay).display();
            }
        }
    }
}
