/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.membership.network;

import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

public final class ClientboundMembershipGuiOpenPacket implements Message {

    public boolean isAdmin;
    public int skillLevel;
    public double availableFunds;
    public int currentMembershipLevel;

    public ClientboundMembershipGuiOpenPacket(){
    }

    public ClientboundMembershipGuiOpenPacket(boolean isAdmin, int skillLevel, double availableFunds, int currentMembershipLevel) {
        this.isAdmin = isAdmin;
        this.skillLevel = skillLevel;
        this.availableFunds = availableFunds;
        this.currentMembershipLevel = currentMembershipLevel;
    }

    @Override
    public void readFrom(ChannelBuf buf) {
        this.isAdmin = buf.readBoolean();
        this.skillLevel = buf.readInteger();
        this.availableFunds = buf.readDouble();
        this.currentMembershipLevel = buf.readInteger();
    }

    @Override
    public void writeTo(ChannelBuf buf) {
        buf.writeBoolean(this.isAdmin);
        buf.writeInteger(this.skillLevel);
        buf.writeDouble(this.availableFunds);
        buf.writeInteger(this.currentMembershipLevel);
    }
}
