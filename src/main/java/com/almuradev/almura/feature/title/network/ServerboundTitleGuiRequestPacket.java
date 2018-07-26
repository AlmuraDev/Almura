/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.title.network;

import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

public final class ServerboundTitleGuiRequestPacket implements Message {

    public ServerboundTitleGuiRequestPacket() {}

    @Override
    public void readFrom(ChannelBuf buf) {}

    @Override
    public void writeTo(ChannelBuf buf) {}
}
