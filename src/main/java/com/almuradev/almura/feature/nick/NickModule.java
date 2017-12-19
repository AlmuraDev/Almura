package com.almuradev.almura.feature.nick;

import com.almuradev.almura.feature.nick.network.ClientboundNucleusNameChangeMappingPacket;
import com.almuradev.almura.feature.nick.network.ClientboundNucleusNameChangeMappingPacketHandler;
import com.almuradev.almura.feature.nick.network.ClientboundNucleusNameMappingsPacket;
import com.almuradev.almura.feature.nick.network.ClientboundNucleusNameMappingsPacketHandler;
import com.almuradev.shared.inject.ClientBinder;
import net.kyori.violet.AbstractModule;
import org.spongepowered.api.Platform;

public final class NickModule extends AbstractModule implements ClientBinder {

    @Override
    protected void configure() {

        this.packet()
                .bind(ClientboundNucleusNameChangeMappingPacket.class, binder -> {
                    binder.channel(2);
                    binder.handler(ClientboundNucleusNameChangeMappingPacketHandler.class, Platform.Type.CLIENT);
                })
                .bind(ClientboundNucleusNameMappingsPacket.class, binder -> {
                    binder.channel(3);
                    binder.handler(ClientboundNucleusNameMappingsPacketHandler.class, Platform.Type.CLIENT);
                });

        this.requestStaticInjection(ClientboundNucleusNameChangeMappingPacketHandler.class);
        this.requestStaticInjection(ClientboundNucleusNameMappingsPacketHandler.class);

        this.facet()
                .add(NickManager.class);
    }
}
