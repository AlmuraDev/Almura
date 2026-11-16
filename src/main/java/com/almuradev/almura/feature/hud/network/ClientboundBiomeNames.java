/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.hud.network;

import com.almuradev.almura.network.AlmuraNetwork;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.HashMap;
import java.util.Map;

/**
 * Names of biomes the client has no translation for (TerrainControl's, which only the server knows), by the biome's id, for the HUD's
 * "Biome Name". Added to what the client was told before.
 */
public record ClientboundBiomeNames(Map<String, String> names) implements CustomPacketPayload {

    public static final Type<ClientboundBiomeNames> TYPE = AlmuraNetwork.type("biome_names");
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundBiomeNames> CODEC = StreamCodec.composite(
        ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.STRING_UTF8), ClientboundBiomeNames::names,
        ClientboundBiomeNames::new);

    @Override
    public Type<ClientboundBiomeNames> type() {
        return TYPE;
    }
}
