/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.death.network;

import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

public final class ClientboundPlayerDiedPacket implements Message {

    public double droppedAmount, deathTaxAmount;
    public boolean dropCoins, canRevive;

    public ClientboundPlayerDiedPacket(){
    }


    public ClientboundPlayerDiedPacket(double dropAmount, double deathTaxAmount, boolean dropCoins, boolean canRevive) {
        this.droppedAmount = dropAmount;
        this.deathTaxAmount = deathTaxAmount;
        this.dropCoins = dropCoins;
        this.canRevive = canRevive;
    }

    @Override
    public void readFrom(ChannelBuf buf) {
        this.droppedAmount = buf.readDouble();
        this.deathTaxAmount = buf.readDouble();
        this.dropCoins = buf.readBoolean();
        this.canRevive = buf.readBoolean();
    }

    @Override
    public void writeTo(ChannelBuf buf) {
        buf.writeDouble(this.droppedAmount);
        buf.writeDouble(this.deathTaxAmount);
        buf.writeBoolean(this.dropCoins);
        buf.writeBoolean(this.canRevive);
    }
}
