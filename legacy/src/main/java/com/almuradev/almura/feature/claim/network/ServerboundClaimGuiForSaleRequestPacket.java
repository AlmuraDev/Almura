/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.claim.network;

import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

public final class ServerboundClaimGuiForSaleRequestPacket implements Message {

    public double x, y, z;
    public String worldName;
    public boolean setForSale;
    public double salePrice;

    public ServerboundClaimGuiForSaleRequestPacket() {}

    public ServerboundClaimGuiForSaleRequestPacket(final double x, final double y, final double z, final String worldName, final boolean setForSale, final double salePrice) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.worldName = worldName;
        this.setForSale = setForSale;
        this.salePrice = salePrice;
    }

    @Override
    public void readFrom(ChannelBuf buf) {
        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.z = buf.readDouble();
        this.worldName = buf.readString();
        this.setForSale = buf.readBoolean();
        this.salePrice = buf.readDouble();
    }

    @Override
    public void writeTo(ChannelBuf buf) {
        buf.writeDouble(this.x);
        buf.writeDouble(this.y);
        buf.writeDouble(this.z);
        buf.writeString(this.worldName);
        buf.writeBoolean(this.setForSale);
        buf.writeDouble(this.salePrice);
    }
}
