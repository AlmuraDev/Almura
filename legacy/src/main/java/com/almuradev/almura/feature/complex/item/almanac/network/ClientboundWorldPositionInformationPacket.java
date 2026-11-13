/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.complex.item.almanac.network;

import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.Message;

public final class ClientboundWorldPositionInformationPacket implements Message {

    public int x, y, z;
    public float hitX, hitY, hitZ;
    public String biomeId;
    public float biomeTemperature, biomeRainfall; // TODO One day we will have the client know all of the biome's information if from TC
    public int blockLight, skyLight, combinedLight;
    public boolean isDaytime, canSeeSky, hasAdditionalLightHeatSource, irrigationPipe, irrigationPipeNear;

    public ClientboundWorldPositionInformationPacket() {

    }

    public ClientboundWorldPositionInformationPacket(int x, int y, int z, float hitX, float hitY, float hitZ, String biomeId, float
            biomeTemperature, float biomeRainfall, int blockLight, int skyLight, int combinedLight, boolean isDaytime, boolean canSeeSky,
            boolean hasAdditionalLightHeatSource, boolean irrigationPipe, boolean irrigationPipeNear) {
        this.x = x;
        this.y = y;
        this.z = z;

        this.hitX = hitX;
        this.hitY = hitY;
        this.hitZ = hitZ;

        this.biomeId = biomeId;

        this.biomeTemperature = biomeTemperature;
        this.biomeRainfall = biomeRainfall;

        this.blockLight = blockLight;
        this.skyLight = skyLight;
        this.combinedLight = combinedLight;
        this.isDaytime = isDaytime;
        this.canSeeSky = canSeeSky;
        this.hasAdditionalLightHeatSource = hasAdditionalLightHeatSource;
        this.irrigationPipe = irrigationPipe;
        this.irrigationPipeNear = irrigationPipeNear;
    }

    @Override
    public void readFrom(ChannelBuf buf) {
        this.x = buf.readInteger();
        this.y = buf.readInteger();
        this.z = buf.readInteger();

        this.hitX = buf.readFloat();
        this.hitY = buf.readFloat();
        this.hitZ = buf.readFloat();

        this.biomeId = buf.readString();

        this.biomeTemperature = buf.readFloat();
        this.biomeRainfall = buf.readFloat();

        this.blockLight = buf.readInteger();
        this.skyLight = buf.readInteger();
        this.combinedLight = buf.readInteger();

        this.isDaytime = buf.readBoolean();
        this.canSeeSky = buf.readBoolean();
        this.hasAdditionalLightHeatSource = buf.readBoolean();
        this.irrigationPipe = buf.readBoolean();
        this.irrigationPipeNear = buf.readBoolean();
    }

    @Override
    public void writeTo(ChannelBuf buf) {
        buf.writeInteger(this.x);
        buf.writeInteger(this.y);
        buf.writeInteger(this.z);

        buf.writeFloat(this.hitX);
        buf.writeFloat(this.hitY);
        buf.writeFloat(this.hitZ);

        buf.writeString(this.biomeId);

        buf.writeFloat(this.biomeTemperature);
        buf.writeFloat(this.biomeRainfall);

        buf.writeInteger(this.blockLight);
        buf.writeInteger(this.skyLight);
        buf.writeInteger(this.combinedLight);

        buf.writeBoolean(this.isDaytime);
        buf.writeBoolean(this.canSeeSky);
        buf.writeBoolean(this.hasAdditionalLightHeatSource);
        buf.writeBoolean(this.irrigationPipe);
        buf.writeBoolean(this.irrigationPipeNear);
    }
}
