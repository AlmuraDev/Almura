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

public final class ClientboundClaimDataPacket implements Message {

    public boolean isClaim;
    public String claimName;
    public String claimOwner;
    public boolean isWilderness;
    public boolean isTownClaim;
    public boolean isAdminClaim;
    public boolean isBasicClaim;
    public boolean isSubdivision;
    public double claimEconBalance;
    public String claimGreeting;
    public String claimFarewell;
    public int claimSize;
    public boolean isForSale;
    public boolean showWarnings;
    public double claimTaxes;
    public double claimBlockCost;
    public double claimBlockSell;

    public ClientboundClaimDataPacket() {
    }

    public ClientboundClaimDataPacket(final boolean isClaim, final String claimName, final String claimOwner, final boolean isWilderness, final boolean isTownClaim, final boolean isAdminClaim, final boolean isBasicClaim, final boolean isSubdivision,
            final double claimEconBalance, final String claimGreeting, final String claimFarewell, final int claimSize, final boolean isForSale, final boolean showWarnings, final double claimTaxes, final double claimBlockCost, final double claimBlockSell) {
        this.isClaim = isClaim;
        this.claimName = claimName;
        this.claimOwner = claimOwner;
        this.isWilderness = isWilderness;
        this.isTownClaim = isTownClaim;
        this.isAdminClaim = isAdminClaim;
        this.isBasicClaim = isBasicClaim;
        this.isSubdivision = isSubdivision;
        this.claimEconBalance = claimEconBalance;
        this.claimGreeting = claimGreeting;
        this.claimFarewell = claimFarewell;
        this.claimSize = claimSize;
        this.isForSale = isForSale;
        this.showWarnings = showWarnings;
        this.claimTaxes = claimTaxes;
        this.claimBlockCost = claimBlockCost;
        this.claimBlockSell = claimBlockSell;
    }

    @Override
    public void readFrom(final ChannelBuf buf) {
        this.isClaim = buf.readBoolean();
        this.claimName = buf.readString();
        this.claimOwner = buf.readString();
        this.isWilderness = buf.readBoolean();
        this.isTownClaim = buf.readBoolean();
        this.isAdminClaim = buf.readBoolean();
        this.isBasicClaim = buf.readBoolean();
        this.isSubdivision = buf.readBoolean();
        this.claimEconBalance = buf.readDouble();
        this.claimGreeting = buf.readString();
        this.claimFarewell = buf.readString();
        this.claimSize = buf.readInteger();
        this.isForSale = buf.readBoolean();
        this.showWarnings = buf.readBoolean();
        this.claimTaxes = buf.readDouble();
        this.claimBlockCost = buf.readDouble();
        this.claimBlockSell = buf.readDouble();
    }

    @Override
    public void writeTo(final ChannelBuf buf) {
        buf.writeBoolean(this.isClaim);
        buf.writeString(this.claimName);
        buf.writeString(this.claimOwner);
        buf.writeBoolean(this.isWilderness);
        buf.writeBoolean(this.isTownClaim);
        buf.writeBoolean(this.isAdminClaim);
        buf.writeBoolean(this.isBasicClaim);
        buf.writeBoolean(this.isSubdivision);
        buf.writeDouble(this.claimEconBalance);
        buf.writeString(this.claimGreeting);
        buf.writeString(this.claimFarewell);
        buf.writeInteger(this.claimSize);
        buf.writeBoolean(this.isForSale);
        buf.writeBoolean(this.showWarnings);
        buf.writeDouble(this.claimTaxes);
        buf.writeDouble(this.claimBlockCost);
        buf.writeDouble(this.claimBlockSell);
    }
}
