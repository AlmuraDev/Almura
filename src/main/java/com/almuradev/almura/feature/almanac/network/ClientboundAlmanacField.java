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
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Opens the Farmer's Almanac on a spot of a field, with what the server knows about it: its biome, the biome's temperature there and its
 * rain, the light (block, sky as it is now, and the two together), whether it is day and the spot sees the sky, a heat lamp over it, and
 * whether it is an irrigation pipe or one is near.
 */
public record ClientboundAlmanacField(BlockPos pos, String biome, float temperature, float rainfall, int blockLight, int skyLight,
    int combinedLight, boolean daytime, boolean seesSky, boolean heatLamp, boolean pipe, boolean pipeNear) implements CustomPacketPayload {

    public static final Type<ClientboundAlmanacField> TYPE = AlmuraNetwork.type("almanac_field");
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundAlmanacField> CODEC = StreamCodec.of(
        (buffer, field) -> field.write(buffer), ClientboundAlmanacField::read);

    private void write(final ByteBuf buffer) {
        BlockPos.STREAM_CODEC.encode(buffer, this.pos);
        net.minecraft.network.codec.ByteBufCodecs.STRING_UTF8.encode(buffer, this.biome);
        buffer.writeFloat(this.temperature);
        buffer.writeFloat(this.rainfall);
        buffer.writeByte(this.blockLight);
        buffer.writeByte(this.skyLight);
        buffer.writeByte(this.combinedLight);
        buffer.writeBoolean(this.daytime);
        buffer.writeBoolean(this.seesSky);
        buffer.writeBoolean(this.heatLamp);
        buffer.writeBoolean(this.pipe);
        buffer.writeBoolean(this.pipeNear);
    }

    private static ClientboundAlmanacField read(final ByteBuf buffer) {
        return new ClientboundAlmanacField(BlockPos.STREAM_CODEC.decode(buffer),
            net.minecraft.network.codec.ByteBufCodecs.STRING_UTF8.decode(buffer), buffer.readFloat(), buffer.readFloat(), buffer.readByte(),
            buffer.readByte(), buffer.readByte(), buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean(),
            buffer.readBoolean());
    }

    @Override
    public Type<ClientboundAlmanacField> type() {
        return TYPE;
    }
}
