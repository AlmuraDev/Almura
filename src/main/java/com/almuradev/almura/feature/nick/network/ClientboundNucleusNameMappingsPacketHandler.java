package com.almuradev.almura.feature.nick.network;

import com.almuradev.almura.feature.nick.NickManager;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;
import org.spongepowered.api.text.Text;

import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

public class ClientboundNucleusNameMappingsPacketHandler implements MessageHandler<ClientboundNucleusNameMappingsPacket> {

    @Inject static NickManager nickManager;

    @Override
    public void handleMessage(ClientboundNucleusNameMappingsPacket message, RemoteConnection connection, Platform.Type side) {
        final Map<UUID, Text> nicknames = message.nicknames;

        nickManager.putAll(nicknames);

        final World world = Minecraft.getMinecraft().world;

        if (world != null) {
            for (EntityPlayer player : world.playerEntities) {

                // Triggers Forge event, mod compat
                player.refreshDisplayName();
            }
        }
    }
}
