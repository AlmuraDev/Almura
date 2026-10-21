/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.accessory.network;

import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

/**
 * Asks the server to open the accessory screen.
 */
public final class ServerboundAccessoryGuiRequestPacket implements Message {

    @Override
    public void readFrom(final ChannelBuf buf) {
    }

    @Override
    public void writeTo(final ChannelBuf buf) {
    }
}
