package com.almuradev.almura.feature.nick;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.nick.network.ClientboundNucleusNameChangeMappingPacket;
import com.almuradev.almura.feature.nick.network.ClientboundNucleusNameChangeMappingPacketHandler;
import com.almuradev.almura.feature.nick.network.ClientboundNucleusNameMappingsPacket;
import com.almuradev.almura.feature.nick.network.ClientboundNucleusNameMappingsPacketHandler;
import com.almuradev.shared.inject.ClientBinder;
import com.almuradev.shared.inject.CommonBinder;
import com.almuradev.shared.network.NetworkConfig;
import com.almuradev.shared.network.PacketBinder;
import com.google.inject.Injector;
import net.kyori.violet.AbstractModule;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;
import org.spongepowered.api.network.Message;

import java.util.Set;

import javax.inject.Inject;

public final class NickModule extends AbstractModule implements ClientBinder {

    @Override
    protected void configure() {

        final ChannelBinding.IndexedMessageChannel nucleusChannel = Sponge.getChannelRegistrar().getOrCreate(Almura.instance.container, "NUCLEUS");

        nucleusChannel.registerMessage(ClientboundNucleusNameChangeMappingPacket.class, 0, new ClientboundNucleusNameChangeMappingPacketHandler());
        nucleusChannel.registerMessage(ClientboundNucleusNameMappingsPacket.class, 1, new ClientboundNucleusNameMappingsPacketHandler());

        this.facet()
                .add(NickManager.class);
    }
}
