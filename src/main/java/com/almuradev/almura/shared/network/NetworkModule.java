/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.network;

import com.google.inject.Injector;
import net.kyori.violet.AbstractModule;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;
import org.spongepowered.api.network.Message;

import java.util.Set;

import javax.inject.Inject;

public class NetworkModule extends AbstractModule {

    @Override
    protected void configure() {
        this.requestInjection(this);
    }

    @Inject
    private void configure(
            final Platform platform,
            final Injector injector,
            @ChannelId(NetworkConfig.CHANNEL) final ChannelBinding.IndexedMessageChannel channel,
            final Set<PacketBinder.EntryImpl<? extends Message>> entries
    ) {
        for (final PacketBinder.EntryImpl<? extends Message> entry : entries) {
            entry.register(channel, platform.getType(), injector);
        }
    }
}
