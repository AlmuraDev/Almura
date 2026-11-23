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
 * How many players are online and how many the server takes, for the HUD's player count, as 3.5's: sent when a player joins and once a
 * second with the server's speed.
 */
public record ClientboundPlayerCount(int online, int max) implements CustomPacketPayload {

    public static final Type<ClientboundPlayerCount> TYPE = AlmuraNetwork.type("player_count");
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundPlayerCount> CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, ClientboundPlayerCount::online,
        ByteBufCodecs.VAR_INT, ClientboundPlayerCount::max,
        ClientboundPlayerCount::new);

    @Override
    public Type<ClientboundPlayerCount> type() {
        return TYPE;
    }
}
