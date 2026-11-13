/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.hud.network;

import com.almuradev.almura.network.AlmuraNetwork;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * The worlds players are in, for the player list's World column: every player's when {@code replace} (on joining), otherwise changes to
 * the ones known (an empty world for a player who left).
 */
public record ClientboundPlayerWorlds(boolean replace, Map<UUID, String> worlds) implements CustomPacketPayload {

    public static final Type<ClientboundPlayerWorlds> TYPE = AlmuraNetwork.type("player_worlds");
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundPlayerWorlds> CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL, ClientboundPlayerWorlds::replace,
        ByteBufCodecs.map(HashMap::new, UUIDUtil.STREAM_CODEC, ByteBufCodecs.STRING_UTF8), ClientboundPlayerWorlds::worlds,
        ClientboundPlayerWorlds::new);

    @Override
    public Type<ClientboundPlayerWorlds> type() {
        return TYPE;
    }
}
