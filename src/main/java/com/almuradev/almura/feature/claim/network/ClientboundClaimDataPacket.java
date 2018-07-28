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

    public ClientboundClaimDataPacket() {
    }

    public ClientboundClaimDataPacket(final boolean isClaim, final String claimName, final String claimOwner, final boolean isWilderness, final boolean isTownClaim, final boolean isAdminClaim, final boolean isBasicClaim, final boolean isSubdivision,
            double claimEconBalance, String claimGreeting, String claimFarewell) {
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
    }
}
