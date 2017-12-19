package com.almuradev.almura.feature.nick.network;

import com.almuradev.almura.feature.nick.NickManager;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.RemoteConnection;
import org.spongepowered.api.text.Text;

import java.util.UUID;

import javax.inject.Inject;

public final class ClientboundNucleusNameChangeMappingPacketHandler implements MessageHandler<ClientboundNucleusNameChangeMappingPacket> {

    @Inject static NickManager nickManager;

    @Override
    public void handleMessage(ClientboundNucleusNameChangeMappingPacket message, RemoteConnection connection, Platform.Type side) {

        if (side.isClient()) {
            final UUID entityUniqueId = message.uuid;
            final Text nickname = message.text;

            final World world = Minecraft.getMinecraft().world;
            final EntityPlayer entity = world.getPlayerEntityByUUID(entityUniqueId);

            nickManager.put(entityUniqueId, nickname);

            if (entity != null) {
                // Triggers Forge event, mod compat
                entity.refreshDisplayName();
            }
        }
    }
}
