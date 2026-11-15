/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.accessory.network;

import com.almuradev.almura.network.AlmuraNetwork;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The accessories' messages, as 3.5's packets. Accessories go by their ids ({@code AccessoryType.id()}): type to variant.
 */
public final class AccessoryMessages {

    private static final StreamCodec<ByteBuf, Map<String, String>> WORN = ByteBufCodecs.map(HashMap::new, ByteBufCodecs.stringUtf8(32),
        ByteBufCodecs.stringUtf8(64));

    private AccessoryMessages() {
    }

    /**
     * The player asks to open the Accessories window (the Features menu).
     */
    public record ServerboundOpen() implements CustomPacketPayload {

        public static final Type<ServerboundOpen> TYPE = AlmuraNetwork.type("accessory_open");
        public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundOpen> CODEC = StreamCodec.unit(new ServerboundOpen());

        @Override
        public Type<ServerboundOpen> type() {
            return TYPE;
        }
    }

    /**
     * The player wears a variant of an accessory, or takes it off (an empty variant).
     */
    public record ServerboundSelect(String accessory, String variant) implements CustomPacketPayload {

        public static final Type<ServerboundSelect> TYPE = AlmuraNetwork.type("accessory_select");
        public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundSelect> CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(32), ServerboundSelect::accessory, ByteBufCodecs.stringUtf8(64), ServerboundSelect::variant,
            ServerboundSelect::new);

        @Override
        public Type<ServerboundSelect> type() {
            return TYPE;
        }
    }

    /**
     * Open the window: the types the player may wear, and what they wear.
     */
    public record ClientboundOpen(List<String> allowed, Map<String, String> worn) implements CustomPacketPayload {

        public static final Type<ClientboundOpen> TYPE = AlmuraNetwork.type("accessory_open_response");
        public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundOpen> CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(32).apply(ByteBufCodecs.list()), ClientboundOpen::allowed, WORN, ClientboundOpen::worn, ClientboundOpen::new);

        @Override
        public Type<ClientboundOpen> type() {
            return TYPE;
        }
    }

    /**
     * What a player wears (none when they took everything off or left), for everyone to draw.
     */
    public record ClientboundWorn(UUID player, Map<String, String> worn) implements CustomPacketPayload {

        public static final Type<ClientboundWorn> TYPE = AlmuraNetwork.type("accessory_worn");
        public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundWorn> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, ClientboundWorn::player, WORN, ClientboundWorn::worn, ClientboundWorn::new);

        @Override
        public Type<ClientboundWorn> type() {
            return TYPE;
        }
    }
}
