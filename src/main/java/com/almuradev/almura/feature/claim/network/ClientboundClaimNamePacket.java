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

public final class ClientboundClaimNamePacket implements Message {

    public boolean isClaim;
    public String name;
    public boolean isWilderness;
    public boolean isTownClaim;
    public boolean isAdminClaim;
    public boolean isBasicClaim;
    public boolean isSubdivision;

    public ClientboundClaimNamePacket() {
    }

    public ClientboundClaimNamePacket(final boolean isClaim, final String name, final boolean isWilderness, final boolean isTownClaim, final boolean isAdminClaim, final boolean isBasicClaim, final boolean isSubdivision) {
        this.isClaim = isClaim;
        this.name = name;
        this.isWilderness = isWilderness;
        this.isTownClaim = isTownClaim;
        this.isAdminClaim = isAdminClaim;
        this.isBasicClaim = isBasicClaim;
        this.isSubdivision = isSubdivision;
    }

    @Override
    public void readFrom(final ChannelBuf buf) {
        this.isClaim = buf.readBoolean();
        this.name = buf.readString();
        this.isWilderness = buf.readBoolean();
        this.isTownClaim = buf.readBoolean();
        this.isAdminClaim = buf.readBoolean();
        this.isBasicClaim = buf.readBoolean();
        this.isSubdivision = buf.readBoolean();
    }

    @Override
    public void writeTo(final ChannelBuf buf) {
        buf.writeBoolean(this.isClaim);
        buf.writeString(this.name);
        buf.writeBoolean(this.isWilderness);
        buf.writeBoolean(this.isTownClaim);
        buf.writeBoolean(this.isAdminClaim);
        buf.writeBoolean(this.isBasicClaim);
        buf.writeBoolean(this.isSubdivision);
    }
}
