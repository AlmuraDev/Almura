/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.death.network;

import com.almuradev.almura.network.AlmuraNetwork;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * The death penalty's messages, as 3.5's {@code ClientboundPlayerDiedPacket} and {@code ServerboundReviveRequestPacket}.
 */
public final class DeathMessages {

    private DeathMessages() {
    }

    /**
     * The player died (or joined while dead): Almura's death screen, with what they dropped and what they lost to death taxes (as text), whether
     * to show those ({@code showMoney}: false when nothing was taken), whether Revive is on, and vanilla's death message (empty when the
     * world hides death messages, or when joining dead).
     */
    public record ClientboundDied(String dropped, String lost, boolean showMoney, boolean canRevive, Component cause)
        implements CustomPacketPayload {

        public static final Type<ClientboundDied> TYPE = AlmuraNetwork.type("death_died");
        public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundDied> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ClientboundDied::dropped,
            ByteBufCodecs.STRING_UTF8, ClientboundDied::lost,
            ByteBufCodecs.BOOL, ClientboundDied::showMoney,
            ByteBufCodecs.BOOL, ClientboundDied::canRevive,
            ComponentSerialization.TRUSTED_STREAM_CODEC, ClientboundDied::cause,
            ClientboundDied::new);

        @Override
        public Type<ClientboundDied> type() {
            return TYPE;
        }
    }

    /**
     * The dead player asks to come back where they died (Revive), just before respawning. Where they died is the server's own record, not
     * the client's say (3.5 sent the client's position).
     */
    public record ServerboundRevive() implements CustomPacketPayload {

        public static final Type<ServerboundRevive> TYPE = AlmuraNetwork.type("death_revive");
        public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundRevive> CODEC = StreamCodec.unit(new ServerboundRevive());

        @Override
        public Type<ServerboundRevive> type() {
            return TYPE;
        }
    }
}
