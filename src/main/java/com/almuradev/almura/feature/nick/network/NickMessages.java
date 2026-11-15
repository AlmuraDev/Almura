/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.nick.network;

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
 * The nicknames' messages, as 3.5's Nucleus name packets. A nickname goes with its colour codes as section signs; an empty one is none.
 */
public final class NickMessages {

    public static final int MAX_NICKNAME = 64;

    private NickMessages() {
    }

    /**
     * Online players' nicknames: all of them ({@code replace}), or changes.
     */
    public record ClientboundNames(boolean replace, Map<UUID, String> nicknames) implements CustomPacketPayload {

        public static final Type<ClientboundNames> TYPE = AlmuraNetwork.type("nick_names");
        public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundNames> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ClientboundNames::replace,
            ByteBufCodecs.map(HashMap::new, UUIDUtil.STREAM_CODEC, ByteBufCodecs.STRING_UTF8), ClientboundNames::nicknames,
            ClientboundNames::new);

        @Override
        public Type<ClientboundNames> type() {
            return TYPE;
        }
    }

    /**
     * Opens the Nicknames window: whether the player may change their nickname, and is a nickname administrator.
     */
    public record ClientboundOpen(boolean canChange, boolean admin) implements CustomPacketPayload {

        public static final Type<ClientboundOpen> TYPE = AlmuraNetwork.type("nick_open");
        public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundOpen> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ClientboundOpen::canChange, ByteBufCodecs.BOOL, ClientboundOpen::admin, ClientboundOpen::new);

        @Override
        public Type<ClientboundOpen> type() {
            return TYPE;
        }
    }

    /**
     * The player asks to open the Nicknames window (the Features menu).
     */
    public record ServerboundOpen() implements CustomPacketPayload {

        public static final Type<ServerboundOpen> TYPE = AlmuraNetwork.type("nick_open_request");
        public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundOpen> CODEC = StreamCodec.unit(new ServerboundOpen());

        @Override
        public Type<ServerboundOpen> type() {
            return TYPE;
        }
    }

    /**
     * The player sets their nickname, or (empty) removes it.
     */
    public record ServerboundChange(String nickname) implements CustomPacketPayload {

        public static final Type<ServerboundChange> TYPE = AlmuraNetwork.type("nick_change");
        public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundChange> CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(MAX_NICKNAME), ServerboundChange::nickname, ServerboundChange::new);

        @Override
        public Type<ServerboundChange> type() {
            return TYPE;
        }
    }
}
