/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.network.play;

import org.spongepowered.api.Server;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

/**
 * Contains information about the {@link Server} in context of the client. Primarily used
 * to show information on the GUI.
 *
 * Sent from the Server -> Client when a {@link Player} joins or leaves the server.
 */
public class SServerInformationMessage implements Message {

    public int playerCount, maxPlayerCount;

    public SServerInformationMessage() {}

    public SServerInformationMessage(int playerCount, int maxPlayerCount) {
        this.playerCount = playerCount;
        this.maxPlayerCount = maxPlayerCount;
    }

    @Override
    public void readFrom(ChannelBuf buf) {
        this.playerCount = buf.readInteger();
        this.maxPlayerCount = buf.readInteger();
    }

    @Override
    public void writeTo(ChannelBuf buf) {
        buf.writeInteger(this.playerCount);
        buf.writeInteger(this.maxPlayerCount);
    }
}
