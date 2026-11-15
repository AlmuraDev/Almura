/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.almanac.network;

import com.almuradev.almura.network.AlmuraNetwork;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;

/**
 * Opens the Farmer's Almanac's list of every crop, as 3.5's {@code ClientboundCropListOpenPacket}: the biome and ground temperature where the
 * player stands, and when the server runs Skills, the player's farming level and each crop's farming levels to plant and to harvest.
 *
 * @param skills whether the server runs Skills (otherwise no levels are shown)
 * @param farmingLevel the player's farming level, or -1 if not known
 */
public record ClientboundAlmanacCrops(String biome, float temperature, boolean skills, int farmingLevel, List<CropLevels> levels)
    implements CustomPacketPayload {

    /**
     * A crop's farming levels, by its planted block's id: to plant it and to harvest it, -1 for either that isn't gated.
     */
    public record CropLevels(String id, int plant, int harvest) {

        public static final StreamCodec<ByteBuf, CropLevels> CODEC = StreamCodec.of((buffer, levels) -> {
            ByteBufCodecs.STRING_UTF8.encode(buffer, levels.id);
            ByteBufCodecs.VAR_INT.encode(buffer, levels.plant + 1);
            ByteBufCodecs.VAR_INT.encode(buffer, levels.harvest + 1);
        }, buffer -> new CropLevels(ByteBufCodecs.STRING_UTF8.decode(buffer), ByteBufCodecs.VAR_INT.decode(buffer) - 1,
            ByteBufCodecs.VAR_INT.decode(buffer) - 1));
    }

    public static final Type<ClientboundAlmanacCrops> TYPE = AlmuraNetwork.type("almanac_crops");
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundAlmanacCrops> CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, ClientboundAlmanacCrops::biome,
        ByteBufCodecs.FLOAT, ClientboundAlmanacCrops::temperature,
        ByteBufCodecs.BOOL, ClientboundAlmanacCrops::skills,
        ByteBufCodecs.VAR_INT.map(value -> value - 1, value -> value + 1), ClientboundAlmanacCrops::farmingLevel,
        CropLevels.CODEC.apply(ByteBufCodecs.list()), ClientboundAlmanacCrops::levels,
        ClientboundAlmanacCrops::new);

    @Override
    public Type<ClientboundAlmanacCrops> type() {
        return TYPE;
    }
}
