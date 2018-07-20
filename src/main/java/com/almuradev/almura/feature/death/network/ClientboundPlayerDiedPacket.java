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
    public boolean dropCoins;

    public ClientboundPlayerDiedPacket(){
        this.droppedAmount = 0.00;
        this.deathTaxAmount = 0.00;
        this.dropCoins = false;
    }


    public ClientboundPlayerDiedPacket(double dropAmount, double deathTaxAmount, boolean dropCoins) {
        this.droppedAmount = dropAmount;
        this.deathTaxAmount = deathTaxAmount;
        this.dropCoins = dropCoins;
    }

    @Override
    public void readFrom(ChannelBuf buf) {
        this.droppedAmount = buf.readDouble();
        this.deathTaxAmount = buf.readDouble();
        this.dropCoins = buf.readBoolean();
    }

    @Override
    public void writeTo(ChannelBuf buf) {
        buf.writeDouble(this.droppedAmount);
        buf.writeDouble(this.deathTaxAmount);
        buf.writeBoolean(this.dropCoins);
    }
}
