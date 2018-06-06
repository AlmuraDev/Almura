package com.almuradev.almura.feature.nick.network.handler;

import com.almuradev.almura.feature.nick.client.gui.NicknameGUI;
import com.almuradev.almura.feature.nick.network.ClientboundNicknameOpenResponsePacket;
import net.minecraft.client.Minecraft;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;

public final class ClientboundNicknameOpenResponsePacketHandler implements MessageHandler<ClientboundNicknameOpenResponsePacket> {

    @Override
    public void handleMessage(ClientboundNicknameOpenResponsePacket message, RemoteConnection connection, Platform.Type side) {
        if (side.isClient()) {
            new NicknameGUI(Minecraft.getMinecraft().player).display();
        }
    }
}
