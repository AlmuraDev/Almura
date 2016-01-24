/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.network.play;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;
import org.spongepowered.api.world.World;

/**
 * Contains information about the {@link World} in context of the client. Primarily used
 * to show information on the GUI.
 *
 * Sent from the Server -> Client when a {@link Player} joins the server or changes world.
 */
public class S00WorldInformation implements Message {
    public String worldName;

    public S00WorldInformation() {}

    public S00WorldInformation(String worldName) {
        this.worldName = worldName;
    }

    @Override
    public void readFrom(ChannelBuf buf) {
        this.worldName = buf.readString();
    }

    @Override
    public void writeTo(ChannelBuf buf) {
        buf.writeString(this.worldName);
    }
}
