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
 * The name of the world the player is in, for the HUD's top middle panel.
 */
public record ClientboundWorldName(String name) implements CustomPacketPayload {

    public static final Type<ClientboundWorldName> TYPE = AlmuraNetwork.type("world_name");
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundWorldName> CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, ClientboundWorldName::name,
        ClientboundWorldName::new);

    @Override
    public Type<ClientboundWorldName> type() {
        return TYPE;
    }
}
