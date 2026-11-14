/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.almanac.network;

import com.almuradev.almura.network.AlmuraNetwork;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Opens the Farmer's Almanac's list of every crop, with the biome and its ground temperature where the player stands (which crops grow
 * there). 3.5 also sent the Skills plugin's farming levels; Skills has no 26.3 version yet.
 */
public record ClientboundAlmanacCrops(String biome, float temperature) implements CustomPacketPayload {

    public static final Type<ClientboundAlmanacCrops> TYPE = AlmuraNetwork.type("almanac_crops");
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundAlmanacCrops> CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, ClientboundAlmanacCrops::biome,
        ByteBufCodecs.FLOAT, ClientboundAlmanacCrops::temperature,
        ClientboundAlmanacCrops::new);

    @Override
    public Type<ClientboundAlmanacCrops> type() {
        return TYPE;
    }
}
