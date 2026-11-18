/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.worldload.network;

import com.almuradev.almura.network.AlmuraNetwork;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Tells the client the server started or finished loading a world, by the world's name as the HUD shows it.
 */
public record ClientboundWorldLoad(boolean started, String world) implements CustomPacketPayload {

    public static final Type<ClientboundWorldLoad> TYPE = AlmuraNetwork.type("world_load");
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundWorldLoad> CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL, ClientboundWorldLoad::started,
        ByteBufCodecs.STRING_UTF8, ClientboundWorldLoad::world,
        ClientboundWorldLoad::new);

    @Override
    public Type<ClientboundWorldLoad> type() {
        return TYPE;
    }
}
