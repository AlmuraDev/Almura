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

/**
 * The server's ticks per second, sent to everyone once a second, for the HUD.
 */
public record ClientboundServerTps(double tps) implements CustomPacketPayload {

    public static final Type<ClientboundServerTps> TYPE = AlmuraNetwork.type("server_tps");
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundServerTps> CODEC = StreamCodec.composite(
        ByteBufCodecs.DOUBLE, ClientboundServerTps::tps,
        ClientboundServerTps::new);

    @Override
    public Type<ClientboundServerTps> type() {
        return TYPE;
    }
}
